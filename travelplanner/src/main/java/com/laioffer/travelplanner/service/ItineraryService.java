package com.laioffer.travelplanner.service;

import com.laioffer.travelplanner.entity.Itinerary;
import com.laioffer.travelplanner.entity.Pois;
import com.laioffer.travelplanner.repository.ItineraryRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

// ItineraryService.java
@Service
public class ItineraryService {
    private final ItineraryRepository itineraryRepository;

    public ItineraryService(ItineraryRepository itineraryRepository) {
        this.itineraryRepository = itineraryRepository;
    }

    @Transactional
    public UUID createItinerary(Long userId, String city, int days, List<Pois> positions) {
        for (Pois poi : positions) { poi.setId(null); } // 保证新建保存
        Itinerary saved = itineraryRepository.save(
                new Itinerary(null, userId, city, days, OffsetDateTime.now(), positions)
        ); // 你现有的构造器用的就是这个签名 :contentReference[oaicite:9]{index=9} :contentReference[oaicite:10]{index=10}
        return saved.getId(); // 返回真实 UUID
    }

    @Transactional
    public Itinerary getItinerary(UUID id) {
        return itineraryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Itinerary not found: " + id));
    }
}
