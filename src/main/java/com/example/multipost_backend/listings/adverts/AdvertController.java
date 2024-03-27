package com.example.multipost_backend.listings.adverts;


import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.listings.dbmodels.Listing;
import com.example.multipost_backend.listings.dbmodels.ListingRepository;
import com.example.multipost_backend.listings.dbmodels.ListingState;
import com.example.multipost_backend.listings.dbmodels.UserAccessKeys;
import com.example.multipost_backend.listings.services.AllegroService;
import com.example.multipost_backend.listings.services.EbayService;
import com.example.multipost_backend.listings.services.GeneralService;
import com.example.multipost_backend.listings.services.OlxService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jdi.request.InvalidRequestStateException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/v1/advert")
@RequiredArgsConstructor
public class AdvertController {

    private final ListingRepository listingRepository;
    private final GeneralService generalService;
    private final OlxService olxService;
    private final AllegroService allegroService;
    private final EbayService ebayService;

    @GetMapping("/{id}")
    public ResponseEntity<Listing> getAdvert(@PathVariable Integer id) throws AdvertNotFoundException {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new AdvertNotFoundException("Advert with the provided ID does not exist"));

        return ResponseEntity.ok(listing);
    }

    @GetMapping("")
    public ResponseEntity<List<Listing>> getAdverts(HttpServletRequest request) throws NoUserAdvertsFoundException {

        User user = generalService.getUser(request);

        List<Listing> listings = listingRepository.findAllByUser(user)
                .orElseThrow(() -> new NoUserAdvertsFoundException("User has no adverts"));

        return ResponseEntity.ok(listings);
    }

    @PostMapping("/create")
    public ResponseEntity<String> postAdvert(HttpServletRequest request, @RequestBody JsonNode jsonData) throws IOException {

        User user = generalService.getUser(request);

        listingRepository.save(advertHandler(jsonData, user));

        return ResponseEntity.ok("Advert was successfully added");
    }

    private Listing advertHandler(JsonNode jsonData, User user) throws IOException {

        Listing listing = Listing.builder()
                .listingName(String.valueOf(jsonData.get("name")))
                .user(user)
                .build();

        JsonNode platforms = jsonData.get("platforms");

        if (platforms != null) {
            for (JsonNode platform : platforms) {
                switch (platform.asText()) {
                    case "OLX":
                        JsonNode olxData = olxService.createAdvert(jsonData.get("olxData"), user);
                        listing.setOlxUrl(olxData.get("url").asText());
                        listing.setOlxState(ListingState.ACTIVE);
                        break;
                    case "Allegro":
                        JsonNode allegroData = allegroService.createAdvert(jsonData.get("allegroData"), user);
                        listing.setAllegroUrl(allegroData.get("url").asText());
                        listing.setAllegroState(ListingState.ACTIVE);
                        break;
                    case "Ebay":
                        JsonNode ebayData = ebayService.createAdvert(jsonData.get("ebayData"), user);
                        listing.setEbayUrl(ebayData.get("url").asText());
                        listing.setEbayState(ListingState.ACTIVE);
                        break;
                    default:
                        throw new InvalidRequestStateException("Invalid advert platform: " + platform);
                }
            }
        } else {
            throw new InvalidRequestStateException("Platform not specified");
        }

        return listing;
    }
}