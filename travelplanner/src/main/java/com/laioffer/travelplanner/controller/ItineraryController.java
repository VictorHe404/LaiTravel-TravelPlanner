package com.laioffer.travelplanner.controller;

import com.laioffer.travelplanner.entity.Itinerary;
import com.laioffer.travelplanner.entity.UserEntity;
import com.laioffer.travelplanner.service.ItineraryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/itinerary")
public class ItineraryController {
    private final ItineraryService itineraryService;

    private final UserEntity user = new UserEntity(4L, "peter", "123456");

    public ItineraryController(ItineraryService itineraryService) {
        this.itineraryService = itineraryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Map<String, String>> createItinerary(@RequestBody Itinerary itinerary) {
        itineraryService.createItinerary(user.getId(), itinerary.getCity(), itinerary.getDays(), itinerary.getPois());

        Map<String, String> response = new HashMap<>();
        response.put("itineraryId", UUID.randomUUID().toString());
        response.put("message", "Itinerary saved successfully");

        return ResponseEntity.ok(response);
    }
}
