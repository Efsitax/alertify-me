package com.alertify.identity.application.port.out;

import com.alertify.identity.domain.model.User;

import java.util.UUID;

public interface IdentityPort {

    User saveUser(User user);
    boolean validateEmail(String email);
    User findByEmail(String email);
    User findById(UUID id);
}
