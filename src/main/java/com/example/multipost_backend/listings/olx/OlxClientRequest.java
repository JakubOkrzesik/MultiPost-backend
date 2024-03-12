package com.example.multipost_backend.listings.olx;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OlxClientRequest {
    private String grant_type;
    private String client_id;
    private String client_secret;
    private String scope;
}
