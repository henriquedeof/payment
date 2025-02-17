package com.xpto.distancelearning.payment.services;

import com.xpto.distancelearning.payment.models.UserModel;

import java.util.Optional;
import java.util.UUID;

public interface UserService {
    UserModel save(UserModel userModel);
    void delete(UUID userId);
    Optional<UserModel> findById(UUID userId);
}