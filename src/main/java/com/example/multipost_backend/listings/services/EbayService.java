package com.example.multipost_backend.listings.services;


import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.allegro.AllegroTokenResponse;
import com.example.multipost_backend.listings.dbmodels.UserAccessKeys;
import com.example.multipost_backend.listings.ebay.EbayTokenResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service
@AllArgsConstructor
public class EbayService {

    private final WebClient EbayClient;
    private final EnvService envService;
    private final GeneralService generalService;
    private final Map<String, Map<String, Object>> userTokenCache = new ConcurrentHashMap<>();
    private final UserRepository userRepository;


    // Getting ebay user token after receiving the user's code
    public EbayTokenResponse getEbayToken(String code) {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "authorization_code");
        requestBody.add("code", code);
        requestBody.add("redirect_uri", String.format("%s/api/v1/auth/ebay", envService.getREDIRECT_URI()));


        return EbayClient.post()
                .uri("/identity/v1/oauth2/token")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .headers(h -> h.addAll(getEbayAuthHeaders()))
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(EbayTokenResponse.class)
                .block();
    }

    // Getting the application's tokens
    public EbayTokenResponse getClientToken() {

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "client_credentials");
        requestBody.add("scope", "https://api.ebay.com/oauth/api_scope");
        // inventory api scope needed

        return EbayClient.post()
                .uri("/identity/v1/oauth2/token")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .headers(h -> h.addAll(getEbayAuthHeaders()))
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException("Client error: " + errorBody))))
                .bodyToMono(EbayTokenResponse.class)
                .block();
    }

    public JsonNode createAdvert(JsonNode data, User user) {

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, getUserToken(user));
        headers.set("Content-Language", "en-US");

        return EbayClient.post()
                .uri("sell/inventory/v1/offer")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(headers))
                .bodyValue(data)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    // Required data
    // Conditional (required on offer creation) - inventory location, offer price, quantity,
    // ebay listing category, listing policy regarding payment return and fulfillment values
    // Required - format(FIXED_PRICE), marketplaceId, SKU value (item value from inventory)

    // This method checks if the user has set up the fulfillment, payment and return policies for their account
/*
    public String ebayPolicyCheck(String userToken) {
        if (getPaymentPolicy(userToken).get("total").asInt()==0) {

        }
        if (getReturnPolicy(userToken).get("total").asInt()==0){

        }
        if (getFulfillmentPolicy(userToken).get("total").asInt()==0) {

        }
    }

    public ObjectNode getFulfillmentPolicy(String userToken) {
        return EbayClient.get()
                .uri("/sell/account/v1/fulfillment_policy?marketplace_id=EBAY_PL")
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getEbayUserHeaders(userToken)))
                .retrieve()
                .bodyToMono(ObjectNode.class)
                .block();
    }

    private ObjectNode createFulfillmentPolicy(String userToken) {

    }

    public ObjectNode getPaymentPolicy(String userToken) {
        return EbayClient.get()
                .uri("/sell/account/v1/payment_policy?marketplace_id=EBAY_PL")
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getEbayUserHeaders(userToken)))
                .retrieve()
                .bodyToMono(ObjectNode.class)
                .block();
    }

    private ObjectNode createPaymentPolicy(String userToken) {

    }

    public ObjectNode getReturnPolicy(String userToken) {
        return EbayClient.get()
                .uri("/sell/account/v1/return_policy?marketplace_id=EBAY_PL")
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(getEbayUserHeaders(userToken)))
                .retrieve()
                .bodyToMono(ObjectNode.class)
                .block();
    }

    private ObjectNode createReturnPolicy(String userToken) {

    }
*/


    public String getUserToken(User user) {
        // Check if user is in cache and if the token is expired
        Map<String, Object> tokenData = userTokenCache.get(user.getEmail());
        if (tokenData != null && generalService.isTokenExpired((Date) tokenData.get("expDate"))) {
            return (String) tokenData.get("cachedToken");
        }

        Map<String, Object> innerTokenMap = new ConcurrentHashMap<>();

        UserAccessKeys keys = user.getKeys();
        if (keys.getEbayAccessToken() != null) {
            if (generalService.isTokenExpired(keys.getEbayTokenExpiration())) {
                JsonNode response = updateUserToken(keys.getEbayRefreshToken());
                // Updating the database
                // The refresh token response does not contain a new refresh token so the db does not update this field
                keys.setEbayAccessToken(response.get("access_token").asText());
                keys.setEbayTokenExpiration(generalService.calculateExpiration(response.get("expires_in").asText()));
                // Updating the cache

                innerTokenMap.put("cachedToken", response.get("access_token").asText());
                innerTokenMap.put("expDate", generalService.calculateExpiration(response.get("expires_in").asText()));

                userTokenCache.put(user.getEmail(), innerTokenMap);
                userRepository.save(user);
                return keys.getEbayAccessToken();
            }
            // Data up to date but not in cache
            innerTokenMap.put("cachedToken", keys.getEbayAccessToken());
            innerTokenMap.put("expDate", keys.getEbayTokenExpiration());
            userTokenCache.put(user.getEmail(), innerTokenMap);
            return keys.getEbayAccessToken();
        }
        // User does not have Ebay credentials set up
        return "User needs to be signed in to the Allegro Api again";
    }

    private JsonNode updateUserToken(String ebayRefreshToken) {

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "refresh_token");
        requestBody.add("refresh_token", ebayRefreshToken);

        return EbayClient.post()
                .uri("/identity/v1/oauth2/token")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .headers(h -> h.addAll(getEbayAuthHeaders()))
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    private HttpHeaders getEbayAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, generalService.getAuthorizationHeader(envService.getEBAY_CLIENT_ID(), envService.getEBAY_CLIENT_SECRET()));
        return headers;
    }

    private HttpHeaders getEbayUserHeaders(String userToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept-Language", "en-US");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + userToken);

        return headers;
    }
}
