package com.laioffer.travelplanner.service;

import com.laioffer.travelplanner.dto.ItineraryDTO;
import com.laioffer.travelplanner.dto.PoiDTO;
import com.laioffer.travelplanner.entity.Itinerary;
import com.laioffer.travelplanner.repository.ItineraryRepository;
import com.laioffer.travelplanner.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ItineraryService {

    private final ItineraryRepository repo;

    public ItineraryService(ItineraryRepository repo) {
        this.repo = repo;
    }

    public ItineraryDTO getItinerary(UUID id) {
        Itinerary it = repo.findByIdWithPois(id)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerary not found: " + id));
        var pois = it.getPois().stream()
                .map(p -> new PoiDTO(p.getName(), p.getLat(), p.getLng(), p.getSequence()))
                .collect(Collectors.toList());
        return new ItineraryDTO(
                it.getId(),
                it.getCity(),
                it.getDays(),
                it.getCreatedAt(),
                pois
        );
    }
}