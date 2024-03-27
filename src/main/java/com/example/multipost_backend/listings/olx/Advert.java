package com.example.multipost_backend.listings.olx;


import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Advert {
    @NonNull
    private String title;
    @NonNull
    private String description;
    @NonNull
    private String category_id;
    @Enumerated(EnumType.STRING)
    @NonNull
    private AdvertiserType advertiserType;
    @NonNull
    private Location location;
    @NonNull
    private String name;
    @NonNull
    private String phone;
    @NonNull
    private String[] images;
    @NonNull
    private String price;
}
