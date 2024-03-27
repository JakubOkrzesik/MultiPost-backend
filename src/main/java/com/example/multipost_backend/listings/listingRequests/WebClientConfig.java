package com.example.multipost_backend.listings.listingRequests;


import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
@AllArgsConstructor
public class WebClientConfig {

    @Bean
    public WebClient OlxClient(){
        return WebClient.create("https://www.olx.pl/api");
    }

    @Bean
    public WebClient AllegroClient() { return WebClient.create("https://allegro.pl"); }

    @Bean
    public WebClient EbayClient() { return WebClient.create("https://api.sandbox.ebay.com") ;}

}
