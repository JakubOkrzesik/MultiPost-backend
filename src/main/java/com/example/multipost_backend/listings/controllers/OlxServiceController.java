package com.example.multipost_backend.listings.controllers;

import com.example.multipost_backend.listings.olxModels.CategoryAttribs;
import com.example.multipost_backend.listings.olxModels.advertClasses.Location;
import com.example.multipost_backend.listings.services.OlxService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/olx")
@RequiredArgsConstructor
public class OlxServiceController {
    private final OlxService olxService;

    @PostMapping(value = "/attribs", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CategoryAttribs>> getCategoryAttributes(@RequestParam("categoryID") String categoryID) {

        List<CategoryAttribs> catAttribs = olxService.getCategoryAttributes(categoryID);
        return ResponseEntity.ok(catAttribs);
    }

    @GetMapping(value = "/category", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getCategorySuggestion(@RequestParam("title") String title) throws JsonProcessingException {
        return ResponseEntity.ok(olxService.getCategorySuggestion(title));
    }

    @PostMapping(value = "/location", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Location> getLocationID(@RequestParam("lat") String lat, @RequestParam("lng") String lng) throws JsonProcessingException {
        return ResponseEntity.ok(olxService.getLocation(lat, lng));
    }


}