package com.example.multipost_backend.listings.services;


import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.ebay.EbayTokenRequest;
import com.example.multipost_backend.listings.ebay.EbayTokenResponse;
import com.example.multipost_backend.listings.olx.OlxClientRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class EbayService {

    private final WebClient EbayClient;
    private final UserRepository userRepository;


    // Getting ebay user token after receiving the user's code
    public EbayTokenResponse getEbayToken(String code, String grant_type) {
        return EbayClient.post()
                .uri("/identity/v1/oauth2/token")
                .headers(h -> h.addAll(getHeaders()))
                .bodyValue(new EbayTokenRequest(grant_type, code, "your redirect uri"))
                .retrieve()
                .bodyToMono(EbayTokenResponse.class)
                .block();
    }

    // Getting the application's tokens
    public EbayTokenResponse getClientToken() {
        return EbayClient.post()
                .uri("/identity/v1/oauth2/token")
                .headers(h -> h.addAll(getHeaders()))
                .bodyValue(new EbayTokenRequest("client_credentials", "v2 read write", "placeholder"))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException("Client error: " + errorBody))))
                .bodyToMono(EbayTokenResponse.class)
                .block();
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        headers.add("Authorization", String.format("Basic %s", String.format("%s:%s",
                System.getenv("EBAY_CLIENT_ID"), System.getenv("EBAY_CLIENT_SECRET"))));
        return headers;
    }
}
