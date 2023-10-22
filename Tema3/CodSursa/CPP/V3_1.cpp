#include <chrono>
#include <fstream>
#include <iostream>
#include <string>
#include "utils.h"
#include "mpi.h"

using std::chrono::duration;
using std::chrono::steady_clock;
using std::cout;
using std::ifstream;
using std::ofstream;
using std::string;


/// general comment: tag 0 is for number related exchanges
/// tag 1 is for carry flag related exchanges
/// this is done so i don't have to add extra logic
int main(int argc, char* argv[])
{
	int myRank;
	int worldSize;

	MPI_Init(&argc, &argv);
	MPI_Comm_size(MPI_COMM_WORLD, &worldSize);
	MPI_Comm_rank(MPI_COMM_WORLD, &myRank);

	steady_clock::time_point startWatch, endWatch;
	double time;
	if (myRank == 0) {
		startWatch = std::chrono::high_resolution_clock::now();
	}

	int idLastProcess = worldSize - 1;
	big_number N1, N2;
	bool receivedCarry, carryToSend;
	MPI_Request receiveCarryRequest, sendCarryRequest;

	if (myRank == 0) {
		ifstream streamN1(FIRST_NUMBER_FILE_NAME);
		ifstream streamN2(SECOND_NUMBER_FILE_NAME);

		streamN1 >> N1.size;
		streamN2 >> N2.size;

		N1.digits = new char[N1.size];
		N2.digits = new char[N2.size];
		
		//we need the bigger number (by digit count) - the bigger number is N1 in the worker processes
		big_number& biggerNumber = N1.size > N2.size ? N1 : N2;
		big_number& smallerNumber = N1.size > N2.size ? N2 : N1;
		ifstream& biggerStream = N1.size > N2.size ? streamN1 : streamN2;
		ifstream& smallerStream = N1.size > N2.size ? streamN2 : streamN1;

		int whole = biggerNumber.size / idLastProcess;
		int remainder = biggerNumber.size % idLastProcess;
		int currentPosition = 0;
		int currentSize = whole;
		int smallerCurrentSize;

		//this for reads the numbers from files and sends them
		for (int idCurrentProcess = 1; idCurrentProcess < worldSize; idCurrentProcess++) {
			if (remainder > 0) {
				remainder--;
				currentSize++;
			}
			for (int i = currentPosition; i < currentPosition + currentSize; i++) {
				biggerStream >> biggerNumber.digits[i];
				biggerNumber.digits[i] -= '0';
			}
			MPI_Send(&currentSize, 1, MPI_INT, idCurrentProcess, 0, MPI_COMM_WORLD);
			MPI_Send(biggerNumber.digits + currentPosition, currentSize, MPI_CHAR, idCurrentProcess, 0, MPI_COMM_WORLD);

			smallerCurrentSize = currentSize;
			if (currentPosition + smallerCurrentSize > smallerNumber.size) {
				smallerCurrentSize = smallerNumber.size - currentPosition;
				if (smallerCurrentSize < 0) {
					smallerCurrentSize = 0;
				}
			}
			for (int i = currentPosition; i < currentPosition + smallerCurrentSize; i++) {
				smallerStream >> smallerNumber.digits[i];
				smallerNumber.digits[i] -= '0';
			}
			MPI_Send(&smallerCurrentSize, 1, MPI_INT, idCurrentProcess, 0, MPI_COMM_WORLD);
			// if we don't have digits to send, we don't send them (i tried and it gave out an error)
			if (smallerCurrentSize > 0) {
				MPI_Send(smallerNumber.digits + currentPosition, smallerCurrentSize, MPI_CHAR, idCurrentProcess, 0, MPI_COMM_WORLD);
			}

			currentPosition += currentSize;
			currentSize = whole;
		}
		streamN1.close();
		streamN2.close();

		whole = biggerNumber.size / idLastProcess;
		remainder = biggerNumber.size % idLastProcess;
		currentPosition = 0;
		currentSize = whole;

		//this for receives the result
		for (int idCurrentProcess = 1; idCurrentProcess < worldSize; idCurrentProcess++) {
			if (remainder > 0) {
				remainder--;
				currentSize++;
			}

			MPI_Recv(biggerNumber.digits + currentPosition, currentSize, MPI_CHAR, idCurrentProcess, 0, MPI_COMM_WORLD, MPI_STATUSES_IGNORE);

			currentPosition += currentSize;
			currentSize = whole;
		}

		//we need to receive the final carry from the last process
		MPI_Recv(&receivedCarry, 1, MPI_C_BOOL, idLastProcess, 1, MPI_COMM_WORLD, MPI_STATUSES_IGNORE);

		writeBigNumberWithCarry(OUTPUT_NUMBER_FILE_NAME, biggerNumber, receivedCarry);
	}
	else {
		//we create a request for receiving the carry in time, so we don't block the processes that sent it and we have it prepared for later
		//worker 1 doesn't receive a carry so it doesn't wait
		if (myRank != 1) {
			MPI_Irecv(&receivedCarry, 1, MPI_C_BOOL, myRank - 1, 1, MPI_COMM_WORLD, &receiveCarryRequest);
		}

		MPI_Recv(&N1.size, 1, MPI_INT, 0, 0, MPI_COMM_WORLD, MPI_STATUSES_IGNORE);
		N1.digits = new char[N1.size];
		MPI_Recv(N1.digits, N1.size, MPI_CHAR, 0, 0, MPI_COMM_WORLD, MPI_STATUSES_IGNORE);

		MPI_Recv(&N2.size, 1, MPI_INT, 0, 0, MPI_COMM_WORLD, MPI_STATUSES_IGNORE);
		N2.digits = new char[N2.size];
		if (N2.size > 0) {
			MPI_Recv(N2.digits, N2.size, MPI_CHAR, 0, 0, MPI_COMM_WORLD, MPI_STATUSES_IGNORE);
		}
		//all the carries are also sent async because we can't know when the receiving process catches some CPU time
		//i tested this, and it's true, it spends more time with normal send
		if (myRank == 1) {
			carryToSend = N1.add(N2);
			MPI_Isend(&carryToSend, 1, MPI_C_BOOL, (myRank + 1) % worldSize, 1, MPI_COMM_WORLD, &sendCarryRequest);
			MPI_Send(N1.digits, N1.size, MPI_CHAR, 0, 0, MPI_COMM_WORLD);
			MPI_Wait(&sendCarryRequest, MPI_STATUS_IGNORE);
		}
		else {
			bool offByOne;
			carryToSend = N1.addOffByOne(N2, offByOne);
			if (!offByOne) {
				//if we're not off by one, we know the carry won't change whatever the received carry will be, so we can send it directly as it is
				MPI_Isend(&carryToSend, 1, MPI_C_BOOL, (myRank + 1) % worldSize, 1, MPI_COMM_WORLD, &sendCarryRequest);
				//we wait for the carry
				MPI_Wait(&receiveCarryRequest, MPI_STATUS_IGNORE);
				if (receivedCarry) {
					N1.inc();
				}
				MPI_Send(N1.digits, N1.size, MPI_CHAR, 0, 0, MPI_COMM_WORLD);
				MPI_Wait(&sendCarryRequest, MPI_STATUS_IGNORE);
			}
			else {
				//if we're off by one, we need to wait for the carry before we can send it
				MPI_Wait(&receiveCarryRequest, MPI_STATUS_IGNORE);
				if (receivedCarry) {
					carryToSend = N1.inc();
				}
				MPI_Isend(&carryToSend, 1, MPI_C_BOOL, (myRank + 1) % worldSize, 1, MPI_COMM_WORLD, &sendCarryRequest);
				MPI_Send(N1.digits, N1.size, MPI_CHAR, 0, 0, MPI_COMM_WORLD);
				MPI_Wait(&sendCarryRequest, MPI_STATUS_IGNORE);
			}
		}
	}
	N1.destroy();
	N2.destroy();
	if (myRank == 0) {
		endWatch = std::chrono::high_resolution_clock::now();
		time = duration<double, std::milli>(endWatch - startWatch).count();
		cout << "\n" << time;
	}
	MPI_Finalize();
	return 0;
}