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

/// Because of the way async functions work, if we have at least an async send with other sends to the same process, we have no guarantee of the
/// order they are received. Because of this, we'll use tags, more specifically:
/// 0 - larger number/N1 size
/// 1 - larger number/N1 digits
/// 2 - smaller number/N2 size
/// 3 - smaller number/N2 digits
/// 4 - carry
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

		MPI_Request* send1SizeRequests = new MPI_Request[worldSize];
		MPI_Request* send2SizeRequests = new MPI_Request[worldSize];
		MPI_Request* send1DigitsRequests = new MPI_Request[worldSize];
		MPI_Request* send2DigitsRequests = new MPI_Request[worldSize];
		MPI_Request* receiveDigitsRequests = new MPI_Request[worldSize];

		//because we're using full async sends/recvs, we don't know when data will be sent to other processes, so we can't reuse the
		//currentSize and smallerCUrrentSize variables because they might be modified - we'll use arrays
		int* biggerSizes = new int[worldSize];
		int* smallerSizes = new int[worldSize];

		int lastSmallerId = 1;

		//this for reads the numbers from files and sends them
		for (int idCurrentProcess = 1; idCurrentProcess < worldSize; idCurrentProcess++) {
			if (remainder > 0) {
				remainder--;
				currentSize++;
			}
			biggerSizes[idCurrentProcess - 1] = currentSize;
			MPI_Isend(biggerSizes + idCurrentProcess - 1, 1, MPI_INT, idCurrentProcess, 0, MPI_COMM_WORLD, send1SizeRequests + idCurrentProcess - 1);

			for (int i = currentPosition; i < currentPosition + currentSize; i++) {
				biggerStream >> biggerNumber.digits[i];
				biggerNumber.digits[i] -= '0';
			}
			MPI_Isend(biggerNumber.digits + currentPosition, currentSize, MPI_CHAR, idCurrentProcess, 1, MPI_COMM_WORLD, send1DigitsRequests + idCurrentProcess - 1);

			smallerCurrentSize = currentSize;
			if (currentPosition + smallerCurrentSize > smallerNumber.size) {
				smallerCurrentSize = smallerNumber.size - currentPosition;
				if (smallerCurrentSize < 0) {
					smallerCurrentSize = 0;
				}
			}
			smallerSizes[idCurrentProcess - 1] = smallerCurrentSize;
			MPI_Isend(smallerSizes + idCurrentProcess - 1, 1, MPI_INT, idCurrentProcess, 2, MPI_COMM_WORLD, send2SizeRequests + idCurrentProcess - 1);
			
			for (int i = currentPosition; i < currentPosition + smallerCurrentSize; i++) {
				smallerStream >> smallerNumber.digits[i];
				smallerNumber.digits[i] -= '0';
			}
			// if we don't have digits to send, we don't send them (i tried and it gave out an error)
			if (smallerCurrentSize > 0) {
				lastSmallerId = idCurrentProcess + 1;
				MPI_Isend(smallerNumber.digits + currentPosition, smallerCurrentSize, MPI_CHAR, idCurrentProcess, 3, MPI_COMM_WORLD, send2DigitsRequests + idCurrentProcess - 1);
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

		for (int idCurrentProcess = 1; idCurrentProcess < worldSize; idCurrentProcess++) {
			if (remainder > 0) {
				remainder--;
				currentSize++;
			}

			MPI_Irecv(biggerNumber.digits + currentPosition, currentSize, MPI_CHAR, idCurrentProcess, 1, MPI_COMM_WORLD, receiveDigitsRequests + idCurrentProcess - 1);

			currentPosition += currentSize;
			currentSize = whole;
		}

		//waiting for all the requests to be done - maybe the order could be tweaked
		for (int idCurrentProcess = 1; idCurrentProcess < worldSize; idCurrentProcess++) {
			MPI_Wait(send1SizeRequests + idCurrentProcess - 1, MPI_STATUS_IGNORE);
			MPI_Wait(send1DigitsRequests + idCurrentProcess - 1, MPI_STATUS_IGNORE);
			MPI_Wait(send2SizeRequests + idCurrentProcess - 1, MPI_STATUS_IGNORE);
			if (idCurrentProcess < lastSmallerId) {
				MPI_Wait(send2DigitsRequests + idCurrentProcess - 1, MPI_STATUS_IGNORE);
			}
			MPI_Wait(receiveDigitsRequests + idCurrentProcess - 1, MPI_STATUS_IGNORE);
		}

		//the carry doesn't really need to be async as we only need it after we have everything else
		MPI_Recv(&receivedCarry, 1, MPI_C_BOOL, idLastProcess, 4, MPI_COMM_WORLD, MPI_STATUSES_IGNORE);

		writeBigNumberWithCarry(OUTPUT_NUMBER_FILE_NAME, biggerNumber, receivedCarry);
		delete[] send1SizeRequests;
		delete[] send1DigitsRequests;
		delete[] send2SizeRequests;
		delete[] send2DigitsRequests;
		delete[] receiveDigitsRequests;
		delete[] biggerSizes;
		delete[] smallerSizes;
	}
	else {
		//we create a request for receiving the carry in time, so we don't block the processes that sent it and we have it prepared for later
		//worker 1 doesn't receive a carry so it doesn't wait
		if (myRank != 1) {
			MPI_Irecv(&receivedCarry, 1, MPI_C_BOOL, myRank - 1, 4, MPI_COMM_WORLD, &receiveCarryRequest);
		}

		MPI_Recv(&N1.size, 1, MPI_INT, 0, 0, MPI_COMM_WORLD, MPI_STATUSES_IGNORE);
		N1.digits = new char[N1.size];
		MPI_Recv(N1.digits, N1.size, MPI_CHAR, 0, 1, MPI_COMM_WORLD, MPI_STATUSES_IGNORE);

		MPI_Recv(&N2.size, 1, MPI_INT, 0, 2, MPI_COMM_WORLD, MPI_STATUSES_IGNORE);
		N2.digits = new char[N2.size];
		if (N2.size > 0) {
			MPI_Recv(N2.digits, N2.size, MPI_CHAR, 0, 3, MPI_COMM_WORLD, MPI_STATUSES_IGNORE);
		}
		//all the carries are also sent async because we can't know when the receiving process catches some CPU time
		//i tested this, and it's true, it spends more time with normal send
		if (myRank == 1) {
			carryToSend = N1.add(N2);
			MPI_Isend(&carryToSend, 1, MPI_C_BOOL, (myRank + 1) % worldSize, 4, MPI_COMM_WORLD, &sendCarryRequest);
			MPI_Send(N1.digits, N1.size, MPI_CHAR, 0, 1, MPI_COMM_WORLD);
			MPI_Wait(&sendCarryRequest, MPI_STATUS_IGNORE);
		}
		else {
			bool offByOne;
			carryToSend = N1.addOffByOne(N2, offByOne);
			if (!offByOne) {
				//if we're not off by one, we know the carry won't change whatever the received carry will be, so we can send it directly as it is
				MPI_Isend(&carryToSend, 1, MPI_C_BOOL, (myRank + 1) % worldSize, 4, MPI_COMM_WORLD, &sendCarryRequest);
				//we wait for the carry
				MPI_Wait(&receiveCarryRequest, MPI_STATUS_IGNORE);
				if (receivedCarry) {
					N1.inc();
				}
				MPI_Send(N1.digits, N1.size, MPI_CHAR, 0, 1, MPI_COMM_WORLD);
				MPI_Wait(&sendCarryRequest, MPI_STATUS_IGNORE);
			}
			else {
				//if we're off by one, we need to wait for the carry before we can send it
				MPI_Wait(&receiveCarryRequest, MPI_STATUS_IGNORE);
				if (receivedCarry) {
					carryToSend = N1.inc();
				}
				MPI_Isend(&carryToSend, 1, MPI_C_BOOL, (myRank + 1) % worldSize, 4, MPI_COMM_WORLD, &sendCarryRequest);
				MPI_Send(N1.digits, N1.size, MPI_CHAR, 0, 1, MPI_COMM_WORLD);
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