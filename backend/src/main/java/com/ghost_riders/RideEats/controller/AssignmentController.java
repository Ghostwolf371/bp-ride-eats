package com.ghost_riders.RideEats.controller;

import com.ghost_riders.RideEats.model.Assignment;
import com.ghost_riders.RideEats.service.OrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {
    @Autowired
    private OrderService orderService;

    @GetMapping
    public List<Assignment> getAssignmentHistory() {
        return orderService.getAssignmentHistory();
    }
}
