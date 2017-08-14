import cv2
import numpy as np
import time

LOWER_IR = np.array([0,100,200])
UPPER_IR = np.array([180,255,255])


def mouseEvent(type, posx, posy):
    theEvent = CGEventCreateMouseEvent(
        None,
        type,
        (posx,posy),
        kCGMouseButtonLeft)
    CGEventPost(kCGHIDEventTap, theEvent)


def mousemove(posx,posy):
    mouseEvent(kCGEventMouseMoved, posx,posy);


def mouseclick(posx,posy):
    # uncomment this line if you want to force the mouse
    # to MOVE to the click location first (I found it was not necessary).
    #mouseEvent(kCGEventMouseMoved, posx,posy);
    mouseEvent(kCGEventLeftMouseDown, posx,posy);
    mouseEvent(kCGEventLeftMouseUp, posx,posy);

"""
cam = cv2.VideoCapture(0)
while(True):
    if cam.isOpened():
        ret, frame = cam.read()
        hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)
        mask = cv2.inRange(hsv, LOWER_IR, UPPER_IR)
        mask2, contours,hierarchy = cv2.findContours(mask, 1,
                                               cv2.CHAIN_APPROX_SIMPLE)

        final_cnt = None
        max_area = 0
        for i in range(len(contours)):
            cnt = contours[i]
            area = cv2.contourArea(cnt)
            if area > max_area:
                max_area = area
                final_cnt = cnt

        M = cv2.moments(final_cnt)
        cx = int(M['m10']/M['m00'])
        cy = int(M['m01']/M['m00'])

        # todo - insert mouse control with cx & cy

        time.sleep(0.1)
"""