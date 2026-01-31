from PIL import Image
import os

for file in os.listdir():
    if not(str(file).find(".png") > 0): continue
    im = Image.open(file)
    pixels = im.load()
    w = im.size[0]
    h = im.size[1]

    output = ""+str(w)+","+str(h)+";"

    for x in range(w):
        for y in range(h):
            if pixels[x,y] == (255,255,255):
                output += "1"
            else: output += "0"
    output = "\""+file.removesuffix(".png")+";"+output+"\""
    print(output)
    
