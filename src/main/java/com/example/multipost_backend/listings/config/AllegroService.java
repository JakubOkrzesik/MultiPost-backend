package com.example.multipost_backend.listings.config;


import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.allegro.AllegroTokenRequest;
import com.example.multipost_backend.listings.allegro.AllegroTokenResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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
}
