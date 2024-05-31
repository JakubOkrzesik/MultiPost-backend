package com.example.multipost_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MultiPostBackendApplication {

    public static void main(String[] args) {

        SpringApplication.run(MultiPostBackendApplication.class, args);
    }

}
