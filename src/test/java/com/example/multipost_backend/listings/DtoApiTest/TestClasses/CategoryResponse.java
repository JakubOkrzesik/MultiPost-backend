package com.example.multipost_backend.listings.DtoApiTest.TestClasses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryResponse {
    private List<Category> matchingCategories;
}
