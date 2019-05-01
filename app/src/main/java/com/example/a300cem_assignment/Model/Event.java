package com.example.a300cem_assignment.Model;

public class Event {
    private String Name;
    private String Image;
    private String Address;
    private Double Lat;
    private Double Lng;
    public Event() {
    }

    public Event(String name, String image, String address, Double lat, Double lng) {
        Name = name;
        Image = image;
        Address = address;
        Lat = lat;
        Lng = lng;
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
}
