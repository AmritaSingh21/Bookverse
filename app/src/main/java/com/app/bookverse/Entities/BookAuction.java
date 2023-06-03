package com.app.bookverse.Entities;

import java.util.ArrayList;

public class BookAuction extends Book {
//    private String startTime;
    private String endTime;
    private ArrayList<Bid> bids;
    private String winnerId;
    private String status = "Open";
    private String soldPrice;
    private String startingPrice;

    public BookAuction() {
    }

    public BookAuction(ArrayList<Bid> bids, String endTime, String id,
                       String title, String author, String genre, String year,
                       String isbn, String price, String picId, String ownerId) {
        super(id, title, author, genre, year, isbn, price, picId, ownerId);
//        this.startTime = startTime;
        this.endTime = endTime;
        this.bids = bids;
    }

    public String getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(String startingPrice) {
        this.startingPrice = startingPrice;
    }

    public String getSoldPrice() {
        return soldPrice;
    }

    public void setSoldPrice(String soldPrice) {
        this.soldPrice = soldPrice;
    }

    //
//    public String getStartTime() {
//        return startTime;
//    }
//
//    public void setStartTime(String startTime) {
//        this.startTime = startTime;
//    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<Bid> getBids() {
        return bids;
    }

    public void setBids(ArrayList<Bid> bids) {
        this.bids = bids;
    }
}
