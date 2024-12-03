package com.example.multipost_backend.listings.olxModels.advertClasses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Price {
    private int value;
    private final String currency = "PLN";
    private boolean negotiable;
    private boolean budget;
    private boolean trade;
}
