package com.example.multipost_backend.listings.listingRequests;

import com.example.multipost_backend.auth.user.Role;
import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.allegro.AllegroTokenResponse;
import com.example.multipost_backend.listings.dbModels.UserAccessKeys;
import com.example.multipost_backend.listings.dbModels.UserKeysRepository;
import com.example.multipost_backend.listings.olx.authentication.OlxTokenResponse;
import com.example.multipost_backend.listings.services.AllegroService;
import com.example.multipost_backend.listings.services.EnvService;
import com.example.multipost_backend.listings.services.GeneralService;
import com.example.multipost_backend.listings.services.OlxService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@SpringBootTest
class AppCredentialsTest {

    @Autowired
    private AllegroService allegroService;
    @Autowired
    private OlxService olxService;
    @Autowired
    private GeneralService generalService;
    @Autowired
    private EnvService envService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserKeysRepository userKeysRepository;

    @Test
    public void getClientCredentialswithNoAdminUser() {

        User user = userRepository.findByEmail("admin@admin.com").orElseThrow(() -> new UsernameNotFoundException("Username not found"));

        userRepository.delete(user);

        String password = envService.getADMIN_PASSWORD();
        if (password == null) {
            throw new IllegalStateException("Admin password not found in environmental variables");
        }

        User user1 = userRepository.findByEmail("admin@admin.com").orElse(
                User.builder()
                        .email("admin@admin.com")
                        .password(password)
                        .role(Role.ADMIN)
                        .build()
        );


        UserAccessKeys newKeys = user1.getKeys();

        if (!(newKeys==null)) {
            if (generalService.isTokenExpired(newKeys.getOlxTokenExpiration()) || generalService.isTokenExpired(newKeys.getAllegroTokenExpiration())) {
                OlxTokenResponse olxResponse = olxService.getApplicationToken();
                AllegroTokenResponse allegroResponse = allegroService.getClientToken();
                setTokenData(newKeys, olxResponse, allegroResponse);
            }
        } else {
            newKeys = UserAccessKeys.builder()
                    .user(user1)
                    .build();

            OlxTokenResponse olxResponse = olxService.getApplicationToken();
            AllegroTokenResponse allegroResponse = allegroService.getClientToken();
            setTokenData(newKeys, olxResponse, allegroResponse);
        }

        user1.setKeys(newKeys);
        userRepository.save(user1);
        userKeysRepository.save(newKeys);
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