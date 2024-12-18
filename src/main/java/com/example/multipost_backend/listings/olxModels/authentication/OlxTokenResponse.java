package com.example.multipost_backend.listings.olxModels.authentication;

import com.example.multipost_backend.listings.sharedApiModels.GrantCodeResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class OlxTokenResponse extends GrantCodeResponse {
    private String scope;

    public OlxTokenResponse(String access_token, String expires_in, String token_type, String scope, String refresh_token){
        super(access_token, expires_in, token_type,refresh_token);
        this.scope = scope;
    }
}
