package com.alertify.identity.application.service;

import com.alertify.common.exception.BadCredentialsException;
import com.alertify.common.exception.ResourceAlreadyExistsException;
import com.alertify.identity.application.port.in.IdentityUseCase;
import com.alertify.identity.application.port.out.IdentityPort;
import com.alertify.identity.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdentityService implements IdentityUseCase {

    private final IdentityPort identityPort;
    private final PasswordEncoder passwordEncoder;

    @Override
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
        User savedUser = identityPort.register(user);
        log.info("New User created User ID: {} ", savedUser.getId());
        return savedUser;
    }

    @Override
    public User login(String email, String password) {

        User user = identityPort.findByEmail(email);
        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        log.info("User logged in User ID: {} ", user.getId());
        return user;
    }
}
