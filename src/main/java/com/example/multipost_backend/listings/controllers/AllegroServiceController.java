package com.example.multipost_backend.listings.controllers;

import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.listings.services.AllegroService;
import com.example.multipost_backend.listings.services.GeneralService;
import com.example.multipost_backend.listings.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/allegro")
@RequiredArgsConstructor
public class AllegroServiceController {

    private final AllegroService allegroService;

    @GetMapping(value = "/category/suggestion", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getCategorySuggestion(@RequestParam("phrase") String phrase) {
        return ResponseEntity.ok(allegroService.getCategorySuggestion(phrase));
    }

    @GetMapping(value = "/product/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getProductDetails(@PathVariable("id") String id) {
        return ResponseEntity.ok(allegroService.getProduct(id));
    }

    @GetMapping(value = "/product/search/{GTIN}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getGTINProductSuggestion(@PathVariable("GTIN") String GTIN) {
        if (GTIN==null) {
            return ResponseEntity.badRequest().body("You need to provide a valid GTIN in the request");
        }
        return ResponseEntity.ok(allegroService.allegroGTINProductSearch(Long.parseLong(GTIN)));
    }

    @GetMapping(value = "/product/suggestion", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getProductSuggestion(@RequestParam("phrase") String phrase, @RequestParam("id") String id) {
        return ResponseEntity.ok(allegroService.allegroProductSearch(phrase, id));
    }

    @GetMapping(value = "category/{categoryID}/params", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getCategoryParams(@PathVariable("categoryID") String categoryID) {
        return ResponseEntity.ok(allegroService.getParams(categoryID));
    }

    @GetMapping(value = "/advert/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getOlxAdvert(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader, @PathVariable("id") String id) {
        return ResponseEntity.ok(allegroService.getAdvert(authHeader, id));
    }

}
