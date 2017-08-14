import serial
import tkinter

ser = serial.Serial('/dev/cu.usbmodem81', 115200)
root = tkinter.Tk()

class ArduinoCom:
    def __init__(self):
        self.ser = ser

    def get_color(self):
        pass


def make_rgb(color,sum):
    color = color/sum
    color = color * 255
    return color

def break_line(str):
    str_list = str.split(';;')
    r = int(str_list[1]) * 2.3
    g = int(str_list[3]) * 0.97
    b = int(str_list[5]) * 0.95

    r = min(r,15000)
    g = min(g,14000)
    b = min(b,14000)

    #color_sum = r + g + b
    #color_sum = max(r,g,b)
    #if color_sum > 65535:
    #    color_sum = 65535
    color_sum = 15000

    #print(r,g,b)
    r = make_rgb(r,color_sum)
    g = make_rgb(g,color_sum)
    b = make_rgb(b,color_sum)
    #print(r,g,b)

    return hex(int(r)), hex(int(g)), hex(int(b))

def straight_rgb(str):
    str_list = str.split(';;')
    r = float(str_list[1])
    g = float(str_list[3])
    b = float(str_list[5])

    return hex(int(r)), hex(int(g)), hex(int(b))

def read_from():
    line = ser.readline()
    line = line.decode('ascii')
    print(line)
    if line.startswith('Red'):
        global label
        r, g, b = straight_rgb(line)
        color = [str(r)[2:],str(g)[2:],str(b)[2:]]
        for i in range(len(color)):
            if len(color[i]) < 2:
                color[i] = '0' + color[i]

        label['bg'] = '#' + color[0] + color[1] + color[2]
    root.after(100,read_from)


label = tkinter.Label(root,height=40,width=40,bg='#333333')
label.pack()
root.after(1000,read_from)
root.mainloop()

