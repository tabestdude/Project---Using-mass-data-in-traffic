import os
from selenium import webdriver
import urllib.request

# Set the path to the ChromeDriver executable
driver_path = 'C:/Users/Luka/Downloads/chromedriver_win32'

# Initialize the Chrome driver
driver = webdriver.Chrome(executable_path=driver_path)

# Specify the URL of the website
url = 'https://thispersondoesnotexist.com/'

# Specify the number of times to repeat the process
num_images = 500

# Set the custom User-Agent header
headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'}

# Create a folder to save the images
folder_name = 'images'
os.makedirs(folder_name, exist_ok=True)

# Iterate over the specified number of times, starting from 501
for i in range(1001, num_images + 1815):
    # Open the URL
    driver.get(url)
    
    # Get the image source URL
    img_element = driver.find_element('css selector', 'img')
    img_src = img_element.get_attribute('src')
    
    # Save the image
    filename = f'image_{i}.jpg'
    file_path = os.path.join(folder_name, filename)
    
    request = urllib.request.Request(img_src, headers=headers)
    with urllib.request.urlopen(request) as response:
        with open(file_path, 'wb') as out_file:
            out_file.write(response.read())
    
    print(f'Saved {file_path}')
    
# Quit the driver
driver.quit()
