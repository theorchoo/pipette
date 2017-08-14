import cv2
import numpy as np


def draw_points(event,x,y,flags,param):
    if event == cv2.EVENT_LBUTTONDOWN:
        """
        global img, mask
        this_mask = mask
        a = cv2.floodFill(img,this_mask,(x,y),100)
        img = a[1]
        cv2.imshow('image',img)
        """
        global contours
        final_cnt = None
        min_dis = 8000
        for cnt in contours:
            if cv2.pointPolygonTest(cnt,(x,y),False) == 1:
                dis = abs(cv2.pointPolygonTest(cnt, (x,y),True))
                if dis < min_dis:
                    final_cnt = contours.index(cnt)
                    min_dis = dis
        global img

        cont = contours[final_cnt]
        #img = cv2.drawContours(img,contours,final_cnt,130)
        img = cv2.fillPoly(img,[cont],100)
        cv2.imshow('image',img)

cv2.namedWindow('image')
cv2.setMouseCallback('image',draw_points)

img = cv2.imread('Coloring-book.jpg',0)

mask = cv2.copyMakeBorder(img,1,1,1,1,cv2.BORDER_REFLECT)
ret, mask = cv2.threshold(mask,10,250,cv2.THRESH_BINARY_INV)

image, contours, hierarchy = cv2.findContours(img,cv2.RETR_TREE,
                                              cv2.CHAIN_APPROX_NONE)

img = cv2.drawContours(img, contours, -1,0, 1)
cv2.imshow('image', img)
print(len(contours))


k = cv2.waitKey(0)

if k == 27:
    cv2.destroyAllWindows()
