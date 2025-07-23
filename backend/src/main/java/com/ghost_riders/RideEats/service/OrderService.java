package com.ghost_riders.RideEats.service;

import com.ghost_riders.RideEats.model.Order;
import com.ghost_riders.RideEats.model.Biker;
import com.ghost_riders.RideEats.model.Assignment;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final List<Assignment> assignments = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public List<Order> getAllOrders() {
        return new ArrayList<>(orders.values());
    }

    public List<Order> getOrdersByStatus(String status) {
        return orders.values().stream()
                .filter(order -> status.equals(order.getStatus()))
                .collect(Collectors.toList());
    }

    public Order getOrderById(String id) {
        return orders.get(id);
    }

    public Order createOrder(Order order) {
        if (order.getId() == null || order.getId().isEmpty()) {
            order.setId(String.valueOf(orders.size() + 1));
        }
        order.setStatus("PREPARING");
        order.setCreatedAt(LocalDateTime.now());
        orders.put(order.getId(), order);

        // Schedule status change
        int prepTime = order.getPreparationTime();
        scheduler.schedule(() -> {
            Order o = orders.get(order.getId());
            if (o != null && "PREPARING".equals(o.getStatus())) {
                o.setStatus("AVAILABLE");
            }
        }, prepTime, TimeUnit.MINUTES);

        return order;
    }

    public Order updateOrder(String id, Order updated) {
        Order existing = orders.get(id);
        if (existing == null) return null;
        updated.setId(id);
        updated.setCreatedAt(existing.getCreatedAt());
        orders.put(id, updated);
        return updated;
    }

    public void deleteOrder(String id) {
        orders.remove(id);
    }

    public Order assignOrderToBiker(String orderId, Biker biker) {
        Order order = orders.get(orderId);
        if (order == null || !"AVAILABLE".equals(order.getStatus())) return null;
        order.setAssignedBiker(biker.getName());
        order.setStatus("COMPLETED");
        Assignment assignment = new Assignment(UUID.randomUUID().toString(), orderId, biker.getName(), LocalDateTime.now(), "COMPLETED");
        assignments.add(assignment);
        return order;
    }

    public List<Assignment> getAssignmentHistory() {
        return assignments;
    }

    // Sorting by preparation time
    public List<Order> getOrdersSortedByPreparationTime() {
        return orders.values().stream()
                .sorted(Comparator.comparingInt(Order::getPreparationTime))
                .collect(Collectors.toList());
    }

    // Search by ID
    public Order searchOrderById(String id) {
        return orders.get(id);
    }

    // Completed orders sorted to bottom
    public List<Order> getOrdersWithCompletedAtBottom() {
        return orders.values().stream()
                .sorted(Comparator.comparing((Order o) -> "COMPLETED".equals(o.getStatus()) ? 1 : 0))
                .collect(Collectors.toList());
    }

    public List<Order> getOrdersByStatuses(List<String> statuses) {
        return orders.values().stream()
            .filter(order -> statuses.contains(order.getStatus()))
            .collect(Collectors.toList());
    }
}
