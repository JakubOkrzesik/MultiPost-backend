package com.example.multipost_backend.listings.olxModels.advertClasses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SimplifiedOlxAdvert {
    private int id;
    private String status;
    private String url;
}
