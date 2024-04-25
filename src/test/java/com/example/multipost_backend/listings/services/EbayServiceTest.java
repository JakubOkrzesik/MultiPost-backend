package com.example.multipost_backend.listings.services;

import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.auth.user.UserRepository;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class EbayServiceTest {

    @Autowired
    private EbayService ebayService;
    @Autowired
    private UserRepository userRepository;


/*    @Test
    void getFulfillmentPolicyTest() {
        User user = userRepository.findByEmail("test@user.com")
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));

        ObjectNode response = ebayService.getFulfillmentPolicy(ebayService.getUserToken(user));
        System.out.println(response);
    }

    @Test
    void getPaymentPolicyTest() {
        User user = userRepository.findByEmail("test@user.com")
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));

        ObjectNode response = ebayService.getPaymentPolicy(ebayService.getUserToken(user));
        System.out.println(response);
    }

    @Test
    void getReturnPolicyTest() {
        User user = userRepository.findByEmail("test@user.com")
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));

        ObjectNode response = ebayService.getReturnPolicy(ebayService.getUserToken(user));
        System.out.println(response);
    }*/
}