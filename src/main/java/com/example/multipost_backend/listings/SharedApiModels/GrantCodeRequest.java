package com.example.multipost_backend.listings.SharedApiModels;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GrantCodeRequest {
    private String grant_type;
    private String code;
}
