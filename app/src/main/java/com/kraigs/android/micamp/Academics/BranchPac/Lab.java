package com.kraigs.android.micamp.Academics.BranchPac;

public class Lab {
    private String image,name,branch;
    public Lab(){}

    public Lab(String image, String name) {
        this.image = image;
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
