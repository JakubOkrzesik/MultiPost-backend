package com.example.multipost_backend.listings.adverts;

import com.example.multipost_backend.auth.user.Role;
import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.dbmodels.Listing;
import com.example.multipost_backend.listings.dbmodels.ListingRepository;
import com.example.multipost_backend.listings.dbmodels.AllegroListingState;
import com.example.multipost_backend.listings.dbmodels.OlxListingState;
import com.example.multipost_backend.listings.services.AllegroService;
import com.example.multipost_backend.listings.services.OlxService;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class ScheduledAdvertMethodsTest {

    @Autowired
    private ListingRepository listingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OlxService olxService;
    @Autowired
    private AllegroService allegroService;
    private static final Logger log = LoggerFactory.getLogger(ScheduledTask.class);


    @Test
    void savingListingTest() {
        Listing listing = Listing.builder()
                .id(254)
                .allegroId("7763961362")
                .allegroState(AllegroListingState.ACTIVATING)
                .user(userRepository.findById(2).orElseThrow(() -> new UsernameNotFoundException("User not found")))
                .build();

        listingRepository.save(listing);

        listingRepository.delete(listing);
    }

    @Test
    void listingQueryTest() {
        User user = userRepository.findByEmail("test@user.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<Listing> listings = listingRepository.findAllByUserId(user.getId()).
                orElse(new ArrayList<>());
        System.out.println(listings);
        assertNotNull(listings);
    }

    @Test
    void listingsRequestsTest() {
        User user = userRepository.findByEmail("test@user.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<Listing> listings = listingRepository.findAllByUserId(user.getId()).
                orElse(new ArrayList<>());
        assertNotNull(listings);

        int olxCounter = 0;
        int allegroCounter = 0;
        boolean isUpdated = false;

        for (Listing listing: listings) {

            boolean listingUpdated = false;

            String olxAdvertId = listing.getOlxId();
            String allegroAdvertId = listing.getAllegroId();

            if (olxAdvertId!=null) {
               JsonNode olxResponse = olxService.getAdvert(olxAdvertId, user);
               String olxListingState = olxResponse.get("data").get("status").asText();
               OlxListingState olxListingStateEnum = olxService.mapStateToEnum(olxListingState);
               if (olxListingStateEnum!=listing.getOlxState()) {
                   listing.setOlxState(olxListingStateEnum);
                   listingUpdated = true;
                   olxCounter++;
               }
            }

            if (allegroAdvertId!=null) {
                JsonNode allegroResponse = allegroService.getAdvert(allegroAdvertId, user);
                String allegroAdvertState = allegroResponse.get("publication").get("status").asText();
                AllegroListingState allegroListingStateEnum = allegroService.mapStateToEnum(allegroAdvertState);
                if (allegroListingStateEnum!=listing.getAllegroState()) {
                    listing.setAllegroState(allegroListingStateEnum);
                    listingUpdated = true;
                    allegroCounter++;
                }
            }

            if (listingUpdated) {
                isUpdated = true;
                listingRepository.save(listing);
            }
        }

        if (isUpdated) {
            listingRepository.saveAll(listings);
        }

        System.out.println("Olx counter: " + olxCounter);
        System.out.println("==================");
        System.out.println("Allegro counter: " + allegroCounter);
    }

    @Test
    void getAllUsersTest() {
        List<User> userList = userRepository.findAllByRole(Role.USER)
                .orElse(new ArrayList<>());

        System.out.println(userList.size());
    }

    @Test
    void brotherManTest() {
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
                        if (olxResponse==null) {
                            listing.setOlxId(null);
                            listing.setOlxState(OlxListingState.REMOVED_BY_USER);
                        } else {
                            String olxListingState = olxResponse.get("data").get("status").asText();
                            OlxListingState olxListingStateEnum = olxService.mapStateToEnum(olxListingState);
                            if (olxListingStateEnum != listing.getOlxState()) {
                                listing.setOlxState(olxListingStateEnum);
                                listingUpdated = true;
                                olxCounter++;
                            }
                        }
                    }

                    if (allegroAdvertId != null) {
                        JsonNode allegroResponse = allegroService.getAdvert(allegroAdvertId, user);
                        String allegroAdvertState = allegroResponse.get("publication").get("status").asText();
                        AllegroListingState allegroListingStateEnum = allegroService.mapStateToEnum(allegroAdvertState);
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
