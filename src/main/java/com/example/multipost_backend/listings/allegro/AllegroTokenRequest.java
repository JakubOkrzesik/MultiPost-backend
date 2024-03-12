package com.example.multipost_backend.listings.allegro;

import com.example.multipost_backend.listings.SharedApiModels.GrantCodeRequest;
import lombok.Data;

@Data
public class AllegroTokenRequest extends GrantCodeRequest {

    private String redirect_uri;

    public AllegroTokenRequest(String grant_type, String code, String redirectUri) {
        super(grant_type, code);
        this.redirect_uri = redirectUri;
    }
}