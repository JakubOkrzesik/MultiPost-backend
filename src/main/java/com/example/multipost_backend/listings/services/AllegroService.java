package com.example.multipost_backend.listings.services;


import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.allegro.AllegroTokenRequest;
import com.example.multipost_backend.listings.allegro.AllegroTokenResponse;
import com.example.multipost_backend.listings.dbmodels.UserAccessKeys;
import com.example.multipost_backend.listings.olx.OlxTokenResponse;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@AllArgsConstructor
public class AllegroService {

    private final WebClient AllegroClient;
    private final GeneralService generalService;
    private final EnvService envService;
    private final Map<String, Map<String, Object>> userTokenCache = new ConcurrentHashMap<>();
    private final UserRepository userRepository;

    public AllegroTokenResponse getAllegroToken(String code) {
        return AllegroClient.post() // The allegro api does not work with the .bodyValue method inside webclient or im doing sth wrong (probably the case)
                .uri(String.format("auth/oauth/token?grant_type=authorization_code&code=%s&redirect_uri=%s/api/v1/auth/allegro", code, envService.getREDIRECT_URI()))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getAllegroHeaders()))
                .retrieve()
                .bodyToMono(AllegroTokenResponse.class)
                .block();
    }

    public AllegroTokenResponse getClientToken() {
        return AllegroClient.post()
                .uri("/auth/oauth/token?grant_type=client_credentials")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getAllegroHeaders()))
                .retrieve()
                .bodyToMono(AllegroTokenResponse.class)
                .block();
    }

    public JsonNode createAdvert(JsonNode data, User user) {
        return AllegroClient.mutate().baseUrl("https://api.allegro.pl").build()
                .post()
                .uri("/sale/product-offers")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUserToken(user))
                .bodyValue(data)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    public JsonNode editAllegroOffer(JsonNode data, User user) {
        return AllegroClient.mutate().baseUrl("https://api.allegro.pl").build()
                .patch()
                .uri("sale/product-offers")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUserToken(user))
                .bodyValue(data)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    private JsonNode allegroProductSearch(String searchPhrase) {

        User user = userRepository.findByEmail("admin@admin.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return AllegroClient.mutate().baseUrl("https://api.allegro.pl").build()
                .get()
                .uri(String.format("/sale/products?phrase=%s", searchPhrase))
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUserToken(user))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    public String getUserToken(User user) {
        // Check if user is in cache and if the token is expired
        Map<String, Object> tokenData = userTokenCache.get(user.getEmail());
        if (tokenData != null && generalService.isTokenExpired((Date) tokenData.get("expDate"))) {
            return (String) tokenData.get("cachedToken");
        }

        Map<String, Object> innerTokenMap = new ConcurrentHashMap<>();

        UserAccessKeys keys = user.getKeys();
        if (keys.getAllegroAccessToken() != null) {
            if (generalService.isTokenExpired(keys.getAllegroTokenExpiration())) {
                AllegroTokenResponse response = updateUserToken(keys.getAllegroRefreshToken());
                // Updating the database
                keys.setAllegroAccessToken(response.getAccess_token());
                keys.setAllegroRefreshToken(response.getRefresh_token());
                keys.setAllegroTokenExpiration(generalService.calculateExpiration(response.getExpires_in()));
                // Updating the cache

                innerTokenMap.put("cachedToken", response.getAccess_token());
                innerTokenMap.put("expDate", generalService.calculateExpiration(response.getExpires_in()));

                userTokenCache.put(user.getEmail(), innerTokenMap);
                userRepository.save(user);
                return keys.getOlxAccessToken();
            }
            // Data up to date but not in cache
            innerTokenMap.put("cachedToken", keys.getAllegroAccessToken());
            innerTokenMap.put("expDate", keys.getAllegroTokenExpiration());
            userTokenCache.put(user.getEmail(), innerTokenMap);
            return keys.getAllegroAccessToken();
        }
        // User does not have OLX credentials set up
        return "User needs to be signed in to the OLX Api again";
    }

    private AllegroTokenResponse updateUserToken(String allegroRefreshToken) {
        return AllegroClient.post()
                .uri(String.format("/auth/oauth/token?grant_type=client_credentials?refresh_token=%s", allegroRefreshToken))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getAllegroHeaders()))
                .retrieve()
                .bodyToMono(AllegroTokenResponse.class)
                .block();
    }

    // Creating Allegro headers
    private HttpHeaders getAllegroHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, generalService.getAuthorizationHeader(envService.getALLEGRO_CLIENT_ID(), envService.getALLEGRO_CLIENT_SECRET()));
        return headers;
    }

}
