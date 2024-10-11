package com.example.multipost_backend.listings.services;

import com.example.multipost_backend.MultiPostBackendApplication;
import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.olx.Location;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(classes = MultiPostBackendApplication.class)
class OlxServiceTest {

    @Autowired
    private OlxService olxService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getLocation() throws JsonProcessingException {
        Location location = olxService.getLocation("49.815012", "18.923237");
        System.out.println(location);
        assertNotNull(location.getCity_id());
    }

    @Test
    void getCategorySuggestion() throws JsonProcessingException {
        String suggestion = olxService.getCategorySuggestion("Audi A3");
        assertEquals(182, Integer.parseInt(suggestion));
    }

    @Test
    void getCategoryAttributes() throws JsonProcessingException {
        String suggestion = olxService.getCategorySuggestion("Audi A3");
        System.out.println(suggestion);
        List<JsonNode> data1 = olxService.getCategoryAttributes(suggestion);
        List<JsonNode> emptyList = new ArrayList<>();
        System.out.println(data1);
        assertNotEquals(emptyList, data1);
    }

    @Test
    void olxPriceUpdateTest() {
        User user = userRepository.findByEmail("test@user.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        JsonNode response = olxService.updateAdvertPrice(6900, "925323675", user);
        ObjectNode emptyNode = objectMapper.createObjectNode();
        assertNotEquals(emptyNode, response);
    }

    @Test
    void olxAdvertDeletionTest() {

        User user = userRepository.findByEmail("test@user.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        // Provide your desired ID
        String adId = "923733586";
        String command = "finish";

        ResponseEntity<Void> statusChangeResponse = olxService.changeAdvertStatus(adId, command, user);
        System.out.println(statusChangeResponse);
        assertThat(statusChangeResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void getUserAdverts() {
        User user = userRepository.findByEmail("test@user.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        JsonNode response = olxService.getAdverts(user);
        System.out.println(response);
    }
}