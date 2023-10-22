package threads;

import utils.MatrixConvolutionSolver;

public class ConvolutionThread extends Thread {

    private final MatrixConvolutionSolver solver;

    public ConvolutionThread(MatrixConvolutionSolver solver) {
        this.solver = solver;
    }

    @Override
    public void run() {
        solver.solve();
    }
}
