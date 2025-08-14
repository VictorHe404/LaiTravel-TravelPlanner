package com.laioffer.travelplanner.controller;

import com.laioffer.travelplanner.entity.Itinerary;
import com.laioffer.travelplanner.entity.Pois;
import com.laioffer.travelplanner.entity.UserEntity;
import com.laioffer.travelplanner.service.ItineraryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

// ItineraryController.java
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api") // 由原来的 "/itinerary" 改为 "/api" :contentReference[oaicite:12]{index=12}
public class ItineraryController {
    private final ItineraryService itineraryService;
    private final UserEntity user = new UserEntity(4L, "peter", "123456"); // 你当前的临时用户保持不变 :contentReference[oaicite:13]{index=13}

    public ItineraryController(ItineraryService itineraryService) {
        this.itineraryService = itineraryService;
    }

    @PostMapping("/itinerary")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Map<String, String>> createItinerary(@RequestBody Itinerary itinerary) {
        UUID newId = itineraryService.createItinerary(
                user.getId(), itinerary.getCity(), itinerary.getDays(), itinerary.getPois()
        ); // 复用你现有的入参对象字段 :contentReference[oaicite:14]{index=14}

        Map<String, String> resp = new HashMap<>();
        resp.put("itineraryId", newId.toString()); // 不再随机 UUID
        resp.put("message", "Itinerary saved successfully");
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/itinerary/{id}")
    public ResponseEntity<?> getItinerary(@PathVariable UUID id) {
        try {
            Itinerary it = itineraryService.getItinerary(id);
            // 确保 pois 有序（按 day, sequence）；若不想在 DB 层加 @OrderBy，这里排序即可：
            List<Pois> pois = it.getPois(); // 你的实体暴露了 getter :contentReference[oaicite:15]{index=15}
            if (pois != null) {
                pois.sort(Comparator
                        .comparingInt(Pois::getDay)       // 你有 day 字段 :contentReference[oaicite:16]{index=16}
                        .thenComparingInt(Pois::getSequence)); // 也有 sequence 字段 :contentReference[oaicite:17]{index=17}
            }

            // 组装与 API 文档一致的返回结构
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("id", it.getId());
            body.put("city", it.getCity());
            body.put("days", it.getDays());
            body.put("createdAt", it.getCreatedAt()); // 你在保存时设置了 now() :contentReference[oaicite:18]{index=18}
            List<Map<String, Object>> poiList = new ArrayList<>();
            for (Pois p : pois) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("name", p.getName());
                m.put("lat", p.getLat());
                m.put("lng", p.getLng());
                m.put("sequence", p.getSequence());
                m.put("day", p.getDay()); // 前端用得到，可保留；若想严格最小响应可去掉 day
                poiList.add(m);
            }
            body.put("pois", poiList);

            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException notFound) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Itinerary not found", "itineraryId", id.toString()));
        }
    }
}
