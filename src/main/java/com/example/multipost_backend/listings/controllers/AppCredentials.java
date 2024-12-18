package com.example.multipost_backend.listings.controllers;

import com.example.multipost_backend.auth.user.Role;
import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.listings.allegroModels.AllegroTokenResponse;
import com.example.multipost_backend.listings.services.*;
import com.example.multipost_backend.listings.dbModels.UserAccessKeys;
import com.example.multipost_backend.listings.olxModels.authentication.OlxTokenResponse;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AppCredentials {

    /*private final EbayService ebayService;*/
    private final AllegroService allegroService;
    private final OlxService olxService;
    private final GeneralService generalService;
    private final EnvService envService;
    private final UserService userService;
    private final UserKeysService userKeysService;


    // Getting the application's OLX and Allegro credentials on startup. These can be used to access parameters needed to complete
    // the details of a listing like categories, locations etc.
    @PostConstruct
    public void getClientCredentials() {

        String password = envService.getADMIN_PASSWORD();
        if (password == null) {
            throw new IllegalStateException("Admin password not found in environmental variables");
        }

        User user = userService.findByEmail("admin@admin.com").orElse(
                User.builder()
                        .email("admin@admin.com")
                        .password(password)
                        .role(Role.ADMIN)
                        .build()
        );


        UserAccessKeys newKeys = user.getKeys();

        if (!(newKeys==null)) {
            if (generalService.isTokenExpired(newKeys.getOlxTokenExpiration()) || generalService.isTokenExpired(newKeys.getAllegroTokenExpiration())) {
                OlxTokenResponse olxResponse = olxService.getApplicationToken();
                AllegroTokenResponse allegroResponse = allegroService.getClientToken();
                setTokenData(newKeys, olxResponse, allegroResponse);

                user.setKeys(newKeys);
                userService.saveUser(user);
                userKeysService.saveKeys(newKeys);
            }
        } else {
            newKeys = UserAccessKeys.builder()
                    .user(user)
                    .build();

            OlxTokenResponse olxResponse = olxService.getApplicationToken();
            AllegroTokenResponse allegroResponse = allegroService.getClientToken();
            setTokenData(newKeys, olxResponse, allegroResponse);

            user.setKeys(newKeys);
            userService.saveUser(user);
            userKeysService.saveKeys(newKeys);
        }
    }

    // Sets the new data to keys
    private void setTokenData(UserAccessKeys keys, OlxTokenResponse olxResponse, AllegroTokenResponse allegroResponse/*, EbayTokenResponse ebayResponse*/) {
        keys.setOlxAccessToken(olxResponse.getAccess_token());
        keys.setOlxRefreshToken(olxResponse.getRefresh_token());
        keys.setOlxTokenExpiration(generalService.calculateExpiration(olxResponse.getExpires_in()));

        keys.setAllegroAccessToken(allegroResponse.getAccess_token());
        keys.setAllegroRefreshToken(allegroResponse.getRefresh_token());
        keys.setAllegroTokenExpiration(generalService.calculateExpiration(allegroResponse.getExpires_in()));

        /*keys.setEbayAccessToken(ebayResponse.getAccess_token());
        keys.setEbayRefreshToken(ebayResponse.getRefresh_token());
        keys.setEbayTokenExpiration(generalService.calculateExpiration(ebayResponse.getExpires_in()));*/
    }
}
