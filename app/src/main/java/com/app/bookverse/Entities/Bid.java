package com.app.bookverse.Entities;

public class Bid {
    private String userIds;
    private String price;
    private String userName;

    public Bid() {
    }

    public Bid(String userIds, String price, String userName) {
        this.userIds = userIds;
        this.price = price;
        this.userName= userName;
    }

    public String getUserIds() {
        return userIds;
    }

    public void setUserIds(String userIds) {
        this.userIds = userIds;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
