package com.example.multipost_backend.listings.allegroModels;

import org.springframework.http.HttpStatusCode;

import java.io.IOException;
import java.io.InputStream;

public class AllegroApiException extends IOException {
    public AllegroApiException(String s){}

    public AllegroApiException(HttpStatusCode statusCode, InputStream body) {
    }
}
