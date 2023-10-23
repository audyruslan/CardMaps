package com.example.cardmaps.model;

public class PoskoModel {
    private String namaPosko;
    private double latitude;
    private double longitude;

    public PoskoModel(String namaPosko, double latitude, double longitude) {
        this.namaPosko = namaPosko;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getNamaPosko() {
        return namaPosko;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
