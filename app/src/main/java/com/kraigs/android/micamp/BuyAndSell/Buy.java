package com.kraigs.android.micamp.BuyAndSell;

public class Buy {
    String image,price,itemName,sellerUid,key;

    public Buy(){}

    public Buy(String image, String price, String itemName, String sellerUid, String key) {
        this.image = image;
        this.price = price;
        this.itemName = itemName;
        this.sellerUid = sellerUid;
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSellerUid() {
        return sellerUid;
    }

    public void setSellerUid(String sellerUid) {
        this.sellerUid = sellerUid;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
}
