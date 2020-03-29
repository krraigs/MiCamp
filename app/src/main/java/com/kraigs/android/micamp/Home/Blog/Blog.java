package com.kraigs.android.micamp.Home.Blog;

public class Blog {

    private String mTitle;
    private String mAuthor;
    private String mInfo;
    private String mPhotoUrl,uid;

    public Blog(){
    }

    public Blog(String mTitle, String mAuthor, String mInfo, String mPhotoUrl, String uid) {
        this.mTitle = mTitle;
        this.mAuthor = mAuthor;
        this.mInfo = mInfo;
        this.mPhotoUrl = mPhotoUrl;
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public void setmAuthor(String mAuthor) {
        this.mAuthor = mAuthor;
    }

    public void setmInfo(String mInfo) {
        this.mInfo = mInfo;
    }

    public void setmPhotoUrl(String mPhotoUrl){
        this.mPhotoUrl = mPhotoUrl;
    }


    public String getmTitle() {
        return mTitle;
    }

    public String getmAuthor() {
        return mAuthor;
    }

    public String getmInfo() {
        return mInfo;
    }

    public String getmPhotoUrl(){
        return mPhotoUrl;
    }
}
