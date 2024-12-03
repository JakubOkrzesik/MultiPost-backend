package com.example.multipost_backend.listings.DtoApiTest.OlxDto;

import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.olxModels.*;
import com.example.multipost_backend.listings.olxModels.Category;
import com.example.multipost_backend.listings.olxModels.CategoryAttribs;
import com.example.multipost_backend.listings.olxModels.advertClasses.SimplifiedOlxAdvert;
import com.example.multipost_backend.listings.services.OlxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class OlxDtoTest {
    User user;
    MultiValueMap<String, String> headers;

    final int size = 16 * 1024 * 1024;
    final ExchangeStrategies strategies = ExchangeStrategies.builder()
            .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size))
            .build();

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OlxService olxService;
    
    private final WebClient TestOlxClient = WebClient.builder()
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
    
    @BeforeEach
    void beforeTest() {
        user = userRepository.findByEmail("admin@admin.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        headers = olxService.getUserHeaders(user);
    }
    
    @Test
    void getAdvertDTOTest() {

        user = userRepository.findByEmail("test@user.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        headers = olxService.getUserHeaders(user);
        
        String id = "927859243";

        OlxObjectWrapperClass<SimplifiedOlxAdvert> response = TestOlxClient.get()
                .uri(String.format("/partner/adverts/%s", id))
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(headers))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<OlxObjectWrapperClass<SimplifiedOlxAdvert>>() {
                })
                .block();

        assert response != null;
        System.out.println(response.getData());
    }

    @Test
    void getAdvertsDTOTest() {
        user = userRepository.findByEmail("test@user.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        headers = olxService.getUserHeaders(user);

        OlxListWrapperClass<SimplifiedOlxAdvert> response = TestOlxClient.get()
                .uri("/partner/adverts")
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(headers))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<OlxListWrapperClass<SimplifiedOlxAdvert>>() {
                })
                .block();
        
        System.out.println(response);
    }

    @Test
    void getCategorySuggestionDTOTest() {

        String title = "Audi A3";

        OlxListWrapperClass<Category> response = TestOlxClient.get()
                .uri(String.format("/partner/categories/suggestion?q=%s", title))
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(headers))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<OlxListWrapperClass<Category>>() {
                })
                .block();

        assert response != null;
        System.out.println(response.getData().get(0).getId());
    }

    @Test
    void DTOGetCategoryAttributes() {

        String id = "182";

        String uri = String.format("/partner/categories/%s/attributes", id);

        OlxListWrapperClass<CategoryAttribs> result = TestOlxClient.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> h.addAll(headers))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<OlxListWrapperClass<CategoryAttribs>>() {
                })
                .block();

        List<CategoryAttribs> attribsList = new ArrayList<>();

        assert result != null;
        for (CategoryAttribs attribs: result.getData()) {
            if (attribs.getValidation().isRequired()) {
                attribsList.add(attribs);
            }
        }

        System.out.println(attribsList);
    }

}
