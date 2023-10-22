package threaded.thread;


import polynomial.PolynomialUtils;
import polynomial.entity.Polynomial;
import threaded.ConcurrentQueue;
import threaded.CyclicCountingCondition;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ProducerThread extends Thread{

    private final int numberOfFiles;

    private final ConcurrentQueue<String> queue;

    private final CyclicCountingCondition finishedConsumingCondition;

    private final Polynomial result;

    public ProducerThread(int numberOfFiles, ConcurrentQueue<String> queue,
                          CyclicCountingCondition finishedConsumingCondition, Polynomial result) {
        this.numberOfFiles = numberOfFiles;
        this.queue = queue;
        this.finishedConsumingCondition = finishedConsumingCondition;
        this.result = result;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < numberOfFiles; i++) {
                BufferedReader reader = new BufferedReader(new FileReader(i + ".txt"));
                String line = reader.readLine();
                while (line != null) {
                    queue.push(line);
                    line = reader.readLine();
                }
                reader.close();
            }

            for (int i = 0; i < finishedConsumingCondition.getCapacity(); i++) {
                queue.push(null);
            }

            finishedConsumingCondition.await();
            PolynomialUtils.writePolynomialToFile(result, "out.txt");
        } catch (IOException ignored) {
        }
    }
}
