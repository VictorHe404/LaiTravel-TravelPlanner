package com.laioffer.travelplanner.controller;

import com.laioffer.travelplanner.entity.Itinerary;
import com.laioffer.travelplanner.service.ItineraryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/itinerary")
public class ItineraryController {

    private final ItineraryService service;

    public ItineraryController(ItineraryService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Itinerary> create(@RequestBody Itinerary req) {
        var created = service.create(req);
        return ResponseEntity
                .created(URI.create("/api/itinerary/" + created.getId()))
                .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Itinerary> update(@PathVariable UUID id, @RequestBody Itinerary req) {
        var updated = service.update(id, req);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Itinerary> getById(@PathVariable UUID id) {
        try {
            var it = service.getById(id);
            return ResponseEntity.ok(it);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Itinerary>> listMine() {
        return ResponseEntity.ok(service.listMine());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
