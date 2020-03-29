package com.kraigs.android.micamp.Home.GalleryPac;

public class Gallery {
    private String name,ePhoto,photos;

    public Gallery(){
    }

    public Gallery(String name, String ePhoto, String photos) {
        this.name = name;
        this.ePhoto = ePhoto;
        this.photos = photos;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getePhoto() {
        return ePhoto;
    }

    public void setePhoto(String ePhoto) {
        this.ePhoto = ePhoto;
    }

    public String getPhotos() {
        return photos;
    }

    public void setPhotos(String photos) {
        this.photos = photos;
    }
}
