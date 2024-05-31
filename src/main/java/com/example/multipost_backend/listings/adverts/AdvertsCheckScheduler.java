package com.example.multipost_backend.listings.adverts;

import com.example.multipost_backend.auth.user.Role;
import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.dbmodels.Listing;
import com.example.multipost_backend.listings.dbmodels.ListingRepository;
import com.example.multipost_backend.listings.dbmodels.allegroListingState;
import com.example.multipost_backend.listings.dbmodels.olxListingState;
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

    /*@Scheduled(fixedDelay = 30000)*/
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

                boolean listingUpdated = false;

                String olxAdvertId = listing.getOlxId();
                String allegroAdvertId = listing.getAllegroId();

                try {
                    if (olxAdvertId != null) {
                        JsonNode olxResponse = olxService.getAdvert(olxAdvertId, user);
                        String olxListingState = olxResponse.get("data").get("status").asText();
                        olxListingState olxListingStateEnum = olxService.mapStateToEnum(olxListingState);
                        if (olxListingStateEnum != listing.getOlxState()) {
                            listing.setOlxState(olxListingStateEnum);
                            listingUpdated = true;
                            olxCounter++;
                        }
                    }

                    if (allegroAdvertId != null) {
                        JsonNode allegroResponse = allegroService.getAdvert(allegroAdvertId, user);
                        String allegroAdvertState = allegroResponse.get("publication").get("status").asText();
                        allegroListingState allegroListingStateEnum = allegroService.mapStateToEnum(allegroAdvertState);
                        if (allegroListingStateEnum != listing.getAllegroState()) {
                            listing.setAllegroState(allegroListingStateEnum);
                            listingUpdated = true;
                            allegroCounter++;
                        }
                    }

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
}
