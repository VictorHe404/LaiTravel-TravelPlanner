package com.laioffer.travelplanner.dto;

import jakarta.validation.constraints.NotBlank;

public class AuthDTOs {
    public record RegisterRequest(@NotBlank String username, @NotBlank String password) {}
    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record MeResponse(String id, String username) {}
}
