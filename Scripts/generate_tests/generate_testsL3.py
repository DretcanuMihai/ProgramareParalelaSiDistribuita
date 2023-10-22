import random

CASES = [(1000, 1000), (100, 100000)]


def generate_number(digit_count):
    number = ""
    for j in range(digit_count):
        if j == 0:
            number += str(random.randint(1, 9))
        else:
            number += str(random.randint(0, 9))
    return number


def write_number(number, filename):
    file = open(filename,'w')
    file.write(str(len(number)))
    file.write("\n")
    file.write(number)
    file.close()


for (case, i) in zip(CASES, range(len(CASES))):
    nr1 = generate_number(case[0])
    nr2 = generate_number(case[1])
    write_number(nr1, str(i+1) + "_Numar1.txt")
    write_number(nr2, str(i+1) + "_Numar2.txt")
