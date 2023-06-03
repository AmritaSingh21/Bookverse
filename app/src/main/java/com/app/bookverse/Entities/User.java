package com.app.bookverse.Entities;

import java.util.ArrayList;

public class User {
    private String id;
    private String name;
    private String email;
    private String addressLine;
    private String city;
    private String province;
    private String country;
    private String postalCode;
    private ArrayList<String> bookIds;
    private ArrayList<String> auctionIds;
    private ArrayList<String> boughtBooks;
    private ArrayList<String> auctionsWon;
    private ArrayList<Offer> myOffers;
    private Double longitude;
    private Double latitude;

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public User() {
    }

    public User(String id, String name, String email, String addressLine,
                String city, String province, String country, String postalCode) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.addressLine = addressLine;
        this.city = city;
        this.province = province;
        this.country = country;
        this.postalCode = postalCode;
    }

    public ArrayList<String> getAuctionsWon() {
        return auctionsWon;
    }

    public void setAuctionsWon(ArrayList<String> auctionsWon) {
        this.auctionsWon = auctionsWon;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public ArrayList<String> getBookIds() {
        return bookIds;
    }

    public void setBookIds(ArrayList<String> bookIds) {
        this.bookIds = bookIds;
    }

    public ArrayList<String> getAuctionIds() {
        return auctionIds;
    }

    public void setAuctionIds(ArrayList<String> auctionIds) {
        this.auctionIds = auctionIds;
    }

    public ArrayList<String> getBoughtBooks() {
        return boughtBooks;
    }

    public void setBoughtBooks(ArrayList<String> boughtBooks) {
        this.boughtBooks = boughtBooks;
    }

    public ArrayList<Offer> getMyOffers() {
        return myOffers;
    }

    public void setMyOffers(ArrayList<Offer> myOffers) {
        this.myOffers = myOffers;
    }
}
