import cv2
import numpy as np
import serial
import tkinter
from PIL import Image, ImageTk
import time
from fill import Paint
from os import listdir
from os.path import isfile, join

i = 0
l = []

CAL_MOV = ['NW','NE','SW','SE']
WINDOW_SIZE = (800,600)
LOWER_V = np.array([0,100,100])
UPPER_V = np.array([150,255,255])
#LOWER_V = np.array([0,50,50])
#UPPER_V = np.array([179,255,255])
LOWER_IR = np.array([0,20,200])
UPPER_IR = np.array([180,255,255])
PAINT_PATH = 'paintings/'

###########################################
# App Class
###########################################

class App:

    def __init__(self,root):
        self._root = root
        self._cx = None
        self._cy = None
        self._ser = serial.Serial('/dev/cu.usbmodem12341', 115200,timeout=0.8)
        self._root.resizable(width=tkinter.FALSE, height=tkinter.FALSE)
        #self._root.geometry('{}x{}'.format(800,600))
        self._root.geometry("{0}x{1}+0+0".format(root.winfo_screenwidth(),
                                            root.winfo_screenheight()))
        self._frame = tkinter.Frame(self._root,width=root.winfo_screenwidth(),
                                    height=root.winfo_screenheight())
        self._frame.pack(fill=tkinter.BOTH)
        self._img_label = tkinter.Label(self._frame,
                    width=root.winfo_screenwidth(),height=root.winfo_screenheight())
        self._img_label.pack()
        self._cal_obj = Calibrate(self._img_label,self._root)
        #self._root.after(2,self._calibrate)
        self._root.after(2,self._manual_calibrate)

    def _calibrate(self):
        self._cal_obj.calibrate(self)

    def _manual_calibrate(self):
        self._cal_obj.man_calibrate(self)

    def _end_calibrate(self):
        self.choosing_screen()
        self._cam = cv2.VideoCapture(1)

    def choosing_screen(self):
        self._img_list = self._get_all_paintings()
        self._clean_frame()
        tkinter.Frame(self._frame,height=150).pack(fill=tkinter.X)
        tkinter.Frame(self._frame,width=100).pack(side=tkinter.LEFT,
                                                  fill=tkinter.Y)
        tkinter.Frame(self._frame,width=100).pack(side=tkinter.RIGHT,
                                                  fill=tkinter.Y)
        txt = tkinter.Label(self._frame,text='Choose Painting:',
                            font=("Helvetica", 30))
        txt.pack(fill=tkinter.X,pady=30)
        for img in self._img_list:
            img_th = img.resize((200, 150),Image.ANTIALIAS)
            imgtk = ImageTk.PhotoImage(img_th)
            lbl = tkinter.Label(self._frame,image=imgtk,bd=4,bg='#eeeeee')
            lbl.photo = imgtk
            lbl.pack(side=tkinter.LEFT,padx=5)
            lbl.bind('<Button-1>',self._generate_choose_function(
                self._img_list.index(img)))
            lbl.bind('<Enter>',self._generate_choose_function(
                self._img_list.index(img),True))
            lbl.bind('<Leave>',self._generate_choose_function(
                self._img_list.index(img),True))

    def _generate_choose_function(self,id,hover=False):
        if not hover:
            def f(event):
                self.choose(id)
        else:
            def f(event):
                if event.widget['bg'] == '#eeeeee':
                    event.widget['bg'] = '#999999'
                else:
                    event.widget['bg'] = '#eeeeee'

        return f

    def _clean_frame(self):
        for widg in self._frame.pack_slaves():
            widg.pack_forget()

    def choose(self,id):
        self._clean_frame()
        self.painter(self._img_list[id])

    def painter(self,img):
        self._img_label['bg'] = 'white'
        self._img_label['width'] = root.winfo_screenwidth()
        self._img_label['height'] = root.winfo_screenheight()
        imgtk = ImageTk.PhotoImage(img)
        self._img_label['image'] = imgtk
        self.photo = imgtk
        self._img_label.pack()
        self.pa = Paint(img,self._root,self._img_label)
        self._root.after(25,self.scan_ir)
        self._root.after(100,self._wait_for_input)

    def _get_all_paintings(self):
        files_list = [f for f in listdir(PAINT_PATH) if isfile(join(PAINT_PATH,
                                                                    f))]
        image_list = []
        for file in files_list:
            image_list.append(Image.open(join(PAINT_PATH,file)))

        return image_list

    def scan_ir(self):
        if self._cam.isOpened():
            ret, frame = self._cam.read()
            frame = self._cal_obj.perspective_adj(frame)
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
            if M['m00'] != 0:
                self._cx = int(M['m10']/M['m00'])
                self._cy = int(M['m01']/M['m00'])
                self._cx,self._cy = self.xy_adj(self._cx,self._cy)
                print(self._cx,self._cy)
                tracked = cv2.circle(frame,(self._cx,self._cy),10,
                                     (255,0,0))
            else:
                self._cx = None
                self._cy = None
                tracked = frame

            #cv2.imshow('mask',tracked)
            self._root.after(25,self.scan_ir)

    def xy_adj(self,x,y):
        x1 = x/self._cal_obj._dest_size[0] * self.pa.image.width()
        y1 = y/self._cal_obj._dest_size[1] * self.pa.image.height()
        return int(x1),int(y1)

    def _wait_for_input(self):
        line = self._ser.readline()
        line = line.decode('ascii')
        if line.startswith('R;;'):
            str_list = line.split(';;')
            r = min(int(str_list[1])*3,255)
            g = min(int(str_list[3])*3,255)
            b = min(int(str_list[5])*3,255)
            if self._cx:
                print("filling: ",self._cx,self._cy," with color: ",(r,g,b))
                self.pa.fill(self._cx,self._cy,(r,g,b))

        self._root.after(100,self._wait_for_input)

###########################################
# Calibrate Class
###########################################

class Calibrate:

    def __init__(self,label,root):
        self._root = root
        self.__cal_matrix = None
        self._label = label
        self._calibrating_list = [1,2,3,4]

    def calibrate(self,parent):
        self.parent = parent
        self._calibrating_list = [[0,0],[800,0],[0,600],[800,600]]
        self._mov_str = 'cal.mov'
        self._cap = cv2.VideoCapture(self._mov_str)
        self._cam = cv2.VideoCapture(1)
        self._frame_counter = 0
        self._start_cal_helper()

    def man_calibrate(self, parent):
        self.parent = parent
        self._cam = cv2.VideoCapture(1)
        self._cal_click_lst = []
        self._start_man_cal()

    def _start_man_cal(self):
        self._label['bg'] = 'blue'
        img = Image.open('paintings/fish_coloring_pages.gif')
        imgtk = ImageTk.PhotoImage(img)
        self._label['image'] = imgtk
        self._label.photo = imgtk
        self._root.after(500,self._man_cal)

    def _man_cal(self):
        if self._cam.isOpened():
            # Capture frame-by-frame
            ret, frame = self._cam.read()
            tkimg = cv2tk(frame)
            self._dest_size = (tkimg.width(),tkimg.height())
            self._cam.release()
            self._label['image'] = tkimg
            self._label.photo = tkimg
            self._label['width'] = tkimg.width()
            self._label['height'] = tkimg.height()
            self._label.bind('<Button-1>',self._wait_for_click)

    def _wait_for_click(self,event):
        if len(self._cal_click_lst) < 3:
            self._cal_click_lst.append([event.x, event.y])
        else:
            self._cal_click_lst.append([event.x, event.y])
            pts1 = np.float32(self._cal_click_lst)
            pts2 = np.float32([[0,0],[self._dest_size[0],0],[0,self._dest_size[1]],
                           [self._dest_size[0],self._dest_size[1]]])
            print(pts1,pts2)
            self.__cal_matrix = cv2.getPerspectiveTransform(pts1,pts2)
            print(self._cal_click_lst)
            self.parent._end_calibrate()

    def _put_cnt(self,img):
        box = np.int_(self._cal_box)
        img = cv2.drawContours(img,[box],0,(255,0,0),3)
        return img

    def _show_img(self,img):
        self._label['image'] = img
        self._label.photo = img

    def _start_cal_helper(self):
        cam_frame = self._start_cal()
        if cam_frame != None:
            if self.__cal_matrix != None:
                self._root.after(2,self._end_calibrate)
                return
            self._calibrate_helper(cam_frame)
            self._frame_counter = 0
            self._cap.set(cv2.CAP_PROP_POS_FRAMES,0)
            self._root.after(30,self._start_cal_helper)
        else:
            self._root.after(30,self._start_cal_helper)

    def _start_cal(self,last=False):
        if self._cap.isOpened():
            # Capture frame-by-frame
            ret, frame = self._cap.read()
            self._frame_counter += 1

            tkimg = cv2tk(frame)
            self._show_img(tkimg)

            if self._frame_counter == self._cap.get(cv2.CAP_PROP_FRAME_COUNT):
                if last:
                    return
                cam_ret, cam_frame = self._cam.read()
                return cam_frame

    def _calibrate_helper(self,img):
        hsv = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)
        mask = cv2.inRange(hsv, LOWER_V, UPPER_V)
        mask2, self.contours,self.hierarchy = cv2.findContours(mask, 1,
                                               cv2.CHAIN_APPROX_SIMPLE)
        self._final_cnt = None
        max_area = 0
        for i in range(len(self.contours)):
            cnt = self.contours[i]
            area = cv2.contourArea(cnt)
            if area > max_area:
                max_area = area
                self._final_cnt = cnt

        rect = cv2.minAreaRect(self._final_cnt)
        self._cal_box = cv2.boxPoints(rect)

        for i in self._cal_box:
            x,y = i.ravel()
            print(x)
            print(y)
            if x < WINDOW_SIZE[0]/2:
                if y < WINDOW_SIZE[1]/2:
                    self._calibrating_list[0] = [x,y]
                else:
                    self._calibrating_list[2] = [x,y]
            else:
                if y < WINDOW_SIZE[1]/2:
                    self._calibrating_list[1] = [x,y]
                else:
                    self._calibrating_list[3] = [x,y]

        pts1 = np.float32(self._calibrating_list)
        pts2 = np.float32([[0,0],[WINDOW_SIZE[0],0],[0,WINDOW_SIZE[1]],
                           [WINDOW_SIZE[0],WINDOW_SIZE[1]]])
        self.__cal_matrix = cv2.getPerspectiveTransform(pts1,pts2)

    def _end_calibrate(self):
        self._cap.release()
        self._cam.release()
        self._root.after(2,self.parent._end_calibrate)

    def get_cal_matrix(self):
        return self.__cal_matrix

    def perspective_adj(self,img):
        new = cv2.warpPerspective(img,self.get_cal_matrix(),WINDOW_SIZE)
        return new

#################################################
# Helper functions
#################################################

def cv2tk(img,gray=False):
    #Rearrang the color channel
    if not gray:
        b,g,r = cv2.split(img)
        img = cv2.merge((r,g,b))

    # Convert the Image object into a TkPhoto object
    im = Image.fromarray(img)
    imgtk = ImageTk.PhotoImage(image=im)

    return imgtk

def check_cam():
    print("Checking Camera...")
    cam = cv2.VideoCapture(0)
    if not cam.isOpened():
        return False
    else:
        cam.release()
        cam = cv2.VideoCapture(1)
        if not cam.isOpened():
            return False
        else:
            cam.release()

    return True

"""
cap = cv2.VideoCapture(0)
cv2.namedWindow('image',cv2.WINDOW_NORMAL)
#cv2.namedWindow('mask')

while(True):
    # Capture frame-by-frame
    ret, frame = cap.read()

    # Display the resulting frame
    cv2.imshow('image',frame)
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

# When everything done, release the capture
cap.release()
cv2.destroyAllWindows()
"""

"""
cv2.namedWindow('image')
cv2.namedWindow('tracked')
cv2.setMouseCallback('image',draw_points)

img2 = cv2.imread('dot.jpg')
rows,cols,ch = img.shape

cv2.imshow('image',img)
tracked = find_dot(img)
cv2.imshow('tracked',tracked)
k = cv2.waitKey(0)

if k == 27:         # wait for ESC key to exit
    cv2.destroyAllWindows()
"""

"""
cv2.namedWindow('Pipette',cv2.WINDOW_NORMAL)
cal = Calibrate()
cal.calibrate('Pipette')
app = App('Pipette')
"""

if __name__ == "__main__":
    if not check_cam():
        print('No Camera!')
    else:
        root = tkinter.Tk()
        app = App(root)
        app._root.mainloop()

