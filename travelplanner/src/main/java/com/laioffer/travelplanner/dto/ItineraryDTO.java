package com.laioffer.travelplanner.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ItineraryDTO(
        UUID id,
        String city,
        int days,
        Instant createdAt,
        List<PoiDTO> pois
) { }
