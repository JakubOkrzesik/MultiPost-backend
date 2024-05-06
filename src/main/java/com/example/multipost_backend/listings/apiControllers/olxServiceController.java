package com.example.multipost_backend.listings.apiControllers;

import com.example.multipost_backend.listings.services.OlxService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/olx")
@RequiredArgsConstructor
public class olxServiceController {
    private final OlxService olxService;

    @PostMapping(value = "/attribs", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getCategoryAttributes(@RequestParam("title") String title) throws JsonProcessingException {
        String catSuggestion = olxService.getCategorySuggestion(title);
        List<JsonNode> catAttribs = olxService.getCategoryAttributes(catSuggestion);
        return ResponseEntity.ok(catAttribs);
    }

}