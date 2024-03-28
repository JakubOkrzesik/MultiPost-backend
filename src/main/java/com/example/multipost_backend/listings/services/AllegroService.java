package com.example.multipost_backend.listings.services;


import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.allegro.AllegroTokenRequest;
import com.example.multipost_backend.listings.allegro.AllegroTokenResponse;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@AllArgsConstructor
public class AllegroService {

    private final WebClient AllegroClient;
    private final GeneralService generalService;
    private final EnvService envService;
    private final Map<String, Map<String, Object>> userTokenCache = new ConcurrentHashMap<>();

    public AllegroTokenResponse getAllegroToken(String code) {
        return AllegroClient.post() // The allegro api does not work with the .bodyValue method inside webclient or im doing sth wrong (probably the case)
                .uri(String.format("auth/oauth/token?grant_type=authorization_code&code=%s&redirect_uri=%s/api/v1/auth/allegro", code, envService.getREDIRECT_URI()))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, generalService.getAuthorizationHeader(envService.getALLEGRO_CLIENT_ID(), envService.getALLEGRO_CLIENT_SECRET()))
                /*.bodyValue(AllegroTokenRequest.atRequestBuilder()
                        .grant_type("authorization_code")
                        .code(code)
                        .redirect_uri("https://ebac-77-255-216-45.ngrok-free.app/api/v1/auth/allegro") // provide your application's allegro redirect uri
                        .build())*/
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException("Client error: " + errorBody))))
                .bodyToMono(AllegroTokenResponse.class)
                .block();
    }

    public AllegroTokenResponse getClientToken() {
        return AllegroClient.post()
                .uri("/auth/oauth/token?grant_type=client_credentials")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, generalService.getAuthorizationHeader(envService.getALLEGRO_CLIENT_ID(), envService.getALLEGRO_CLIENT_SECRET()))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException("Client error: " + errorBody))))
                .bodyToMono(AllegroTokenResponse.class)
                .block();
    }

    public JsonNode createAdvert(JsonNode data, User user) {
        return AllegroClient.mutate().baseUrl("https://api.allegro.pl").build()
                .post()
                .uri("/sale/product-offers")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getUserToken())
                .bodyValue(data)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException("Client error: " + errorBody))))
                .bodyToMono(JsonNode.class)
                .block();
    }

    private String getUserToken() {
        return null;
    }

}
