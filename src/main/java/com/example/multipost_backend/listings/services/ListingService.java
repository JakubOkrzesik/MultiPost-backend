package com.example.multipost_backend.listings.services;

import com.example.multipost_backend.listings.dbModels.Listing;
import com.example.multipost_backend.listings.dbModels.ListingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ListingService {
    private final ListingRepository listingRepository;


    public Optional<Listing> findById(Integer id) {
        return listingRepository.findById(id);
    }


    public List<Listing> findAllByUserId(Integer userId) {
        return listingRepository.findAllByUserId(userId).orElse(Collections.emptyList());
    }

    public void saveAll(List<Listing> listings) {
        listingRepository.saveAll(listings);
    }


    public Listing save(Listing listing) {
        return listingRepository.save(listing);
    }


    public void delete(Listing listing) {
        listingRepository.delete(listing);
    }
}
