package com.alertify.identity.adapter.in.web.mapper;

import com.alertify.identity.adapter.in.web.dto.response.UserResponse;
import com.alertify.identity.domain.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserWebMapper {
    public UserResponse toResponse(User user) {

        if (user == null) return null;
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getCreatedAt()
        );
    }
}