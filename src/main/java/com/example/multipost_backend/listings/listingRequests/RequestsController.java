package com.example.multipost_backend.listings.listingRequests;


import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.allegro.AllegroTokenResponse;
import com.example.multipost_backend.listings.services.AllegroService;
import com.example.multipost_backend.listings.services.EbayService;
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
public class RequestsController {

    private final UserRepository userRepository;
    private final UserKeysRepository userKeysRepository;
    private final OlxService olxService;
    private final EbayService ebayService;
    private final AllegroService allegroService;

    @GetMapping("/olx")
    public ResponseEntity<String> olxAuth(@RequestParam("code") String code){
        User user = userRepository.findByEmail("test@user.com")
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));
        GrantCodeResponse response = olxService.getOlxToken(code);

        Optional<UserAccessKeys> userKeysOptional = userKeysRepository.findByUser(user);

        if (userKeysOptional.isPresent()) {
            UserAccessKeys keys = userKeysOptional.get();
            keys.setOlxAccessToken(response.getAccess_token());
            keys.setOlxRefreshToken(response.getRefresh_token());
            keys.setOlxTokenExpiration(new Date(System.currentTimeMillis() + 1000L * Integer.parseInt(response.getExpires_in())));
            userKeysRepository.save(keys);
        } else {
            userKeysRepository.save(UserAccessKeys
                    .builder()
                    .olxAccessToken(response.getAccess_token())
                    .olxRefreshToken(response.getRefresh_token())
                    .olxTokenExpiration(new Date(System.currentTimeMillis() + 1000L * Integer.parseInt(response.getExpires_in())))
                    .user(user)
                    .build()
            );
        }
        return ResponseEntity.ok("OLX Authorization successful");
    }

    @GetMapping("/allegro")
    public ResponseEntity<String> allegroAuth(@RequestParam("code") String code){
        User user = userRepository.findByEmail("test@user.com")
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));
        AllegroTokenResponse response = allegroService.getAllegroToken(code);

        Optional<UserAccessKeys> userKeysOptional = userKeysRepository.findByUser(user);

        if (userKeysOptional.isPresent()) {
            UserAccessKeys keys = userKeysOptional.get();
            keys.setAllegroAccessToken(response.getAccess_token());
            keys.setAllegroAccessToken(response.getRefresh_token());
            keys.setAllegroTokenExpiration(new Date(System.currentTimeMillis() + 1000L * Integer.parseInt(response.getExpires_in())));
            userKeysRepository.save(keys);
        } else {
            userKeysRepository.save(UserAccessKeys
                    .builder()
                    .allegroAccessToken(response.getAccess_token())
                    .allegroRefreshToken(response.getRefresh_token())
                    .allegroTokenExpiration(new Date(System.currentTimeMillis() + 1000L * Integer.parseInt(response.getExpires_in())))
                    .user(user)
                    .build()
            );
        }

        return ResponseEntity.ok("Allegro authorization successful");
    }

    @GetMapping("/ebay")
    public ResponseEntity<String> ebayAuth(@RequestParam("code") String code){
        User user = userRepository.findByEmail("test@user.com")
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));

        EbayTokenResponse response = ebayService.getUserToken(code);

        // App needs to create ebay policies here

        Optional<UserAccessKeys> userKeysOptional = userKeysRepository.findByUser(user);

        if (userKeysOptional.isPresent()) {
            UserAccessKeys keys = userKeysOptional.get();
            keys.setEbayAccessToken(response.getAccess_token());
            keys.setEbayAccessToken(response.getRefresh_token());
            keys.setEbayTokenExpiration(new Date(System.currentTimeMillis() + 1000L * Integer.parseInt(response.getExpires_in())));
            userKeysRepository.save(keys);
        } else {
            userKeysRepository.save(UserAccessKeys
                    .builder()
                    .ebayAccessToken(response.getAccess_token())
                    .ebayRefreshToken(response.getRefresh_token())
                    .ebayTokenExpiration(new Date(System.currentTimeMillis() + 1000L * Integer.parseInt(response.getExpires_in())))
                    .user(user)
                    .build()
            );
        }

        return ResponseEntity.ok("Ebay authorization successful");
    }

}
