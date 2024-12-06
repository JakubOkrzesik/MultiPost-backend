package com.example.multipost_backend.listings.DtoApiTest.AllegroDto;

import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.allegroModels.AllegroApiException;
import com.example.multipost_backend.listings.allegroModels.AllegroProduct;
import com.example.multipost_backend.listings.allegroModels.CategoryResponse;
import com.example.multipost_backend.listings.allegroModels.ProductWrapper;
import com.example.multipost_backend.listings.services.AllegroService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.client.RestClient;

@SpringBootTest
public class AllegroDtoTest {

    User user;
    String token;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AllegroService allegroService;

    private final RestClient TestAllegroClient = RestClient.builder()
            .baseUrl("https://api.allegro.pl.allegrosandbox.pl")
            .defaultStatusHandler(httpStatusCode -> httpStatusCode.is4xxClientError() || httpStatusCode.is5xxServerError(),
                    (request, response) -> {throw new AllegroApiException(response.getStatusCode(), response.getBody());})
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
                .body(CategoryResponse.class)
                ;

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
                .body(ProductWrapper.class)
                ;

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
                .body(AllegroProduct.class)
                ;

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
                .body(ProductWrapper.class)
                ;

        System.out.println(response);
    }

}
