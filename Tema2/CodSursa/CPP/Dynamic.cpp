//#include <assert.h>
//#include <barrier>
//#include <chrono>
//#include <fstream>
//#include <iostream>
//#include <string>
//#include <thread>
//#include <vector>
//
//using std::barrier;
//using std::chrono::duration;
//using std::chrono::steady_clock;
//using std::cout;
//using std::ifstream;
//using std::ofstream;
//using std::ref;
//using std::thread;
//using std::stoi;
//using std::string;
//using std::vector;
//
//const string INPUT_FILE_NAME = "date.txt";
//
//const string OUTPUT_FILE_NAME1 = "output1.txt";
//
//const string OUTPUT_FILE_NAME2 = "output2.txt";
//
///*
//structure that should keep all the information related to the matrix
//*/
//typedef struct {
//	const int rows;
//	const int columns;
//	double** matrix;
//}matrix_info;
//
///*
//I created this structure only to pass all the needed information after reading with a return
//*/
//typedef struct {
//	matrix_info inputMatrixInfo;
//	matrix_info kernelMatrixInfo;
//}matrix_package;
//
//
///**
// * Finds the correspondent for a kernel coordinate in the i/o matrix coordinates given a reference point
// *
// * currentCoordinate      - the reference points
// * kernelCoordinate       - the kernel coordinate
// * coordinateDisplacement - the displacement of the coordinate
// * upperBound             - the maximum value for the correspondent coordinate
// *
// * returns the correct coordinate
// */
//int moveCoordinate(const int currentCoordinate, const int kernelCoordinate, const int coordinateDisplacement, const int upperBound) {
//	int resultCoordinate;
//	//calculate the position of the coordinate
//	resultCoordinate = currentCoordinate + kernelCoordinate - coordinateDisplacement;
//	//correct the coordinate if out of bounds
//	if (resultCoordinate < 0) {
//		resultCoordinate = 0;
//	}
//	else if (resultCoordinate > upperBound) {
//		resultCoordinate = upperBound;
//	}
//	return resultCoordinate;
//}
//
//
///**
// * Function that solves the matrix convolution problem for a matrix
// *
// * inputMatrixInfo  - the info of the input matrix
// * outputMatrixInfo - the info of the matrix in which the results will be saved
// * kernelMatrixInfo - the info of the kernel matrix for the convolution
// * start			- the index at which the calculations should start (as if the matrix is linearized)
// * nrElements       - the number of elements that should be considered for calculation (the calculations
// *                     will be done for the element [start, start+nrElements))
// * convolutionBarrier - barrier for synchronizing - if null, it's sequential
// */
//void solveConvolution(const matrix_info& inputMatrixInfo, const matrix_info& kernelMatrixInfo,
//	const int start, const int nrElements, barrier<>* convolutionBarrier) {
//
//	double** const inputMatrix = inputMatrixInfo.matrix;
//	double** const kernelMatrix = kernelMatrixInfo.matrix;
//
//	//helpful metrics for the io matrices
//	const int ioRows = inputMatrixInfo.rows;
//	const int ioColumns = inputMatrixInfo.columns;
//	const int ioMaxRow = ioRows - 1;       //such that this calculation isn't done each time (needed for coordinate
//	const int ioMaxColumn = ioColumns - 1; //correction
//
//	//helpful metrics for the kernel
//	const int kernelRows = kernelMatrixInfo.rows;
//	const int kernelColumns = kernelMatrixInfo.columns;
//
//	//displacements metrics for moving along the kernel
//	const int rowDisplacement = kernelRows / 2;
//	const int columnDisplacement = kernelColumns / 2;
//
//	//describe the current element's position
//	int currentRow = start / ioColumns;
//	int currentColumn = start % ioColumns;
//
//	//variables used inside the while/for blocks
//	//decided to put them here such that they are not declared for every iteration
//	int elementIndex;
//	int currentCursorRow;    //the "cursor" describes the current element from the input matrix to be used in
//	int currentCursorColumn; //calculations
//	int currentKernelRow;
//	int currentKernelColumn;
//	double accumulator; //acumulates the value to be saved in the output matrix
//	double* buffer = new double[nrElements]; // buffer for storing values before saving them
//
//	for (elementIndex = 0; elementIndex < nrElements; elementIndex++) {
//		accumulator = 0;
//
//		for (currentKernelRow = 0; currentKernelRow < kernelRows; currentKernelRow++) {
//			currentCursorRow = moveCoordinate(currentRow, currentKernelRow, rowDisplacement, ioMaxRow);
//			for (currentKernelColumn = 0; currentKernelColumn < kernelColumns; currentKernelColumn++) {
//				currentCursorColumn = moveCoordinate(currentColumn, currentKernelColumn, columnDisplacement, ioMaxColumn);
//				accumulator += kernelMatrix[currentKernelRow][currentKernelColumn]
//					* inputMatrix[currentCursorRow][currentCursorColumn];
//			}
//		}
//		buffer[elementIndex] = accumulator;
//
//		//move to the next element
//		currentColumn++;
//		//when the Column coordinate exits the bounds, we move to the next row
//		if (currentColumn == ioColumns) {
//			currentRow++;
//			currentColumn = 0;
//		}
//	}
//
//	currentRow = start / ioColumns;
//	currentColumn = start % ioColumns;
//
//	if (convolutionBarrier != NULL) {
//		convolutionBarrier->arrive_and_wait();
//	}
//
//	for (elementIndex = 0; elementIndex < nrElements; elementIndex++) {
//		inputMatrix[currentRow][currentColumn] = buffer[elementIndex];
//
//		currentColumn++;
//		//when the Column coordinate exits the bounds, we move to the next row
//		if (currentColumn == ioColumns) {
//			currentRow++;
//			currentColumn = 0;
//		}
//	}
//	delete[] buffer;
//}
//
///**
// * solves the convolution problem sequentially
// *
// * @param inputMatrixInfo  - the input matrix
// * @param outputMatrixInfo - the output matrix to store the result
// * @param kernelMatrixInfo - the kernel matrix
// */
//void solveSequential(const matrix_info& inputMatrixInfo, const matrix_info& kernelMatrixInfo) {
//
//	const int size = inputMatrixInfo.rows * inputMatrixInfo.columns;
//	solveConvolution(inputMatrixInfo, kernelMatrixInfo, 0, size, NULL);
//}
//
///**
// * solves the convolution problem with threads
// *
// * @param inputMatrixInfo  - the input matrix
// * @param outputMatrixInfo - the output matrix to store the result
// * @param kernelMatrixInfo - the kernel matrix
// * @param nrThreads        - the number of threads to use
// * @throws InterruptedException if the threads fail
// */
//void solveThreaded(const matrix_info& inputMatrixInfo, const matrix_info& kernelMatrixInfo,
//	const int nrThreads) {
//
//	const int rows = inputMatrixInfo.rows;
//	const int columns = inputMatrixInfo.columns;
//	const int totalSize = rows * columns;
//
//	int whole = totalSize / nrThreads;
//	int rest = totalSize % nrThreads;
//
//	int start = 0;
//	int nrElements;
//
//	vector<thread> threads(nrThreads);
//	barrier convolutionBarrier(nrThreads);
//	for (int i = 0; i < nrThreads; i++) {
//		nrElements = whole;
//		if (rest > 0) {
//			nrElements++;
//			rest--;
//		}
//		threads[i] = thread(solveConvolution, ref(inputMatrixInfo), ref(kernelMatrixInfo),
//			start, nrElements, &convolutionBarrier);
//
//		start += nrElements;
//	}
//	for (int i = 0; i < nrThreads; i++) {
//		threads[i].join();
//	}
//}
//
///**
// * reads the current matrix from the input stream
// *
// * @param inputStream - the stream from which the matrix is read
// * @return said matrix
// */
//matrix_info readMatrix(ifstream& inputStream) {
//	int rows;
//	int columns;
//	double** matrix;
//
//	inputStream >> rows >> columns;
//
//	matrix = new double* [rows];
//	for (int i = 0; i < rows; i++) {
//		matrix[i] = new double[columns];
//		for (int j = 0; j < columns; j++) {
//			inputStream >> matrix[i][j];
//		}
//	}
//
//	return { rows,columns,matrix };
//}
//
///**
// * reads the necessary data for solving the problem from a given file
// *
// * @param filename said file's filename
// * @return a package containing the needed information (input, output and kernel matrix - the output matrix is just
// * declared, such that enough space is allocated for it)
// */
//matrix_package readPackage(const string& filename) {
//
//	ifstream inputStream(filename);
//
//	const matrix_info inputMatrixInfo = readMatrix(inputStream);
//
//	const matrix_info kernelMatrixInfo = readMatrix(inputStream);
//
//	inputStream.close();
//
//	return { inputMatrixInfo, kernelMatrixInfo };
//}
//
///**
// * writes a given matrix to a file
// *
// * @param filename     the file's name
// * @param matrixInfo   the matrix to be written
// */
//void writeMatrix(const string& filename, const matrix_info& matrixInfo) {
//	ofstream outputStream(filename);
//
//	int rows = matrixInfo.rows;
//	int columns = matrixInfo.columns;
//	double** matrix = matrixInfo.matrix;
//
//	outputStream << rows << " " << columns << "\n";
//	for (int i = 0; i < rows; i++) {
//		for (int j = 0; j < columns; j++) {
//			outputStream << matrix[i][j] << " ";
//		}
//		outputStream << "\n";
//	}
//	outputStream.close();
//}
//
///*
//function to free the heap
//@param matrixInfo - the structure with the matrix which needs to be dealocated
//*/
//void dealocate(matrix_info matrixInfo) {
//
//	double** matrix = matrixInfo.matrix;
//	int rows = matrixInfo.rows;
//
//	for (int i = 0; i < rows; i++) {
//		delete[] matrix[i];
//	}
//
//	delete[] matrix;
//}
//
///**
// * asserts that two files contain the same matrices
// *
// * @param filename1 - first file's name
// * @param filename2 - second file's name
// */
//void assertContentsEqual(const string& filename1, const string& filename2) {
//
//	ifstream inputStream1(filename1);
//	ifstream inputStream2(filename2);
//
//	const matrix_info inputMatrixInfo1 = readMatrix(inputStream1);
//	const matrix_info inputMatrixInfo2 = readMatrix(inputStream2);
//
//	int rows1 = inputMatrixInfo1.rows;
//	int columns1 = inputMatrixInfo1.columns;
//	double** matrix1 = inputMatrixInfo1.matrix;
//
//	int columns2 = inputMatrixInfo2.columns;
//	int rows2 = inputMatrixInfo2.rows;
//	double** matrix2 = inputMatrixInfo2.matrix;
//
//	assert(rows1 == rows2);
//	assert(columns1 == columns2);
//	for (int i = 0; i < rows1; i++) {
//		for (int j = 0; j < columns1; j++) {
//			assert(matrix1[i][j] == matrix2[i][j]);
//		}
//	}
//	dealocate(inputMatrixInfo1);
//	dealocate(inputMatrixInfo2);
//	inputStream1.close();
//	inputStream2.close();
//}
//
//int main(int argc, char** argv) {
//	int nrThreads = stoi(argv[1]);
//
//	matrix_package matrixPackage = readPackage(INPUT_FILE_NAME);
//	matrix_info inputMatrixInfo = matrixPackage.inputMatrixInfo;
//	matrix_info kernelMatrixInfo = matrixPackage.kernelMatrixInfo;
//
//	steady_clock::time_point startWatch, endWatch;
//	double threadedTime, sequentialTime;
//
//	//execute the threaded solution
//	if (nrThreads > 0) {
//		startWatch = std::chrono::high_resolution_clock::now();
//		solveThreaded(inputMatrixInfo, kernelMatrixInfo, nrThreads);
//		endWatch = std::chrono::high_resolution_clock::now();
//
//		writeMatrix(OUTPUT_FILE_NAME1, inputMatrixInfo);
//
//		threadedTime = duration<double, std::milli>(endWatch - startWatch).count();
//	}
//
//	//execute the sequential solution
//	startWatch = std::chrono::high_resolution_clock::now();
//	solveSequential(inputMatrixInfo, kernelMatrixInfo);
//	endWatch = std::chrono::high_resolution_clock::now();
//
//	writeMatrix(OUTPUT_FILE_NAME2, inputMatrixInfo);
//
//	sequentialTime = duration<double, std::milli>(endWatch - startWatch).count();
//
//	cout << "\n";
//	if (nrThreads > 0) {
//		//compare the outputs
//		assertContentsEqual(OUTPUT_FILE_NAME1, OUTPUT_FILE_NAME2);
//		cout << threadedTime;
//	}
//	else {
//		//if nrThreads is negative, we consider that we need the sequential execution
//		cout << sequentialTime;
//	}
//
//	//free the memory
//	dealocate(inputMatrixInfo);
//	dealocate(kernelMatrixInfo);
//
//	return 0;
//}