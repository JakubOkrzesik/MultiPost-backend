package com.example.multipost_backend.listings.services;


import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.dbmodels.UserAccessKeys;
import com.example.multipost_backend.listings.olx.*;
import com.example.multipost_backend.listings.SharedApiModels.GrantCodeResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.web.reactive.function.BodyInserters.fromFormData;

@Service
@AllArgsConstructor
public class OlxService {

    private final WebClient OlxClient;
    private final UserRepository userRepository;
    // Cached user tokens are added to HashMap to reduce amount of db queries
    private final Map<String, Map<String, Object>> userTokenCache = new ConcurrentHashMap<>();
    private final EnvService envService;
    private final GeneralService generalService;
    private final ObjectMapper objectMapper;

    // Form content from advert creation is passed as a map
    public JsonNode createAdvert(Advert newAdvert, User user) {
        return OlxClient.post()
                .uri("/partner/adverts")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> {
                    h.add("Content-Type", "application/json header");
                    h.addAll(getUserHeaders(user));
                })
                .bodyValue(newAdvert)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    public JsonNode getUserAdverts(User user) {
        return OlxClient.get()
                .uri("/partner/adverts")
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    public JsonNode getAdvert(String advertID, User user) {
        return OlxClient.get()
                .uri(String.format("/partner/adverts/%s", advertID))
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    public JsonNode updateAdvert(ObjectNode updatedAdvert, String advertID, User user) {
        return OlxClient.put()
                .uri(String.format("/partner/adverts/%s", advertID))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .bodyValue(updatedAdvert)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }


    public ResponseEntity<Void> changeAdvertStatus(String advertID, String command,User user) {

        String uri = String.format("/partner/adverts/%s/commands?command", advertID);

        return OlxClient.post()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .body(fromFormData("command", "deactivate").with("is_success", "false"))
                .retrieve()
                .toBodilessEntity()
                .block();
    }


    public ResponseEntity<Void> deleteAdvert(String advertID, User user) {
        return OlxClient.delete()
                .uri(String.format("/partner/adverts/%s", advertID))
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .retrieve()
                .toBodilessEntity()
                .block();
    }


    // OLX requires location to be acquired from their api - retrieving user specified coordinates from Google Maps api
    // and parsing them into the request body
    public Location getLocation(String lat, String lon) throws JsonProcessingException {

        User user = userRepository.findByEmail("admin@admin.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String data = OlxClient.get()
                .uri(String.format("/partner/locations?latitude=%s&longitude=%s", lat, lon))
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JsonNode node = objectMapper.readTree(data).get("data").get(0);

        return Location.builder()
                .city_id(node.get("city").get("id").asText())
                .district_id(node.get("district").get("id").asText())
                .build();
    }

    // Title is provided to extract a category ID
    public String getCategorySuggestion(String title) throws JsonProcessingException {

        // Not the most efficient way of going about it needs tweaking
        User user = userRepository.findByEmail("admin@admin.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String data = OlxClient.get()
                .uri(String.format("/partner/categories/suggestion?q=%s", title))
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JsonNode jsonData = objectMapper.readTree(data);

        return jsonData.get("data").get(0).get("id").asText();
    }

    public List<Attrib> getCategoryAttributes(String id) throws JsonProcessingException {

        User user = userRepository.findByEmail("admin@admin.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String uri = String.format("/partner/categories/%s/attributes", id);

        JsonNode rootNode = OlxClient.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        assert rootNode != null;

        List<Attrib> attribList = new ArrayList<>();

        for (JsonNode node : rootNode.get("data")) {
            String code = node.get("code").asText();
            String value = node.get("values").get(0).get("code").asText();

            attribList.add(Attrib.builder()
                    .code(code)
                    .value(value)
                    .build());
        }

        return attribList;
    }


    // Method provides the two always required headers for OLX Api requests
    private HttpHeaders getUserHeaders(User user) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", getUserToken(user)));
        headers.set("Version", "2.0");
        return headers;
    }

    // Retrieves the access token, refresh token and access token expiration date after
    // getting auth code from olx
    public GrantCodeResponse getOlxToken(String code) {
        return OlxClient.post()
                .uri("/open/oauth/token")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(OlxTokenRequest.otRequestBuilder()
                        .grant_type("authorization_code")
                        .client_id(envService.getOLX_CLIENT_ID())
                        .client_secret(envService.getOLX_CLIENT_SECRET())
                        .code(code)
                        .scope("v2 read write")
                        .build()
                )
                .retrieve()
                .bodyToMono(GrantCodeResponse.class)
                .block();
    }


    // Retrieving the olx user token from the database. If the token is expired a request is made
    // with the refresh token to update the user token
    public String getUserToken(User user) {
        // Check if user is in cache and if the token is expired
        Map<String, Object> tokenData = userTokenCache.get(user.getEmail());
        if (tokenData != null && generalService.isTokenExpired((Date) tokenData.get("expDate"))) {
            return (String) tokenData.get("cachedToken");
        }

        Map<String, Object> innerTokenMap = new ConcurrentHashMap<>();

        UserAccessKeys keys = user.getKeys();
        if (keys.getOlxAccessToken() != null) {
            if (generalService.isTokenExpired(keys.getOlxTokenExpiration())) {
                OlxTokenResponse response = updateUserToken(keys.getOlxRefreshToken());
                // Updating the database
                keys.setOlxAccessToken(response.getAccess_token());
                keys.setOlxRefreshToken(response.getRefresh_token());
                keys.setOlxTokenExpiration(generalService.calculateExpiration(response.getExpires_in()));
                // Updating the cache

                innerTokenMap.put("cachedToken", response.getAccess_token());
                innerTokenMap.put("expDate", generalService.calculateExpiration(response.getExpires_in()));

                userTokenCache.put(user.getEmail(), innerTokenMap);
                userRepository.save(user);
                return keys.getOlxAccessToken();
            }
            // Data up to date but not in cache
            innerTokenMap.put("cachedToken", keys.getOlxAccessToken());
            innerTokenMap.put("expDate", keys.getOlxTokenExpiration());
            userTokenCache.put(user.getEmail(), innerTokenMap);
            return keys.getOlxAccessToken();
        }
        // User does not have OLX credentials set up
        return "User needs to be signed in to the OLX Api again";
    }

    // Method makes request with the refresh token to update the user token
    private OlxTokenResponse updateUserToken(String refreshToken) {
        return OlxClient.post()
                .uri("/open/oauth/token").accept(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(OlxRefreshRequest.orRequestBuilder()
                        .grant_type("refresh_token")
                        .client_id(envService.getOLX_CLIENT_ID())
                        .client_secret(envService.getOLX_CLIENT_SECRET())
                        .refresh_token(refreshToken)
                        .build())
                .retrieve()
                .bodyToMono(OlxTokenResponse.class)
                .block();
    }


    // Function that differentiates from getOlxToken is needed because of the differences in request body
    public OlxTokenResponse getApplicationToken() {
        return OlxClient.post()
                .uri("/open/oauth/token")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(OlxClientRequest.ocRequestBuilder()
                        .grant_type("client_credentials")
                        .client_id(envService.getOLX_CLIENT_ID())
                        .client_secret(envService.getOLX_CLIENT_SECRET())
                        .scope("v2 read write")
                        .build())
                .retrieve()
                .bodyToMono(OlxTokenResponse.class)
                .block();
    }

}
