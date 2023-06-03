from flask import Flask, request, jsonify # uvozi knižnico flask za flask
import cv2   # uvozi knjižnico OpenCV za računalniški vid
import numpy # uvozi knjižnico NumPy za numerične operacije
import pandas # uvozi knjižnico pandas za obdelavo in analizo podatkov
import glob   # uvozi modul glob za obdelavo datotek
import pickle # uvozi funkcio joblib za shranjevanje modela
import base64
from sklearn.model_selection import train_test_split   # uvozi funkcijo train_test_split za razdelitev podatkov
from sklearn.neighbors import KNeighborsClassifier   # uvozi razred KNeighborsClassifier za k najbližjih sosedov
from sklearn.metrics import accuracy_score # uvozi funkcijo accuracy_score iz modula metrics v knjižnici scikit-learn
from sklearn.metrics import classification_report   # uvozi funkcijo classification_report iz modula metrics v knjižnici scikit-learn

app = Flask(__name__)

@app.route('/predict', methods=['POST'])
def predict_from_image():
    
    def LBPlocBinPattern(picture):
        height, width = picture.shape
        lbpImage = numpy.zeros((height - 2, width - 2), dtype=numpy.uint8)
        
        # iteracija po slikovnih pikslih (brez robov)
        for i in range(1, height - 1):
            for j in range(1, width - 1):
                center = picture[i, j]
                
                # pridobitev 8 sosednjih pikslov
                neigbhours = [picture[i-1, j-1], picture[i-1, j], picture[i-1, j+1],
                        picture[i, j-1],                 picture[i, j+1],
                        picture[i+1, j-1], picture[i+1, j], picture[i+1, j+1]]
                
                # izračun binarnega vzorca
                binaryPattern = ''
                for neighbour in neigbhours:
                    binaryPattern += '1' if neighbour >= center else '0'
                
                # Pretvorba binarnega vzorca v vrednost LBP
                lbpValue = int(binaryPattern, 2)
                
                # Dodelitev vrednosti LBP na ustrezno mesto v LBP sliki
                lbpImage[i-1, j-1] = lbpValue
    
        return lbpImage
    
    def HOGhistogramOrientatedGradients(grayScalePicture, cellsSize, blocksSize, segments):
        # Izračunaj gradient v smeri x in y
        gx = numpy.gradient(grayScalePicture, axis=1)
        gy = numpy.gradient(grayScalePicture, axis=0)
        
        # Izračunaj magnitudo in orientacijo gradientov
        magnitude = numpy.sqrt(gx ** 2 + gy ** 2)
        orientation = numpy.arctan2(gy, gx) * (180 / numpy.pi) % 180

        # Pridobi dimenzije sivinske slike
        height, width = grayScalePicture.shape
        
        # Izračunaj število celic v smeri x in y
        num_cells_y = height // cellsSize
        num_cells_x = width // cellsSize
        
        # Izračunaj velikost histograma za vsak blok
        hist_size = blocksSize * blocksSize * segments
        
        features = []

        # Iteriraj čez celice in bloke
        for y in range(num_cells_y - blocksSize + 1):
            for x in range(num_cells_x - blocksSize + 1):
                block_hist = numpy.zeros(hist_size)

                # Iteriraj čez celice znotraj posameznega bloka
                for i in range(blocksSize):
                    for j in range(blocksSize):
                        # Pridobi orientacije in magnitude znotraj celice
                        cell_orientations = orientation[y + i: y + i + cellsSize, x + j: x + j + cellsSize]
                        cell_magnitudes = magnitude[y + i: y + i + cellsSize, x + j: x + j + cellsSize]
                        
                        # Izračunaj histogram orientacij z uteženimi magnitudami
                        hist, _ = numpy.histogram(cell_orientations, bins=segments, range=(0, 180), weights=cell_magnitudes)
                        
                        # Akumuliraj histogram v histogram bloka
                        block_hist[i * blocksSize * segments + j * segments: (i * blocksSize * segments + j * segments) + segments] = hist

                # Dodaj histogram bloka v seznam značilk
                features.append(block_hist)

        # Združi vse histograme blokov v vektor značilk
        return numpy.concatenate(features)
    
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

    # Now you have the image. You can process it as in your original code.
    
    # velikost celic, velikost blokov, segmenti, percentil
    cellsSize = 8
    blocksSize = 2
    segments = 9

    # Resize and convert the image
    image = cv2.resize(image, (120, 140))
    gray_image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    # Get HOG and LBP features
    HOG_features = HOGhistogramOrientatedGradients(gray_image, cellsSize, blocksSize, segments)
    LBP_features = LBPlocBinPattern(gray_image)

    # Combine features
    HOG_flat = HOG_features.flatten()
    LBP_flat = LBP_features.flatten()
    combined_features = numpy.hstack((HOG_flat, LBP_flat))

    # Clean combined features
    dataframe = pandas.DataFrame([combined_features])
    dataframe.fillna(dataframe.mean(), inplace=True)
    cleaned_features = dataframe.values.tolist()

    if not cleaned_features:
        return jsonify(error="No features found."), 400

    with open('trained_model.pkl', 'rb') as file:
        model = pickle.load(file)
        
    # Make predictions
    prediction = model.predict(cleaned_features)[0]

    # Return the prediction as JSON
    return jsonify(prediction=prediction)
    
if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0')