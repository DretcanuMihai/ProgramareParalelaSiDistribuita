package com.ppd.p1.networking;

import com.ppd.p1.networking.protocol.RequestHandler;
import com.ppd.p1.networking.protocol.Response;
import com.ppd.p1.service.SuperService;
import org.modelmapper.internal.Pair;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.*;

import static com.ppd.p1.commons.Constants.*;


public class Server {

    private final ExecutorService executor;

    private final int port;

    private final SuperService superService;

    private final Queue<Pair<RequestHandler, Future<Response>>> results;

    private long startTime;

    private ServerSocket server = null;

    public Server(int port, SuperService superService) {
        this.port = port;
        this.executor = Executors.newFixedThreadPool(SERVER_THREADS);
        this.superService = superService;
        this.results = new LinkedList<>();
    }

    /**
     * starts the server for a fixed duration
     */
    public void start() {
        startTime = System.currentTimeMillis();
        executor.execute(this::handleResponses);
        executor.execute(this::handleValidation);
        try {
            server = new ServerSocket(port);
            while (getElapsedTime() < RUNNING_DURATION) {
                log("Waiting for clients ...");
                Socket client = server.accept();
                log("Client connected ...");
                processRequest(client);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            log("Stopping server...");
            stop();
        }
        synchronized (results){
            results.notify();
        }
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * gets the elapsed time since the start of the server
     *
     * @return said time
     */
    public long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }

    /**
     * stops the server
     */
    public void stop() {
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * processes a request for a client
     *
     * @param client said client
     */
    private void processRequest(Socket client) {
        log("Processing client:" + client);
        RequestHandler handler = createWorker(client);
        Future<Response> result = executor.submit(handler);
        synchronized (results) {
            results.add(Pair.of(handler, result));
            results.notify();
        }
        log("Saved client:" + client);
    }

    /**
     * creates a handler for a client
     *
     * @param client said client
     */
    private RequestHandler createWorker(Socket client) {
        return new RequestHandler(superService, client);
    }

    /**
     * handles all responses and the need to send them
     */
    private void handleResponses() {
        while (true) {
            Pair<RequestHandler, Future<Response>> result;
            synchronized (results) {
                while (results.isEmpty()) {
                    if(getElapsedTime() >= RUNNING_DURATION){
                        return;
                    }
                    try {
                        results.wait();
                    } catch (InterruptedException ignored) {
                    }
                }
                result = results.remove();
            }
            RequestHandler handler = result.getLeft();
            try {
                Response response = result.getRight().get();
                if (response != null) {
                    handler.sendResponse(response);
                }
            } catch (ExecutionException | InterruptedException ignored) {
            }
            handler.close();
        }
    }

    /**
     * handles the recurrent validation of the system
     */
    private void handleValidation() {
        while (true) {
            if(getElapsedTime() >= RUNNING_DURATION){
                return;
            }
            try {
                Thread.sleep(VALIDATION_INTERVAL);
                log("Validating...");
                superService.blockAndValidate();
                log("Validated...");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void log(String message) {
        if (SERVER_LOGGING_ENABLED) {
            System.out.println(message);
        }
    }
}
