package com.laioffer.travelplanner.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "itineraries")
public class Itinerary {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private int days;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(
            mappedBy = "itinerary",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @OrderBy("sequence ASC")
    private List<ItineraryPoi> pois;

    protected Itinerary() {
        // JPA requires a default constructor
    }

    public Itinerary(UUID id, String city, int days) {
        this.id = id;
        this.city = city;
        this.days = days;
        this.createdAt = Instant.now();
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<ItineraryPoi> getPois() {
        return pois;
    }

    public void setPois(List<ItineraryPoi> pois) {
        this.pois = pois;
    }
}