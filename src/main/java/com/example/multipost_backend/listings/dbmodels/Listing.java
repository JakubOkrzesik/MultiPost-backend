package com.example.multipost_backend.listings.dbmodels;

import com.example.multipost_backend.auth.user.User;
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
@Table(name = "Listing")
public class Listing {
    @Id
    @GeneratedValue
    private Integer id;
    private String listingName;
    private String olxUrl;
    private String allegroUrl;
    private String ebayUrl;
    @Enumerated(EnumType.STRING)
    private ListingState olxState;
    @Enumerated(EnumType.STRING)
    private ListingState allegroState;
    @Enumerated(EnumType.STRING)
    private ListingState ebayState;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
