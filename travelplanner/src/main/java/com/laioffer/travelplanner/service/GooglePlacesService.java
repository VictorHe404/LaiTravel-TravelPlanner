package com.laioffer.travelplanner.service;

import com.laioffer.travelplanner.dto.PoiDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Service
public class GooglePlacesService {

    @Value("${google.places.apiKey}")
    private String apiKey;

    @Value("${google.places.useProxy:false}")
    private boolean useProxy;

    @Value("${google.places.proxyHost:127.0.0.1}")
    private String proxyHost;

    @Value("${google.places.proxyPort:7890}")
    private int proxyPort;

    private final RestTemplate restTemplate;

    public GooglePlacesService() {
        this.restTemplate = buildRestTemplate(false, "127.0.0.1", 7890); // 默认构造函数先用占位参数
    }

    // 使用 @PostConstruct 来延迟初始化并根据配置重新构造 restTemplate
    @jakarta.annotation.PostConstruct
    private void init() {
        this.restTemplate.setRequestFactory(
                useProxy ? buildRequestFactoryWithProxy(proxyHost, proxyPort)
                        : new SimpleClientHttpRequestFactory()
        );
    }

    private SimpleClientHttpRequestFactory buildRequestFactoryWithProxy(String host, int port) {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setProxy(proxy);
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(10000);
        return factory;
    }

    private RestTemplate buildRestTemplate(boolean proxyEnabled, String host, int port) {
        SimpleClientHttpRequestFactory factory = proxyEnabled
                ? buildRequestFactoryWithProxy(host, port)
                : new SimpleClientHttpRequestFactory();
        return new RestTemplate(factory);
    }

    public List<PoiDTO> searchPois(String city, String keyword) {
        String location = getCityCenter(city);
        int radius = 50000;

        UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host("maps.googleapis.com")
                .path("/maps/api/place/nearbysearch/json")
                .queryParam("location", location)
                .queryParam("radius", radius)
                .queryParam("keyword", keyword)
                .queryParam("key", apiKey);

        String url = builder.toUriString();

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        // System.out.println("Google API Response = " + response.getBody());

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to fetch from Google Places API");
        }

        return parseResponse(response.getBody());
    }

    private String getCityCenter(String city) {
        return switch (city.toLowerCase()) {
            case "beijing" -> "39.9042,116.4074";
            case "tokyo" -> "35.6895,139.6917";
            default -> throw new IllegalArgumentException("Unsupported city: " + city);
        };
    }

    private List<PoiDTO> parseResponse(String json) {
        List<PoiDTO> pois = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode results = mapper.readTree(json).get("results");

            for (JsonNode node : results) {
                String name = node.get("name").asText();
                double lat = node.get("geometry").get("location").get("lat").asDouble();
                double lng = node.get("geometry").get("location").get("lng").asDouble();
                pois.add(new PoiDTO(name, lat, lng));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Google API response", e);
        }

        return pois;
    }
}
