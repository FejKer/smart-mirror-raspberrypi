# **🪞 Smart Mirror Project (Raspberry Pi)**
> **A smart mirror project built using Raspberry Pi hardware.**

---

## **📋 Project Overview**
This project transforms a Raspberry Pi into a functional smart mirror. The mirror displays weather, news, and other useful information. A dedicated Android mobile app can be used to configure and control the mirror.

---

## **🔧 Initial Setup**
> Follow these steps to set up your Raspberry Pi for the smart mirror.

### **1️⃣ Install Required Software**
Install the following packages:  
```bash
sudo apt-get update && sudo apt-get upgrade
sudo apt-get install chromium openjdk-21-jdk python3-pyqt5 libqt5gui5 libqt5test5 \
libatlas-base-dev libhdf5-dev libhdf5-serial-dev python3-libcamera python3-kms++ -y
```

### **2️⃣ Create Python Virtual Environment**
Create a Python virtual environment:  
```bash
python3 -m venv --system-site-packages camera
```
Activate the environment and install required Python packages:  
```bash
source camera/bin/activate
pip install opencv-python
pip install picamera2
pip install opencv-contrib-python==4.5.5.62
```

---

## **🗂️ File Structure**
Add the following scripts to `crontab` to run them automatically on boot:  
```bash
@reboot /home/admin/Desktop/startup-scripts/conf.sh
@reboot /home/admin/Desktop/startup-scripts/front.sh
@reboot /home/admin/Desktop/startup-scripts/detector.sh
```

Add the **Chromium Service** to `/etc/systemd/system/chromium.service`, then reload and enable it:  
```bash
sudo systemctl daemon-reload
sudo systemctl enable chromium
sudo systemctl start chromium
```

---

## **⚙️ Permissions**
Edit `visudo` to allow passwordless execution of certain commands:  
```bash
admin ALL=(ALL) NOPASSWD:SETENV: /home/admin/camera/bin/python
admin ALL=(ALL) NOPASSWD: /usr/bin/nmcli
```

---

## **📱 Mobile App Integration**
> You can interact with the smart mirror service using a dedicated Android app.

### **Option 1: Build the Mobile App**
- Clone and build the mobile app yourself.  
- The app scans the network for the Raspberry Pi and displays its IP address.  

### **Option 2: Reverse Engineer the REST API**
- Identify the available endpoints and control the service manually.  
- Note that the service operates using an **H2 database** and a **properties file** created at runtime.  

> 📸 **Image Placeholder**

---

## **🌐 Network Configuration**
1. **Hotspot Mode:** Each time the Raspberry Pi service starts, it switches to hotspot mode to allow app-based configuration.  
2. **Wi-Fi Connection:** Once Wi-Fi details (SSID and password) are configured, the Raspberry Pi attempts to connect.  
   - If Wi-Fi fails, it reverts to **hotspot mode**.  
   - No continuous connection check is performed, so a restart may be needed if the connection fails.  

> 📸 **Image Placeholder**

---

## **🛠️ Configuration Settings**
The following configuration entries can be set via the app or properties file:

| **Key**           | **Example Value**       | **Description**                  |
|-------------------|-----------------------|-----------------------------------|
| `location`        | `Seattle`             | Location for weather information. |
| `news-api-key`    | `some-key`            | API key for fetching news.        |
| `wifi-password`   | `test`                | Wi-Fi password.                   |
| `news-categories` | `sports,tech,health`  | Categories for news updates.      |
| `wifi-ssid`       | `test`                | Wi-Fi SSID.                       |
| `news-language`   | `en`                  | Language for news articles.       |
| `weather-api-key` | `some-key`            | API key for weather service.      |

> 📸 **Image Placeholder**

---

## **🔄 Data Refresh Flow**
| **Component**      | **Update Interval**  | **Details**                         |
|-------------------|---------------------|--------------------------------------|
| **Frontend**       | Every 2 seconds     | Requests new data from the backend.  |
| **Backend (Weather)** | Every 15 minutes | Pulls weather data from API.        |
| **Backend (News)**    | Every 3 hours    | Pulls news updates from API.        |

> 📸 **Image Placeholder** 

---

## **🔍 How Mobile App Detects Raspberry Pi**
1. The app scans the local subnet (IP range `x.x.x.1` to `x.x.x.254`).  
2. When the Raspberry Pi is detected, its IP address is displayed in the app.  
3. Ensure **port 8080** is open for communication.  

> 📸 **Image Placeholder**

---

## **📢 Notes and Tips**
- **Firewall:** Ensure port 8080 is open for app-to-Raspberry Pi communication.  
- **Service Notification:** Every configuration change triggers an event to notify other components to refresh data.  
- **Restart Requirements:** If the Wi-Fi connection fails, a full restart of the Raspberry Pi may be required.  

## **Optionally disable touch screen input**
---

### **1️⃣ Create a New udev Rule File**

Run the following command to create a new udev rule file:

```bash
sudo nano /etc/udev/rules.d/99-touchscreen-disable.rules
```

---

### **2️⃣ Add the Following Content to the File**

Copy and paste the following line into the newly created file:

```bash
ACTION=="add|change", ATTRS{name}=="YOUR_TOUCHSCREEN_NAME", ENV{LIBINPUT_IGNORE_DEVICE}="1"
```

> **🔍 Note:** Replace `YOUR_TOUCHSCREEN_NAME` with the name of your touchscreen device.

---

### **3️⃣ Find the Name of Your Touchscreen Device**

Run the following command to list input devices and find the name of your touchscreen:

```bash
cat /proc/bus/input/devices | grep -i "Name="
```

Look for the device name that matches your touchscreen.

---

### **4️⃣ Apply the Changes**

Reload the udev rules using the command below:

```bash
sudo udevadm control --reload-rules
```

---

### **5️⃣ Reboot the System**

Finally, reboot your system to apply the changes:

```bash
sudo reboot
```

---
