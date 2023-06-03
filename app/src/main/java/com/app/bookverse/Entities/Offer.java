package com.app.bookverse.Entities;

public class Offer {
    private String auctionId;
    private String price;
    private String title;

    public Offer() {
    }

    public Offer(String auctionId, String price, String title) {
        this.auctionId = auctionId;
        this.price = price;
        this.title = title;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
