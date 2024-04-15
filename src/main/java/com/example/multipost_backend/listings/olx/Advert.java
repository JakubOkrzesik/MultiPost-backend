package com.example.multipost_backend.listings.olx;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@AllArgsConstructor
@Data
public class Advert {
    @JsonProperty("title")
    private String title;
    @JsonProperty("description")
    private String description;
    @JsonProperty("category_id")
    private String categoryId;
    @JsonProperty("advertiser_type")
    private String advertiserType;
    private Contact contact;
    private Location location;
    private List<Image> images;
    private Price price;
    private List<Attrib> attributes;
}

