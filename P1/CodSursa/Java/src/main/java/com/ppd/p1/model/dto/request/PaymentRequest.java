package com.ppd.p1.model.dto.request;

import java.io.Serializable;

public class PaymentRequest implements Serializable {


    private Integer appointmentId;

    public PaymentRequest() {
    }

    public Integer getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }

    @Override
    public String toString() {
        return "PaymentRequest{" +
                "appointmentId=" + appointmentId +
                '}';
    }
}
