from PIL import Image, ImageTk, ImageFilter
import tkinter

TOLERANCE = 100

class Paint:

    def __init__(self,im,root,label=None):
        self.im = im.convert('RGB')
        self.im = self.im.filter(ImageFilter.EDGE_ENHANCE)
        self.root = root
        self.pix = self.im.load()

        if label == None:
            self.container = tkinter.Label(self.root)
            self.container.pack()
            self.container.bind('<Button-1>',self.mouse_on_image)
        else:
            self.container = label

        self.display_my_img()

    def display_my_img(self):
        self.image = ImageTk.PhotoImage(self.im)
        self.container['image'] = self.image

    def distance(self,c1,c2):
        r1,g1,b1 = c1
        r2,g2,b2 = c2

        dis = abs(r1-r2) + abs(g1-g2) + abs(b1-b2)
        return dis

    def _fill_helper(self,x,y,origin,target):
        to_fill = set()
        to_fill.add((x,y))
        while len(to_fill) > 0:
            p = to_fill.pop()
            cur = self.pix[p[0],p[1]]
            if self.distance(cur,origin) > TOLERANCE:
                continue
            if cur == target:
                continue
            self.pix[p[0],p[1]] = target
            if p[0] > 0:
                to_fill.add((p[0]-1,p[1]))
            if p[1] < self.image.height()-1:
                to_fill.add((p[0],p[1]+1))
            if p[0] < self.image.width()-1:
                to_fill.add((p[0]+1,p[1]))
            if p[1] > 0:
                to_fill.add((p[0],p[1]-1))

    def fill(self,x,y,color):
        origin = self.pix[x,y]
        self._fill_helper(x,y,origin,color)
        self.display_my_img()

    def mouse_on_image(self,event):
        color = (100,0,0)
        self.fill(event.x,event.y,color)
        self.display_my_img()

if __name__ == "__main__":
    im = Image.open("paintings/Coloring-book.gif")
    root = tkinter.Tk()
    paint = Paint(im,root)
    root.mainloop()
