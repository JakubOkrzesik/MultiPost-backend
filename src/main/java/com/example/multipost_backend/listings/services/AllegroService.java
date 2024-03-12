package com.example.multipost_backend.listings.services;


import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.allegro.AllegroTokenRequest;
import com.example.multipost_backend.listings.allegro.AllegroTokenResponse;
import com.example.multipost_backend.listings.olx.OlxClientRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Base64;

@Service
@AllArgsConstructor
public class AllegroService {

    private final WebClient AllegroClient;
    private final UserRepository userRepository;


    public AllegroTokenResponse getAllegroToken(String code) {
        return AllegroClient.post()
                .uri("auth/oauth/token")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> h.add("Authorization", Base64.getEncoder().encodeToString
                        (String.format("%s:%s", System.getenv("ALLEGRO_CLIENT_ID"), System.getenv("ALLEGRO_CLIENT_SECRET"))
                                .getBytes()))) // Allegro requires client id and client secret to be separated by : and be base64 encoded
                .bodyValue(new AllegroTokenRequest("authorization_code", code, "redirect_uri")) // provide your application's allegro redirect uri
                .retrieve()
                .bodyToMono(AllegroTokenResponse.class)
                .block();
    }

    public AllegroTokenResponse getClientToken() {
        return AllegroClient.post()
                .uri("/api/open/oauth/token")
                .bodyValue(new OlxClientRequest("client_credentials", System.getenv("OLX_CLIENT_ID"),
                        System.getenv("OLX_CLIENT_SECRET"), "v2 read write"))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException("Client error: " + errorBody))))
                .bodyToMono(AllegroTokenResponse.class)
                .block();
    }
}
