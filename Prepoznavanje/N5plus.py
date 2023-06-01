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
def splitData(pictures, lowPercentile):
    # int deljenje
    halfDistance = len(pictures) // 2
    # Lukas procent
    percentLuka = int(halfDistance * (lowPercentile / 100))
    # Aless procent
    percentAles = int(halfDistance * (lowPercentile / 100))

    # seznam za učenje (luka)
    learnLuka = pictures[:percentLuka]
    # seznam za učenje (ales)
    learnAles = pictures[halfDistance:halfDistance+percentAles]
    # seznam za testiranje (luka)
    testLuka = pictures[percentLuka:halfDistance]
    # seznam za testiranje (ales)
    testAles = pictures[halfDistance+percentAles:]
    
    # končni seznam za učenje
    learn = learnLuka + learnAles
    # končni seznam za testiranje
    test = testLuka + testAles
    
    return learn, test
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
inputPictures = [cv2.imread(file) for file in glob.glob("C:/Users/Luka/Desktop/FERI/PROJEKTNANaloga/Prepoznavanje/dataset/**/*.jpg")]
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

# če slika ni None, jo zmanjšamo na velikost 100x100 in jo dodamo v seznam pictures
pictures=[]

for picture in inputPictures:
    if(picture is not None):
        picture=cv2.resize(picture,(120,120))
        pictures.append(picture)
    else:
        print(picture)

print("Length images", len(pictures))

# hog in lbp polja za Luka
HOGLuka=[]
LBPLuka=[]

for picture in pictures:
    grayscalePicture = cv2.cvtColor(picture, cv2.COLOR_BGR2GRAY)
    HOGLuka.append(HOGhistogramOrientatedGradients(grayscalePicture, cellsSize, blocksSize, segments))
    LBPLuka.append(LBPlocBinPattern(grayscalePicture))

# HOGLuka in LBPLuka so polja z HOG in LBP informacijami za vsako sliko
combinedLearning=[]

for HOGcurrent, LBPcurrent, in zip(HOGLuka, LBPLuka):
    # 'ravnanje' HOG in LBP
    HOGflat = HOGcurrent.flatten()
    LBPflat = LBPcurrent.flatten()
    # združenje 'porovnanih' informacij
    HOG_LBP_combined = numpy.hstack((HOGflat, LBPflat))
    combinedLearning.append(HOG_LBP_combined)

# ustvarjanje label
personList=[]

for i in range(int(len(pictures))):
    if(i < 1800):
        personList.append("Luka")
    if(i < 3600):
        personList.append("Ales")
    if(i < 5400):
        personList.append("Other") 

labels = numpy.array(personList)

# krajšemu seznamu se doda 0, če je to potrebno
maximumLength = max(len(combinedLearning), len(labels))

if len(combinedLearning) < maximumLength:
    combinedLearning += [numpy.zeros_like(combinedLearning[0])] * (maximumLength - len(combinedLearning))
elif len(labels) < maximumLength:
    labels = numpy.hstack((labels, [None] * (maximumLength - len(labels))))

# če so kje vrednosti NaN, se napolnijo z column mean (stolpec)
# ustvari pandas DataFrame iz seznama combinedLearning
combinedLearningDataFrame = pandas.DataFrame(combinedLearning)
# zamenjaj manjkajoče vrednosti (NaN) z povprečjem stolpca
combinedLearningDataFrame.fillna(combinedLearningDataFrame.mean(), inplace=True)
# pretvori DataFrame combinedLearningDataFrame nazaj v seznam seznamov
combinedLearningCleaning = combinedLearningDataFrame.values.tolist()

# razdelitev podatkov in treniranje calssifier-jev
# x za treniranje in destiranje podrobnosti (features), y za treniranje in testiranje label (labels)
xTraining, xTesting, yTraining, yTesting = train_test_split(combinedLearningCleaning, labels, test_size=(lowPercentile/100), random_state=2)
# K-Nearest neighbhours classifier z 3 'sosedi'
KNN = KNeighborsClassifier(n_neighbors=3)
# treniranja classifier-a z podatki za treniranje
KNN.fit(xTraining, yTraining)

# shrani se model
with open('trained_model.pkl', 'wb') as file:
    pickle.dump(KNN, file)


# naloži se shranjen model
with open('trained_model.pkl', 'rb') as file:
    model = pickle.load(file)

# pripravijo se testne slike
test_images = [cv2.imread(file) for file in glob.glob("C:/Users/Luka/Desktop/FERI/PROJEKTNANaloga/Prepoznavanje/testImages/*.jpg")]

processed_test_images = []
for image in test_images:
    if image is not None:
        image = cv2.resize(image, (100, 100))
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
    cv2.imshow("Test Image", image)
    if prediction == "Luka":
        print("Prediction: Luka")
    elif prediction == "Ales":
        print("Prediction: Ales")
    else:
        print("Prediction: Other")
    cv2.waitKey(0)
# predikcije na testnem delu
yPredicting = KNN.predict(xTesting)
# računanje classifier, izračun natančnosti z primerjanjem label yPredicting z pravimi labelami yTesting
predictionAccuracy = accuracy_score(yTesting, yPredicting)

# izpis ugibanja
print(f"Accuracy: {predictionAccuracy:.3f} \n")

# izpis reporta
print("Classification report:\n", classification_report(yTesting, yPredicting))

#cv2.imshow("Test result.", pictures[0])
cv2.waitKey(0)
cv2.destroyAllWindows()