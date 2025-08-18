package com.laioffer.travelplanner.service;

import com.laioffer.travelplanner.dto.AuthDTOs.*;
import com.laioffer.travelplanner.entity.UserEntity;
import com.laioffer.travelplanner.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    public AuthService(UserRepository userRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    @Transactional
    public void register(RegisterRequest req) {
        if (userRepo.existsByUsername(req.username())) {
            throw new IllegalArgumentException("Username already exists");
        }
        var u = new UserEntity(null, req.username(), encoder.encode(req.password()));
        userRepo.save(u);
    }

    @Transactional
    public UserEntity login(LoginRequest req) {
        var u = userRepo.findByUsername(req.username())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        if (!encoder.matches(req.password(), u.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        var auth = new UsernamePasswordAuthenticationToken(
                u.getUsername(), null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        return u;
    }

    public MeResponse me() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        var username = auth.getName();
        var u = userRepo.findByUsername(username).orElseThrow();
        return new MeResponse(String.valueOf(u.getId()), u.getUsername());
    }
}
