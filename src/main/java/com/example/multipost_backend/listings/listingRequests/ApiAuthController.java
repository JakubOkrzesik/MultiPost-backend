package com.example.multipost_backend.listings.listingRequests;


import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.allegro.AllegroTokenResponse;
import com.example.multipost_backend.listings.services.AllegroService;
import com.example.multipost_backend.listings.services.EbayService;
import com.example.multipost_backend.listings.services.GeneralService;
import com.example.multipost_backend.listings.services.OlxService;
import com.example.multipost_backend.listings.dbmodels.UserAccessKeys;
import com.example.multipost_backend.listings.dbmodels.UserKeysRepository;
import com.example.multipost_backend.listings.ebay.EbayTokenResponse;
import com.example.multipost_backend.listings.SharedApiModels.GrantCodeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class ApiAuthController {

    private final UserRepository userRepository;
    private final UserKeysRepository userKeysRepository;
    private final OlxService olxService;
    private final EbayService ebayService;
    private final AllegroService allegroService;
    private final GeneralService generalService;

    @GetMapping("/olx")
    public ResponseEntity<String> olxAuth(@RequestParam("code") String code){
        User user = userRepository.findByEmail("test@user.com")
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));
        GrantCodeResponse response = olxService.getOlxToken(code);

        UserAccessKeys keys;

        Optional<UserAccessKeys> userKeysOptional = userKeysRepository.findByUser(user);

        if (userKeysOptional.isPresent()) {
            keys = userKeysOptional.get();
            keys.setOlxAccessToken(response.getAccess_token());
            keys.setOlxRefreshToken(response.getRefresh_token());
            keys.setOlxTokenExpiration(generalService.calculateExpiration(response.getExpires_in()));
        } else {
            keys = UserAccessKeys
                    .builder()
                    .olxAccessToken(response.getAccess_token())
                    .olxRefreshToken(response.getRefresh_token())
                    .olxTokenExpiration(generalService.calculateExpiration(response.getExpires_in()))
                    .user(user)
                    .build();
        }
        user.setKeys(keys);
        userKeysRepository.save(keys);
        userRepository.save(user);

        return ResponseEntity.ok("OLX authentication successful");
    }

    @GetMapping("/allegro")
    public ResponseEntity<String> allegroAuth(@RequestParam("code") String code){
        User user = userRepository.findByEmail("test@user.com")
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));
        AllegroTokenResponse response = allegroService.getAllegroToken(code);

        UserAccessKeys keys;

        Optional<UserAccessKeys> userKeysOptional = userKeysRepository.findByUser(user);

        if (userKeysOptional.isPresent()) {
            keys = userKeysOptional.get();
            keys.setAllegroAccessToken(response.getAccess_token());
            keys.setAllegroRefreshToken(response.getRefresh_token());
            keys.setAllegroTokenExpiration(generalService.calculateExpiration(response.getExpires_in()));

        } else {
            keys = UserAccessKeys
                    .builder()
                    .allegroAccessToken(response.getAccess_token())
                    .allegroRefreshToken(response.getRefresh_token())
                    .allegroTokenExpiration(generalService.calculateExpiration(response.getExpires_in()))
                    .user(user)
                    .build();
        }
        user.setKeys(keys);
        userKeysRepository.save(keys);
        userRepository.save(user);


        return ResponseEntity.ok("Allegro authentication successful");
    }

    @GetMapping("/ebay")
    public ResponseEntity<String> ebayAuth(@RequestParam("code") String code){
        User user = userRepository.findByEmail("test@user.com")
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));

        EbayTokenResponse response = ebayService.getEbayToken(code);

        UserAccessKeys keys;

        // App needs to create ebay policies here

        Optional<UserAccessKeys> userKeysOptional = userKeysRepository.findByUser(user);

        if (userKeysOptional.isPresent()) {
            keys = userKeysOptional.get();
            keys.setEbayAccessToken(response.getAccess_token());
            keys.setEbayRefreshToken(response.getRefresh_token());
            keys.setEbayTokenExpiration(generalService.calculateExpiration(response.getExpires_in()));
        } else {
            keys = UserAccessKeys
                    .builder()
                    .ebayAccessToken(response.getAccess_token())
                    .ebayRefreshToken(response.getRefresh_token())
                    .ebayTokenExpiration(generalService.calculateExpiration(response.getExpires_in()))
                    .user(user)
                    .build();
        }
        user.setKeys(keys);
        userKeysRepository.save(keys);
        userRepository.save(user);

        return ResponseEntity.ok("Ebay authorization successful");
    }

}
