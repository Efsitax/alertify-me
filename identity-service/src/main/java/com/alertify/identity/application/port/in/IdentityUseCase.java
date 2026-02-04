package com.alertify.identity.application.port.in;

import com.alertify.identity.domain.model.User;

public interface IdentityUseCase {

    User register(String email, String password, String firstName, String lastName);
}
