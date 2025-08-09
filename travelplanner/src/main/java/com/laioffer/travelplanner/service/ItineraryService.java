package com.laioffer.travelplanner.service;

import com.laioffer.travelplanner.entity.Itinerary;
import com.laioffer.travelplanner.entity.Pois;
import com.laioffer.travelplanner.repository.ItineraryRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ItineraryService {

    private ItineraryRepository itineraryRepository;

    public ItineraryService(ItineraryRepository itineraryRepository) {
        this.itineraryRepository = itineraryRepository;
    }

    @Transactional
    public void createItinerary(Long userId, String city, int days, List<Pois> positions) {
        for (Pois poi : positions) {
            poi.setId(null); // 强制作为新 entity
        }
        itineraryRepository.save(new Itinerary(null, userId, city, days, OffsetDateTime.now(), positions));
    }
}
