package com.example.multipost_backend.listings.olx;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Builder(builderMethodName = "orRequestBuilder")
public class OlxRefreshRequest {
    private final String grant_type;
    private final String client_id;
    private final String client_secret;
    private final String refresh_token;
}
