package com.alertify.identity.adapter.in.web.controller;

import com.alertify.identity.adapter.in.web.dto.request.ChangePasswordRequest;
import com.alertify.identity.adapter.in.web.dto.request.UserUpdateRequest;
import com.alertify.identity.adapter.in.web.dto.response.UserResponse;
import com.alertify.identity.adapter.in.web.mapper.UserWebMapper;
import com.alertify.identity.application.port.in.IdentityUseCase;
import com.alertify.identity.domain.model.User;
import com.alertify.identity.infrastucture.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final IdentityUseCase useCase;
    private final UserWebMapper mapper;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable UUID id
    ) {

        User user = useCase.getUserById(id);
        UserResponse response = mapper.toResponse(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {

        User user = useCase.getUserById(currentUser.getId());
        return ResponseEntity.ok(mapper.toResponse(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequest request
    ) {

        User updatedUser = useCase.updateUser(
                id,
                request.email(),
                request.firstName(),
                request.lastName()
        );
        return ResponseEntity.ok(mapper.toResponse(updatedUser));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable UUID id,
            @Valid @RequestBody ChangePasswordRequest request
    ) {

        useCase.changePassword(id, request.oldPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID id
    ) {

        useCase.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
