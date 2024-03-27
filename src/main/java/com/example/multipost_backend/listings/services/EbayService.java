package com.example.multipost_backend.listings.services;


import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.ebay.EbayTokenRequest;
import com.example.multipost_backend.listings.ebay.EbayTokenResponse;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service
@AllArgsConstructor
public class EbayService {

    private final WebClient EbayClient;
    private final EnvService envService;
    private final GeneralService generalService;
    private final Map<String, Map<String, Object>> userTokenCache = new ConcurrentHashMap<>();


    // Getting ebay user token after receiving the user's code
    public EbayTokenResponse getUserToken(String code) {
        return EbayClient.post()
                .uri("/identity/v1/oauth2/token")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HttpHeaders.AUTHORIZATION, generalService.getAuthorizationHeader(envService.getEBAY_CLIENT_ID(), envService.getEBAY_CLIENT_SECRET()))
                .bodyValue(EbayTokenRequest.etRequestBuilder()
                        .grant_type("authorization_code")
                        .code(code)
                        .redirect_uri("https://1398-77-255-216-45.ngrok-free.app/api/v1/auth/ebay"))
                .retrieve()
                .bodyToMono(EbayTokenResponse.class)
                .block();
    }

    public String getUserToken(User user) {
        return "token";
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
                .header(HttpHeaders.AUTHORIZATION, generalService.getAuthorizationHeader(envService.getEBAY_CLIENT_ID(), envService.getEBAY_CLIENT_SECRET()))
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
                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException("Client error: " + errorBody))))
                .bodyToMono(JsonNode.class)
                .block();
    }

    // Required data
    // Conditional (required on offer creation) - inventory location, offer price, quantity,
    // ebay listing category, listing policy regarding payment return and fulfillment values
    // Required - format(FIXED_PRICE), marketplaceId, SKU value (item value from inventory)
    private String createPolicies(User user) {
        return "Policy";
    }

    private String createFulfillmentPolicy() {
        return "FulfillmentPolicy";
    }

    private String createPaymentPolicy() {
        return "PaymentPolicy";
    }

    private String createReturnPolicy() {
        return "ReturnPolicy";
    }
}
