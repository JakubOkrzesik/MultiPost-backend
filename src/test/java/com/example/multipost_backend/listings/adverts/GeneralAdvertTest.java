package com.example.multipost_backend.listings.adverts;

import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.dbmodels.Listing;
import com.example.multipost_backend.listings.dbmodels.ListingRepository;
import com.example.multipost_backend.listings.services.GeneralService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

@SpringBootTest
public class GeneralAdvertTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ListingRepository listingRepository;


    @Test
    void getUserAdverts() throws NoUserAdvertsFoundException {
        User user = userRepository.findByEmail("test@user.com")
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<Listing> listings = listingRepository.findAllByUserId(user.getId())
                .orElseThrow(() -> new NoUserAdvertsFoundException("User has no adverts"));

        System.out.println(listings);
    }

    @Test
    void advertDeletionTest() {

    }

    @Test
    void priceChangeTest() {

    }
}
