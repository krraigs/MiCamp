package com.kraigs.android.micamp.LostAndFoundActivity;

public class LostFound {
    String name,place,image,uid;

    public LostFound(){}

    public LostFound(String name, String place, String image, String uid) {
        this.name = name;
        this.place = place;
        this.image = image;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public String getPlace() {
        return place;
    }

    public String getImage() {
        return image;
    }

    public String getUid() {
        return uid;
    }
}
