package com.example.multipost_backend.listings.ebay;

import com.example.multipost_backend.listings.SharedApiModels.GrantCodeRequest;
import lombok.Data;

@Data
public class EbayTokenRequest extends GrantCodeRequest {

    private String redirect_uri;

    public EbayTokenRequest(String grant_type, String code, String redirect_uri) {
        super(grant_type, code);
        this.redirect_uri = redirect_uri;
    }
}