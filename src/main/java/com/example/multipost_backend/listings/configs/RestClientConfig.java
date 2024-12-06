package com.example.multipost_backend.listings.configs;


import com.example.multipost_backend.listings.allegroModels.AllegroApiException;
import com.example.multipost_backend.listings.olxModels.OlxApiException;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;


@Configuration
@AllArgsConstructor
public class RestClientConfig {

    @Bean
    public RestClient OlxClient(){

        return RestClient.builder()
                .baseUrl("https://www.olx.pl/api")
                .defaultStatusHandler(httpStatusCode -> httpStatusCode.is4xxClientError() || httpStatusCode.is5xxServerError(),
                        (request, response) -> {throw new OlxApiException(response.getStatusCode(), response.getBody());})
                .build();
    }


    // "https://api.allegro.pl.allegrosandbox.pl"
    @Bean
    public RestClient AllegroClient() { return RestClient.builder()
            .baseUrl("https://api.allegro.pl.allegrosandbox.pl")
            .defaultStatusHandler(httpStatusCode -> httpStatusCode.is4xxClientError() || httpStatusCode.is5xxServerError(),
                    (request, response) -> {throw new AllegroApiException(response.getStatusCode(), response.getBody());})
            .build();
    }

    /*@Bean
    public RestClient EbayClient() { return WebClient.builder()
            .baseUrl("https://api.sandbox.ebay.com")
            .filter(ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
                if (clientResponse.statusCode().is4xxClientError()) {
                    return clientResponse.body(String.class)
                            .flatMap(errorBody -> Mono.error(new RuntimeException("Client error: " + errorBody)));
                }
                return Mono.just(clientResponse);
            }))
            .exchangeStrategies(strategies)
            .build();
    }*/

}
