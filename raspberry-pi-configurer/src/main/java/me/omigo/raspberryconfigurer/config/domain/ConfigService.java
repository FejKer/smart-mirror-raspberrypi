package me.omigo.raspberryconfigurer.config.domain;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.omigo.raspberryconfigurer.config.Attributes;
import me.omigo.raspberryconfigurer.config.command.ConfigCommandService;
import me.omigo.raspberryconfigurer.config.command.UpdateAttributesDTO;
import me.omigo.raspberryconfigurer.config.query.ConfigQueryDto;
import me.omigo.raspberryconfigurer.config.query.ConfigQueryService;
import me.omigo.raspberryconfigurer.news.domain.ConfigRefresher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConfigService implements ConfigQueryService, ConfigCommandService {

    private static final String file = "news-config.properties";
    private static final List<String> attributes = Attributes.getKeysList();
    private Config config;
    private final NotificationEmitter notificationEmitter;

    /**
     * Each startup - go into hotspot mode
     * On rest api call try to connect to wifi and check if success
     * https://www.makeuseof.com/connect-to-wifi-with-nmcli/
     *
     * Check if connected each 10 seconds, else go into hotspot mode
     *
     * todo check internet connection
     *
     * @throws IOException
     */
    @PostConstruct
    void initConfig() throws IOException {
        log.info("Initializing config");
        checkIfFileExists();
        readConfig();
        log.info("Config initialized. {}", config);
        try {
            log.info("Initializing hotspot");
            initializeHotSpotConnection();
            log.info("After hotspot init");
        } catch (Exception e) {
            log.error("Problem with init hotspot", e);
        }
        //switch to hotspot and listen for rest api call to switch to wifi client
    }

    private void initializeHotSpotConnection() throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec("sudo nmcli device wifi hotspot con-name rpi-ap ssid RPI-AP band bg password raspberrypi");
        process.waitFor();
        var errors = process.getErrorStream();
        var infos = process.getInputStream();
        String errorString = new String(errors.readAllBytes());
        String infosString = new String(infos.readAllBytes());
        log.info("{}", infosString);
        log.error("{}", errorString);
//        var output = new String(process.getInputStream().readAllBytes());
//        if (output.contains("")) {
//
//        }
    }

    private void updateConfig() {
        try (Writer wr = new FileWriter(file)) {
            for (var prop : config.getAttributes().entrySet()) {
                wr.write(prop.getKey() + "=" + prop.getValue() + "\n");
            }
            wr.flush();
            notificationEmitter.emitNotification();
        } catch (IOException e) {
            log.error("Error while updating config", e);
        }
    }

    private void readConfig() throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(file));
        scanner.useDelimiter("\n");
        Config config = new Config(new HashMap<>());
        while (scanner.hasNext()) {
            String line = scanner.next();
            String[] split = line.split("=");
            if (split.length == 2) {
                config.addAttribute(new UpdateAttributesDTO(split[0], split[1]));
            } else if (split.length == 1) {
                config.addAttribute(new UpdateAttributesDTO(split[0], ""));
            }
        }
        this.config = config;
    }

    private void checkIfFileExists() throws IOException {
        File f = new File(file);
        if (!f.exists()) {
            log.info("Config file not found, creating new one");
            try (Writer wr = new FileWriter(file)) {
                for (var prop : attributes) {
                    wr.write(prop + "=\n");
                }
                wr.flush();
            }
        }
    }

    @Override
    public void addAttribute(UpdateAttributesDTO dto) {
        config.addAttribute(dto);
        updateConfig();
    }

    @Override
    public void removeAttribute(String key) {
        config.removeAttribute(key);
        updateConfig();
    }

    @Override
    public ConfigQueryDto getConfigDto() {
        return new ConfigQueryDto(config.getAttributes());
    }

    @Override
    @Async
    public void tryToConnectToWifi() {
        try {
            Process turnOfHotspotCommand = Runtime.getRuntime().exec("sudo nmcli connection down RPI");
            turnOfHotspotCommand.waitFor();
            Thread.sleep(10_000);
            String wifiPassword = getAttribute(Attributes.WIFI_PASSWORD);
            String wifiSsid = getAttribute(Attributes.WIFI_SSID);
            if (wifiPassword != null && !wifiPassword.isEmpty()) {
                Process connectToWifiCommand = Runtime.getRuntime().exec(String.format("sudo nmcli device wifi connect %s password %s", wifiSsid, wifiPassword));
                connectToWifiCommand.waitFor();
                var outputStream = connectToWifiCommand.getErrorStream();
                var errorString = new String(outputStream.readAllBytes());
                if (!errorString.isEmpty()) {
                    log.error("There was a problem when connecting to wifi {}", errorString);
                    throw new IOException();
                }
            } else {
                Process connectToWifiCommand = Runtime.getRuntime().exec(String.format("sudo nmcli device wifi connect %s", wifiSsid));
                connectToWifiCommand.waitFor();
                var outputStream = connectToWifiCommand.getErrorStream();
                var errorString = new String(outputStream.readAllBytes());
                if (!errorString.isEmpty()) {
                    log.error("There was a problem when connecting to wifi {}", errorString);
                    throw new IOException();
                }
            }
            Thread.sleep(10_000);
            notificationEmitter.emitNotification();
        } catch (IOException | InterruptedException e) {
            try {
                initializeHotSpotConnection();
            } catch (IOException | InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
    }

    private String getAttribute(Attributes attributes) {
        return config.getAttributes().get(attributes.getKey());
    }

    public static class ConfigReloadEvent {
    }

    public static class NewsWeatherReloadEvent {

    }
}
