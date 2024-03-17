package com.example.multipost_backend.listings.olx;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(builderMethodName = "ocRequestBuilder")
public class OlxClientRequest {
    private final String grant_type;
    private final String client_id;
    private final String client_secret;
    private final String scope;
}
