package com.example.multipost_backend.listings.olx;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GrantCodeRequest {
    private String grant_type;
    private String client_id;
    private String client_secret;
    private String code;
    private String scope;
}
