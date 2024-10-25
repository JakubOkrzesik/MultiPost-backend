package com.example.multipost_backend.listings.olx;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Validation {
    private String type;
    private boolean required;
    private boolean numeric;
    private int min;
    private int max;
    private boolean allow_multiple_values;
}
