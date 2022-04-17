import cv2
import numpy as np
import math
from os.path import dirname, join

def main():

    ## Read
    #path = r'Documents/wound.PNG'
    #path = 'C:/Users/annik/wound.png'
    path = join(dirname(__file__), 'wound2.png')
    frame = cv2.imread(path)
    print(frame)

    ## convert to hsv
    hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)

    ## mask of green (36,0,0) ~ (70, 255,255)
    mask = cv2.inRange(hsv, (0, 120, 70), (10, 255,255))
    # Convert BGR to HSV color scheme
    #frame = cv2.imread("wound.png")
    #hsv = cv2.imread("wound.png", cv2.COLOR_RGB2HSV)
    #hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)

    # define range of wound red color in HSV

    #lower_red = np.array([0,120,70])
    #upper_red = np.array([10,255,255])
    
    # Threshold the HSV image to get only red colors that match with wound colors
    #mask = cv2.inRange(hsv, lower_red, upper_red)

    # Bitwise-AND mask and original image
    res = cv2.bitwise_and(frame,frame, mask= mask)
    
    font = cv2.FONT_HERSHEY_SIMPLEX
    cv2.putText(frame,'Scan the wound in the frame: ',(0,50), font, 1, (99,74,154), 3, cv2.LINE_AA)
    
    # Calculating percentage area
    try: 
        contours,hierarchy= cv2.findContours(mask,cv2.RETR_TREE,cv2.CHAIN_APPROX_SIMPLE)
        cnt = max(contours, key = lambda x: cv2.contourArea(x))

        epsilon = 0.0005*cv2.arcLength(cnt,True)
        approx= cv2.approxPolyDP(cnt,epsilon,True)

        areacnt = cv2.contourArea(cnt)
        arearatio=((areacnt)/208154)*100
        
        boxes = []
        for c in cnt:
            (x, y, w, h) = cv2.boundingRect(c)
            boxes.append([x,y, x+w,y+h])

        boxes = np.asarray(boxes)
        # need an extra "min/max" for contours outside the frame
        left = np.min(boxes[:,0])
        top = np.min(boxes[:,1])
        right = np.max(boxes[:,2])
        bottom = np.max(boxes[:,3])
        
        cv2.rectangle(frame, (left,top), (right,bottom), (255, 0, 0), 2)
        
    except:
        pass
    
    # Press SpaceBar for 
    '''if m==32:
        print("The area of the wound is: ", arearatio * 0.6615, "cm squared.")
        print("The area of the Custom-Aid is: ", (right - left)*(bottom - top)*2.989*pow(10, -4), "cm squared.")
        print("The length is equal to: ", (right - left) / 95.23, "cm.")
        print("The width is equal to: ", (bottom - top) / 95.23, "cm.")'''
    return "The area of the bandage should be: "+str((right-left)*(bottom-top)*2.989*pow(10, -4))+" cm squared.\nLength: "+str((right-left)/57.8)+" cm, Width: "+ str((bottom - top) / 57.8)+" cm."
    #return arearatio, (right - left)*(bottom - top)*2.989*pow(10, -4), (right - left) / 95.23, (bottom - top) / 95.23
        
#cv2.destroyAllWindows()