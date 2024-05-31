package com.example.multipost_backend.listings.adverts;


import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.dbmodels.Listing;
import com.example.multipost_backend.listings.dbmodels.ListingRepository;
import com.example.multipost_backend.listings.dbmodels.allegroListingState;
import com.example.multipost_backend.listings.dbmodels.olxListingState;
import com.example.multipost_backend.listings.services.AllegroService;
import com.example.multipost_backend.listings.services.EbayService;
import com.example.multipost_backend.listings.services.GeneralService;
import com.example.multipost_backend.listings.services.OlxService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.jdi.request.InvalidRequestStateException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/v1/advert")
@RequiredArgsConstructor
public class AdvertController {

    private final GeneralService generalService;
    private final OlxService olxService;
    private final AllegroService allegroService;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private static final Logger logger = LoggerFactory.getLogger(AdvertController.class);

    /*@GetMapping("/{id}")
    public ResponseEntity<Listing> getAdvert(@PathVariable Integer id) throws AdvertNotFoundException {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new AdvertNotFoundException("Advert with the provided ID does not exist"));

        return ResponseEntity.ok(listing);
    }*/

    @GetMapping(value = "/user_adverts", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Listing>> getAdverts(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) throws NoUserAdvertsFoundException {

        logger.info("Received request to /userAdverts with authHeader: {}", authHeader);

        String email = generalService.getUsername(authHeader);
        logger.info("Extracted email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        logger.info("Found user: {}", user);

        List<Listing> listings = listingRepository.findAllByUserId(user.getId())
                .orElseThrow(() -> new NoUserAdvertsFoundException("User has no adverts"));
        logger.info("Found listings: {}", listings);

        return ResponseEntity.ok(listings);
    }

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> postAdvert(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader, @RequestBody JsonNode jsonData) {

        String email = generalService.getUsername(authHeader);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Listing listing = Listing.builder()
                .listingName(jsonData.get("advert").get("name").asText())
                .user(user)
                .build();

        JsonNode data = jsonData.get("advert").requireNonNull();

        if (!data.get("platforms").isEmpty()){
            if (data.get("platforms").get("olxToggle").asBoolean()) {
                try {
                    ObjectNode olxData = (ObjectNode) data.get("olxData");
                    JsonNode olxAdvertResponse = olxService.createAdvert(olxData, user).get("data");
                    listing.setOlxId(olxAdvertResponse.get("id").asText());
                    listing.setOlxState(olxListingState.NEW);
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(e);
                }
            }

            if (data.get("platforms").get("allegroToggle").asBoolean()) {
                try {
                    ResponseEntity<JsonNode> allegroData = allegroService.createAdvert(data.get("allegroData"), user);
                    /*String advertStatusUrl = Objects.requireNonNull(allegroData.getHeaders().getLocation()).toString();*/
                    JsonNode allegroRequestBody = allegroData.getBody();
                    assert allegroRequestBody != null;
                    listing.setAllegroId(allegroRequestBody.get("id").asText());
                    // if the response code is 202 the advert has not been posted yet and will be posted in about an hour
                    // the "location" header is provided with an endpoint that enables the application to check current status of advert
                    // on the test environment the advert gets posted immediately so the location header won't be used but in a production setting
                    // the use of the location header is mandatory
                    if (allegroData.getStatusCode().value()==202) {
                        /*JsonNode statusResponse = allegroService.getAdvertStatus(advertStatusUrl, user);*/
                        listing.setAllegroState(allegroListingState.INACTIVE);
                    }
                    else {
                        listing.setAllegroState(allegroListingState.ACTIVE);
                    }

                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(e);
                }
            }
        }
        else {
            throw new InvalidRequestStateException("Platform not specified");
        }

        listingRepository.save(listing);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Advert successfully added");

        return ResponseEntity.ok(response);
    }

}