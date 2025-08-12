package com.laioffer.travelplanner;

import com.laioffer.travelplanner.entity.Itinerary;
import com.laioffer.travelplanner.entity.ItineraryPoi;
import com.laioffer.travelplanner.repository.ItineraryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;


import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)   // 关键：禁用安全过滤器，避免 401
//@WithMockUser
@ActiveProfiles("test")                     // 使用 application-test.yml
public class ItineraryIntegrationTest {

//    @Container
//    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
//
//    // 将容器里的连接信息注入到 Spring 环境
//    @DynamicPropertySource
//    static void datasourceProps(DynamicPropertyRegistry r) {
//        r.add("spring.datasource.url", postgres::getJdbcUrl);
//        r.add("spring.datasource.username", postgres::getUsername);
//        r.add("spring.datasource.password", postgres::getPassword);
//        r.add("spring.sql.init.mode", () -> "never");     // 测试环境不跑 database-init.sql
//        r.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop"); // 按你 test 配置来
//    }

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ItineraryRepository repo;

    private final UUID TEST_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @BeforeEach
    void setUp() {
        repo.deleteAll();
        Itinerary it = new Itinerary(TEST_ID, "Tokyo", 2);
        ItineraryPoi p1 = new ItineraryPoi(it, "Shibuya Crossing", 35.6595, 139.7004, 1);
        ItineraryPoi p2 = new ItineraryPoi(it, "Meiji Shrine",    35.6764, 139.6993, 2);
        it.setPois(List.of(p1, p2));
        repo.save(it);
    }

    @Test
    void getItinerary_ShouldReturnCorrectDto() throws Exception {
        mvc.perform(get("/api/itineraries/{id}", TEST_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Tokyo"))
                .andExpect(jsonPath("$.pois[0].name").value("Shibuya Crossing"))
                .andExpect(jsonPath("$.pois[1].name").value("Meiji Shrine"));
    }

    @Test
    void getItinerary_NotFound_ShouldReturn404() throws Exception {
        mvc.perform(get("/api/itineraries/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
