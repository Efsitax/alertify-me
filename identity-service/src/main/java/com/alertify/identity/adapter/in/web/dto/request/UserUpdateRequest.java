package com.alertify.identity.adapter.in.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email should be valid")
        String email,
        @NotBlank(message = "First name cannot be blank")
        @Size(max = 50, message = "First name must be less than 50 characters")
        String firstName,
        @NotBlank(message = "Last name cannot be blank")
        @Size(max = 50, message = "Last name must be less than 50 characters")
        String lastName
) {}
