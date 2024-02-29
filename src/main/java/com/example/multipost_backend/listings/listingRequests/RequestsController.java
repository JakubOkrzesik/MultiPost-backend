package com.example.multipost_backend.listings.listingRequests;


import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.example.multipost_backend.listings.config.OlxService;
import com.example.multipost_backend.listings.dbmodels.UserAccessKeys;
import com.example.multipost_backend.listings.dbmodels.UserKeysRepository;
import com.example.multipost_backend.listings.olx.GrantCodeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import java.util.Date;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class RequestsController {

    private final UserRepository userRepository;
    private final UserKeysRepository userKeysRepository;
    private final OlxService olxService;

    @GetMapping("/olx")
    public ResponseEntity<String> olxTest(@RequestParam("code") String code){
        User user = userRepository.findByEmail("test@user.com")
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));
        GrantCodeResponse response = olxService.getGrantAuthcode(code);

        userKeysRepository.save(UserAccessKeys
                        .builder()
                        .olxAccessToken(response.getAccess_token())
                        .olxRefreshToken(response.getRefresh_token())
                        .olxTokenExpiration(new Date(System.currentTimeMillis() + 1000L * Integer.parseInt(response.getExpires_in())))
                        .user(user)
                        .build()
        );
        return ResponseEntity.ok("OLX Authorization successful");
    }
}
