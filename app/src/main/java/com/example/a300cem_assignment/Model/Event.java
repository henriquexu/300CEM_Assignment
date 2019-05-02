package com.example.a300cem_assignment.Model;

public class Event {
    private String Name;
    private String Image;
    private String Address;
    private Double Lat;
    private Double Lng;
    private String UserId;
    private String Date;
    public Event() {
    }

    public Event(String name, String image, String address, Double lat, Double lng, String userId, String date) {
        Name = name;
        Image = image;
        Address = address;
        Lat = lat;
        Lng = lng;
        UserId = userId;
        Date = date;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public Double getLat() {
        return Lat;
    }

    public void setLat(Double lat) {
        Lat = lat;
    }

    public Double getLng() {
        return Lng;
    }

    public void setLng(Double lng) {
        Lng = lng;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }
}
