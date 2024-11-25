package com.example.multipost_backend.listings.services;

import com.example.multipost_backend.auth.user.User;
import com.example.multipost_backend.listings.dbModels.UserAccessKeys;
import com.example.multipost_backend.listings.dbModels.UserKeysRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserKeysService {

    private final UserKeysRepository userKeysRepository;

    public Optional<UserAccessKeys> findByUser(User user) {
        return userKeysRepository.findByUser(user);
    }

    public void saveKeys(UserAccessKeys keys) {
        userKeysRepository.save(keys);
    }
}
