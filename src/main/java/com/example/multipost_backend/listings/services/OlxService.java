package com.example.multipost_backend.listings.services;


import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.dbmodels.UserAccessKeys;
import com.example.multipost_backend.listings.olx.*;
import com.example.multipost_backend.listings.SharedApiModels.GrantCodeResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    public JsonNode createAdvert(JsonNode newAdvert, User user) throws IOException {

        String data = OlxClient.post()
                .uri("/partner/adverts")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> {
                    h.add("Content-Type", "application/json header");
                    h.addAll(getUserHeaders(user));
                })
                .bodyValue(newAdvert)
                // could replace Advert class with JsonNode
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException("Client error: " + errorBody))))
                .bodyToMono(String.class)
                .block();

        return objectMapper.readTree(data);
    }

    public String getAdvert(User user) {
        return OlxClient.get()
                .uri("/partner/adverts")
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException("Client error: " + errorBody))))
                .toString();
    }

    public String getAdvert(String advertID, User user) {
        return OlxClient.get()
                .uri(String.format("/partner/adverts/%s", advertID))
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> {
                    h.add("Content-Type", "application/json header");
                    h.addAll(getUserHeaders(user));
                })
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException("Client error: " + errorBody))))
                .toString();
    }


    // OLX requires location to be acquired from their api - retrieving user specified coordinates from Google Maps api
    // and parsing them into the request body
    public Location getLocation(String lat, String lon) {

        User user = userRepository.findByEmail("admin@admin.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return OlxClient.get()
                .uri(String.format("/partner/locations?latitude=%s&longitude=%s", lat, lon))
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                /*.attributes(a -> {
                    a.put("latitude", lat);
                    a.put("longitude", lon);
                })*/
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException("Client error: " + errorBody))))
                .bodyToMono(Location.class)
                .block();
    }

    // Title is provided to extract a category ID
    public String getCategorySuggestion(String title) throws JsonProcessingException {

        // Not the most efficient way of going about it needs tweaking
        User user = userRepository.findByEmail("admin@admin.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String data =  OlxClient.get()
                .uri(String.format("partner/categories/suggestion?q=%s", title))
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(user)))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException("Client error: " + errorBody))))
                .bodyToMono(String.class)
                .block();

        assert data != null;
        JsonNode jsonData = objectMapper.readTree(data);

        return jsonData.get("path").get("id").asText();
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
                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException("Client error: " + errorBody))))
                .bodyToMono(GrantCodeResponse.class)
                .block();
    }


    // Retrieving the olx user token from the database. If the token is expired a request is made
    // with the refresh token to update the user token
    @Transactional
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
                .bodyValue(OlxRefreshRequest.orRequestBuilder()
                        .grant_type("refresh_token")
                        .client_id(envService.getOLX_CLIENT_ID())
                        .client_secret(envService.getOLX_CLIENT_SECRET())
                        .refresh_token(refreshToken))
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
                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException("Client error: " + errorBody))))
                .bodyToMono(OlxTokenResponse.class)
                .block();
    }

}
