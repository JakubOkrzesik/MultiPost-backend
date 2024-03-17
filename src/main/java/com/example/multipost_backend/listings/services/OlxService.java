package com.example.multipost_backend.listings.services;


import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.dbmodels.UserAccessKeys;
import com.example.multipost_backend.listings.olx.*;
import com.example.multipost_backend.listings.SharedApiModels.GrantCodeResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Service
@AllArgsConstructor
public class OlxService {

    private final WebClient OlxClient;
    private final UserRepository userRepository;
    // Cached user tokens are added to HashMap to reduce amount of db queries
    private final Map<String, String> userTokenCache = new ConcurrentHashMap<>();
    private final EnvService envService;

    // Form content from advert creation is passed as a map
    public String advertHandler(Map<String, String> newAdvert, String email) {

        return OlxClient.post()
                .uri("/partner/adverts")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> {
                    h.add("Content-Type", "application/json header");
                    h.addAll(getUserHeaders(email));
                })
                .bodyValue(Advert.builder()
                        .title(newAdvert.get("title"))
                        .description(newAdvert.get("description"))
                        .category_id(getCategorySuggestion(newAdvert.get("title"), email))
                        .advertiserType(AdvertiserType.PRIVATE)
                        .location(getLocation(newAdvert.get("lat"), newAdvert.get("lon"), email))
                        .name(newAdvert.get("name"))
                        .images(newAdvert.get("images").split(","))
                        .price(newAdvert.get("price"))
                )
                .retrieve()
                .toString();
    }

    public String advertHandler(String email) {
        return OlxClient.get()
                .uri("/partner/adverts")
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(email)))
                .retrieve()
                .toString();
    }

    public String advertHandler(String advertID, String email) {
        return OlxClient.get()
                .uri(String.format("/partner/adverts/%s", advertID))
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> {
                    h.add("Content-Type", "application/json header");
                    h.addAll(getUserHeaders(email));
                })
                .retrieve()
                .toString();
    }


    // OLX requires location to be acquired from their api - retrieving user specified coordinates from Google Maps api
    // and parsing them into the request body
    private Location getLocation(String lat, String lon, String email) {
        return OlxClient.get()
                .uri("/partner/location")
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(email)))
                .attributes(a -> {
                    a.put("latitude", lat);
                    a.put("longitude", lon);
                })
                .retrieve()
                .bodyToMono(Location.class)
                .block();
    }

    // Title is provided to extract a category ID
    private String getCategorySuggestion(String title, String email) {
        return OlxClient.get()
                .uri("/categories/suggestion")
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getUserHeaders(email)))
                .attribute("q", title)
                .retrieve()
                .toString();
    }


    // Method provides the two always required headers for OLX Api requests
    private HttpHeaders getUserHeaders(String email){
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", getUserToken(email)));
        headers.set("Version", "2.0");
        return headers;
    }

    private String matchCategory(String email) {
        return OlxClient.get()
                .uri("/categories/suggestion")
                .headers(h -> h.addAll(getUserHeaders(email)))
                .retrieve()
                .toString();
    }

    // Retrieves the access token, refresh token and access token expiration date after
    // getting auth code from olx
    public GrantCodeResponse getOlxToken(String code){
        return OlxClient.post()
                .uri("/open/oauth/token")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new OlxTokenRequest("authorization_code", envService.getOLX_CLIENT_ID(),
                        envService.getOLX_CLIENT_SECRET(), code, "v2 read write"))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException("Client error: " + errorBody))))
                .bodyToMono(GrantCodeResponse.class)
                .block();
    }


    // Retrieving the olx user token from the database. If the token is expired a request is made
    // with the refresh token to update the user token
    public String getUserToken(String email) {
        // Check if user is in cache
        String cachedToken = userTokenCache.get(email);
        if (cachedToken != null) {
            return cachedToken;
        }
        // User not in cache check db
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserAccessKeys keys = user.getKeys();
        if (keys.getOlxAccessToken() != null) {
            if (keys.getOlxTokenExpiration().before(new Date(System.currentTimeMillis()))) {
                OlxTokenResponse response = updateUserToken(keys.getOlxRefreshToken());
                keys.setOlxAccessToken(response.getAccess_token());
                keys.setOlxRefreshToken(response.getRefresh_token());
                keys.setOlxTokenExpiration(new Date(System.currentTimeMillis() + Integer.parseInt(response.getExpires_in())));
                userTokenCache.put(email, response.getAccess_token());
                userRepository.save(user);
                return keys.getOlxAccessToken();
            }
            userTokenCache.put(email, keys.getOlxAccessToken());
            return keys.getOlxAccessToken();
        }
        return "User needs to be signed in to the OLX Api again";
        }

    // Method makes request with the refresh token to update the user token
    private OlxTokenResponse updateUserToken(String refreshToken){
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
