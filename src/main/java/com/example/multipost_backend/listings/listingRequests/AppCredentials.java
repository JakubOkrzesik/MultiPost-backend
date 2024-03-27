package com.example.multipost_backend.listings.listingRequests;

import com.example.multipost_backend.auth.user.Role;
import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.allegro.AllegroTokenResponse;
import com.example.multipost_backend.listings.services.*;
import com.example.multipost_backend.listings.dbmodels.UserAccessKeys;
import com.example.multipost_backend.listings.dbmodels.UserKeysRepository;
import com.example.multipost_backend.listings.ebay.EbayTokenResponse;
import com.example.multipost_backend.listings.olx.OlxTokenResponse;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@AllArgsConstructor
public class AppCredentials {

    private final EbayService ebayService;
    private final AllegroService allegroService;
    private final OlxService olxService;
    private final GeneralService generalService;
    private final EnvService envService;
    private final UserRepository userRepository;
    private final UserKeysRepository userKeysRepository;


    // Getting the application's OLX, Allegro and Ebay credentials on startup. These can be used to access parameters needed to complete
    // the details of a listing like categories, locations etc.
    // @PostConstruct
    public void getClientCredentials() {
        Optional<User> user = userRepository.findByEmail("admin@admin.com");
        UserAccessKeys newKeys;

        OlxTokenResponse olxResponse = olxService.getApplicationToken();
        AllegroTokenResponse allegroResponse = allegroService.getClientToken();
        EbayTokenResponse ebayResponse = ebayService.getClientToken();

        String password = envService.getADMIN_PASSWORD();
        if (password == null) {
            throw new IllegalStateException("Admin password not found in environmental variables");
        }

        if (user.isEmpty()) {
            User newUser = User.builder()
                    .email("admin@admin.com")
                    .password(password)
                    .role(Role.ADMIN)
                    .build();
            user = Optional.of(userRepository.save(newUser));
        }

        newKeys = user.get().getKeys();

        if (newKeys==null) {
            newKeys = UserAccessKeys.builder()
                    .user(user.get())
                    .build();
        }

        setTokenData(newKeys, olxResponse, allegroResponse, ebayResponse);

        userKeysRepository.save(newKeys);

    }

    private void setTokenData(UserAccessKeys keys, OlxTokenResponse olxResponse, AllegroTokenResponse allegroResponse, EbayTokenResponse ebayResponse) {
        keys.setOlxAccessToken(olxResponse.getAccess_token());
        keys.setOlxRefreshToken(olxResponse.getRefresh_token());
        keys.setOlxTokenExpiration(generalService.calculateExpiration(olxResponse.getExpires_in()));

        keys.setAllegroAccessToken(allegroResponse.getAccess_token());
        keys.setAllegroRefreshToken(allegroResponse.getRefresh_token());
        keys.setAllegroTokenExpiration(generalService.calculateExpiration(allegroResponse.getExpires_in()));

        keys.setEbayAccessToken(ebayResponse.getAccess_token());
        keys.setEbayRefreshToken(ebayResponse.getRefresh_token());
        keys.setEbayTokenExpiration(generalService.calculateExpiration(ebayResponse.getExpires_in()));
    }


}
