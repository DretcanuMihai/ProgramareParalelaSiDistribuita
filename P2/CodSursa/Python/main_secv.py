import numpy as np
from PIL import Image
import time

INPUT_FILE_NAME = "in.jpg"
OUTPUT_FILE_NAME = "out.jpg"


def apply_grayscale(pixels):
    """
    applies the grayscale filter on a bidimensional array of pixels
    :param pixels: is the array of pixels on which the function operates - all the modifications are applied on it
    :return: None
    """
    for row in pixels:
        for pixel in row:
            red, green, blue = pixel[0], pixel[1], pixel[2]
            new_value = min(int(0.299 * red + 0.587 * green + 0.114 * blue), 255)
            pixel[0], pixel[1], pixel[2] = new_value, new_value, new_value


input_image = Image.open(INPUT_FILE_NAME)
input_image_pixels = np.array(input_image)

start = time.time()
apply_grayscale(input_image_pixels)
end = time.time()
print(end - start)

output_image = Image.fromarray(input_image_pixels, mode="RGB")
output_image.save(OUTPUT_FILE_NAME)
