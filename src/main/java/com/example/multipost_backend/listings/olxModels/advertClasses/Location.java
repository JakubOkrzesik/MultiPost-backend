package com.example.multipost_backend.listings.olxModels.advertClasses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Location {
    private String city_id;
    private String district_id;
    private String latitude;
    private String longitude;
}
