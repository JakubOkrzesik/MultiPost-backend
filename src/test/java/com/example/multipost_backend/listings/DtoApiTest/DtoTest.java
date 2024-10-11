package com.example.multipost_backend.listings.DtoApiTest;

import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.DtoApiTest.TestClasses.AllegroProduct;
import com.example.multipost_backend.listings.DtoApiTest.TestClasses.Category;
import com.example.multipost_backend.listings.DtoApiTest.TestClasses.CategoryResponse;
import com.example.multipost_backend.listings.DtoApiTest.TestClasses.ProductWrapper;
import com.example.multipost_backend.listings.services.AllegroService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@SpringBootTest
public class DtoTest {

    final int size = 16 * 1024 * 1024;
    final ExchangeStrategies strategies = ExchangeStrategies.builder()
            .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size))
            .build();

    User user;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AllegroService allegroService;
    private final WebClient TestAllegroClient = WebClient.builder()
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

    @BeforeEach
    void setUser() {
        this.user = userRepository.findByEmail("admin@admin.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }


    // Need for a wrapper class is justified because of the array being behind the matchingCategories key not standalone
    @Test
    public void CustomObjectMappingTest() {

        CategoryResponse list = TestAllegroClient.get()
                .uri(String.format("sale/matching-categories?name=%s", "Telefon Iphone 12"))
                .accept(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + allegroService.getUserToken(user))
                .retrieve()
                .bodyToMono(CategoryResponse.class)
                .block();

        assert list != null;
        System.out.println(list);
    }

    @Test
    public void CustomObjectGTINProductRetrieval() {
        // The new approach will use AllegroProduct instead of JsonNode
        String url = String.format("/sale/products?phrase=%s&language=pl-PL&mode=GTIN", 888462600712L);

        String token = allegroService.getUserToken(user);

        ProductWrapper response = TestAllegroClient.get()
                .uri(url)
                .accept(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(ProductWrapper.class)
                .block();

        System.out.println(response);
    }


    @Test
    public void CustomObjectProductRetrieval() {
        String product_id = "065bb735-4257-44d4-93f6-4f7decc71150";


    }


}
