package com.example.multipost_backend.listings.services;

import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.adverts.AdvertNotFoundException;
import com.example.multipost_backend.listings.dbmodels.AllegroListingState;
import com.example.multipost_backend.listings.dbmodels.ListingRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getCategorySuggestion() {
        JsonNode response = allegroService.getCategorySuggestion("Telefon Iphone 12");
        ArrayNode array = objectMapper.createArrayNode();
        assertNotEquals(array, response);
    }

    @Test
    void allegroProductSearch() {
        JsonNode response = allegroService.allegroProductSearch("Iphone 12 telefon", "165").get("products");
        ArrayNode array = objectMapper.createArrayNode();
        assertNotEquals(array, response);
    }

    @Test
    void allegroProductTest() {
        JsonNode response = allegroService.getProduct("065bb735-4257-44d4-93f6-4f7decc71150");
        ArrayNode array = objectMapper.createArrayNode();
        assertNotEquals(array, response);
    }

    @Test
    void getParamsTest() {
        JsonNode response = allegroService.getParams("165");
        ObjectNode array = objectMapper.createObjectNode();
        assertNotEquals(array, response);
    }

    @Test
    void getProductByGTIN() {
        long gtin = 888462600712L;
        JsonNode response = allegroService.allegroGTINProductSearch(gtin);
        ObjectNode array = objectMapper.createObjectNode();
        assertNotEquals(array, response);
    }

    @Test
    void updatePrice() {
        User user = userRepository.findByEmail("test@user.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        JsonNode response = allegroService.updateAdvertPrice(7000, "7767839558", user);
        ObjectNode array = objectMapper.createObjectNode();
        assertNotEquals(array, response);
    }

    @Test
    void delistAdvert() {
        User user = userRepository.findByEmail("test@user.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        JsonNode response = allegroService.changeAdvertStatus("7767839558", AllegroListingState.ENDED, user);
        ObjectNode array = objectMapper.createObjectNode();
        assertNotEquals(array, response);
    }
}