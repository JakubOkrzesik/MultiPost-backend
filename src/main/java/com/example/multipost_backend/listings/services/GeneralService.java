package com.example.multipost_backend.listings.services;

import com.example.multipost_backend.auth.config.JwtService;
import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class GeneralService {

    // This service provides quality of life methods that might be needed throughout the app

    private final JwtService jwtService;
    private final UserRepository userRepository;

    // Calculates expiration date for tokens
    public Date calculateExpiration(String expiresIn) {
        return new Date(System.currentTimeMillis() + 1000L*Integer.parseInt(expiresIn));
    }

    // Calculates if given token is expired
    public boolean isTokenExpired(Date date) {
        return date.before(new Date(System.currentTimeMillis()));
    }

    // Retrieves user email from jwt token
    public User getUser(HttpServletRequest request) {
        String jwt = request.getHeader("Authentication").substring(7);
        String email = jwtService.extractUsername(jwt);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found in database"));
    }

    // Returns Base64 encoded credentials needed for client side token retrieval
    public String getAuthorizationHeader(String client_id, String client_secret) {
        String credentials = client_id + ":" + client_secret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        return "Basic " + encodedCredentials;
    }
}
