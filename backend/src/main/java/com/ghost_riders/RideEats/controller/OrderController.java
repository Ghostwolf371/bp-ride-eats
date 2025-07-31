package com.ghost_riders.RideEats.controller;

import com.ghost_riders.RideEats.model.Order;
import com.ghost_riders.RideEats.model.Biker;
import com.ghost_riders.RideEats.model.Assignment;
import com.ghost_riders.RideEats.service.OrderService;
import com.ghost_riders.RideEats.service.BikerService;
import com.ghost_riders.RideEats.service.AssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Arrays;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private BikerService bikerService;
    @Autowired
    private AssignmentService assignmentService;

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/available")
    public List<Order> getAvailableOrders() {
        return orderService.getOrdersByStatus("AVAILABLE");
    }

    @GetMapping("/assignable")
    public List<Order> getAssignableOrders() {
        return orderService.getOrdersByStatuses(Arrays.asList("AVAILABLE", "PREPARING"));
    }

    @GetMapping("/completed")
    public List<Order> getOrdersByStatus() {
        return orderService.getOrdersByStatus("COMPLETED");
    }

    @GetMapping("/binary/{id}")
    public Order getOrderByIdBinary(@PathVariable int id) {
        return orderService.binarySearchOrderById(id);
    }

    @GetMapping("/assignable/binary/{id}")
    public Order getAssignableOrderByIdBinary(@PathVariable int id) {
        return orderService.binarySearchAssignableOrderById(id);
    }

    @PostMapping
    public Order createOrder(@RequestBody Order order) {
        return orderService.createOrder(order);
    }

    @PutMapping("/{id}")
    public Order updateOrder(@PathVariable int id, @RequestBody Order order) {
        return orderService.updateOrder(id, order);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteOrder(@PathVariable int id) {
        orderService.deleteOrder(id);
        return java.util.Collections.singletonMap("success", true);
    }

    @PutMapping("/{id}/assign")
    public Order assignOrderToBiker(@PathVariable int id, @RequestBody Map<String, String> body) {
        String bikerId = body.get("bikerId");
        Biker biker = bikerService.getBikerById(Integer.parseInt(bikerId));
        if (biker == null) return null;
        Order order = orderService.assignOrderToBiker(id, biker);
        if (order != null) {
            Assignment assignment = new Assignment(null, String.valueOf(id), biker.getName(), java.time.LocalDateTime.now(), "COMPLETED");
            assignmentService.addAssignment(assignment);
        }
        return order;
    }
}
