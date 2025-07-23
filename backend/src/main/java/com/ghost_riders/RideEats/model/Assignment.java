package com.ghost_riders.RideEats.model;

import java.time.LocalDateTime;

public class Assignment {
    private String id;
    private String orderId;
    private String bikerName;
    private LocalDateTime assignedAt;
    private String status;

    public Assignment() {}

    public Assignment(String id, String orderId, String bikerName, LocalDateTime assignedAt, String status) {
        this.id = id;
        this.orderId = orderId;
        this.bikerName = bikerName;
        this.assignedAt = assignedAt;
        this.status = status;
    }

    // Getters and setters for all fields
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getBikerName() { return bikerName; }
    public void setBikerName(String bikerName) { this.bikerName = bikerName; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
