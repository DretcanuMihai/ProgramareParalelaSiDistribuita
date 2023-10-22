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
	int originalNSize;
	int NSize, NPSize;
	big_number N1, N2;
	bool receivedCarry, carryToSend;

	//master process reads the numbers and completes them with 0s
	if (myRank == 0) {
		ifstream streamN1(FIRST_NUMBER_FILE_NAME);
		ifstream streamN2(SECOND_NUMBER_FILE_NAME);

		streamN1 >> N1.size;
		streamN2 >> N2.size;

		originalNSize = N1.size > N2.size ? N1.size : N2.size;
		NSize = originalNSize;
		NSize += (worldSize - NSize % worldSize) % worldSize;

		N1.digits = new char[NSize];
		for (int i = 0; i < N1.size; i++) {
			streamN1 >> N1.digits[i];
			N1.digits[i] -= '0';
		}
		for (int i = N1.size; i < NSize; i++) {
			N1.digits[i] = 0;
		}
		N1.size = NSize;

		N2.digits = new char[NSize];
		for (int i = 0; i < N2.size; i++) {
			streamN2 >> N2.digits[i];
			N2.digits[i] -= '0';
		}
		for (int i = N2.size; i < NSize; i++) {
			N2.digits[i] = 0;
		}
		N2.size = NSize;

		streamN1.close();
		streamN2.close();
	}
	//we want all the processes to know the NSize (the size of the numbers), so we broadcast it from 1
	MPI_Bcast(&NSize, 1, MPI_INT, 0, MPI_COMM_WORLD);
	N1.size = NSize;
	N2.size = NSize;
	if (myRank != 0) {
		N1.digits = new char[NSize];
		N2.digits = new char[NSize];
	}

	big_number N1Aux, N2Aux;
	NPSize = NSize / worldSize;
	N1Aux.size = NPSize;
	N2Aux.size = NPSize;
	N1Aux.digits = new char[NPSize];
	N2Aux.digits = new char[NPSize];


	MPI_Scatter(N1.digits, NPSize, MPI_CHAR, N1Aux.digits, NPSize, MPI_CHAR, 0, MPI_COMM_WORLD);
	MPI_Scatter(N2.digits, NPSize, MPI_CHAR, N2Aux.digits, NPSize, MPI_CHAR, 0, MPI_COMM_WORLD);

	carryToSend = N1Aux.add(N2Aux);

	//all the worker processes besides 0 needs to receive a carry and increment if carry is set
	if (myRank != 0) {
		MPI_Recv(&receivedCarry, 1, MPI_C_BOOL, myRank - 1, 0, MPI_COMM_WORLD, MPI_STATUSES_IGNORE);
		if (receivedCarry) {
			carryToSend = N1Aux.inc() || carryToSend;
		}
	}

	//all processes besides the last sends a carry to the next
	if (myRank != idLastProcess) {
		MPI_Send(&carryToSend, 1, MPI_C_BOOL, myRank + 1, 0, MPI_COMM_WORLD);
	}
	MPI_Gather(N1Aux.digits, NPSize, MPI_CHAR, N1.digits, NPSize, MPI_CHAR, 0, MPI_COMM_WORLD);
	//last process sends carry to 0 - it's important that this stays AFTER the gather because otherwise we'd have a deadlock - 0 waits for carry
	//only at the end
	if (myRank == idLastProcess) {
		MPI_Send(&carryToSend, 1, MPI_C_BOOL, 0, 0, MPI_COMM_WORLD);
	}

	if (myRank == 0) {
		MPI_Recv(&receivedCarry, 1, MPI_C_BOOL, idLastProcess, 0, MPI_COMM_WORLD, MPI_STATUSES_IGNORE);
		// if the current size matches original size, everything is ok
		if (N1.size == originalNSize) {
			writeBigNumberWithCarry(OUTPUT_NUMBER_FILE_NAME, N1, receivedCarry);
		}
		//otherwise, we have some extra 0s we need to delete
		else {
			N1.reduce();
			writeBigNumberWithCarry(OUTPUT_NUMBER_FILE_NAME, N1, false);
		}
	}
	N1Aux.destroy();
	N2Aux.destroy();
	N1.destroy();
	N2.destroy();
	if (myRank == 0)
	{
		endWatch = std::chrono::high_resolution_clock::now();
		time = duration<double, std::milli>(endWatch - startWatch).count();
		cout << "\n" << time;
	}
	MPI_Finalize();
	return 0;
}