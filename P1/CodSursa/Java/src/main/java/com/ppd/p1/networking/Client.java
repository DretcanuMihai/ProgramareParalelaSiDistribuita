package com.ppd.p1.networking;


import com.ppd.p1.model.dto.request.AppointmentRequest;
import com.ppd.p1.model.dto.request.PaymentRequest;
import com.ppd.p1.model.dto.response.AppointmentResponse;
import com.ppd.p1.networking.protocol.Request;
import com.ppd.p1.networking.protocol.RequestType;
import com.ppd.p1.networking.protocol.Response;
import com.ppd.p1.networking.protocol.ResponseType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Date;
import java.util.Random;

import static com.ppd.p1.commons.Constants.*;


public class Client {

    private final String host;

    private final int port;

    private ObjectInputStream input;

    private ObjectOutputStream output;

    private Socket connection;

    private boolean running;

    private final int index;

    public Client(String host, int port, int index) {
        this.host = host;
        this.port = port;
        this.index = index;
        this.running = true;
    }

    /**
     * starts the client
     */
    public void run() {
        while (running) {
            log(index + " - Making request...");
            try {
                Thread.sleep(REQUEST_INTERVAL);
                makeRequests();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log(index + " - Made request...");
        }
    }

    /**
     * generates a random set of requests
     */
    private void makeRequests() {
        Response appointmentResponse = sendAppointmentRequest();
        if (!running || appointmentResponse == null || appointmentResponse.type() == ResponseType.ERROR) {
            return;
        }

        AppointmentResponse appointmentResponseData = (AppointmentResponse) appointmentResponse.data();
        Integer id = appointmentResponseData.getId();
        if (id == null) {
            return;
        }

        Response response = sendPaymentRequest(id);
        if (!running || response == null || response.type() == ResponseType.ERROR) {
            return;
        }

        if (new Random().nextBoolean()) {
            sendCancelRequest(id);
        }
    }

    /**
     * sends a random appointment request
     *
     * @return the response to the request
     */
    private Response sendAppointmentRequest() {
        AppointmentRequest appointmentRequest = randomRequest();
        Request request = new Request.Builder().data(appointmentRequest).type(RequestType.MAKE_APPOINTMENT).build();
        if (!initializeConnection()) {
            return null;
        }
        sendRequest(request);
        Response response = readResponse();
        closeConnection();
        return response;
    }

    /**
     * sends a payment request for a given appointment
     *
     * @param id - the id of the appointment
     * @return the response to the request
     */
    private Response sendPaymentRequest(Integer id) {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAppointmentId(id);

        Request request = new Request.Builder().data(paymentRequest).type(RequestType.MAKE_PAYMENT).build();
        if (!initializeConnection()) {
            return null;
        }
        sendRequest(request);
        Response response = readResponse();
        closeConnection();
        return response;
    }

    /**
     * sends a cancel request for a given appointment
     *
     * @param id - the id of the appointment
     * @return the response to the request
     */
    private Response sendCancelRequest(Integer id) {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAppointmentId(id);

        Request request = new Request.Builder().data(paymentRequest).type(RequestType.CANCEL_APPOINTMENT).build();
        if (!initializeConnection()) {
            return null;
        }
        sendRequest(request);
        Response response = readResponse();
        closeConnection();
        return response;
    }

    /**
     * generates a random appointment Request
     *
     * @return said request
     */
    private AppointmentRequest randomRequest() {
        Random random = new Random();
        AppointmentRequest request = new AppointmentRequest();
        request.setName(Integer.toString(random.nextInt()));
        request.setCnp(Integer.toString(random.nextInt()));
        request.setLocationId(random.nextInt(1, NR_LOCATIONS));
        request.setTreatmentId(random.nextInt(1, NR_TREATMENTS));
        request.setDate(new Date(0));
        request.setTime(random.nextInt(0, NR_TIME_UNITS - 1));
        return request;
    }

    /**
     * initializes connection to server
     *
     * @return true if successful, false otherwise
     */
    private boolean initializeConnection() {
        log(index + " - Initializing connection...");
        try {
            connection = new Socket(host, port);
            output = new ObjectOutputStream(connection.getOutputStream());
            output.flush();
            input = new ObjectInputStream(connection.getInputStream());
            log(index + " - Initialized connection...");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            running = false;
            return false;
        }
    }

    /**
     * sends a request to the server
     *
     * @param request - said request
     */
    private void sendRequest(Request request) {
        log(index + " - Sending request - " + request);
        try {
            output.writeObject(request);
            output.flush();
            log(index + " - Sent request");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * reads a response from the server
     *
     * @return said response
     */
    private Response readResponse() {
        log(index + " - Reading response");
        Response response = (new Response.Builder()).type(ResponseType.ERROR).build();
        try {
            response = (Response) input.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        log(index + " - Received response - " + response);
        return response;
    }

    /**
     * closes the connection to the server
     */
    private void closeConnection() {
        log(index + " - Closing connection...");
        try {
            input.close();
            output.close();
            connection.close();
            log(index + " - Closed connection...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void log(String message){
        if(CLIENT_LOGGING_ENABLED){
            System.out.println(message);
        }
    }
}
