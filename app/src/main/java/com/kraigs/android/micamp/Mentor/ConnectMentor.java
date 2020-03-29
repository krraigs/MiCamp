package com.kraigs.android.micamp.Mentor;

public class ConnectMentor {
    private String image;
    private String name;
    private String year;
    private String branch;
    private String connections;
    private String checked,quality,key;

    public ConnectMentor(){}

    public ConnectMentor(String image, String name, String year, String branch, String connections, String checked, String quality, String key) {
        this.image = image;
        this.name = name;
        this.year = year;
        this.branch = branch;
        this.connections = connections;
        this.checked = checked;
        this.quality = quality;
        this.key = key;
    }

    public String getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public String getYear() {
        return year;
    }

    public String getBranch() {
        return branch;
    }

    public String getConnections() {
        return connections;
    }

    public String getChecked() {
        return checked;
    }

    public String getQuality() {
        return quality;
    }

    public String getKey() {
        return key;
    }
}
