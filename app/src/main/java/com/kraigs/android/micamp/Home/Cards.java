package com.kraigs.android.micamp.Home;

public class Cards {

    private String url,info;

    Cards(){

    }

    public Cards(String url, String info) {
        this.url = url;
        this.info = info;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
