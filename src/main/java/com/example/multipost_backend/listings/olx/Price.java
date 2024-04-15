package com.example.multipost_backend.listings.olx;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Price {
    private int value;
    private final String currency = "PLN";
    private boolean negotiable;
}
