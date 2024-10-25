package com.example.multipost_backend.listings.DtoApiTest.AllegroDto;

import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.allegro.AllegroProduct;
import com.example.multipost_backend.listings.allegro.CategoryResponse;
import com.example.multipost_backend.listings.allegro.ProductWrapper;
import com.example.multipost_backend.listings.services.AllegroService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@SpringBootTest
public class AllegroDtoTest {

    final int size = 16 * 1024 * 1024;
    final ExchangeStrategies strategies = ExchangeStrategies.builder()
            .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size))
            .build();

    User user;
    String token;

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
        user = userRepository.findByEmail("admin@admin.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        token = allegroService.getUserToken(user);
    }


    // Need for a wrapper class is justified because of the array being behind the matchingCategories key not standalone
    @Test
    public void getCategorySuggestionDTOTest() {

        CategoryResponse list = TestAllegroClient.get()
                .uri(String.format("sale/matching-categories?name=%s", "Telefon Iphone 12"))
                .accept(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(CategoryResponse.class)
                .block();

        assert list != null;
        System.out.println(list);
    }

    @Test
    public void allegroGTINProductSearchDTOTest() {
        // The new approach will use AllegroProduct instead of JsonNode
        String url = String.format("/sale/products?phrase=%s&language=pl-PL&mode=GTIN", 888462600712L);

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
    public void getProductDTOTest() {
        String product_id = "065bb735-4257-44d4-93f6-4f7decc71150";

        AllegroProduct response = TestAllegroClient.get()
                .uri(String.format("sale/products/%s", product_id))
                .accept(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(AllegroProduct.class)
                .block();

        System.out.println(response);
    }

    @Test
    public void allegroProductSearchDTOTest() {
        String product_name = "Iphone 12 telefon";
        String cat_num = "165";
        String url = String.format("/sale/products?phrase=%s&language=pl-PL&category.id=%s", product_name, cat_num);

        ProductWrapper response = TestAllegroClient.get()
                .uri(url)
                .accept(MediaType.valueOf("application/vnd.allegro.public.v1+json"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(ProductWrapper.class)
                .block();

        System.out.println(response);
    }

}
