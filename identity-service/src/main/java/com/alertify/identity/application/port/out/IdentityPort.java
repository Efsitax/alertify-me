package com.alertify.identity.application.port.out;

import com.alertify.identity.domain.model.User;

public interface IdentityPort {

    User register(User user);
    boolean validateEmail(String email);
    User findByEmail(String email);
}
