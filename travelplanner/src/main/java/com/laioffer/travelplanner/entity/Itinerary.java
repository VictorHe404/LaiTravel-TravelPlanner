package com.laioffer.travelplanner.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "itineraries")
public class Itinerary {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @Column(name = "user_id")
    private Long userId;
    private String city;
    private int days;
    private OffsetDateTime createdAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "itinerary_id", foreignKey = @ForeignKey(name = "position"))
    private List<Pois> pois;

    @ManyToOne
    @OnDelete(action= OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "user"), insertable = false, updatable = false)
    private UserEntity user;

    public Itinerary() {
    }


    public Itinerary(UUID id, Long userId, String city, int days, OffsetDateTime createdAt, List<Pois> position) {
        this.id = id;
        this.userId = userId;
        this.city = city;
        this.days = days;
        this.createdAt = createdAt;
        this.pois = position;
    }

    public UUID getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getCity() {
        return city;
    }

    public int getDays() {
        return days;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public UserEntity getUser() {
        return user;
    }

    public List<Pois> getPois() {
        return pois;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setPos(List<Pois> position) {
        this.pois = position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Itinerary that = (Itinerary) o;
        return Objects.equals(id, that.id) && Objects.equals(userId, that.userId) && Objects.equals(city, that.city) && Objects.equals(days, that.days) && Objects.equals(user, that.user);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, userId, city, days, user);
    }


    @Override
    public String toString() {
        return "BookingEntity{" +
                "id=" + id +
                ", userId=" + userId +
                ", city=" + city +
                ", days=" + days +
                ", user=" + user +
                '}';
    }
}
