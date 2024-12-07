#!/usr/bin/python3

import cv2
import os
import time
from picamera2 import Picamera2


# download the cascade file from the link below
# https://github.com/opencv/opencv/blob/4.x/data/haarcascades/haarcascade_frontalface_default.xml

face_detector = cv2.CascadeClassifier("haarcascade_frontalface_default.xml")
cv2.startWindowThread()

picam2 = Picamera2()
picam2.configure(picam2.create_preview_configuration(main={"format": 'XRGB8888', "size": (640, 480)}))
picam2.start()

last_turned_on = 0
turned_on = False

while True:
    im = picam2.capture_array()

    grey = cv2.cvtColor(im, cv2.COLOR_BGR2GRAY)
    faces = face_detector.detectMultiScale(grey, 1.1, 5)


    if len(faces) > 0:
        if not turned_on:  # Only turn on if it's not already on
            print("Face detected - turning on")
            os.system("echo 0 > /sys/class/backlight/10-0045/bl_power")  # Turn on
            last_turned_on = time.time()
            turned_on = True
    else:
        # If no face is detected, turn off the system only if 10 seconds have passed since the last on
        if turned_on and time.time() - last_turned_on >= 10:
            print("No face detected - turning off")
            os.system("echo 1 > /sys/class/backlight/10-0045/bl_power")  # Turn off
            turned_on = False  # Reset turned_on status to allow future detection

    #cv2.imshow("Camera", im)
    cv2.waitKey(1)
