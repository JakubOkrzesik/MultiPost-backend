package com.example.multipost_backend.listings.allegro;

import com.example.multipost_backend.listings.SharedApiModels.GrantCodeResponse;
import lombok.Data;

@Data
public class AllegroTokenResponse extends GrantCodeResponse {
    private String scope;
    private boolean allegro_api;
    private String jti;

    public AllegroTokenResponse(String access_token, String expires_in, String token_type, String scope, String refresh_token, Boolean allegro_api, String jti){
        super(access_token, expires_in, token_type,refresh_token);
        this.scope = scope;
        this.allegro_api = allegro_api;
        this.jti = jti;
    }
}
