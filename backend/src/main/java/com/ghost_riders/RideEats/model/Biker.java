package com.ghost_riders.RideEats.model;

public class Biker {
    private String id;
    private String name;
    private String phone;
    private String email;
    private double rating;
    private int totalDeliveries;

    public Biker() {}

    public Biker(String id, String name, String phone, String email, double rating, int totalDeliveries) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.rating = rating;
        this.totalDeliveries = totalDeliveries;
    }

    // Getters and setters for all fields
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public int getTotalDeliveries() { return totalDeliveries; }
    public void setTotalDeliveries(int totalDeliveries) { this.totalDeliveries = totalDeliveries; }
}
