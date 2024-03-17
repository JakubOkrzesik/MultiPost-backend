package com.example.multipost_backend.listings.ebay;


import com.example.multipost_backend.listings.SharedApiModels.GrantCodeResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
@Data
public class EbayTokenResponse extends GrantCodeResponse {

    private String refresh_token_expires_in;

    public EbayTokenResponse(String access_token, String expires_in, String token_type, String refresh_token, String refresh_token_expires_in) {
        super(access_token, expires_in, token_type, refresh_token);
        this.refresh_token_expires_in = refresh_token_expires_in;
    }
}
