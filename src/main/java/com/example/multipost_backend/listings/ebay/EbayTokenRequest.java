package com.example.multipost_backend.listings.ebay;

import com.example.multipost_backend.listings.SharedApiModels.GrantCodeRequest;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class EbayTokenRequest extends GrantCodeRequest {

    private final String redirect_uri;

    @Builder(builderMethodName = "etRequestBuilder")
    public EbayTokenRequest(String grant_type, String code, String redirect_uri) {
        super(grant_type, code);
        this.redirect_uri = redirect_uri;
    }
}
