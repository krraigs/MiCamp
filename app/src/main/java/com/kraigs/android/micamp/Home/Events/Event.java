package com.kraigs.android.micamp.Home.Events;

public class Event {
    private String date;
    private String eventLocation;
    private String eventName;
    private String eventPhotoUrl;

    private Event(){}

    public Event(String mEventDay,String mEventLocation,String mEventName,String mEventPhotoUrl){
        this.date = mEventDay;
        this.eventLocation = mEventLocation;
        this.eventName = mEventName;
        this.eventPhotoUrl = mEventPhotoUrl;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventPhotoUrl() {
        return eventPhotoUrl;
    }

    public void setEventPhotoUrl(String eventPhotoUrl) {
        this.eventPhotoUrl = eventPhotoUrl;
    }
}
