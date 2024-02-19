package com.example.multipost_backend.listings.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Listings {
    @Id
    @GeneratedValue
    private Integer id;
    private String listingName;
    private String olxUrl;
    private String allegroUrl;
    private String ebayUrl;
    @Enumerated(EnumType.STRING)
    private ListingState state;

}
