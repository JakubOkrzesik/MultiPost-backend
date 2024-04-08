package com.example.multipost_backend.listings.services;

import com.example.multipost_backend.listings.olx.Location;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class OlxServiceTest {

    @Autowired
    private OlxService olxService;

    @Test
    void getAdvert() {
    }

    @Test
    void testGetAdvert() {
    }

    @Test
    void getLocation() {
        Location location = olxService.getLocation("50.049683", "19.944544");
        assertNotNull(location.toString());
    }

    @Test
    void getCategorySuggestion() throws JsonProcessingException {
        String suggestion = olxService.getCategorySuggestion("Audi A3");
        System.out.println(suggestion);
        assertNotNull(suggestion);
    }
}