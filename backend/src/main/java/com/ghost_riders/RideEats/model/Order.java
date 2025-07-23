package com.ghost_riders.RideEats.model;

import java.time.LocalDateTime;

public class Order {
    private String id;
    private String customerName;
    private String restaurant;
    private int preparationTime;
    private double total;
    private String deliveryAddress;
    private String status;
    private String assignedBiker;
    private LocalDateTime createdAt;

    public Order() {}

    public Order(String id, String customerName, String restaurant, int preparationTime, double total, String deliveryAddress, String status, String assignedBiker, LocalDateTime createdAt) {
        this.id = id;
        this.customerName = customerName;
        this.restaurant = restaurant;
        this.preparationTime = preparationTime;
        this.total = total;
        this.deliveryAddress = deliveryAddress;
        this.status = status;
        this.assignedBiker = assignedBiker;
        this.createdAt = createdAt;
    }

    // Getters and setters for all fields
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getRestaurant() { return restaurant; }
    public void setRestaurant(String restaurant) { this.restaurant = restaurant; }
    public int getPreparationTime() { return preparationTime; }
    public void setPreparationTime(int preparationTime) { this.preparationTime = preparationTime; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAssignedBiker() { return assignedBiker; }
    public void setAssignedBiker(String assignedBiker) { this.assignedBiker = assignedBiker; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
