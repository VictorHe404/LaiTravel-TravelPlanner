package com.laioffer.travelplanner.entity;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Pois {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private double lat;
    private double lng;
    private int sequence;
    private int day;

    public Pois() {
    }

    public Pois(Long id, String name, double lat, double lng, int sequence, int day) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.sequence = sequence;
        this.day = day;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public int getSequence() {
        return sequence;
    }

    public int getDay() {
        return day;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pois)) return false;
        Pois pos = (Pois) o;
        return Long.compare(pos.id, id) == 0 &&
                Double.compare(pos.lat, lat) == 0 &&
                Double.compare(pos.lng, lng) == 0 &&
                day == pos.day &&
                sequence == pos.sequence &&
                Objects.equals(name, pos.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, lat, lng, sequence, day);
    }

    @Override
    public String toString() {
        return "Pois{" +
                "id=" + id +
                "name='" + name + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                ", sequence=" + sequence +
                ", day=" + day +
                '}';
    }
}
