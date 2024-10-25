package com.example.multipost_backend.listings.allegro;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AllegroProduct {
    private String id;
    private String name;
    private Category category;
    private ArrayNode parameters;
}
