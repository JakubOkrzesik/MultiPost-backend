package com.example.multipost_backend.listings.services;


import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.allegro.AllegroTokenRequest;
import com.example.multipost_backend.listings.allegro.AllegroTokenResponse;
import com.example.multipost_backend.listings.dbmodels.UserAccessKeys;
import com.example.multipost_backend.listings.dbmodels.allegroListingState;
import com.example.multipost_backend.listings.olx.OlxTokenResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
        return AllegroClient.mutate().baseUrl("https://allegro.pl.allegrosandbox.pl").build().post() // The allegro api does not work with the .bodyValue method inside webclient or im doing sth wrong (probably the case)
                .uri(String.format("/auth/oauth/token?grant_type=authorization_code&code=%s&redirect_uri=%s/allegro-auth-callback", code, envService.getREDIRECT_URI()))
                .accept(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .contentType(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .headers(h -> h.addAll(getAllegroHeaders()))
                .retrieve()
                .bodyToMono(AllegroTokenResponse.class)
                .block();
    }

    public AllegroTokenResponse getClientToken() {
        return AllegroClient.mutate().baseUrl("https://allegro.pl.allegrosandbox.pl").build().post()
                .uri("/auth/oauth/token?grant_type=client_credentials")
                .accept(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .contentType(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .headers(h -> h.addAll(getAllegroHeaders()))
                .retrieve()
                .bodyToMono(AllegroTokenResponse.class)
                .block();
    }

    private AllegroTokenResponse updateUserToken(String allegroRefreshToken) {
        return AllegroClient.mutate().baseUrl("https://allegro.pl.allegrosandbox.pl").build().post()
                .uri(String.format("/auth/oauth/token?grant_type=refresh_token&refresh_token=%s&redirect_uri=%s/api/v1/auth/allegro", allegroRefreshToken, envService.getREDIRECT_URI()))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getAllegroHeaders()))
                .retrieve()
                .bodyToMono(AllegroTokenResponse.class)
                .block();
    }

    public ResponseEntity<JsonNode> createAdvert(JsonNode data, User user) {
        return AllegroClient.post()
                .uri("/sale/product-offers")
                .accept(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .contentType(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken(user))
                .bodyValue(data)
                .retrieve()
                .toEntity(JsonNode.class)
                .block();
    }

    // method checks for allegro advert status
    public JsonNode getAdvertStatus(String locationUrl, User user) {
        return AllegroClient.mutate().baseUrl(locationUrl).build().get()
                .accept(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken(user))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    public JsonNode getAdvert(String advertId, User user) {
        return AllegroClient.get()
                .uri(String.format("sale/product-offers/%s", advertId))
                .accept(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken(user))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    public JsonNode editAllegroOffer(JsonNode data, User user) {
        return AllegroClient.patch()
                .uri("sale/product-offers")
                .accept(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .contentType(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken(user))
                .bodyValue(data)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    public JsonNode getCategorySuggestion(String suggestion) {

        User user = userRepository.findByEmail("admin@admin.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return AllegroClient.get()
                .uri(String.format("sale/matching-categories?name=%s", suggestion))
                .accept(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken(user))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    public JsonNode allegroProductSearch(String suggestion, String categoryID) {

        String url = String.format("/sale/products?phrase=%s&language=pl-PL&category.id=%s", suggestion, categoryID);

        User user = userRepository.findByEmail("admin@admin.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return AllegroClient.get()
                .uri(url)
                .accept(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken(user))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    public JsonNode allegroGTINProductSearch(long GTIN) {

        String url = String.format("/sale/products?phrase=%s&language=pl-PL&mode=GTIN", GTIN);

        User user = userRepository.findByEmail("admin@admin.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return AllegroClient.get()
                .uri(url)
                .accept(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken(user))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    public JsonNode getProduct(String ID) {
        User user = userRepository.findByEmail("admin@admin.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return AllegroClient.get()
                .uri(String.format("sale/products/%s", ID))
                .accept(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken(user))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    public JsonNode getParams (String ID) {
        User user = userRepository.findByEmail("admin@admin.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return AllegroClient.get()
                .uri(String.format("sale/categories/%s/parameters", ID))
                .accept(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getUserToken(user))
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
                return keys.getAllegroAccessToken();
            }
            // Data up to date but not in cache
            innerTokenMap.put("cachedToken", keys.getAllegroAccessToken());
            innerTokenMap.put("expDate", keys.getAllegroTokenExpiration());
            userTokenCache.put(user.getEmail(), innerTokenMap);
            return keys.getAllegroAccessToken();
        }
        // User does not have Allegro credentials set up
        return "User needs to be signed in to the Allegro Api again";
    }

    // Creating Allegro headers
    private HttpHeaders getAllegroHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, generalService.getAuthorizationHeader(envService.getALLEGRO_CLIENT_ID(), envService.getALLEGRO_CLIENT_SECRET()));
        return headers;
    }

    public allegroListingState mapStateToEnum(String state) {
        return switch (state.toUpperCase()) {
            case "ACTIVE" -> allegroListingState.ACTIVE;
            case "INACTIVE" -> allegroListingState.INACTIVE;
            case "ACTIVATING" -> allegroListingState.ACTIVATING;
            case "ENDED" -> allegroListingState.ENDED;
            default -> throw new IllegalArgumentException("Unknown state: " + state);
        };
    }

}
