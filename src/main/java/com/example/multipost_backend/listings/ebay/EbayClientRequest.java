package com.example.multipost_backend.listings.ebay;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(builderMethodName = "ecRequestBuilder")
public class EbayClientRequest {
    private final String grant_type;
    private final String scope;
}
