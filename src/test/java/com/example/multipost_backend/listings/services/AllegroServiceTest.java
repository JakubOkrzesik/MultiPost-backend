package com.example.multipost_backend.listings.services;

import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class AllegroServiceTest {

    @Autowired
    private AllegroService allegroService;
    @Autowired
    private UserRepository userRepository;

    @Test
    void getUserToken() {
        User user = userRepository.findByEmail("test@user.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        System.out.println(allegroService.getUserToken(user));
    }

    @Test
    void getCategorySuggestion() {
        User user = userRepository.findByEmail("admin@admin.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        System.out.println(allegroService.getCategorySuggestion("Iphone XS Max", user));
    }
    // f24f824d-6088-4042-8d8e-8d928f06515e
    @Test
    void allegroProductSearch() {
        JsonNode response = allegroService.allegroProductSearch("Iphone XS Max", "165").get("products");
        System.out.println(response.get(0));
    }

    @Test
    void allegroProductTest() {
        JsonNode response = allegroService.getProduct("065bb735-4257-44d4-93f6-4f7decc71150");
        System.out.println(response);
    }

    @Test
    void getParamsTest() {
        JsonNode response = allegroService.getParams("165");
        System.out.println(response);
    }
}