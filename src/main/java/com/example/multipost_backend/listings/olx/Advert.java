package com.example.multipost_backend.listings.olx;


import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.lang.reflect.Array;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Advert {
    private String title;
    private String description;
    private int category_id;
    @Enumerated(EnumType.STRING)
    private AdvertiserType advertiserType;
    private String name;
    private String phone;
    private Array images;
    private String price;
}
