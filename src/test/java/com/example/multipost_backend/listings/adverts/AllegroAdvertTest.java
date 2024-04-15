package com.example.multipost_backend.listings.adverts;

import com.example.multipost_backend.listings.services.AllegroService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class AllegroAdvertTest {

    @Autowired
    private AllegroService allegroService;

    @Test
    void advertCreationTest() {
    }
}