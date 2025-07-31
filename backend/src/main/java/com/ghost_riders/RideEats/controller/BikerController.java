package com.ghost_riders.RideEats.controller;

import com.ghost_riders.RideEats.model.Biker;
import com.ghost_riders.RideEats.service.BikerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bikers")
public class BikerController {
    @Autowired
    private BikerService bikerService;

    @GetMapping
    public List<Biker> getAllBikers() {
        return bikerService.getAllBikers();
    }

    @GetMapping("/binary/{id}")
    public Biker getBikerByIdBinary(@PathVariable int id) {
        return bikerService.binarySearchBikerById(id);
    }

    @PostMapping
    public Biker createBiker(@RequestBody Biker biker) {
        return bikerService.createBiker(biker);
    }

    @PutMapping("/{id}")
    public Biker updateBiker(@PathVariable int id, @RequestBody Biker biker) {
        return bikerService.updateBiker(id, biker);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteBiker(@PathVariable int id) {
        bikerService.deleteBiker(id);
        return java.util.Collections.singletonMap("success", true);
    }
}
