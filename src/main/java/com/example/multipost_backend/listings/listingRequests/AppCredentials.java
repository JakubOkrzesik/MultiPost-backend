package com.example.multipost_backend.listings.listingRequests;

import com.example.multipost_backend.auth.user.Role;
import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.allegro.AllegroTokenResponse;
import com.example.multipost_backend.listings.services.AllegroService;
import com.example.multipost_backend.listings.services.EbayService;
import com.example.multipost_backend.listings.services.OlxService;
import com.example.multipost_backend.listings.dbmodels.UserAccessKeys;
import com.example.multipost_backend.listings.dbmodels.UserKeysRepository;
import com.example.multipost_backend.listings.ebay.EbayTokenResponse;
import com.example.multipost_backend.listings.olx.OlxTokenResponse;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

@Component
@AllArgsConstructor
public class AppCredentials {

    private EbayService ebayService;
    private AllegroService allegroService;
    private OlxService olxService;
    private final UserRepository userRepository;
    private final UserKeysRepository userKeysRepository;
    // Getting the application's OLX credentials on startup. These can be used to access parameters needed to complete
    // the details of a listing like categories, locations etc.
    @PostConstruct
    public void getClientCredentials() {
        Optional<User> user = userRepository.findByEmail("admin@admin.com");

        OlxTokenResponse olxResponse = olxService.getApplicationToken();
        AllegroTokenResponse allegroResponse = allegroService.getClientToken();
        EbayTokenResponse ebayResponse = ebayService.getClientToken();

        if (user.isPresent()) {
            Optional<UserAccessKeys> keys = userKeysRepository.findByUser(user.get());
            if (keys.isPresent()){

            }
            else {

            }
        }
        else {
            User newUser = User.builder()
                    .email("admin@admin.com")
                    .password(System.getenv("ADMIN_PASSWORD"))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(newUser);

            UserAccessKeys newKeys = UserAccessKeys.builder()
                    .olxAccessToken(olxResponse.getAccess_token())
                    .olxRefreshToken(olxResponse.getRefresh_token())
                    .olxTokenExpiration(new Date(System.currentTimeMillis() + Integer.parseInt(olxResponse.getExpires_in())))
                    .ebayAccessToken(ebayResponse.getAccess_token())
                    .ebayRefreshToken(ebayResponse.getRefresh_token())
                    .ebayTokenExpiration(new Date(System.currentTimeMillis() + Integer.parseInt(ebayResponse.getExpires_in())))
                    .allegroAccessToken(allegroResponse.getAccess_token())
                    .allegroRefreshToken(allegroResponse.getRefresh_token())
                    .allegroTokenExpiration(new Date(System.currentTimeMillis() + Integer.parseInt(allegroResponse.getExpires_in())))
                    .user(newUser)
                    .build();

            userKeysRepository.save(newKeys);

        }







    }




}
