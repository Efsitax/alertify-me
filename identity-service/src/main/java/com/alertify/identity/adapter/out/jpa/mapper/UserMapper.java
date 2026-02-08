package com.alertify.identity.adapter.out.jpa.mapper;

import com.alertify.identity.adapter.out.jpa.entity.UserEntity;
import com.alertify.identity.domain.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserEntity toEntity(User domain) {

        if(domain == null) return null;

        return UserEntity.builder()
                .id(domain.getId())
                .email(domain.getEmail())
                .passwordHash(domain.getPasswordHash())
                .firstName(domain.getFirstName())
                .lastName(domain.getLastName())
                .createdAt(domain.getCreatedAt())
                .isDeleted(domain.getIsDeleted())
                .build();
    }

    public User toDomain(UserEntity entity) {

        if(entity == null) return null;

        return User.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .createdAt(entity.getCreatedAt())
                .isDeleted(entity.getIsDeleted())
                .build();
    }
}
