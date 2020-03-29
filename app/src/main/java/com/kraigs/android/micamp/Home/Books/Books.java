package com.kraigs.android.micamp.Home.Books;

public class Books {
    private String name;
    private String image;
    private String author;
    private String booksUrl;

    private Books(){}

    private Books(String name,String image,String author,String booksUrl){
        this.name = name;
        this.image = image;
        this.author = author;
        this.booksUrl = booksUrl;
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBooksUrl() {
        return booksUrl;
    }

    public void setBooksUrl(String booksUrl) {
        this.booksUrl = booksUrl;
    }
}
