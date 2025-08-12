package com.laioffer.travelplanner.repository;

import com.laioffer.travelplanner.entity.Itinerary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ItineraryRepository extends JpaRepository<Itinerary, UUID> {

    @Query("SELECT i FROM Itinerary i JOIN FETCH i.pois WHERE i.id = :id")
    Optional<Itinerary> findByIdWithPois(@Param("id") UUID id);
}
