package com.example.multipost_backend.listings.dbModels;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ListingRepository extends JpaRepository<Listing, Integer> {

    Optional<Listing> findById (Integer id);

    Optional<List<Listing>> findAllByUserId (Integer id);
}
