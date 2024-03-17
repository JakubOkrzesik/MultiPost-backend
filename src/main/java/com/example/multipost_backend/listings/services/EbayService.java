package com.example.multipost_backend.listings.services;


import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.ebay.EbayTokenRequest;
import com.example.multipost_backend.listings.ebay.EbayTokenResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Service
@AllArgsConstructor
public class EbayService {

    private final WebClient EbayClient;


    // Getting ebay user token after receiving the user's code
    public EbayTokenResponse getUserToken(String code) {
        return EbayClient.post()
                .uri("/identity/v1/oauth2/token")
                .bodyValue(EbayTokenRequest.etRequestBuilder()
                        .grant_type("authorization_code")
                        .code(code)
                        .redirect_uri("your redirect uri"))
                .retrieve()
                .bodyToMono(EbayTokenResponse.class)
                .block();
    }

    // Getting the application's tokens
    public EbayTokenResponse getClientToken() {

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "client_credentials");
        requestBody.add("scope", "https://api.ebay.com/oauth/api_scope");

        return EbayClient.post()
                .uri("/identity/v1/oauth2/token")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException("Client error: " + errorBody))))
                .bodyToMono(EbayTokenResponse.class)
                .block();
    }


}
