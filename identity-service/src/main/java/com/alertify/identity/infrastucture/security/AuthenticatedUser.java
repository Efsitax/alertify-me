package com.alertify.identity.infrastucture.security;

import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

@Getter
@Builder
public class AuthenticatedUser {
    private final UUID id;
    private final String email;
}