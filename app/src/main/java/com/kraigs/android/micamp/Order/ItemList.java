package com.kraigs.android.micamp.Order;

public class ItemList{

    private String name;
    private String price;
    long qty;

    public ItemList(){}

    public ItemList(String name, String price, long qty) {
        this.name = name;
        this.price = price;
        this.qty = qty;
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


}
