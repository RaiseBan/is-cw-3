package com.example.prac.controllers;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.hc.core5.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.OutputStream;
import java.util.Objects;

@RestController
public class ProxyController {

    private final RestTemplate restTemplate;

    public ProxyController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/proxy/meals")
    public ResponseEntity<String> getMeals(@RequestParam String searchTerm) {
        String apiUrl = "https://www.themealdb.com/api/json/v1/1/search.php?s=" + searchTerm;

        try {
            // Запрос через прокси
            String response = restTemplate.getForObject(apiUrl, String.class);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching meals: " + e.getMessage());
        }
    }


    @GetMapping("/proxy/image")
    public void proxyImage(@RequestParam String url, HttpServletResponse response) {
        try {
            // Выполняем запрос через RestTemplate
            ResponseEntity<byte[]> imageResponse = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    byte[].class
            );

            // Проверяем статус ответа
            if (imageResponse.getStatusCode().is2xxSuccessful()) {
                // Устанавливаем тип контента
                String contentType = imageResponse.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
                response.setContentType(contentType != null ? contentType : "image/jpeg");

                // Записываем данные в ответ
                try (OutputStream os = response.getOutputStream()) {
                    os.write(Objects.requireNonNull(imageResponse.getBody()));
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

}
