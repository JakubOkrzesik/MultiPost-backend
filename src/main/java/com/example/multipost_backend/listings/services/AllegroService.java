package com.example.multipost_backend.listings.services;


import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.allegro.AllegroTokenRequest;
import com.example.multipost_backend.listings.allegro.AllegroTokenResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class AllegroService {

    private final WebClient AllegroClient;

    public AllegroTokenResponse getAllegroToken(String code) {
        return AllegroClient.post()
                .uri("auth/oauth/token")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                // Allegro requires client id and client secret to be separated by ":"
                .bodyValue(AllegroTokenRequest.atRequestBuilder()
                        .grant_type("authorization_code")
                        .code(code)
                        .redirectUri("redirect_uri")
                        .build()) // provide your application's allegro redirect uri
                .retrieve()
                .bodyToMono(AllegroTokenResponse.class)
                .block();
    }

    public AllegroTokenResponse getClientToken() {
        return AllegroClient.post()
                .uri("/auth/oauth/token?grant_type=client_credentials")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException("Client error: " + errorBody))))
                .bodyToMono(AllegroTokenResponse.class)
                .block();
    }

}
