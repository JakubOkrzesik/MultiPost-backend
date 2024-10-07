package com.example.multipost_backend.listings.apiControllers;

import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.olx.Location;
import com.example.multipost_backend.listings.services.GeneralService;
import com.example.multipost_backend.listings.services.OlxService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/olx")
@RequiredArgsConstructor
public class OlxServiceController {
    private final OlxService olxService;
    private final GeneralService generalService;
    private final UserRepository userRepository;

    @PostMapping(value = "/attribs", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getCategoryAttributes(@RequestParam("categoryID") String categoryID) throws JsonProcessingException {

        List<JsonNode> catAttribs = olxService.getCategoryAttributes(categoryID);
        return ResponseEntity.ok(catAttribs);
    }

    @GetMapping(value = "/category", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getCategorySuggestion(@RequestParam("title") String title) throws JsonProcessingException {
        return ResponseEntity.ok(olxService.getCategorySuggestion(title));
    }

    @PostMapping(value = "/location", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Location> getLocationID(@RequestParam("lat") String lat, @RequestParam("lng") String lng) throws JsonProcessingException {
        return ResponseEntity.ok(olxService.getLocation(lat, lng));
    }

    @GetMapping(value = "/advert/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getOlxAdvert(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader, @PathVariable("id") String id) {
        String email = generalService.getUsername(authHeader);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return ResponseEntity.ok(olxService.getAdvert(id, user));
    }

}