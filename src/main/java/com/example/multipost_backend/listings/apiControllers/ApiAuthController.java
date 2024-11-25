package com.example.multipost_backend.listings.apiControllers;


import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.allegro.AllegroTokenResponse;
import com.example.multipost_backend.listings.services.*;
import com.example.multipost_backend.listings.dbModels.UserAccessKeys;
import com.example.multipost_backend.listings.dbModels.UserKeysRepository;
import com.example.multipost_backend.listings.sharedApiModels.GrantCodeResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/service_auth")
@RequiredArgsConstructor
public class ApiAuthController {

    private final OlxService olxService;
    /*private final EbayService ebayService;*/
    private final AllegroService allegroService;
    private final GeneralService generalService;
    private final UserService userService;
    private final UserKeysService userKeysService;
    private static final Logger log = LoggerFactory.getLogger(ApiAuthController.class);

    @GetMapping("/olx")
    public ResponseEntity<Map<String, String>> olxAuth(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader, @RequestParam("code") String code){
        String email = generalService.getUsername(authHeader);

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        GrantCodeResponse response = olxService.getOlxToken(code);

        UserAccessKeys keys;

        Optional<UserAccessKeys> userKeysOptional = userKeysService.findByUser(user);

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
        return getMapResponseEntity(user, keys);
    }

    @GetMapping("/allegro")
    public ResponseEntity<Map<String, String>> allegroAuth(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader, @RequestParam("code") String code){
        String email = generalService.getUsername(authHeader);

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        AllegroTokenResponse response = allegroService.getAllegroToken(code);
        UserAccessKeys keys;

        Optional<UserAccessKeys> userKeysOptional = userKeysService.findByUser(user);

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
        return getMapResponseEntity(user, keys);
    }

    @NotNull
    private ResponseEntity<Map<String, String>> getMapResponseEntity(User user, UserAccessKeys keys) {
        user.setKeys(keys);
        userKeysService.saveKeys(keys);
        userService.saveUser(user);

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "Authentication successful");

        return ResponseEntity.ok(responseBody);
    }

    /*@GetMapping("/ebay")
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
    }*/

}
