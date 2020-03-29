package com.kraigs.android.micamp.Drawer;

public class Contact {
    private String name,image,type,number,mail;

    public Contact(){}

    public Contact(String name, String image, String type, String number, String mail) {
        this.name = name;
        this.image = image;
        this.type = type;
        this.number = number;
        this.mail = mail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }
}
