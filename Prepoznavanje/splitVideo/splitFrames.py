import cv2
import os

def splitVideoFrames(videoPath, outputFolder):
    # create output folder if it doesn't exist
    if not os.path.exists(outputFolder):
        os.makedirs(outputFolder)

    # open video
    video = cv2.VideoCapture(videoPath)
    frameCount = 0
    saveCount = 0

    while True:
        # read next frame
        ret, frame = video.read()

        if not ret:
            break

        # save frame to disk
        output_path = os.path.join(outputFolder, f"frame_{saveCount}.jpg")
        cv2.imwrite(output_path, frame)
        saveCount += 1

        frameCount += 1

    video.release()
    print(f"Split {frameCount} frames. Saved {saveCount} frames to {outputFolder}.")

videoPath = "input_video.mp4"
outputFolder = "output_frames"
splitVideoFrames(videoPath, outputFolder)