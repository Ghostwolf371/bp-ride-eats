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

    @GetMapping("/available")
    public List<Biker> getAvailableBikers() {
        return bikerService.getAvailableBikers();
    }

    @GetMapping("/{id}")
    public Biker getBikerById(@PathVariable String id) {
        return bikerService.getBikerById(id);
    }
    
    @GetMapping("/binary/{id}")
    public Biker getBikerByIdBinary(@PathVariable String id) {
        return bikerService.binarySearchBikerById(id);
    }

    @PostMapping
    public Biker createBiker(@RequestBody Biker biker) {
        return bikerService.createBiker(biker);
    }

    @PutMapping("/{id}")
    public Biker updateBiker(@PathVariable String id, @RequestBody Biker biker) {
        return bikerService.updateBiker(id, biker);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteBiker(@PathVariable String id) {
        bikerService.deleteBiker(id);
        return java.util.Collections.singletonMap("success", true);
    }
}
