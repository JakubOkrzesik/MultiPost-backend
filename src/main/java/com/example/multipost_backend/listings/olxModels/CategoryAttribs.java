package com.example.multipost_backend.listings.olxModels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryAttribs {
    private String code;
    private String label;
    private String unit;
    private Validation validation;
    private List<Value> values;
}
