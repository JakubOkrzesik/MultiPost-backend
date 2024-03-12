package com.example.multipost_backend.listings.SharedApiModels;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GrantCodeResponse {
    private String access_token;
    private String expires_in;
    private String token_type;
    private String refresh_token;
}