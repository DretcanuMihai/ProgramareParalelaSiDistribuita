#include <chrono>
#include <iostream>
#include "utils.h"

using std::chrono::duration;
using std::chrono::steady_clock;
using std::cout;

int main(int argc, char* argv[])
{
	steady_clock::time_point startWatch, endWatch;
	double time;
	startWatch = std::chrono::high_resolution_clock::now();

	big_number firstNumber = readBigNumber(FIRST_NUMBER_FILE_NAME);
	big_number secondNumber = readBigNumber(SECOND_NUMBER_FILE_NAME);
	
	//we need the bigger number (by digit count) for addition such that the return of "add" function is truly a carry
	big_number& biggerNumber = firstNumber.size > secondNumber.size ? firstNumber : secondNumber;
	big_number& smallerNumber = firstNumber.size > secondNumber.size ? secondNumber : firstNumber;
	bool carry= biggerNumber.add(smallerNumber);

	writeBigNumberWithCarry(OUTPUT_NUMBER_FILE_NAME, biggerNumber, carry);

	firstNumber.destroy();
	secondNumber.destroy();

	endWatch = std::chrono::high_resolution_clock::now();
	time = duration<double, std::milli>(endWatch - startWatch).count();
	cout << "\n" << time;
	return 0;
}