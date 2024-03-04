package com.example.multipost_backend.listings.dbmodels;

import com.example.multipost_backend.auth.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserKeysRepository extends JpaRepository<UserAccessKeys,Integer> {
    Optional<UserAccessKeys> findByUser(User user);
}
