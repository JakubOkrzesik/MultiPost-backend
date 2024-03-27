package com.example.multipost_backend.listings.dbmodels;

import com.example.multipost_backend.auth.user.User;
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
    @Column(length = 2000)
    private String ebayAccessToken;
    @Column(length = 2000)
    private String ebayRefreshToken;
    private Date ebayTokenExpiration;
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
