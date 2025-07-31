package com.ghost_riders.RideEats.service;

import com.ghost_riders.RideEats.model.Biker;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BikerService {
    private final Map<Integer, Biker> bikers = new ConcurrentHashMap<>();

    public List<Biker> getAllBikers() {
        return new ArrayList<>(bikers.values());
    }

    public Biker getBikerById(Integer id) {
        return bikers.get(id);
    }

    public Biker createBiker(Biker biker) {
        if (biker.getId() == 0) {
            // Find the next available ID
            int nextId;
            if (bikers.isEmpty()) {
                nextId = 1;
            } else {
                nextId = bikers.keySet().stream()
                        .mapToInt(Integer::intValue)
                        .max()
                        .orElse(0) + 1;
            }
            biker.setId(nextId);
        }
        bikers.put(biker.getId(), biker);
        return biker;
    }

    public Biker updateBiker(Integer id, Biker updated) {
        Biker existing = bikers.get(id);
        if (existing == null) return null;
        updated.setId(id);
        bikers.put(id, updated);
        return updated;
    }

    public void deleteBiker(Integer id) {
        bikers.remove(id);
    }

    // Binary search algorithm to find a biker by ID
    public Biker binarySearchBikerById(Integer id) {
        List<Biker> bikerList = new ArrayList<>(bikers.values());
        // Sort the list by ID (assuming IDs are Integers, sort numerically)
        bikerList.sort(Comparator.comparing(Biker::getId));

        int left = 0;
        int right = bikerList.size() - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            Biker midBiker = bikerList.get(mid);
            int cmp = Integer.compare(midBiker.getId(), id);
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
