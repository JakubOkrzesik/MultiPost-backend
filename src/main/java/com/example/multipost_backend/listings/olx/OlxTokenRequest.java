package com.example.multipost_backend.listings.olx;

import com.example.multipost_backend.listings.SharedApiModels.GrantCodeRequest;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class OlxTokenRequest extends GrantCodeRequest {
    private final String client_id;
    private final String client_secret;
    private final String scope;

    @Builder(buildMethodName = "otRequestBuilder")
    public OlxTokenRequest(String grant_type, String olxClientId, String olxClientSecret, String code,  String scope) {
        super(grant_type, code);
        this.client_id = olxClientId;
        this.client_secret = olxClientSecret;
        this.scope = scope;
    }
}
