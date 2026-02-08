package com.alertify.identity.adapter.out.jpa;

import com.alertify.identity.adapter.out.jpa.mapper.UserMapper;
import com.alertify.identity.adapter.out.jpa.repository.UserRepository;
import com.alertify.identity.application.port.out.IdentityPort;
import com.alertify.identity.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IdentityAdapter implements IdentityPort {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public User saveUser(User user) {
        return userMapper.toDomain(userRepository.save(userMapper.toEntity(user)));
    }

    @Override
    public boolean validateEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User findByEmail(String email) {
        return userMapper.toDomain(userRepository.findByEmail(email).orElse(null));
    }

    @Override
    public User findById(UUID id) {
        return userMapper.toDomain(userRepository.findById(id).orElse(null));
    }
}
