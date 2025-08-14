package com.laioffer.travelplanner.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public record UserEntity(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id,
        String username,
        String password
) {
    public Long getId() {
        return id;
    }
}
