package com.example.multipost_backend.listings.config;


import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.ebay.EbayTokenRequest;
import com.example.multipost_backend.listings.ebay.EbayTokenResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@AllArgsConstructor
public class EbayService {

    private final WebClient EbayClient;
    private final UserRepository userRepository;

    public EbayTokenResponse getEbayToken(String code) {

        String credentials = String.format("%s:%s", System.getenv("EBAY_CLIENT_ID"), System.getenv("EBAY_CLIENT_SECRET"));

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        headers.add("Authorization", String.format("Basic %s", credentials));

        return EbayClient.post()
                .uri("/identity/v1/oauth2/token")
                .headers(h -> h.addAll(headers))
                .bodyValue(new EbayTokenRequest("authorization_code", code, "your redirect uri"))
                .retrieve()
                .bodyToMono(EbayTokenResponse.class)
                .block();
    }
}
