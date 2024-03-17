package com.example.multipost_backend.listings.allegro;

import com.example.multipost_backend.listings.SharedApiModels.GrantCodeRequest;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AllegroTokenRequest extends GrantCodeRequest {

    private String redirect_uri;

    @Builder(builderMethodName = "atRequestBuilder")
    public AllegroTokenRequest(String grant_type, String code, String redirectUri) {
        super(grant_type, code);
        this.redirect_uri = redirectUri;
    }
}
