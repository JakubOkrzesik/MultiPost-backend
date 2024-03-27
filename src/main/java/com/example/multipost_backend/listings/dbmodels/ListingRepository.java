package com.example.multipost_backend.listings.dbmodels;

import com.example.multipost_backend.auth.user.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ListingRepository extends JpaRepository<Listing, Integer> {

    Optional<Listing> findById (Integer id);

    Optional<List<Listing>> findAllByUser (User user);
}
