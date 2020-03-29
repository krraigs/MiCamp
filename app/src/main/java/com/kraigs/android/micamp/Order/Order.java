package com.kraigs.android.micamp.Order;

import java.util.ArrayList;

public class Order {

    String userOrderKey,user,name,address,otp,status,orderKey,mode;
    ArrayList<Item> order;
    long price;

    public Order(){}

    public Order(String userOrderKey, String user, String name, String address, String otp, String status, String orderKey, String mode, ArrayList<Item> order, long price) {
        this.userOrderKey = userOrderKey;
        this.user = user;
        this.name = name;
        this.address = address;
        this.otp = otp;
        this.status = status;
        this.orderKey = orderKey;
        this.mode = mode;
        this.order = order;
        this.price = price;
    }

    public String getMode() {
        return mode;
    }

    public String getOrderKey() {
        return orderKey;
    }

    public String getStatus() {
        return status;
    }

    public String getOtp() {
        return otp;
    }

    public String getUserOrderKey() {
        return userOrderKey;
    }

    public String getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public ArrayList<Item> getOrder() {
        return order;
    }

    public long getPrice() {
        return price;
    }
}
