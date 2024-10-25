package com.example.multipost_backend.listings.olx;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Category {
    private String id;
}
