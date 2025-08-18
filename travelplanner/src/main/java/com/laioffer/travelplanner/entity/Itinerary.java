package com.laioffer.travelplanner.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "itineraries")
public class Itinerary {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false)
    private int days;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    // 关键：仅保留 JPA 关联到用户
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false) // 外键 -> users.id (Long)
    private UserEntity user;

    // 关键：OneToMany 正确的 mappedBy，和 Pois.itinerary 一致
    @OneToMany(mappedBy = "itinerary", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Pois> pois = new ArrayList<>();

    public Itinerary() {}

    public UUID getId() { return id; }

    public void setId(UUID id) { this.id = id; }

    public String getCity() { return city; }

    public void setCity(String city) { this.city = city; }

    public int getDays() { return days; }

    public void setDays(int days) { this.days = days; }

    public OffsetDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public UserEntity getUser() { return user; }

    public void setUser(UserEntity user) { this.user = user; }

    public List<Pois> getPois() { return pois; }

    public void setPois(List<Pois> pois) {
        this.pois.clear();
        if (pois != null) {
            pois.forEach(this::addPoi);
        }
    }

    public void addPoi(Pois p) {
        p.setItinerary(this);
        this.pois.add(p);
    }

    public void removePoi(Pois p) {
        p.setItinerary(null);
        this.pois.remove(p);
    }
}
