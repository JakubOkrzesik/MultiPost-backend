package com.example.multipost_backend.listings.olx;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Contact {
    private String name;
    private String phone;
}
