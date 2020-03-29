package com.kraigs.android.micamp.Order;

import android.os.Parcel;
import android.os.Parcelable;

public class Item  implements Parcelable{

    private String name;
    private String price,key,halfAvailable,fullPrice,nameFull,image;
    long qty,qtyFull;

    public Item(){}

    public Item(String name, String price, String key, String halfAvailable, String fullPrice, String nameFull, String image, long qty, long qtyFull) {
        this.name = name;
        this.price = price;
        this.key = key;
        this.halfAvailable = halfAvailable;
        this.fullPrice = fullPrice;
        this.nameFull = nameFull;
        this.image = image;
        this.qty = qty;
        this.qtyFull = qtyFull;
    }

    public String getImage() {
        return image;
    }

    protected Item(Parcel in) {
        name = in.readString();
        price = in.readString();
        key = in.readString();
    }

    public String getNameFull() {
        return nameFull;
    }

    public long getQtyFull() {
        return qtyFull;
    }

    public String getFullPrice() {
        return fullPrice;
    }

    public String getHalfAvailable() {
        return halfAvailable;
    }

    public long getQty() {
        return qty;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getKey() {
        return key;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Item> CREATOR = new Parcelable.Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

}
