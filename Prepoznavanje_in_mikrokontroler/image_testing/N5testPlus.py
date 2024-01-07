import cv2   # uvozi knjižnico OpenCV za računalniški vid
import numpy # uvozi knjižnico NumPy za numerične operacije
import pandas # uvozi knjižnico pandas za obdelavo in analizo podatkov
import glob   # uvozi modul glob za obdelavo datotek
import pickle # uvozi funkcio joblib za shranjevanje modela
from numba import njit   # uvozi modul numba za kompilacijo ob izvajanju
from sklearn.model_selection import train_test_split   # uvozi funkcijo train_test_split za razdelitev podatkov
from sklearn.neighbors import KNeighborsClassifier   # uvozi razred KNeighborsClassifier za k najbližjih sosedov
from sklearn.metrics import accuracy_score # uvozi funkcijo accuracy_score iz modula metrics v knjižnici scikit-learn
from sklearn.metrics import classification_report   # uvozi funkcijo classification_report iz modula metrics v knjižnici scikit-learn
# from sklearn.externals import joblib # uvozi funkcio joblib za shranjevanje modela
# --------------------------------------------------------------------------------------------
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
# --------------------------------------------------------------------------------------------
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
# --------------------------------------------------------------------------------------------
# vhodne slike

# velkiost celic, velikost blokov, segmenti, procent
cellsSize = int()
blocksSize = int()
segments = int()
lowPercentile = int()
# vhod
print("Machine learning.")
        #input("Press ENTER to start.")
        #print("Input cells size:")
        #cellsSize = int(input())
        #print("Input blocks size:")
        #blocksSize = int(input())
        #print("Input number of segments:")
        #segments = int(input())
        #print("Input learning percent %:")
        #lowPercentile = int(input())

cellsSize = 8
blocksSize = 2
segments = 9
#lowPercentile = int(0.8 * len(inputPictures))
lowPercentile = 80


# naloži se shranjen model
with open('trained_model.pkl', 'rb') as file:
    model = pickle.load(file)

# pripravijo se testne slike
test_images = [cv2.imread(file) for file in glob.glob("C:/Users/Luka/Desktop/FERI/PROJEKTNANaloga/Prepoznavanje/testImages/*.jpg")]

processed_test_images = []
for image in test_images:
    if image is not None:
        image = cv2.resize(image, (120, 140))
        gray_image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        processed_test_images.append(gray_image)

# pridobivanje HOG in LBP podrobnosti za testne slike
HOG_test_features = []
LBP_test_features = []

for image in processed_test_images:
    HOG_features = HOGhistogramOrientatedGradients(image, cellsSize, blocksSize, segments)
    LBP_features = LBPlocBinPattern(image)
    HOG_test_features.append(HOG_features)
    LBP_test_features.append(LBP_features)

# združi HOG in LBP podrobnosti za vsako testno sliko
combined_test_features = []
for HOG_feat, LBP_feat in zip(HOG_test_features, LBP_test_features):
    HOG_flat = HOG_feat.flatten()
    LBP_flat = LBP_feat.flatten()
    combined_features = numpy.hstack((HOG_flat, LBP_flat))
    combined_test_features.append(combined_features)

# počisti združene testne podrobnosti, da napolni NaN vrednosti z povprečjem stolpca
test_dataframe = pandas.DataFrame(combined_test_features)
test_dataframe.fillna(test_dataframe.mean(), inplace=True)
cleaned_test_features = test_dataframe.values.tolist()

if not cleaned_test_features:
    print("No test features found.")
    exit()

# predikcije z modelom
predictions = model.predict(cleaned_test_features)

# prikaz slik in ugibanja
for image, prediction in zip(test_images, predictions):
    resizedImage = cv2.resize(image, (120, 140))
    cv2.imshow("Test Image", resizedImage)
    if prediction == "Luka":
        print("Prediction: Luka")
    elif prediction == "Ales":
        print("Prediction: Ales")
    else:
        print("Prediction: Other")
    cv2.waitKey(0)

#cv2.imshow("Test result.", pictures[0])
cv2.waitKey(0)
cv2.destroyAllWindows()