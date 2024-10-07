package com.example.multipost_backend.listings.dbModels;

import com.example.multipost_backend.auth.user.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

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
    @NonNull
    private String listingName;
    private String olxId;
    private String allegroId;
    private Integer price;
    private SoldOnEnum soldOn;
    @Enumerated(EnumType.STRING)
    private OlxListingState olxState;
    @Enumerated(EnumType.STRING)
    private AllegroListingState allegroState;
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;
}
