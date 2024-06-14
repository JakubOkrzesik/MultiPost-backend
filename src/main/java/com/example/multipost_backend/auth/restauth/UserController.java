package com.example.multipost_backend.auth.restauth;

import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.dbmodels.UserAccessKeys;
import com.example.multipost_backend.listings.dbmodels.UserKeysRepository;
import com.example.multipost_backend.listings.listingRequests.ResponseHandler;
import com.example.multipost_backend.listings.services.GeneralService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final GeneralService generalService;
    private final UserKeysRepository userKeysRepository;

    // super dumb request could be resolved by adding claims to the JWT token NEEDS FIXING
    @GetMapping("/get")
    public ResponseEntity<Object> getUserDetails(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        try {
            String email = generalService.getUsername(authHeader);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            UserAccessKeys keys = userKeysRepository.findByUser(user).orElseGet(() -> {
                UserAccessKeys newKeys = UserAccessKeys.builder().build();
                userKeysRepository.save(newKeys);
                return newKeys;
            });

            Map<String, Object> userDetails = new HashMap<>();
            userDetails.put("name", user.getFirstName());
            userDetails.put("isOlxAuth", (keys.getOlxAccessToken() != null));
            userDetails.put("isAllegroAuth", (keys.getAllegroAccessToken() != null));

            return ResponseHandler.generateResponse("User retrieved", HttpStatus.OK, userDetails);

        } catch (Exception e) {
            return ResponseHandler.generateResponse("Internal error while processing your request", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }
}
