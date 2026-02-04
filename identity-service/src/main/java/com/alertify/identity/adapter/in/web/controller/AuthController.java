package com.alertify.identity.adapter.in.web.controller;

import com.alertify.identity.adapter.in.web.dto.request.RegisterRequest;
import com.alertify.identity.adapter.in.web.dto.response.UserResponse;
import com.alertify.identity.application.port.in.IdentityUseCase;
import com.alertify.identity.domain.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final IdentityUseCase useCase;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        User createdUser = useCase.register(
                request.email(),
                request.password(),
                request.firstName(),
                request.lastName()
        );
        UserResponse response = new UserResponse(
                createdUser.getId(),
                createdUser.getEmail(),
                createdUser.getFirstName(),
                createdUser.getLastName(),
                createdUser.getCreatedAt()
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
