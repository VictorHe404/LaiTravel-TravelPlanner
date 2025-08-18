package com.laioffer.travelplanner.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "pois")
public class Pois {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false)
    private double lat;

    @Column(nullable = false)
    private double lng;

    @Column(nullable = false)
    private int sequence;

    @Column
    private Integer day; // 可为 null，按你需要

    // 关键：与 Itinerary.pois 对应的 owning-side
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id") // 外键 -> itineraries.id (UUID)
    @JsonBackReference
    private Itinerary itinerary;

    public Pois() {}

    public UUID getId() { return id; }

    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public double getLat() { return lat; }

    public void setLat(double lat) { this.lat = lat; }

    public double getLng() { return lng; }

    public void setLng(double lng) { this.lng = lng; }

    public int getSequence() { return sequence; }

    public void setSequence(int sequence) { this.sequence = sequence; }

    public Integer getDay() { return day; }

    public void setDay(Integer day) { this.day = day; }

    public Itinerary getItinerary() { return itinerary; }

    public void setItinerary(Itinerary itinerary) { this.itinerary = itinerary; }
}
