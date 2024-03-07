package com.example.multipost_backend.listings.olx;

import com.example.multipost_backend.listings.SharedApiModels.GrantCodeRequest;
import lombok.Data;

@Data
public class OlxTokenRequest extends GrantCodeRequest {
    private String client_id;
    private String client_secret;
    private String scope;

    public OlxTokenRequest(String olxClientId, String olxClientSecret, String code, String grant_type, String scope) {
        super(code,grant_type);
        this.client_id = olxClientId;
        this.client_secret = olxClientSecret;
        this.scope = scope;
    }
}
