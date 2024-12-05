from picamera2 import Picamera2
import cv2
import os

# Initialize Picamera2
picam2 = Picamera2()
picam2.configure(picam2.create_preview_configuration(main={"format": "RGB888", "size": (640, 480)}))
picam2.start()

# Initialize the HOG descriptor and person detector
hog = cv2.HOGDescriptor()
hog.setSVMDetector(cv2.HOGDescriptor_getDefaultPeopleDetector())

# Dictionary of OpenCV object trackers
OPENCV_OBJECT_TRACKERS = {
    "csrt": cv2.TrackerCSRT_create if hasattr(cv2, 'TrackerCSRT_create') else cv2.legacy.TrackerCSRT_create,
    "kcf": cv2.TrackerKCF_create if hasattr(cv2, 'TrackerKCF_create') else cv2.legacy.TrackerKCF_create,
    "boosting": cv2.legacy.TrackerBoosting_create,
    "mil": cv2.legacy.TrackerMIL_create,
    "tld": cv2.legacy.TrackerTLD_create,
    "medianflow": cv2.legacy.TrackerMedianFlow_create,
    "mosse": cv2.legacy.TrackerMOSSE_create,
}

# Use CSRT as the default tracker
tracker = OPENCV_OBJECT_TRACKERS["csrt"]()

# Initialize tracker variables
tracking = False
person_detected = False

while True:
    # Capture a new frame from Picamera2
    frame = picam2.capture_array()
    if frame is None:
        print("Failed to capture frame from Picamera2!")
        break

    # Convert frame to grayscale for person detection
    gray = cv2.cvtColor(frame, cv2.COLOR_RGB2GRAY)

    # Perform person detection
    boxes, _ = hog.detectMultiScale(gray, winStride=(8, 8), padding=(16, 16), scale=1.05)

    if len(boxes) > 0:
        # Take the first detected person (you can adjust this logic to track more people)
        (x, y, w, h) = boxes[0]
        
        if not tracking:
            # Initialize the tracker with the first detection
            tracker.init(frame, (x, y, w, h))
            tracking = True
            person_detected = True
    else:
        person_detected = False

    if tracking:
        # Update the tracker with the current frame
        success, box = tracker.update(frame)

        if success:
            # Draw the bounding box if tracking is successful
            os.system("echo 0 > /sys/class/backlight/10-0045/bl_power")
            (x, y, w, h) = [int(v) for v in box]
            cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 255, 0), 2)
        else:
            # If tracking fails, stop tracking
            os.system("echo 1 > /sys/class/backlight/10-0045/bl_power")
            tracking = False
            person_detected = False

    if not person_detected:
        # If no person is detected or being tracked, display message
        cv2.putText(frame, "No person detected", (10, 50), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 0, 255), 2)

    # Display the frame
    cv2.imshow("Frame", frame)

    # Break the loop if the user presses 'q'
    key = cv2.waitKey(1) & 0xFF
    if key == ord("q"):
        break

# Stop the camera and close all windows
picam2.stop()
cv2.destroyAllWindows()
