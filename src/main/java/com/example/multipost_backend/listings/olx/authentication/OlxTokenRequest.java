package com.example.multipost_backend.listings.olx.authentication;

import lombok.Builder;
import lombok.Data;

@Builder(builderMethodName = "otRequestBuilder")
@Data
public class OlxTokenRequest {
    private final String grant_type;
    private final String client_id;
    private final String client_secret;
    private final String code;
    private final String scope;
}
