import serial
import struct
import matplotlib.pyplot as plt
from matplotlib.animation import FuncAnimation
import math
import time

com = 'COM5'
ser = serial.Serial(com, 9600)

xOS = []
yOS = []
zOS = []


def animation(i):
    
    print("In waiting: ", ser.in_waiting)
    while ser.in_waiting:
        data = ser.read(8)
        dataS = struct.unpack('<BBBBBBBB', data)
        print(data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7])
        print(dataS[0], dataS[1], dataS[2], dataS[3], dataS[4], dataS[5], dataS[6], dataS[7])
        if dataS[0] == 0xAA and dataS[1] == 0xAB:
            x = (dataS[3] << 8) | dataS[2]
            y = (dataS[5] << 8) | dataS[4]
            z = (dataS[7] << 8) | dataS[6]
            xOS.append(x)
            yOS.append(y)
            zOS.append(z)
            print("X: ", x, "Y: ", y, "Z: ", z)

    plt.cla()
    plt.plot(xOS, label='X')
    plt.plot(yOS, label='Y')
    plt.plot(zOS, label='Z')
    plt.legend(loc='upper left')
    plt.tight_layout()

def writeTesting():
    # start the board
    ser = serial.Serial(com, 9600)
    dataS = struct.pack('<BBB', 0xAA, 0xAB, 0x01)
    try:
        ser.write(dataS)

    except Exception as e:
        print("An error occurred: ", e)
    finally:
        ser.close()

def main():
    #writeTesting()
    ani = FuncAnimation(plt.gcf(), animation, interval=100)

    plt.tight_layout()
    plt.show()


if __name__ == "__main__":
    main()

