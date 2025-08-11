package com.laioffer.travelplanner.service;

import com.laioffer.travelplanner.entity.City;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CityService {

    public List<City> getSupportedCities() {
        return List.of(
                new City("Beijing", "China", 1)
        );
    }
}
