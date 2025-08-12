package com.laioffer.travelplanner.controller;

import com.laioffer.travelplanner.dto.ItineraryDTO;
import com.laioffer.travelplanner.service.ItineraryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/itineraries")
public class ItineraryController {

    private final ItineraryService service;

    public ItineraryController(ItineraryService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItineraryDTO> getItineraryById(@PathVariable UUID id) {
        ItineraryDTO dto = service.getItinerary(id);
        return ResponseEntity.ok(dto);
    }
}