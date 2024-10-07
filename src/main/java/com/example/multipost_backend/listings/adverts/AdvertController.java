package com.example.multipost_backend.listings.adverts;


import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.dbModels.*;
import com.example.multipost_backend.listings.listingRequests.ResponseHandler;
import com.example.multipost_backend.listings.services.AllegroService;
import com.example.multipost_backend.listings.services.GeneralService;
import com.example.multipost_backend.listings.services.OlxService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.jdi.request.InvalidRequestStateException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/{id}")
    public ResponseEntity<Listing> getAdvert(@PathVariable Integer id) throws AdvertNotFoundException {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new AdvertNotFoundException("Advert with the provided ID does not exist"));

        return ResponseEntity.ok(listing);
    }

    @PatchMapping("/{id}/price")
    public ResponseEntity<Object> changeAdvertPrice(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader, @PathVariable Integer id, @RequestParam("newprice") int price) throws AdvertNotFoundException, JsonProcessingException {

        try {
            String email = generalService.getUsername(authHeader);
            logger.info("Extracted email: {}", email);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            logger.info("Found user: {}", user);

            Listing listing = listingRepository.findById(id)
                    .orElseThrow(() -> new AdvertNotFoundException("Advert with the provided ID does not exist"));
            logger.info("Found advert with id: {}", id);

            if (listing.getOlxId() != null) {
                olxService.updateAdvertPrice(price, listing.getOlxId(), user);
            }

            if (listing.getAllegroId() != null) {
                allegroService.updateAdvertPrice(price, listing.getAllegroId(), user);
            }

            listing.setPrice(price);

            listingRepository.save(listing);

            return ResponseHandler.generateResponse("Price of advert changed", HttpStatus.OK, null);
        } catch (Exception e) {
            logger.error(String.valueOf(e));
            return ResponseHandler.generateResponse("Internal error while fetching adverts", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    @GetMapping(value = "/user_adverts", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getAdverts(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) throws NoUserAdvertsFoundException {
        try {
            String email = generalService.getUsername(authHeader);
            logger.info("Extracted email: {}", email);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            logger.info("Found user: {}", user);

            List<Listing> listings = listingRepository.findAllByUserId(user.getId())
                    .orElseThrow(() -> new NoUserAdvertsFoundException("User has no adverts"));
            logger.info("Found listings: {}", listings);

            return ResponseEntity.ok(listings);
        } catch(Exception e) {
            logger.error(String.valueOf(e));
           return ResponseHandler.generateResponse("Internal error while fetching adverts", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> postAdvert(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader, @RequestBody JsonNode jsonData) {

        try {
            String email = generalService.getUsername(authHeader);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            Listing listing = Listing.builder()
                    .listingName(jsonData.get("advert").get("name").asText())
                    .user(user)
                    .price(jsonData.get("advert").get("price").asInt())
                    .soldOn(SoldOnEnum.NONE)
                    .build();

            JsonNode data = jsonData.get("advert").requireNonNull();

            if (!data.get("platforms").isEmpty()) {
                if (data.get("platforms").get("olxToggle").asBoolean()) {
                    try {
                        ObjectNode olxData = (ObjectNode) data.get("olxData");
                        JsonNode olxAdvertResponse = olxService.createAdvert(olxData, user).get("data");
                        listing.setOlxId(olxAdvertResponse.get("id").asText());
                        listing.setOlxState(OlxListingState.NEW);
                    } catch (Exception e) {
                        logger.error(String.valueOf(e));
                        return ResponseHandler.generateResponse("OLX API error", HttpStatus.BAD_REQUEST, e);
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
                        if (allegroData.getStatusCode().value() == 202) {
                            /*JsonNode statusResponse = allegroService.getAdvertStatus(advertStatusUrl, user);*/
                            listing.setAllegroState(AllegroListingState.INACTIVE);
                        } else {
                            listing.setAllegroState(AllegroListingState.ACTIVE);
                        }

                    } catch (Exception e) {
                        if (data.get("platforms").get("olxToggle").asBoolean()) {
                            // finishing the olx advert if the allegro api returns an error
                            olxService.changeAdvertStatus(listing.getOlxId(), "finish", user);
                        }
                        logger.error(String.valueOf(e));
                        return ResponseHandler.generateResponse("Allegro API error", HttpStatus.INTERNAL_SERVER_ERROR, e);
                    }
                }
            } else {
                throw new InvalidRequestStateException("Platform not specified");
            }

            listingRepository.save(listing);

            return ResponseHandler.generateResponse("Advert successfully posted", HttpStatus.OK, null);

        } catch (Exception e) {
            logger.error(String.valueOf(e));
            return ResponseHandler.generateResponse("Internal error while posting advert", HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> deleteAdvert(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader, @PathVariable int id) throws AdvertNotFoundException {
        try {// advert will be deleted from database and delisted on allegro and olx
            String email = generalService.getUsername(authHeader);
            logger.info("Extracted email: {}", email);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            logger.info("Found user: {}", user);

            Listing listing = listingRepository.findById(id)
                    .orElseThrow(() -> new AdvertNotFoundException("Advert with the provided ID does not exist"));
            logger.info("Found advert with id: {}", id);

            if (listing.getOlxId() != null) {
                try {
                    if (listing.getOlxState()==OlxListingState.ACTIVE) {
                        // if the advert is currently up it needs to get de-listed first and then it could be finished
                        olxService.changeAdvertStatus(listing.getOlxId(), "deactivate", user);
                        listing.setOlxState(OlxListingState.REMOVED_BY_USER);
                    }

                    if (listing.getOlxState()==OlxListingState.LIMITED) {
                        olxService.changeAdvertStatus(listing.getOlxId(), "finish", user);
                    }

                    olxService.deleteAdvert(listing.getOlxId(), user);
                } catch (Exception e) {
                    logger.error(String.valueOf(e));
                    return ResponseHandler.generateResponse("OLX Api Error", HttpStatus.BAD_REQUEST, e);
                }
            }

            if (listing.getAllegroId() != null) {
                try {
                    allegroService.changeAdvertStatus(listing.getAllegroId(), AllegroListingState.ENDED, user);
                } catch (Exception e) {
                    logger.error(String.valueOf(e));
                    return ResponseHandler.generateResponse("Allegro Api Error", HttpStatus.BAD_REQUEST, e);
                }
            }

            listingRepository.delete(listing);

            return ResponseHandler.generateResponse("Deleted advert successfully", HttpStatus.OK, null);
        } catch (Exception e) {
            logger.error(String.valueOf(e));
            return ResponseHandler.generateResponse("Internal error while processing your request", HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

}