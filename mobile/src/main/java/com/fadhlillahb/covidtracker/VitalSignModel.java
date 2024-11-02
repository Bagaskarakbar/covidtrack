package com.fadhlillahb.covidtracker;

import com.google.type.DateTime;

import java.util.Date;

public class VitalSignModel {
    private String id;
    private Date timestamp;
    private int heartRate, spo2, Systole, Diastole, RespiratoryRate;
    private Float temperature;

    public VitalSignModel() {

    }

    public VitalSignModel(String id, Date timestamp, int heartRate, int spo2, int Systole, int Diastole, int RespiratoryRate, Float temperature) {
        this.id = id;
        this.timestamp = timestamp;
        this.heartRate = heartRate;
        this.spo2 = spo2;
        this.Systole = Systole;
        this.Diastole = Diastole;
        this.RespiratoryRate = RespiratoryRate;
        this.temperature = temperature;
    }

    public VitalSignModel(String id, Date timestamp) {
        this.id = id;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public int getSpo2() {
        return spo2;
    }

    public int getSystole() {
        return Systole;
    }

    public int getDiastole() {
        return Diastole;
    }

    public int getRespiratoryRate() {
        return RespiratoryRate;
    }

    public Float getTemperature() {
        return temperature;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDiastole(int diastole) {
        Diastole = diastole;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

    public void setRespiratoryRate(int respiratoryRate) {
        RespiratoryRate = respiratoryRate;
    }

    public void setSpo2(int spo2) {
        this.spo2 = spo2;
    }

    public void setSystole(int systole) {
        Systole = systole;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
