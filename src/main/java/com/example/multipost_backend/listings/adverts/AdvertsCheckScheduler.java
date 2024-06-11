package com.example.multipost_backend.listings.adverts;

import com.example.multipost_backend.auth.user.Role;
import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.dbmodels.*;
import com.example.multipost_backend.listings.services.AllegroService;
import com.example.multipost_backend.listings.services.OlxService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@EnableAsync
public class AdvertsCheckScheduler {
    private final OlxService olxService;
    private final AllegroService allegroService;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private static final Logger log = LoggerFactory.getLogger(ScheduledTask.class);

    @Scheduled(fixedDelay = 30000)
    @Transactional
    @Async
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

                boolean olxListingUpdated;
                boolean allegroListingUpdated;

                String olxAdvertId = listing.getOlxId();
                String allegroAdvertId = listing.getAllegroId();

                try {

                    olxListingUpdated = olxStatusCheck(olxAdvertId, listing, user);
                    allegroListingUpdated = allegroStatusCheck(allegroAdvertId, listing, user);

                    handleCrossPlatformStateConsistency(user, listing);

                    isUpdated = (olxListingUpdated || allegroListingUpdated);
                    olxCounter += (olxListingUpdated ? 1 : 0);
                    allegroCounter += (allegroListingUpdated ? 1 : 0);

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
            allegroService.changeAdvertStatus(listing.getAllegroId(), AllegroListingState.ENDED, user);
        }

        if (listing.getAllegroState() == AllegroListingState.ENDED && listing.getOlxState() != OlxListingState.REMOVED_BY_USER && listing.getOlxState() !=null) {
            log.info("Product sold on Allegro, deactivating OLX listing...");
            listing.setSoldOn(SoldOnEnum.ALLEGRO);
            // listing that is posted or is about to be posted must be first deactivated, then finished
            if (listing.getOlxState() == OlxListingState.NEW || listing.getOlxState() == OlxListingState.ACTIVE) {
                olxService.changeAdvertStatus(listing.getOlxId(), "deactivate", user);
            }
            olxService.changeAdvertStatus(listing.getOlxId(), "finish", user);
        }
    }
}
