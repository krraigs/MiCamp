package com.kraigs.android.micamp.Home.Vedio;

import com.google.firebase.Timestamp;

public class Vedio {

    private String uid,videoID,videoText,text;
    Timestamp timestamp;

    public Vedio(){
    }

    public Vedio(String uid, String videoID, String videoText, String text, Timestamp timestamp) {
        this.uid = uid;
        this.videoID = videoID;
        this.videoText = videoText;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getVideoText() {
        return videoText;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getVideoID() {
        return videoID;
    }

    public String getUid() {
        return uid;
    }
}

