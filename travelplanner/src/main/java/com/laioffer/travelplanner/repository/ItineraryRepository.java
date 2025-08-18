package com.laioffer.travelplanner.repository;

import com.laioffer.travelplanner.entity.Itinerary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItineraryRepository extends JpaRepository<Itinerary, UUID> {

    // 基于 JPA 关联（UserEntity.id）
    Optional<Itinerary> findByIdAndUser_Id(UUID id, Long userId);

    List<Itinerary> findAllByUser_Id(Long userId);
}
