package com.ghost_riders.RideEats.service;

import com.ghost_riders.RideEats.model.Assignment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class AssignmentService {
    private final List<Assignment> assignments = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public List<Assignment> getAssignmentHistory() {
        return assignments;
    }

    public void addAssignment(Assignment assignment) {
        scheduler.schedule(() -> assignments.add(assignment), 10, TimeUnit.SECONDS);
    }
}
