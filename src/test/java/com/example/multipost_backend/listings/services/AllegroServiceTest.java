package com.example.multipost_backend.listings.services;

import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;
@RunWith(SpringRunner.class)
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
        assertNotNull(allegroService.getCategorySuggestion("Telefon Iphone 12"));
    }
    // f24f824d-6088-4042-8d8e-8d928f06515e
    @Test
    void allegroProductSearch() {
        JsonNode response = allegroService.allegroProductSearch("Audi A3", "4031").get("products");
        System.out.println(response);
        assertNotNull(response);
    }

    @Test
    void allegroProductTest() {
        JsonNode response = allegroService.getProduct("065bb735-4257-44d4-93f6-4f7decc71150");
        System.out.println(response);
        assertNotNull(response);
    }

    @Test
    void getParamsTest() {
        JsonNode response = allegroService.getParams("165");
        System.out.println(response);
        assertNotNull(response);
    }

    @Test
    void getProductByGTIN() {
        long gtin = 888462600712L;
        JsonNode response = allegroService.allegroGTINProductSearch(gtin);
        System.out.println(response);
        assertNotNull(response);
    }
}