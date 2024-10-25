package com.example.multipost_backend.listings.olx.advertClasses;

import com.example.multipost_backend.listings.olx.Attribute;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OlxAdvert {
    private String title;
    private String description;
    private int category_id;
    private String advertiser_type;
    private int external_id;
    private String external_url;
    private Contact contact;
    private Location location;
    private List<Image> images;
    private Price price;
    private List<Attribute> attributes;
    private boolean courier;
}
