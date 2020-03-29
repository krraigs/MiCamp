package com.kraigs.android.micamp.extras;

public class Notification {

    String type,from,otp;

    public Notification(){}

    public Notification(String type, String from, String otp) {
        this.type = type;
        this.from = from;
        this.otp = otp;
    }

    public String getOtp() {
        return otp;
    }

    public String getType() {
        return type;
    }

    public String getFrom() {
        return from;
    }
}
