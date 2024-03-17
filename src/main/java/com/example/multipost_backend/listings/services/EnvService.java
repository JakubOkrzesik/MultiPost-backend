package com.example.multipost_backend.listings.services;


import io.github.cdimascio.dotenv.Dotenv;
import lombok.Data;
import org.springframework.stereotype.Service;

@Service
@Data
public class EnvService {

    private String OLX_CLIENT_ID;
    private String OLX_CLIENT_SECRET;
    private String ALLEGRO_CLIENT_ID;
    private String ALLEGRO_CLIENT_SECRET;
    private String EBAY_CLIENT_ID;
    private String EBAY_CLIENT_SECRET;
    private String ADMIN_PASSWORD;
    private final Dotenv dotenv;

    private EnvService(Dotenv dotenv) {
        this.dotenv = dotenv;
        this.OLX_CLIENT_ID = dotenv.get("OLX_CLIENT_ID");
        this.OLX_CLIENT_SECRET = dotenv.get("OLX_CLIENT_SECRET");
        this.ALLEGRO_CLIENT_ID = dotenv.get("ALLEGRO_CLIENT_ID");
        this.ALLEGRO_CLIENT_SECRET = dotenv.get("ALLEGRO_CLIENT_SECRET");
        this.EBAY_CLIENT_ID = dotenv.get("EBAY_CLIENT_ID");
        this.EBAY_CLIENT_SECRET = dotenv.get("EBAY_CLIENT_SECRET");
        this.ADMIN_PASSWORD = dotenv.get("ADMIN_PASSWORD");
    }



}
