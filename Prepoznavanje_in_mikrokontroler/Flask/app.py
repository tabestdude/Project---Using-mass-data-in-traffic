from flask import Flask, request, jsonify # uvozi knižnico flask za flask
import requests # uvozi knjižnico requests za delo s spletnimi zahtevami
import cv2   # uvozi knjižnico OpenCV za računalniški vid
import numpy # uvozi knjižnico NumPy za numerične operacije
import pandas # uvozi knjižnico pandas za obdelavo in analizo podatkov
import pickle 
import base64
from sklearn.model_selection import train_test_split   # uvozi funkcijo train_test_split za razdelitev podatkov
from sklearn.neighbors import KNeighborsClassifier   # uvozi razred KNeighborsClassifier za k najbližjih sosedov
from sklearn.metrics import accuracy_score # uvozi funkcijo accuracy_score iz modula metrics v knjižnici scikit-learn
from sklearn.metrics import classification_report   # uvozi funkcijo classification_report iz modula metrics v knjižnici scikit-learn
from skimage.feature import local_binary_pattern
import serial
import json
from serial.serialutil import SerialException
import struct
from threading import Thread, Lock

com = 'COM5'

lock = Lock()

app = Flask(__name__)

@app.route('/gpsData', methods=['POST'])
def getGpsData():
    ser = serial.Serial(com, 9600)
    counter = 0
    xArray = []
    yArray = []
    zArray = []
    # start the board
    data = struct.pack('<BBB', 0xAA, 0xAB, 0x01)
    try:
        ser.write(data)
    except Exception as e:
        print("An error occurred: ", e)
    # read 10 times
    try:
        while counter < 15:
            while ser.in_waiting:
                data = ser.read(8)
                dataS = struct.unpack('<BBBBBBBB', data)
                if dataS[0] == 0xAA and dataS[1] == 0xAB:
                        xArray.append((dataS[3] << 8) | dataS[2])
                        yArray.append((dataS[5] << 8) | dataS[4])
                        zArray.append((dataS[7] << 8) | dataS[6])
                        counter += 1
    except Exception as e:
        print("An error occurred: ", e)

    # stop the board and close the serial port
    data = struct.pack('<BBB', 0xAA, 0xAB, 0x00)
    try:
        ser.write(data)
    except Exception as e:
        print("An error occurred: ", e)
    finally:
        ser.close()

    recievedData = request.get_json()

    dataToSend = {
        'accX': xArray,
        'accY': yArray,
        'accZ': zArray,
        'longitude': recievedData.get('longitude'),
        'latitude': recievedData.get('latitude'),
        'ownerId': recievedData.get('ownerId')
    }

    # Convert the dictionary to a JSON object
    jsonDataToSend = json.dumps(dataToSend)
    response = requests.post('http://127.0.0.1:3001/roadState', data=jsonDataToSend)

    if response.status_code == 200:
        return jsonify(Confirmation=200)
    else:
        return jsonify(Error=400)

    
@app.route('/predict', methods=['POST'])
def predictFromImage():

    def lbp(image):
        radius = 1
        neighbors = 8
        lbp_image = local_binary_pattern(image, radius, neighbors)
        return lbp_image

    def hog(image):
        resized_image = cv2.resize(image, (100, 100))
        win_size = (100, 100)       # Size of the detection window
        block_size = (8, 8)      # Size of the block used for normalization
        block_stride = (4, 4)      # Stride between blocks
        cell_size = (8, 8)         # Size of cells used for computing histograms
        nbins = 9                  # Number of bins in the histogram

        hog = cv2.HOGDescriptor(win_size, block_size, block_stride, cell_size, nbins)
        hog_features = hog.compute(resized_image)
        return hog_features
    
    
    
    def decode_image(base64_string):
        decoded_data = base64.b64decode(base64_string)
        np_data = numpy.frombuffer(decoded_data, numpy.uint8)
        image = cv2.imdecode(np_data, cv2.IMREAD_COLOR)
        return image
    
    if 'image' not in request.json:
        print('No image part')
        return jsonify(error='No image part in the request'), 400

    image_base64 = request.json['image']
    
    # decode the base64 string back into an image
    image = decode_image(image_base64)

    if image is not None:
        image = cv2.resize(image, (100, 100))
        gray_image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    
    HOG_features = hog(gray_image)
    LBP_features = lbp(gray_image)

    HOG_flat = HOG_features.flatten()
    LBP_flat = LBP_features.flatten()

    combined_features = numpy.hstack((HOG_flat, LBP_flat))

    test_dataframe = pandas.DataFrame([combined_features])
    test_dataframe.fillna(test_dataframe.mean(), inplace=True)
    cleaned_test_features = test_dataframe.values.tolist()

    if not cleaned_test_features:
        print("No test features found.")
        exit()

    with open('trained_model.pkl', 'rb') as file:
        model = pickle.load(file)

    prediction = model.predict(cleaned_test_features)[0]

    # Return the prediction as JSON
    return jsonify(prediction=prediction)

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0')