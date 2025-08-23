package com.laioffer.travelplanner.controller;

import com.laioffer.travelplanner.entity.Itinerary;
import com.laioffer.travelplanner.service.ItineraryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/itinerary")
public class ItineraryController {

    private final ItineraryService service;

    public ItineraryController(ItineraryService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Itinerary> create(@RequestBody Itinerary req) {
        var created = service.create(req);
        return ResponseEntity
                .created(URI.create("/api/itinerary/" + created.getId()))
                .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Itinerary> update(@PathVariable UUID id, @RequestBody Itinerary req) {
        var updated = service.update(id, req);
        return ResponseEntity.ok(updated);
    }


    // [CHANGED] 详情查询：改为使用 service.getOwned(id) 做所有权校验
    @GetMapping("/{id}")
    public ResponseEntity<Itinerary> getOne(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(service.getOwned(id)); // 非本人或不存在 -> 抛异常走 404
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // [ADDED] 新增“当前用户的全部行程”（最小实现：不分页）
    // 路由：GET /api/itinerary/mine
    @GetMapping("/mine")
    public ResponseEntity<List<Itinerary>> listMine() {
        return ResponseEntity.ok(service.listMine());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
