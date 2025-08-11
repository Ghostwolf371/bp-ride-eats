package com.ghost_riders.RideEats.service;

import com.ghost_riders.RideEats.model.Order;
import com.ghost_riders.RideEats.model.Biker;
import com.ghost_riders.RideEats.model.Assignment;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final List<Order> orders = new ArrayList<>();
    private final List<Assignment> assignments = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public List<Order> getAllOrders() {
        // Create a copy to avoid concurrent modification
        List<Order> allOrders = new ArrayList<>(orders);

        // Insertion Sort Algorithm to put completed orders at the bottom
        if (allOrders.size() <= 1) {
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
        return orders.stream()
                .filter(order -> status.equals(order.getStatus()))
                .collect(Collectors.toList());
    }

    public Order createOrder(Order order) {
        if (order.getId() == 0) {
            // Find the next available ID
            int nextId = orders.stream()
                    .mapToInt(Order::getId)
                    .max()
                    .orElse(0) + 1;
            order.setId(nextId);
        }
        order.setStatus("PREPARING");
        order.setCreatedAt(LocalDateTime.now());
        orders.add(order);

        // Schedule status change
        int prepTime = order.getPreparationTime();
        int orderId = order.getId();
        scheduler.schedule(() -> {
            Order o = orders.stream()
                    .filter(order1 -> order1.getId() == orderId)
                    .findFirst()
                    .orElse(null);
            if (o != null && "PREPARING".equals(o.getStatus())) {
                o.setStatus("AVAILABLE");
            }
        }, prepTime, TimeUnit.MINUTES);

        return order;
    }

    public Order getOrderById(Integer id) {
      return orders.stream()
              .filter(order -> order.getId() == id)
              .findFirst()
              .orElse(null);
    }

    public Order updateOrder(Integer id, Order updated) {
        Order existing = getOrderById(id);
        if (existing == null) return null;

        int index = orders.indexOf(existing);
        if (index != -1) {
            updated.setId(id);
            updated.setAssignedBiker(existing.getAssignedBiker());
            updated.setStatus(existing.getStatus());
            updated.setCreatedAt(existing.getCreatedAt());
            orders.set(index, updated);
            return updated;
        }
        return null;
    }

    public void deleteOrder(Integer id) {
        orders.removeIf(order -> order.getId() == id);
    }

    public Order assignOrderToBiker(Integer orderId, Biker biker) {
        return orders.stream()
                .filter(order -> order.getId() == orderId && "AVAILABLE".equals(order.getStatus()))
                .findFirst()
                .map(order -> {
                    order.setAssignedBiker(biker.getName());
                    order.setStatus("COMPLETED");
                    Assignment assignment = new Assignment(UUID.randomUUID().toString(),
                            String.valueOf(orderId),
                            biker.getName(),
                            LocalDateTime.now(),
                            "COMPLETED");
                    assignments.add(assignment);
                    return order;
                })
                .orElse(null);
    }

    public List<Assignment> getAssignmentHistory() {
        return assignments;
    }

    // Get orders by statuses with insertion sort by preparation time
    public List<Order> getOrdersByStatuses(List<String> statuses) {
        List<Order> filteredOrders = orders.stream()
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
        // Create a copy and sort it
        List<Order> orderList = new ArrayList<>(orders);
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
        List<Order> assignableOrderList = orders.stream()
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
