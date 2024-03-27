package com.example.multipost_backend.listings.allegro;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(builderMethodName = "atRequestBuilder")
public class AllegroTokenRequest{
    private final String redirect_uri;
    private final String grant_type;
    private final String code;
}
