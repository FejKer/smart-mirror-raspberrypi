package com.mradawiec.smart_lustro

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.NetworkInterface
import java.net.Socket
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException

class RaspberryPiDiscovery {
    private val client = OkHttpClient.Builder().callTimeout(1000, TimeUnit.MILLISECONDS).build()
    /**
     * Discover Raspberry Pi server on local network using concurrent IP checking
     * @param port Port number the Raspberry Pi server is running on
     * @param timeout Connection timeout in milliseconds
     * @return IP address of the discovered Raspberry Pi, or null if not found
     */
    suspend fun test(
        port: Int = 8080,
    ): String? {
        val localIp = getLocalIpAddress() ?: return null

        val subnet = localIp.substringBeforeLast(".")

        val channel = Channel<String>(Channel.CONFLATED)

        var found = false;

        for (i in 1..254) {
            if (found) break;
            val testIp = "$subnet.$i"
            val request = Request.Builder()
                .url("http://${testIp}:${port}/api/v1/config/attributes") // Replace with your URL
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // Handle request failure
                    println("Request failed: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    // Handle the response
                    if (response.isSuccessful) {
                        channel.trySend(testIp)
                        println(response.body?.string())
                        found = true;
                    } else {
                        println("Error: ${response.code}")
                    }
                }
            })
        }
        val discoveredIp = select<String?> {
            channel.onReceiveCatching { it.getOrNull() } // React to the first IP
        }

        return discoveredIp
    }

    /**
     * Get the local IP address of the device
     */
    private fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()

                // Skip loopback and inactive interfaces
                if (networkInterface.isLoopback || !networkInterface.isUp) continue

                val interfaceAddresses = networkInterface.interfaceAddresses

                for (interfaceAddress in interfaceAddresses) {
                    val localAddress = interfaceAddress.address

                    // Find a valid local IPv4 address
                    val ip = localAddress.hostAddress
                    if (!ip.contains(":") && localAddress.isSiteLocalAddress) {
                        return ip
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }
}