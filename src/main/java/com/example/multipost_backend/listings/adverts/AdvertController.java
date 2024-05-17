package com.example.multipost_backend.listings.adverts;


import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.dbmodels.Listing;
import com.example.multipost_backend.listings.dbmodels.ListingRepository;
import com.example.multipost_backend.listings.dbmodels.ListingState;
import com.example.multipost_backend.listings.services.AllegroService;
import com.example.multipost_backend.listings.services.EbayService;
import com.example.multipost_backend.listings.services.GeneralService;
import com.example.multipost_backend.listings.services.OlxService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.jdi.request.InvalidRequestStateException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/v1/advert")
@RequiredArgsConstructor
public class AdvertController {

    private final GeneralService generalService;
    private final OlxService olxService;
    private final AllegroService allegroService;
    private final EbayService ebayService;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;

    /*@GetMapping("/{id}")
    public ResponseEntity<Listing> getAdvert(@PathVariable Integer id) throws AdvertNotFoundException {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new AdvertNotFoundException("Advert with the provided ID does not exist"));

        return ResponseEntity.ok(listing);
    }

    @GetMapping("")
    public ResponseEntity<List<Listing>> getAdverts(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) throws NoUserAdvertsFoundException {

        User user = generalService.getUser(authHeader);

        List<Listing> listings = listingRepository.findAllByUser(user)
                .orElseThrow(() -> new NoUserAdvertsFoundException("User has no adverts"));

        return ResponseEntity.ok(listings);
    }*/

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> postAdvert(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader, @RequestBody JsonNode jsonData) throws IOException {

        String email = generalService.getUsername(authHeader);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Listing listing = Listing.builder()
                .listingName(String.valueOf(jsonData.get("name")))
                .user(user)
                .build();

        JsonNode data = jsonData.get("advert").requireNonNull();

        if (!data.get("platforms").isEmpty()){
            /*if (data.get("platforms").get("olxToggle").asBoolean()) {
                try {
                    ObjectNode olxData = (ObjectNode) data.get("olxData");
                    JsonNode olxAdvertResponse = olxService.createAdvert(olxData, user).get("data");
                    listing.setOlxUrl(olxAdvertResponse.get("url").asText());
                    listing.setOlxState(ListingState.ACTIVE);
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(e);
                }
            }*/

            if (data.get("platforms").get("allegroToggle").asBoolean()) {
                try {
                    JsonNode allegroData = allegroService.createAdvert(data.get("allegroData"), user);
                    listing.setAllegroUrl(allegroData.get("url").asText());
                    listing.setAllegroState(ListingState.ACTIVE);
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(e);
                }
            }
        }
        else {
            throw new InvalidRequestStateException("Platform not specified");
        }

        return ResponseEntity.ok(listingRepository.save(listing));
    }

}