package com.alertify.identity.application.port.in;

import com.alertify.identity.domain.model.User;

import java.util.UUID;

public interface IdentityUseCase {

    User register(String email, String password, String firstName, String lastName);
    User login(String email, String password);
    User getUserById(UUID userId);
    User updateUser(UUID id, String email, String firstName, String lastName);
    void changePassword(UUID userId, String currentPassword, String newPassword);
    void deleteUser(UUID userId);
}
