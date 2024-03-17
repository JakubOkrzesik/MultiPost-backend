package com.example.multipost_backend.listings.services;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Data
public class GeneralService {
    public Date calculateExpiration(String expiresIn) {
        return new Date(System.currentTimeMillis() + 1000L*Integer.parseInt(expiresIn));
    }
}
