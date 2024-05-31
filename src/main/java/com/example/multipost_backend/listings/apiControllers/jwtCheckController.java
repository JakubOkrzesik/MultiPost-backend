package com.example.multipost_backend.listings.apiControllers;


import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/v1/jwt")
@RequiredArgsConstructor
public class jwtCheckController {
    @GetMapping(value = "/check", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getCategorySuggestion() {

        Map<String, String> response = new HashMap<>();
        response.put("message", "Jwt Token up to date");

        return ResponseEntity.ok(response);
    }

}
