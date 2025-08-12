package com.laioffer.travelplanner.repository;

import com.laioffer.travelplanner.entity.Itinerary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface ItineraryRepository extends JpaRepository<Itinerary, UUID>{

}