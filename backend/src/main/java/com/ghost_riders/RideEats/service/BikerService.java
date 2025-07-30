package com.ghost_riders.RideEats.service;

import com.ghost_riders.RideEats.model.Biker;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class BikerService {
    private final Map<String, Biker> bikers = new ConcurrentHashMap<>();

    public List<Biker> getAllBikers() {
        return new ArrayList<>(bikers.values());
    }

    public Biker getBikerById(String id) {
        return bikers.get(id);
    }

    public Biker createBiker(Biker biker) {
        if (biker.getId() == null || biker.getId().isEmpty()) {
            biker.setId(String.valueOf(bikers.size() + 1));
        }
        bikers.put(biker.getId(), biker);
        return biker;
    }

    public Biker updateBiker(String id, Biker updated) {
        Biker existing = bikers.get(id);
        if (existing == null) return null;
        updated.setId(id);
        bikers.put(id, updated);
        return updated;
    }

    public void deleteBiker(String id) {
        bikers.remove(id);
    }

    public List<Biker> getAvailableBikers() {
        // For demo, all bikers are available
        return new ArrayList<>(bikers.values());
    }
    
    // Binary search algorithm to find a biker by ID
    public Biker binarySearchBikerById(String id) {
        List<Biker> bikerList = new ArrayList<>(bikers.values());
        // Sort the list by ID (assuming IDs are Strings, sort lexicographically)
        bikerList.sort(Comparator.comparing(Biker::getId));
        
        int left = 0;
        int right = bikerList.size() - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            Biker midBiker = bikerList.get(mid);
            int cmp = midBiker.getId().compareTo(id);
            if (cmp == 0) {
                return midBiker;
            } else if (cmp < 0) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return null;
    }
}
