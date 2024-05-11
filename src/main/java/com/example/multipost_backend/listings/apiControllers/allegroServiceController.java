package com.example.multipost_backend.listings.apiControllers;

import com.example.multipost_backend.listings.services.AllegroService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/allegro")
@RequiredArgsConstructor
public class allegroServiceController {
    private final AllegroService allegroService;


    @GetMapping("/category/suggestion")
    public ResponseEntity<Object> getCategorySuggestion(@RequestParam("phrase") String phrase) {
        return ResponseEntity.ok(allegroService.getCategorySuggestion(phrase));
    }

    @GetMapping("/product/{GTIN}")
    public ResponseEntity<Object> getGTINProductSuggestion(@PathVariable String GTIN) {
        if (GTIN==null) {
            return ResponseEntity.badRequest().body("You need to provide a valid GTIN in the path");
        }
        return ResponseEntity.ok(allegroService.allegroGTINProductSearch(Long.parseLong(GTIN)));
    }

    @GetMapping("/product/suggestion")
    public ResponseEntity<Object> getProductSuggestion(@RequestParam("phrase") String phrase, @RequestParam("id") String id) {
        return ResponseEntity.ok(allegroService.allegroProductSearch(phrase, id));
    }


    /*@GetMapping("/product/{id}")
    public ResponseEntity<Object> getProductDetails(@PathVariable("id") String id) {
        return ResponseEntity.ok(allegroService.getProduct(id));
    }*/
}
