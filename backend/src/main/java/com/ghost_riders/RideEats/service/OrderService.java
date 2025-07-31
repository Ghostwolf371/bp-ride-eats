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
    private final Map<Integer, Order> orders = new ConcurrentHashMap<>();
    private final List<Assignment> assignments = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public List<Order> getAllOrders() {
      List<Order> allOrders = new ArrayList<>(orders.values());

      // Insertion Sort Algorithm to put completed orders at the bottom
      if (allOrders == null || allOrders.size() <= 1) {
          return allOrders;
      }

      // Insertion Sort implementation
      for (int i = 1; i < allOrders.size(); i++) {
          Order key = allOrders.get(i);
          int j = i - 1;

          // Move elements that should come before 'key' to one position ahead
          while (j >= 0 && shouldComeBefore(key, allOrders.get(j))) {
              allOrders.set(j + 1, allOrders.get(j));
              j = j - 1;
          }
          allOrders.set(j + 1, key);
      }

      return allOrders;
  }

      // Helper method to determine if orderA should come before orderB
      private boolean shouldComeBefore(Order orderA, Order orderB) {
        // If both are COMPLETED, sort by creation date (newest first)
        if ("COMPLETED".equals(orderA.getStatus()) && "COMPLETED".equals(orderB.getStatus())) {
            LocalDateTime dateA = orderA.getCreatedAt() != null ? orderA.getCreatedAt() : LocalDateTime.MIN;
            LocalDateTime dateB = orderB.getCreatedAt() != null ? orderB.getCreatedAt() : LocalDateTime.MIN;
            return dateA.isAfter(dateB); // Newest first
        }

        // If only one is COMPLETED, put COMPLETED at the bottom
        if ("COMPLETED".equals(orderA.getStatus())) return false; // COMPLETED should come after
        if ("COMPLETED".equals(orderB.getStatus())) return true;  // COMPLETED should come after

        // For non-completed orders, sort by preparation time (shortest first)
        int prepTimeA = orderA.getPreparationTime();
        int prepTimeB = orderB.getPreparationTime();
        return prepTimeA < prepTimeB;
    }

    public List<Order> getOrdersByStatus(String status) {
        return orders.values().stream()
                .filter(order -> status.equals(order.getStatus()))
                .collect(Collectors.toList());
    }

    public Order createOrder(Order order) {
        if (order.getId() == 0) {
            // Find the next available ID
            int nextId;
            if (orders.isEmpty()) {
                nextId = 1;
            } else {
                nextId = orders.keySet().stream()
                        .mapToInt(Integer::intValue)
                        .max()
                        .orElse(0) + 1;
            }
            order.setId(nextId);
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

    public Order updateOrder(Integer id, Order updated) {
        Order existing = orders.get(id);
        if (existing == null) return null;
        updated.setId(id);
        updated.setCreatedAt(existing.getCreatedAt());
        orders.put(id, updated);
        return updated;
    }

    public void deleteOrder(Integer id) {
        orders.remove(id);
    }

    public Order assignOrderToBiker(Integer orderId, Biker biker) {
        Order order = orders.get(orderId);
        if (order == null || !"AVAILABLE".equals(order.getStatus())) return null;
        order.setAssignedBiker(biker.getName());
        order.setStatus("COMPLETED");
        Assignment assignment = new Assignment(UUID.randomUUID().toString(), String.valueOf(orderId), biker.getName(), LocalDateTime.now(), "COMPLETED");
        assignments.add(assignment);
        return order;
    }

    public List<Assignment> getAssignmentHistory() {
        return assignments;
    }

    // Get orders by statuses with insertion sort by preparation time
    public List<Order> getOrdersByStatuses(List<String> statuses) {
        List<Order> filteredOrders = orders.values().stream()
            .filter(order -> statuses.contains(order.getStatus()))
            .collect(Collectors.toList());

        // Apply insertion sort by preparation time
        return insertionSortByPreparationTime(filteredOrders);
    }

    // Insertion sort algorithm for sorting orders by preparation time
    private List<Order> insertionSortByPreparationTime(List<Order> orderList) {
        if (orderList == null || orderList.size() <= 1) {
            return orderList;
        }

        for (int i = 1; i < orderList.size(); i++) {
            Order key = orderList.get(i);
            int j = i - 1;

            // Move elements that are greater than key's preparation time
            // to one position ahead of their current position
            while (j >= 0 && orderList.get(j).getPreparationTime() > key.getPreparationTime()) {
                orderList.set(j + 1, orderList.get(j));
                j = j - 1;
            }
            orderList.set(j + 1, key);
        }

        return orderList;
    }

    // Binary search algorithm to find an order by ID
    public Order binarySearchOrderById(Integer id) {
        List<Order> orderList = new ArrayList<>(orders.values());
        // Sort the list by ID (assuming IDs are Integers, sort numerically)
        orderList.sort(Comparator.comparing(Order::getId));
        int left = 0;
        int right = orderList.size() - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            Order midOrder = orderList.get(mid);
            int cmp = Integer.compare(midOrder.getId(), id);
            if (cmp == 0) {
                return midOrder;
            } else if (cmp < 0) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return null;
    }

        // Binary search algorithm to find an assignable order by ID
    public Order binarySearchAssignableOrderById(Integer id) {
        // Get only assignable orders (AVAILABLE or PREPARING)
        List<Order> assignableOrderList = orders.values().stream()
            .filter(order -> "AVAILABLE".equals(order.getStatus()) || "PREPARING".equals(order.getStatus()))
            .collect(Collectors.toList());

        // Sort the list by ID (assuming IDs are Integers, sort numerically)
        assignableOrderList.sort(Comparator.comparing(Order::getId));

        int left = 0;
        int right = assignableOrderList.size() - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            Order midOrder = assignableOrderList.get(mid);
            int cmp = Integer.compare(midOrder.getId(), id);
            if (cmp == 0) {
                return midOrder;
            } else if (cmp < 0) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return null;
    }
}
