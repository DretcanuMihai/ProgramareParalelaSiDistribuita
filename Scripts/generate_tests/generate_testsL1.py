import random

CASES = [(10, 10, 3, 3), (1000, 1000, 5, 5), (10, 10000, 5, 5), (10000, 10, 5, 5)]
KERNEL_LOWER_BOUND = 0.1
KERNEL_UPPER_BOUND = 1
INPUT_LOWER_BOUND = 0
INPUT_UPPER_BOUND = 256


def generate_matrix(rows, columns, lower_bound, upper_bound):
    matrix = []
    for i in range(rows):
        row = []
        for j in range(columns):
            row.append(random.uniform(lower_bound, upper_bound))
        matrix.append(row)
    return matrix


def write_matrix(matrix, file):
    file.write(str(len(matrix)))
    file.write(" ")
    file.write(str(len(matrix[0])))
    file.write("\n")
    for row in matrix:
        for elem in row:
            file.write(str(elem))
            file.write(" ")
        file.write("\n")


for k in range(len(CASES)):
    data_pack = CASES[k]
    N = data_pack[0]
    M = data_pack[1]
    n = data_pack[2]
    m = data_pack[3]
    input_matrix = generate_matrix(N, M, INPUT_LOWER_BOUND, INPUT_UPPER_BOUND)
    kernel_matrix = generate_matrix(n, m, KERNEL_LOWER_BOUND, KERNEL_UPPER_BOUND)
    filename = "date" + str(k) + ".txt"
    output_file = open(filename, "w")
    write_matrix(input_matrix, output_file)
    write_matrix(kernel_matrix, output_file)
    output_file.close()
