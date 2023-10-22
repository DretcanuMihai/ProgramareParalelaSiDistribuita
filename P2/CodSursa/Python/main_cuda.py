from numba import cuda
import numpy as np
from PIL import Image
import time

INPUT_FILE_NAME = "in.jpg"
OUTPUT_FILE_NAME = "out.jpg"
GRID_DETAILS = (64, 64, 1)
BLOCK_DETAILS = (32, 32, 1)


@cuda.jit
def apply_grayscale_cuda(gpu_pixels):
    """
    applies the grayscale filter on a bidimensional array of pixels allocated on the GPU
    :param gpu_pixels: is the array of pixels on which the function operates - all the modifications are applied on it
    :return: None
    """
    x_coordinate, y_coordinate = cuda.grid(2)
    if x_coordinate < len(gpu_pixels) and y_coordinate < len(gpu_pixels[0]):
        pixel = gpu_pixels[x_coordinate][y_coordinate]
        red, green, blue = pixel[0], pixel[1], pixel[2]
        new_value = min(int(0.299 * red + 0.587 * green + 0.114 * blue), 255)
        pixel[0], pixel[1], pixel[2] = new_value, new_value, new_value


input_image = Image.open(INPUT_FILE_NAME)
input_image_pixels = np.array(input_image)

start = time.time()
gpu_input_pixels = cuda.to_device(input_image_pixels)

apply_grayscale_cuda[GRID_DETAILS, BLOCK_DETAILS](gpu_input_pixels)
cuda.synchronize()
host_pixels = gpu_input_pixels.copy_to_host()

end = time.time()
print(end - start)

output_image = Image.fromarray(host_pixels, mode="RGB")
output_image.save(OUTPUT_FILE_NAME)
