package com.ppd.p1.model.dto.request;

import java.io.Serializable;
import java.sql.Date;

public class AppointmentRequest implements Serializable {

    private String name;

    private String cnp;

    private Integer locationId;

    private Integer treatmentId;

    private Date date;

    private int time;

    public AppointmentRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCnp() {
        return cnp;
    }

    public void setCnp(String cnp) {
        this.cnp = cnp;
    }

    public Integer getLocationId() {
        return locationId;
    }

    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }

    public Integer getTreatmentId() {
        return treatmentId;
    }

    public void setTreatmentId(Integer treatmentId) {
        this.treatmentId = treatmentId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "AppointmentRequest{" +
                "name='" + name + '\'' +
                ", cnp='" + cnp + '\'' +
                ", locationId=" + locationId +
                ", treatmentId=" + treatmentId +
                ", date=" + date +
                ", time=" + time +
                '}';
    }
}
