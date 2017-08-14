import tkinter
import ast
import serial

active_polygon = False

class PaintPipette:

    def __init__(self,root,coord_file):
        self.root = root
        self.canvas = tkinter.Canvas(self.root,width=1300, height=1000)
        self.canvas.bind('<Button-1>', self.mouse_to_fill)
        self.draw_from_file(coord_file)
        self.canvas.pack()

    def draw_from_file(self,coord_file):
        with open(coord_file) as f:
            for line in f:
                if 'polygon' in line:
                    cor = ast.literal_eval(line[line.find('['):])
                    self.canvas.create_polygon(cor,width=4,fill='white',
                                       outline='black',smooth=1,splinesteps=24)
                elif 'oval' in line:
                    cor = ast.literal_eval(line[line.find('['):])
                    self.canvas.create_oval(cor,width=4,fill='white',outline='black')

    def mouse_to_fill(self,event):
        self.fill(event.x,event.y,'#ff0000')

    def fill(self,x,y,color):
        """
        :param x: x in canvas
        :param y: y in canvas
        :param color: color as rgb hex string: '#aabbcc'
        """
        items = self.canvas.find_overlapping(x-2, y-2,x+2,y+2)

        if len(items) == 1:
            self.canvas.itemconfigure(items[0],fill=color)
            return

        if len(items) > 1:
            for i in range(len(items)):
                items_above = self.canvas.find_above(items[i])
                if not items_above:
                    self.canvas.itemconfigure(items[i],fill=color)
                    return
                else:
                    j = 0
                    for item in items_above:
                        if item in items:
                            break
                        j += 1
                    if len(items_above) == j:
                        self.canvas.itemconfigure(items[i],fill=color)
                        return

    def get_from_arduino(self):
        #serial = serial.Serial(socket, 9600)
        incoming = self.serial.readline()


def track(event):
    canvas = event.widget
    global active_polygon
    if not active_polygon:
        canvas.create_polygon(event.x,event.y,width=7,fill='',
                            outline='red',smooth=1,tags='active',
                            splinesteps=24)
        active_polygon = True
        return
    else:
        cur_coords = canvas.coords('active')
        coords = cur_coords + [event.x,event.y]
        canvas.coords('active',tuple(coords))


def close_track(event):
    canvas = event.widget
    global active_polygon
    if not active_polygon:
        return
    else:
        with open('horse.txt','a') as f:
            f.write(str(canvas.coords('active')))
            f.write('\n')

        print(canvas.coords('active'))
        canvas.dtag('active','active')
        active_polygon = False

if __name__ == "__main__":
    root = tkinter.Tk()

    c = tkinter.Canvas(root,width=1300, height=1000)
    #c.bind('<Button-1>', fill)
    c.bind('<Button-1>', track)
    c.bind('<Button-2>', close_track)
    c.pack()

    photo = tkinter.PhotoImage(file="fish_coloring_pages.gif")
    c.create_image(650,500,image=photo)
    """
    with open('horse.txt') as f:
        for line in f:
            cor = ast.literal_eval(line)
            c.create_polygon(cor,width=4,fill='white',
                            outline='black',smooth=1,splinesteps=24)
    c.create_oval(830,430,900,500,fill='white',outline='black',width=4)
    c.create_oval(847.5,447.5,882.5,482.5,fill='black',outline='black',width=4)
    """
    #app = PaintPipette(root,'horse.txt')
    root.mainloop()