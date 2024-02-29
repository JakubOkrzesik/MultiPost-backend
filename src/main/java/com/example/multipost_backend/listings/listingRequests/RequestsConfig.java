package com.example.multipost_backend.listings.listingRequests;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class RequestsConfig {
    @Bean
    public WebClient client(){
        return WebClient.create("https://www.olx.pl/api");
    }
}
