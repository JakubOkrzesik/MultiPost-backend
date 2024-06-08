package com.example.multipost_backend.listings.adverts;

import com.example.multipost_backend.auth.user.Role;
import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.dbmodels.*;
import com.example.multipost_backend.listings.services.AllegroService;
import com.example.multipost_backend.listings.services.OlxService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.scheduling.config.ScheduledTask;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class AdvertsCheckSchedulerTest {

    @SpyBean
    AdvertsCheckScheduler advertsCheckScheduler;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ListingRepository listingRepository;
    @Autowired
    private OlxService olxService;
    @Autowired
    private AllegroService allegroService;
    private static final Logger log = LoggerFactory.getLogger(AdvertsCheckSchedulerTest.class);

    @Test
    @Transactional
    public void fullAdvertCheckMechanismTest() {
        List<User> userList = userRepository.findAllByRole(Role.USER)
                .orElse(new ArrayList<>());

        if (userList.isEmpty()) {
            log.info("No users found.");
            return;
        }

        userList.parallelStream().forEach(user -> {
            List<Listing> listings = listingRepository.findAllByUserId(user.getId()).
                    orElse(new ArrayList<>());

            int olxCounter = 0;
            int allegroCounter = 0;
            boolean isUpdated = false;

            for (Listing listing: listings) {

                boolean listingUpdated = false;

                String olxAdvertId = listing.getOlxId();
                String allegroAdvertId = listing.getAllegroId();

                try {

                    listingUpdated |= olxStatusCheck(olxAdvertId, listing, user);
                    listingUpdated |= allegroStatusCheck(allegroAdvertId, listing, user);

                    handleCrossPlatformStateConsistency(user, listing);

                    if (listingUpdated) {
                        isUpdated = true;
                    }
                } catch (Exception e) {
                    log.error("Error checking advert state for listing id: " + listing.getId(), e);
                }
            }

            if (isUpdated) {
                try {
                    listingRepository.saveAll(listings);
                } catch (Exception e) {
                    log.error("Error saving updated listings for user id: " + user.getId(), e);
                }
            }

            log.info("Number of state changes in Olx for user id {}: {}", user.getId(), olxCounter);
            log.info("Number of state changes in Allegro for user id {}: {}", user.getId(), allegroCounter);
        });
    }


    private boolean olxStatusCheck(String olxAdvertId, Listing listing, User user) {
        if (olxAdvertId != null) {
            JsonNode olxResponse = olxService.getAdvert(olxAdvertId, user);
            String olxListingState = olxResponse.get("data").get("status").asText();
            OlxListingState olxListingStateEnum = olxService.mapStateToEnum(olxListingState);
            if (olxListingStateEnum != listing.getOlxState()) {
                listing.setOlxState(olxListingStateEnum);
                return true;
            }
        }
        return false;
    }

    private boolean allegroStatusCheck(String allegroAdvertId, Listing listing, User user) {
        if (allegroAdvertId != null) {
            JsonNode allegroResponse = allegroService.getAdvert(allegroAdvertId, user);
            String allegroAdvertState = allegroResponse.get("publication").get("status").asText();
            AllegroListingState allegroListingStateEnum = allegroService.mapStateToEnum(allegroAdvertState);
            if (allegroListingStateEnum != listing.getAllegroState()) {
                listing.setAllegroState(allegroListingStateEnum);
                return true;
            }
        }
        return false;
    }

    // needs reevaluation logic is wonky
    private void handleCrossPlatformStateConsistency(User user, Listing listing) {
        if (listing.getOlxState() == OlxListingState.REMOVED_BY_USER && listing.getAllegroState() != AllegroListingState.ENDED && listing.getAllegroState() != null) {
            log.info("Product sold on OLX, deactivating Allegro listing...");
            listing.setSoldOn(SoldOnEnum.OLX);
            allegroService.changeAdvertStatus(listing.getAllegroId(), AllegroListingState.ENDED, user).toPrettyString();
        }

        if (listing.getAllegroState() == AllegroListingState.ENDED && listing.getOlxState() != OlxListingState.REMOVED_BY_USER && listing.getOlxState() !=null) {
            log.info("Product sold on Allegro, deactivating OLX listing...");
            listing.setSoldOn(SoldOnEnum.ALLEGRO);
            if (listing.getOlxState() == OlxListingState.NEW || listing.getOlxState() == OlxListingState.ACTIVE) {
                olxService.changeAdvertStatus(listing.getOlxId(), "deactivate", user);
            }
        }
    }
}