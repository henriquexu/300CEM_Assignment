package com.example.a300cem_assignment.Model;

public class Event {
    private String Name;
    private String Image;
    private String Address;
    private String Lat_Lng;
    public Event() {
    }

    public Event(String name, String image, String address, String lat_Lng) {
        Name = name;
        Image = image;
        Address = address;
        Lat_Lng = lat_Lng;
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

    public String getLat_Lng() {
        return Lat_Lng;
    }

    public void setLat_Lng(String lat_Lng) {
        Lat_Lng = lat_Lng;
    }
}
