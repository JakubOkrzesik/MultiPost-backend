package com.example.multipost_backend.listings.listingRequests;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeneralConfig {
    @Bean
    public Dotenv dotenv() { return Dotenv.load(); }
}
