package com.example.multipost_backend.listings.olx;

import com.example.multipost_backend.listings.SharedApiModels.GrantCodeResponse;
import lombok.Data;

@Data
public class OlxTokenResponse extends GrantCodeResponse {
    private String scope;

    public OlxTokenResponse(String access_token, String expires_in, String token_type, String scope, String refresh_token){
        super(access_token, expires_in, token_type,refresh_token);
        this.scope = scope;
    }
}
