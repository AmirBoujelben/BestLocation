package com.example.mybestlocation;

public class Position {
    private int idposition;
    private String longitude, latitude, pseudo;

    public Position(int idposition, String pseudo, String longitude, String latitude) {
        this.idposition = idposition;
        this.pseudo = pseudo;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Position(String pseudo, String longitude, String latitude) {
        this(-1, pseudo, longitude, latitude); // Default idposition
    }

    // Getters and Setters
    public int getIdposition() {
        return idposition;
    }

    public void setIdposition(int idposition) {
        this.idposition = idposition;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    // Validation for Location
    public boolean isValidLocation() {
        try {
            double lat = Double.parseDouble(latitude);
            double lon = Double.parseDouble(longitude);
            return lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return pseudo + " (" + latitude + ", " + longitude + ")";
    }
}
