package com.laioffer.travelplanner.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "itinerary_pois")
public class ItineraryPoi {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double lat;

    @Column(nullable = false)
    private double lng;

    @Column(nullable = false)
    private int sequence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id", nullable = false)
    private Itinerary itinerary;

    protected ItineraryPoi() {
        // JPA requires a default constructor
    }

    public ItineraryPoi(Itinerary itinerary, String name, double lat, double lng, int sequence) {
        this.itinerary = itinerary;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.sequence = sequence;
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public Itinerary getItinerary() {
        return itinerary;
    }

    public void setItinerary(Itinerary itinerary) {
        this.itinerary = itinerary;
    }
}