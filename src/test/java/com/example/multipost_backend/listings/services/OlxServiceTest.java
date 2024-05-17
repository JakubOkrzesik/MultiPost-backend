package com.example.multipost_backend.listings.services;

import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.olx.Attrib;
import com.example.multipost_backend.listings.olx.Location;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class OlxServiceTest {

    @Autowired
    private OlxService olxService;
    @Autowired
    private GeneralService generalService;
    @Autowired
    private UserRepository userRepository;

    /*@Test
    void getUser() {
        String email = generalService.getUsername("Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QHVzZXIuY29tIiwiaWF0IjoxNzE1ODEwMjk3LCJleHAiOjE3MTU4OTY2OTd9.EWloIdAs7Yh47VsFybtbWA0j7QgpA4x8xovKGCO32WU");
        System.out.println(email);

        System.out.println(user);
    }*/

    @Test
    void testGetAdvert() {
    }

    @Test
    void getLocation() throws JsonProcessingException {
        Location location = olxService.getLocation("49.815012", "18.923237");
        System.out.println(location);
        assertNotNull(location.getCity_id());
    }

    @Test
    void getCategorySuggestion() throws JsonProcessingException {
        String suggestion = olxService.getCategorySuggestion("Audi A3");
        System.out.println(suggestion);
        assertNotNull(suggestion);
    }

    @Test
    void getCategoryAttributes() throws JsonProcessingException {
        String suggestion = olxService.getCategorySuggestion("Audi A3");
        System.out.println(suggestion);
        List<JsonNode> data1 = olxService.getCategoryAttributes(suggestion);
        System.out.println(data1);
        assertNotNull(data1);
    }
}