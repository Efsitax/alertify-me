/*
 * Copyright 2026 efsitax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alertify.identity.adapter.in.web.controller;

import com.alertify.identity.adapter.in.web.dto.request.LoginRequest;
import com.alertify.identity.adapter.in.web.dto.request.RegisterRequest;
import com.alertify.identity.adapter.in.web.dto.response.AuthResponse;
import com.alertify.identity.adapter.in.web.dto.response.UserResponse;
import com.alertify.identity.adapter.in.web.mapper.UserWebMapper;
import com.alertify.identity.application.port.in.IdentityUseCase;
import com.alertify.identity.domain.model.User;
import com.alertify.identity.infrastucture.utils.JwtProvider;
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
    private final UserWebMapper mapper;
    private final JwtProvider jwtProvider;

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
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapper.toResponse(createdUser));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        User user = useCase.login(
                request.email(),
                request.password()
        );
        String token = jwtProvider.generateToken(user);
        return ResponseEntity
                .ok(AuthResponse.of(
                        token,
                        mapper.toResponse(user),
                        jwtProvider.getExpirationMillis()
                ));
    }
}
