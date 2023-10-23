package com.example.cardmaps.model;

public class LocationModel {
    private String namaLokasi;
    private double latitude;
    private double longitude;

    public LocationModel(String namaLokasi, double latitude, double longitude) {
        this.namaLokasi = namaLokasi;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    public String getNamaLokasi() {
        return namaLokasi;
    }
    public double getLatitude() {
        return latitude;
    }
    public double getLongitude() {
        return longitude;
    }
}
