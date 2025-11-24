package com.example.Bean.controller;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class QuoteService {

    private final RestTemplate restTemplate = new RestTemplate();

    public String getThoughtOfTheDay() {
        String url = "https://zenquotes.io/api/random";

        try {
            ResponseEntity<List<Map<String, String>>> response =
                    restTemplate.exchange(url, HttpMethod.GET, null,
                            new ParameterizedTypeReference<List<Map<String, String>>>() {});

            Map<String, String> quoteObj = response.getBody().get(0);
            return quoteObj.get("q") + " — " + quoteObj.get("a");

        } catch (Exception e) {
            return "Stay positive and keep going! — Bean App";
        }
    }
}

