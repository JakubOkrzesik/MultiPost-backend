package com.example.multipost_backend.listings.olx;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Location {
    private int city_id;
    private int district_id;
    private Float lat;
    private Float lon;
}
