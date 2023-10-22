package com.ppd.p1.networking.protocol;

import com.ppd.p1.model.dto.request.AppointmentRequest;
import com.ppd.p1.model.dto.request.PaymentRequest;
import com.ppd.p1.model.dto.response.AppointmentResponse;
import com.ppd.p1.model.dto.response.MessageResponse;
import com.ppd.p1.service.SuperService;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.concurrent.Callable;

import static com.ppd.p1.commons.Constants.*;


public class RequestHandler implements Callable<Response> {

    private final SuperService superService;

    private final Socket connection;

    private ObjectInputStream input;

    private ObjectOutputStream output;

    public RequestHandler(SuperService superService, Socket connection) {
        this.superService = superService;
        this.connection = connection;
        try {
            output = new ObjectOutputStream(connection.getOutputStream());
            output.flush();
            input = new ObjectInputStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * sends a response to the client
     *
     * @param response said response
     */
    public void sendResponse(Response response) {
        log("Socket:" + connection + " | Sending response:" + response);
        try {
            output.writeObject(response);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        log("Socket:" + connection + " | Sent response:" + response);
    }

    /**
     * closes the connection to the client
     */
    public void close() {
        log("Closing:" + connection);
        try {
            input.close();
            connection.close();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        log("Closed:" + connection);
    }

    @Override
    public Response call() {
        Response response = null;
        try {
            log("Socket reading:" + connection);
            Object request = input.readObject();
            log("Socket:" + connection + "Read:" + request);
            response = handleRequest((Request) request);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * handles a given request
     *
     * @param request - the request
     * @return a response
     */
    private Response handleRequest(Request request) {
        Response response = null;
        String handlerName = "handle" + request.type();
        log("HandlerName: " + handlerName);
        try {
            Method method = this.getClass().getDeclaredMethod(handlerName, Request.class);
            log("Method " + handlerName + " invoking...");
            response = (Response) method.invoke(this, request);
            log("Method " + handlerName + " invoked");
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * handles the creation of an appointment
     *
     * @param request - the request to create
     * @return a response
     */
    private Response handleMAKE_APPOINTMENT(Request request) {
        log("Inside handleMAKE_APPOINTMENT");
        AppointmentRequest appointmentRequest = (AppointmentRequest) request.data();
        AppointmentResponse response = superService.makeAppointment(appointmentRequest);
        if (response.getId() == null) {
            return new Response.Builder().type(ResponseType.ERROR).data(response).build();
        } else {
            return new Response.Builder().type(ResponseType.APPOINTMENT_SUCCESS).data(response).build();
        }
    }

    /**
     * handles the payment of an appointment
     *
     * @param request - the request to pay
     * @return a response
     */
    private Response handleMAKE_PAYMENT(Request request) {
        log("Inside handleMAKE_PAYMENT");
        PaymentRequest paymentRequest = (PaymentRequest) request.data();
        MessageResponse response = superService.payAppointment(paymentRequest);
        if (response.getMessage().equals("error")) {
            return new Response.Builder().type(ResponseType.ERROR).data(response).build();
        } else {
            return new Response.Builder().type(ResponseType.OK).data(response).build();
        }
    }

    /**
     * handles the cancellation of an appointment
     *
     * @param request - the request to cancel
     * @return a response
     */
    private Response handleCANCEL_APPOINTMENT(Request request) {
        log("Inside handleCANCEL_APPOINTMENT");
        PaymentRequest paymentRequest = (PaymentRequest) request.data();
        MessageResponse response = superService.cancelAppointment(paymentRequest);
        if (response.getMessage().equals("error")) {
            return new Response.Builder().type(ResponseType.ERROR).data(response).build();
        } else {
            return new Response.Builder().type(ResponseType.OK).data(response).build();
        }
    }

    private void log(String message){
        if(SERVER_HANDLER_LOGGING_ENABLED){
            System.out.println(message);
        }
    }
}
