package com.ppd.p1.model.dto.response;

import java.io.Serializable;

public class AppointmentResponse implements Serializable {
    private Integer id;

    private String message;

    public AppointmentResponse() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "AppointmentResponse{" +
                "id=" + id +
                ", message='" + message + '\'' +
                '}';
    }
}
