package com.laioffer.travelplanner.service;

import com.laioffer.travelplanner.entity.Itinerary;
import com.laioffer.travelplanner.entity.UserEntity;
import com.laioffer.travelplanner.repository.ItineraryRepository;
import com.laioffer.travelplanner.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ItineraryService {

    private final ItineraryRepository itineraryRepo;
    private final UserRepository userRepo;

    public ItineraryService(ItineraryRepository itineraryRepo, UserRepository userRepo) {
        this.itineraryRepo = itineraryRepo;
        this.userRepo = userRepo;
    }

    private UserEntity currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new IllegalStateException("Unauthenticated"); // 由安全层保证一般到不了
        }
        var username = auth.getName();
        return userRepo.findByUsername(username).orElseThrow(() -> new IllegalStateException("User not found"));
    }

    @Transactional
    public Itinerary create(Itinerary toCreate) {
        var user = currentUser();

        // 绑定当前用户
        toCreate.setUser(user);

        // 让子表建立反向关系
        if (toCreate.getPois() != null) {
            toCreate.getPois().forEach(p -> p.setItinerary(toCreate));
        }

        return itineraryRepo.save(toCreate);
    }

    @Transactional
    public Itinerary update(UUID id, Itinerary update) {
        var user = currentUser();
        var existing = itineraryRepo.findByIdAndUser_Id(id, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Itinerary not found"));

        existing.setCity(update.getCity());
        existing.setDays(update.getDays());

        // 重设 POIs（级联 + orphanRemoval）
        existing.setPois(update.getPois());

        return itineraryRepo.save(existing);
    }

    public Itinerary getById(UUID id) {
        var user = currentUser();
        return itineraryRepo.findByIdAndUser_Id(id, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Itinerary not found"));
    }

    public List<Itinerary> listMine() {
        var user = currentUser();
        return itineraryRepo.findAllByUser_Id(user.getId());
    }

    // [ADDED] 为“按 id 获取，但必须是当前用户的行程”
    public Itinerary getOwned(UUID id) {
        var user = currentUser();
        return itineraryRepo.findByIdAndUser_Id(id, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Itinerary not found"));
    }

    @Transactional
    public void delete(UUID id) {
        var user = currentUser();
        var existing = itineraryRepo.findByIdAndUser_Id(id, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Itinerary not found"));
        itineraryRepo.delete(existing);
    }
}
