package com.kraigs.android.micamp.Home.News;

public class News {

    private String topic;
    private String date;
    private String organisedBy;
    private String content;
    private String view;

    private News(){}

    public News(String topic, String date, String organisedBy, String content, String view) {
        this.topic = topic;
        this.date = date;
        this.organisedBy = organisedBy;
        this.content = content;
        this.view = view;
    }


    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getOrganisedBy() {
        return organisedBy;
    }

    public void setOrganisedBy(String organisedBy) {
        this.organisedBy = organisedBy;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }
}
