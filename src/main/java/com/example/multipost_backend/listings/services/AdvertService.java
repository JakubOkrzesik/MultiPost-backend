package com.example.multipost_backend.listings.services;

import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.listings.adverts.AdvertNotFoundException;
import com.example.multipost_backend.listings.adverts.NoUserAdvertsFoundException;
import com.example.multipost_backend.listings.allegroModels.AllegroApiException;
import com.example.multipost_backend.listings.dbModels.AllegroListingState;
import com.example.multipost_backend.listings.dbModels.Listing;
import com.example.multipost_backend.listings.dbModels.OlxListingState;
import com.example.multipost_backend.listings.dbModels.SoldOnEnum;
import com.example.multipost_backend.listings.olxModels.OlxApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.jdi.request.InvalidRequestStateException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdvertService {
    private final OlxService olxService;
    private final AllegroService allegroService;
    private final GeneralService generalService;
    private final UserService userService;
    private final ListingService listingService;
    private static final Logger logger = LoggerFactory.getLogger(AdvertService.class);


    public Listing getAdvertById(int id) throws AdvertNotFoundException {
        return listingService.findById(id)
                .orElseThrow(() -> new AdvertNotFoundException("Advert with the provided ID does not exist"));
    }

    public List<Listing> getUserAdverts(String authHeader) {
        User user = findUserByAuthHeaderEmail(authHeader);

        List<Listing> listings = listingService.findAllByUserId(user.getId());
        logger.info("Found listings: {}", listings);

        return listings;
    }

    public Listing postAdvert(String authHeader, JsonNode jsonData) throws OlxApiException, AllegroApiException {
        User user = findUserByAuthHeaderEmail(authHeader);

        Listing listing = Listing.builder()
                .user(user)
                .soldOn(SoldOnEnum.NONE)
                .build();

        if (jsonData.get("advert").isNull() || !jsonData.get("advert").get("platforms").isEmpty()) {

            JsonNode data = jsonData.get("advert");

            listing.setPrice(jsonData.get("advert").get("price").asInt());
            listing.setListingName(jsonData.get("advert").get("name").asText());

            if (data.get("platforms").get("olxToggle").asBoolean()) {
                try {
                    JsonNode olxData = data.get("olxData");
                    JsonNode olxAdvertResponse = olxService.createAdvert(olxData, user).get("data");
                    assert olxAdvertResponse!=null;
                    assert olxAdvertResponse.get("id").asText()!=null;
                    listing.setOlxId(olxAdvertResponse.get("id").asText());
                    listing.setOlxState(OlxListingState.NEW);
                } catch (Exception e) {
                    logger.error(String.valueOf(e));
                    throw new OlxApiException(e.getMessage());
                }
            }

            if (data.get("platforms").get("allegroToggle").asBoolean()) {
                try {
                    ResponseEntity<JsonNode> allegroData = allegroService.createAdvert(data.get("allegroData"), user);
                    /*String advertStatusUrl = Objects.requireNonNull(allegroData.getHeaders().getLocation()).toString();*/
                    JsonNode allegroResponseBody = allegroData.getBody();
                    assert allegroResponseBody != null;
                    listing.setAllegroId(allegroResponseBody.get("id").asText());
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
                    throw new AllegroApiException(e.getMessage());
                }
            }
        } else {
            throw new InvalidRequestStateException("Platform not specified");
        }

        return listingService.save(listing);
    }

    public void changeAdvertPrice(String authHeader, int id, int price) throws AdvertNotFoundException {
        User user = findUserByAuthHeaderEmail(authHeader);

        Listing listing = listingService.findById(id)
                .orElseThrow(() -> new AdvertNotFoundException("Advert with the provided ID does not exist"));
        logger.info("Found advert with id: {}", id);

        if (listing.getOlxId() != null) {
            olxService.updateAdvertPrice(price, listing.getOlxId(), user);
        }

        if (listing.getAllegroId() != null) {
            allegroService.updateAdvertPrice(price, listing.getAllegroId(), user);
        }

        listing.setPrice(price);

        listingService.save(listing);
    }

    public void deleteAdvert(String authHeader, int id) throws OlxApiException, AllegroApiException, AdvertNotFoundException {
        User user = findUserByAuthHeaderEmail(authHeader);

        Listing listing = listingService.findById(id)
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
                throw new OlxApiException(e.getMessage());
            }
        }

        if (listing.getAllegroId() != null) {
            try {
                allegroService.changeAdvertStatus(listing.getAllegroId(), AllegroListingState.ENDED, user);
            } catch (Exception e) {
                logger.error(String.valueOf(e));
                throw new AllegroApiException(e.getMessage());
            }
        }

        listingService.delete(listing);
    }

    private User findUserByAuthHeaderEmail(String authHeader){
        String email = generalService.getUsername(authHeader);

        return userService.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
