import random

CASES = [(10, 1000, 50), (5, 10000, 100)]

for (case, i) in zip(CASES, range(len(CASES))):
    nr_polynoms, max_exponent, max_monoms = case
    for pol_index in range(nr_polynoms):
        filename = str(i) + "_" + str(pol_index) + ".txt"
        monom_nr = random.randint(max_monoms // 2, max_monoms)

        exponents = set()
        while len(exponents) != monom_nr:
            exponents.add(random.randint(0, max_exponent))

        monoms = map(lambda v: ((2 * random.randint(0, 1) - 1) * random.randint(1, 1000), v), list(exponents))

        file = open(filename, "w")
        file.write(str(monom_nr) + "\n")
        for monom in monoms:
            file.write(str(monom[0]))
            file.write(" ")
            file.write(str(monom[1]))
            file.write("\n")
        file.close()
