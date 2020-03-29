package com.kraigs.android.micamp.Academics;

public class Member {
    private String image;
    private String name,profileLink;

    public Member(){}

    public Member(String image, String name, String profileLink) {
        this.image = image;
        this.name = name;
        this.profileLink = profileLink;
    }

    public String getProfileLink() {
        return profileLink;
    }

    public void setProfileLink(String profileLink) {
        this.profileLink = profileLink;
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
