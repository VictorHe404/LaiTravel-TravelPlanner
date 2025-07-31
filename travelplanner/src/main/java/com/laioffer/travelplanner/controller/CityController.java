package com.laioffer.travelplanner.controller;

import com.laioffer.travelplanner.entity.City;
import com.laioffer.travelplanner.service.CityService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
public class CityController {

    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    @GetMapping
    public List<City> getCities() {
        return cityService.getSupportedCities();
    }
}
