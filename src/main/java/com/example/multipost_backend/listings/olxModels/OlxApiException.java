package com.example.multipost_backend.listings.olxModels;

import org.springframework.http.HttpStatusCode;

import java.io.IOException;
import java.io.InputStream;

public class OlxApiException extends IOException {
    public OlxApiException(String s){}

    public OlxApiException(HttpStatusCode statusCode, InputStream body) {
    }
}
