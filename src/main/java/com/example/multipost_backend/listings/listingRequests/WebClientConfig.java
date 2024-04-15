package com.example.multipost_backend.listings.listingRequests;


import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Configuration
@AllArgsConstructor
public class WebClientConfig {

    @Bean
    public WebClient OlxClient(){
        return WebClient.builder()
                .baseUrl("https://www.olx.pl/api")
                .filter(ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
                    if (clientResponse.statusCode().is4xxClientError()) {
                        return clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("Client error: " + errorBody)));
                    }
                    return Mono.just(clientResponse);
                }))
                .build();
    }

    @Bean
    public WebClient AllegroClient() { return WebClient.builder()
            .baseUrl("https://allegro.pl")
            .filter(ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
                if (clientResponse.statusCode().is4xxClientError()) {
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(new RuntimeException("Client error: " + errorBody)));
                }
                return Mono.just(clientResponse);
            }))
            .build();
    }

    @Bean
    public WebClient EbayClient() { return WebClient.builder()
            .baseUrl("https://api.sandbox.ebay.com")
            .filter(ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
                if (clientResponse.statusCode().is4xxClientError()) {
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(new RuntimeException("Client error: " + errorBody)));
                }
                return Mono.just(clientResponse);
            }))
            .build();
    }

}
