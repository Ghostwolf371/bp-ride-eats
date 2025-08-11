package com.ghost_riders.RideEats.service;

import com.ghost_riders.RideEats.model.Biker;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BikerService {
    private final List<Biker> bikers = new ArrayList<>();

    public List<Biker> getAllBikers() {
        return new ArrayList<>(bikers);
    }

    public Biker getBikerById(Integer id) {
        return bikers.stream()
                .filter(b -> b.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public Biker createBiker(Biker biker) {
        if (biker.getId() == 0) {
            // Find the next available ID
            int nextId = bikers.stream()
                    .mapToInt(Biker::getId)
                    .max()
                    .orElse(0) + 1;
            biker.setId(nextId);
        }
        bikers.add(biker);
        return biker;
    }

    public Biker updateBiker(Integer id, Biker updated) {
        Biker existing = getBikerById(id);
        if (existing == null) return null;

        int index = bikers.indexOf(existing);
        if (index != -1) {
            updated.setId(id);
            bikers.set(index, updated);
            return updated;
        }
        return null;
    }

    public void deleteBiker(Integer id) {
        bikers.removeIf(b -> b.getId() == id);
    }

    // Binary search algorithm to find a biker by ID
    public Biker binarySearchBikerById(Integer id) {
        // Create a copy and sort it
        List<Biker> sortedBikers = new ArrayList<>(bikers);
        sortedBikers.sort(Comparator.comparing(Biker::getId));

        int left = 0;
        int right = sortedBikers.size() - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            Biker midBiker = sortedBikers.get(mid);
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
