/*
 * Copyright 2026 efsitax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alertify.identity.application.service;

import com.alertify.common.exception.BadCredentialsException;
import com.alertify.common.exception.ResourceAlreadyExistsException;
import com.alertify.common.exception.ResourceNotFoundException;
import com.alertify.identity.application.port.in.IdentityUseCase;
import com.alertify.identity.application.port.out.IdentityPort;
import com.alertify.identity.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdentityService implements IdentityUseCase {

    private final IdentityPort identityPort;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User register(String email, String password, String firstName, String lastName) {

        if (identityPort.validateEmail(email)) {
            throw new ResourceAlreadyExistsException("User", "email", email);
        }
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .build();
        User savedUser = identityPort.saveUser(user);
        log.info("New User created User ID: {} ", savedUser.getId());
        return savedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public User login(String email, String password) {

        User user = identityPort.findByEmail(email);
        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        log.info("User logged in User ID: {} ", user.getId());
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(UUID userId) {

        User user = identityPort.findById(userId);
        if (user == null || user.getIsDeleted()) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        log.info("User found User ID: {} ", user.getId());
        return user;
    }

    @Override
    @Transactional
    public User updateUser(UUID id, String email, String firstName, String lastName) {

        User user = identityPort.findById(id);
        if (user == null || user.getIsDeleted()) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        if (!user.getEmail().equals(email) && identityPort.validateEmail(email)) {
            throw new ResourceAlreadyExistsException("User", "email", email);
        }
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        User updatedUser = identityPort.saveUser(user);
        log.info("User updated User ID: {} ", updatedUser.getId());
        return updatedUser;
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, String currentPassword, String newPassword) {

        User user = identityPort.findById(userId);
        if (user == null || user.getIsDeleted()) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BadCredentialsException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        identityPort.saveUser(user);
        log.info("User changed password User ID: {} ", user.getId());
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {

        User user = identityPort.findById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        user.setIsDeleted(true);
        identityPort.saveUser(user);
        log.info("User deleted User ID: {} ", user.getId());
    }
}
