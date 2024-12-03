package com.example.multipost_backend.listings.configs;


import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Configuration
@AllArgsConstructor
public class WebClientConfig {

    final int size = 16 * 1024 * 1024;
    final ExchangeStrategies strategies = ExchangeStrategies.builder()
            .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size))
            .build();

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
                .exchangeStrategies(strategies)
                .build();
    }

    @Bean
    public WebClient AllegroClient() { return WebClient.builder()
            .baseUrl("https://api.allegro.pl.allegrosandbox.pl")
            .filter(ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
                if (clientResponse.statusCode().is4xxClientError()) {
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(new RuntimeException("Client error: " + errorBody)));
                }
                return Mono.just(clientResponse);
            }))
            .exchangeStrategies(strategies)
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
            .exchangeStrategies(strategies)
            .build();
    }

}
