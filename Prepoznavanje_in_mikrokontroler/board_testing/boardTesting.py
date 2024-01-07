import serial
import struct
import matplotlib.pyplot as plt
from matplotlib.animation import FuncAnimation
import math

com = 'COM5'
ser = serial.Serial(com, 9600)
xOS = []
yOS = []
zOS = []


def animation(i):
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

def main():
    ani = FuncAnimation(plt.gcf(), animation, interval=100)

    plt.tight_layout()
    plt.show()


if __name__ == "__main__":
    main()

