package com.example.multipost_backend.listings.listingRequests;


import com.example.multipost_backend.listings.services.EnvService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;

@Configuration
@AllArgsConstructor
public class WebClientConfig {

    private final EnvService envService;

    // Note to self look out for thread safety of instantiated modules!!!!
    @Bean
    public WebClient OlxClient(){
        return WebClient.create("https://www.olx.pl/api");
    }

    @Bean
    public WebClient AllegroClient() {
        return WebClient.builder()
                .baseUrl("https://allegro.pl")
                .defaultHeader("Authorization", getAuthorizationHeader(envService.getALLEGRO_CLIENT_ID(), envService.getALLEGRO_CLIENT_SECRET()))
                .build();
            }

    @Bean
    public WebClient EbayClient() {
        return WebClient.builder()
                .baseUrl("https://api.sandbox.ebay.com")
                .defaultHeader("Authorization", getAuthorizationHeader(envService.getEBAY_CLIENT_ID(), envService.getEBAY_CLIENT_SECRET()))
                .build()
            ;}

    private String getAuthorizationHeader(String client_id, String client_secret) {
        String credentials = client_id + ":" + client_secret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        return "Basic " + encodedCredentials;
    }
}
