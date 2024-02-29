package com.example.multipost_backend.listings.config;


import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.dbmodels.UserAccessKeys;
import com.example.multipost_backend.listings.olx.Advert;
import com.example.multipost_backend.listings.olx.GrantCodeResponse;
import com.example.multipost_backend.listings.olx.GrantCodeRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Consumer;

@Service
@AllArgsConstructor
public class OlxService {

    private final WebClient client;
    private final UserRepository userRepository;


    public String advertHandler(Advert advert, String email) {
        return client.post()
                .uri("/partner/adverts")
                .headers(getUserHeaders(email))
                .retrieve()
                .toString();
    }

    public String advertHandler(String email) {
        return client.get()
                .uri("/partner/adverts")
                .headers(getUserHeaders(email))
                .retrieve()
                .toString();
    }

    private String getLocation(String lon, String lat, String email) {
        return client.get()
                .uri("/partner/location")
                .headers(getUserHeaders(email))
                .retrieve()
                .toString();
    }

    private Consumer<HttpHeaders> getUserHeaders(String email){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", String.format("Bearer %s", getUserToken(email)));
        headers.add("Version", "2.0");
        return (HttpHeaders httpHeaders) -> httpHeaders.addAll(headers);
    }

    private String matchCategory(String email) {
        return client.get()
                .uri("/categories/suggestion")
                .headers(getUserHeaders(email))
                .retrieve()
                .toString();
    }

    public GrantCodeResponse getGrantAuthcode(String code){
        return client.post()
                .uri("/open/oauth/token")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new GrantCodeRequest("authorization_code", System.getenv("CLIENT_ID"),
                        System.getenv("CLIENT_SECRET"), code, "v2 read write"))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException("Client error: " + errorBody))))
                .bodyToMono(GrantCodeResponse.class)
                .block();
    }

    public String getUserToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        UserAccessKeys keys = user.getKeys();
        if (keys.getOlxAccessToken() != null) {
            if (keys.getOlxTokenExpiration().before(new Date(System.currentTimeMillis()))) {
                GrantCodeResponse response = updateUserToken(keys.getOlxRefreshToken());
                keys.setOlxAccessToken(response.getAccess_token());
                keys.setOlxTokenExpiration(new Date(System.currentTimeMillis() + Integer.parseInt(response.getExpires_in())));
                userRepository.save(user);
            }
            return keys.getOlxAccessToken();
        }
        return "User needs to be signed in to the OLX Api again";
        }


    private GrantCodeResponse updateUserToken(String refreshToken){
        return client.get()
                .uri("/open/oauth/token").accept(MediaType.APPLICATION_JSON)
                .headers((HttpHeaders httpHeaders) -> {
                    httpHeaders.add("grant_type", "refresh_token");
                    httpHeaders.add("client_id", System.getenv("CLIENT_ID"));
                    httpHeaders.add("client_secret", System.getenv("CLIENT_SECRET"));
                    httpHeaders.add("refresh_token", refreshToken);
                })
                .retrieve()
                .bodyToMono(GrantCodeResponse.class)
                .block();
    }

}
