package com.laioffer.travelplanner.controller;

import com.laioffer.travelplanner.dto.PoiDTO;
import com.laioffer.travelplanner.service.GooglePlacesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PoiController {

    private final GooglePlacesService googlePlacesService;

    @Autowired
    public PoiController(GooglePlacesService googlePlacesService) {
        this.googlePlacesService = googlePlacesService;
    }

    @GetMapping("/pois")
    public List<PoiDTO> getPois(@RequestParam String city, @RequestParam String keyword) {
        return googlePlacesService.searchPois(city, keyword);
    }
}
