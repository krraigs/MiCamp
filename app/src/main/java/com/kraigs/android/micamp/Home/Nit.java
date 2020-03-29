package com.kraigs.android.micamp.Home;

public class Nit {
    private String mCardDescription;
    private String mCardPhotoUrl;
    private int priority;

    public Nit(){

    }

    public Nit(String cardDescription, String photoUrl){
        this.mCardDescription = cardDescription;
        this.mCardPhotoUrl = photoUrl;
    }

    public String getmCardDescription() {
        return mCardDescription;
    }

    public void setmCardDescription(String mCardDescription) {
        this.mCardDescription = mCardDescription;
    }

    public String getmCardPhotoUrl() {
        return mCardPhotoUrl;
    }

    public void setmCardPhotoUrl(String mCardPhotoUrl) {
        this.mCardPhotoUrl = mCardPhotoUrl;
    }
}
