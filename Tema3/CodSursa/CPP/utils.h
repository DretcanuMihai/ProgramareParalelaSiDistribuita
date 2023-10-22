#pragma once

#include <fstream>
#include <string>
#include "utils.h"

using std::ifstream;
using std::ofstream;
using std::string;

string FIRST_NUMBER_FILE_NAME = "Numar1.txt";
string SECOND_NUMBER_FILE_NAME = "Numar2.txt";
string OUTPUT_NUMBER_FILE_NAME = "Numar3.txt";

/// <summary>
/// a structure for a big number
/// size is the number of digits
/// digits is a pointer to the start of the digits array
/// the first element is the most unsignifcant digit
/// </summary>
struct big_number {
	int size;
	char* digits;

	/// <summary>
	/// adds a big_number to the current number - the number of digits remains unmodified, the result being modulo 10^size
	/// </summary>
	/// <param name="ot">said other number</param>
	/// <returns>true if the result overflowed</returns>
	bool add(const big_number& ot) {
		char carry = 0;
		int myCurrentPos = 0;

		//adds the numbers until we reach the end of either of them
		while (myCurrentPos < this->size && myCurrentPos < ot.size)
		{
			this->digits[myCurrentPos] += ot.digits[myCurrentPos] + carry;
			carry = this->digits[myCurrentPos] / 10;
			this->digits[myCurrentPos] %= 10;
			myCurrentPos++;
		}
		//if we didn't reach the end of our number and we still have a carry, we keep adding the carry
		while (myCurrentPos < this->size && carry == 1) {
			this->digits[myCurrentPos] += 1;
			carry = this->digits[myCurrentPos] / 10;
			this->digits[myCurrentPos] %= 10;
			myCurrentPos++;
		}

		//if carry is 1 and we got here (that means we reached the end of the number's digits), we have an overflow
		return carry == 1;
	}

	/// <summary>
	/// adds a big_number to the current number - the number of digits remains unmodified, the result being modulo 10^size
	/// if the result is offByOne from an overflow, set the offByOne flag
	/// </summary>
	/// <param name="ot">said other number</param>
	/// <param name="offByOne">the flag</param>
	/// <returns>true if the result overflowed</returns>
	bool addOffByOne(const big_number& ot, bool& offByOne) {
		char carry = 0;
		int myCurrentPos = 0;
		bool all9s = true; //we set this false if we find any non-9 digit

		//adds the numbers until we reach the end of either of them
		while (myCurrentPos < this->size && myCurrentPos < ot.size)
		{
			this->digits[myCurrentPos] += ot.digits[myCurrentPos] + carry;
			carry = this->digits[myCurrentPos] / 10;
			this->digits[myCurrentPos] %= 10;
			all9s = all9s && (this->digits[myCurrentPos] == 9);
			myCurrentPos++;
		}
		//if we didn't reach the end of our number and we still have a carry, we keep adding the carry
		while (myCurrentPos < this->size && carry == 1) {
			this->digits[myCurrentPos] += 1;
			carry = this->digits[myCurrentPos] / 10;
			this->digits[myCurrentPos] %= 10;
			all9s = all9s && (this->digits[myCurrentPos] == 9);
			myCurrentPos++;
		}
		//if we didn't reach the end of our number, we continue going over the digits to look if we find any non-9 digit
		while (all9s && myCurrentPos < this->size) {
			all9s = all9s && (this->digits[myCurrentPos] == 9);
			myCurrentPos++;
		}
		// if the  number is only digits of 9, we're off by one from an overflow
		offByOne = all9s; 

		//if carry is 1 and we got here (that means we reached the end of the number's digits), we have an overflow
		return carry == 1;
	}

	/// <summary>
	/// incremenets the big number - the number of digits remains unmodified, the result being modulo 10^size
	/// </summary>
	/// <returns>true if the result overflowed</returns>
	bool inc() {
		char carry = 1;
		int myCurrentPos = 0;

		while (myCurrentPos < this->size && carry == 1) {
			this->digits[myCurrentPos] += 1;
			carry = this->digits[myCurrentPos] / 10;
			this->digits[myCurrentPos] %= 10;
			myCurrentPos++;
		}

		return carry == 1;
	}

	/// <summary>
	/// reduces the big number, by eliminating all the 0s at the beggining of the number (00019, size 5 -> 19, size 4)
	/// for (00000...0, size n -> 0, size 1)
	/// </summary>
	void reduce() {
		while (this->size > 1 && (this->digits[this->size - 1] == 0)) {
			this->size--;
		}
	}

	/// <summary>
	/// destroy a big number - dealoactes the memory of the digits
	/// </summary>
	void destroy() {
		delete[] this->digits;
	}
};

/// <summary>
/// reads a big number from a file
/// number should later be destroyed by destroyBigNumber function
/// </summary>
/// <param name="filename">the filename</param>
/// <returns>said big number</returns>
big_number readBigNumber(const string& filename) {
	int size;
	char* digits;

	ifstream numberStream(filename);

	numberStream >> size;

	digits = new char[size];
	for (int i = 0; i < size; i++) {
		numberStream >> digits[i];
		digits[i] -= '0';
	}

	numberStream.close();
	return big_number{ size ,digits };
}

/// <summary>
/// writes a big number to a file, taking into account a carry flag
/// if carry is set, the number is written as if it started with an extra 1
/// </summary>
/// <param name="filename">said file</param>
/// <param name="number">said number</param>
/// <param name="carry">said carry</param>
void writeBigNumberWithCarry(const string& filename, const big_number& number, const bool carry) {
	ofstream numberStream(filename);

	if (carry) {
		numberStream << number.size + 1 << "\n";
	}
	else {
		numberStream << number.size << "\n";
	}

	for (int i = 0; i < number.size; i++) {
		numberStream << ((int)(number.digits[i]));
	}
	if (carry) {
		numberStream << 1;
	}

	numberStream.close();
}