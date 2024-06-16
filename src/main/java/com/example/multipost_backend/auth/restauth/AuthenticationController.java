package com.example.multipost_backend.auth.restauth;


import com.example.multipost_backend.listings.listingRequests.ResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authService;

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody RegisterRequest request) throws UserAlreadyExistsException {
        try {
            return ResponseHandler.generateResponse(authService.register(request), HttpStatus.OK, null);
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Registration unsuccessful", HttpStatus.OK, e);
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<Object> authenticate(@RequestBody AuthenticationRequest request) {
        try {
            return ResponseEntity.ok(authService.authenticate(request));
        } catch (Exception e) {
            return ResponseHandler.generateResponse("Email or password are incorrect", HttpStatus.OK, e);
        }
    }

    @GetMapping("/getresponse")
    public ResponseEntity<Object> getResponse() {
        return ResponseHandler.generateResponse("THis is a response", HttpStatus.OK, null);
    }

}
