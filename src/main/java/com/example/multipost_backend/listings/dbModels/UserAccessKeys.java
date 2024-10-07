package com.example.multipost_backend.listings.dbModels;

import com.example.multipost_backend.auth.user.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "UserAccessKeys")
public class UserAccessKeys {
    @Id
    @GeneratedValue
    private Integer id;
    private String olxAccessToken;
    private String olxRefreshToken;
    private Date olxTokenExpiration;
    @Column(length = 2000)
    private String allegroAccessToken;
    @Column(length = 2000)
    private String allegroRefreshToken;
    private Date allegroTokenExpiration;
    @OneToOne(mappedBy = "keys")
    @JsonBackReference
    private User user;
}
