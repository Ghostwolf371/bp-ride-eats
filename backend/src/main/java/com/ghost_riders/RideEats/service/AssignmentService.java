package com.ghost_riders.RideEats.service;

import com.ghost_riders.RideEats.model.Assignment;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class AssignmentService {
    private final List<Assignment> assignments = new ArrayList<>();

    public List<Assignment> getAssignmentHistory() {
        return assignments;
    }

    public void addAssignment(Assignment assignment) {
        assignments.add(assignment);
    }
}
