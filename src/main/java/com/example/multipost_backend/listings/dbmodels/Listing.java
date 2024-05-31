package com.example.multipost_backend.listings.dbmodels;

import com.example.multipost_backend.auth.user.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
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
    private String olxId;
    private String allegroId;
    @Enumerated(EnumType.STRING)
    private olxListingState olxState;
    @Enumerated(EnumType.STRING)
    private allegroListingState allegroState;
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;
}
