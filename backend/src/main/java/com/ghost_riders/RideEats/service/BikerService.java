package com.ghost_riders.RideEats.service;

import com.ghost_riders.RideEats.model.Biker;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
            biker.setId(UUID.randomUUID().toString());
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
}
