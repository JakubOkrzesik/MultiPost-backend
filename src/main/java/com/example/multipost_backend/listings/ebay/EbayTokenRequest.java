package com.example.multipost_backend.listings.ebay;

import lombok.Builder;
import lombok.Data;


@Builder(builderMethodName = "etRequestBuilder")
@Data
public class EbayTokenRequest{
    private final String grant_type;
    private final String code;
    private final String redirect_uri;

}
