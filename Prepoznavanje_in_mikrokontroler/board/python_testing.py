import json

xArray = [1, 2, 3, 4, 5, 6, 7, 8, 9]
yArray = [1, 2, 3, 4, 5, 6, 7, 8, 9]
zArray = [1, 2, 3, 4, 5, 6, 7, 8, 9]

# Create a dictionary
dataToSend = {
    'x': xArray,
    'y': yArray,
    'z': zArray
}

# Convert the dictionary to a JSON object
jsonDataToSend = json.dumps(dataToSend)

print(jsonDataToSend)