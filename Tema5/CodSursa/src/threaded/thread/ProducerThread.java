package threaded.thread;


import threaded.CyclicCountingCondition;
import threaded.ConcurrentQueue;

import java.io.BufferedReader;
import java.io.FileReader;

public class ProducerThread extends Thread {

    private final int firstFileIndex;

    private final int lastFileIndex;

    protected final ConcurrentQueue<String> queue;

    protected final CyclicCountingCondition finishedProducingCondition;

    public ProducerThread(int firstFileIndex, int lastFileIndex, ConcurrentQueue<String> queue,
                          CyclicCountingCondition finishedProducingCondition) {
        this.firstFileIndex = firstFileIndex;
        this.lastFileIndex = lastFileIndex;
        this.queue = queue;
        this.finishedProducingCondition = finishedProducingCondition;
    }

    @Override
    public void run() {
        try {
            for (int i = firstFileIndex; i < lastFileIndex; i++) {
                BufferedReader reader = new BufferedReader(new FileReader(i + ".txt"));
                String line = reader.readLine();
                while (line != null) {
                    queue.push(line);
                    line = reader.readLine();
                }
                reader.close();
            }
            finishedProducingCondition.signal();
        } catch (Exception ignored) {
        }
    }
}
