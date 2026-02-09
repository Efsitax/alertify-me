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

import com.alertify.common.exception.BadCredentialsException;
import com.alertify.common.exception.ResourceAlreadyExistsException;
import com.alertify.common.rest.GlobalExceptionHandler;
import com.alertify.identity.adapter.in.web.dto.request.LoginRequest;
import com.alertify.identity.adapter.in.web.dto.request.RegisterRequest;
import com.alertify.identity.adapter.in.web.mapper.UserWebMapper;
import com.alertify.identity.application.port.in.IdentityUseCase;
import com.alertify.identity.domain.model.User;
import com.alertify.identity.infrastucture.utils.JwtProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({
        GlobalExceptionHandler.class,
        UserWebMapper.class
})
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private IdentityUseCase identityUseCase;

    @Test
    void shouldReturnCreated_When_RequestIsValid() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "test@test.com",
                "-Test123-",
                "Test",
                "User"
        );

        User mockUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@test.com")
                .firstName("Test")
                .lastName("User")
                .build();

        when(identityUseCase.register(any(), any(), any(), any())).thenReturn(mockUser);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.firstName").value("Test"));
    }

    @Test
    void shouldReturnConflict_When_EmailAlreadyExists() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "existing@test.com",
                "-Test123-",
                "Test",
                "User"
        );

        when(identityUseCase.register(
                eq("existing@test.com"),
                any(),
                any(),
                any())
        ).thenThrow(new ResourceAlreadyExistsException("User", "email", "existing.@test.com"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturnBadRequest_When_RequestIsInvalid() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "invalid-email",
                "short",
                "",
                ""
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.errorReason").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldReturnOk_When_LoginIsSuccessful() throws Exception {

        LoginRequest loginRequest = new LoginRequest("test@test.com", "password123");
        User mockUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@test.com")
                .build();

        when(identityUseCase.login(eq("test@test.com"), eq("password123"))).thenReturn(mockUser);
        when(jwtProvider.generateToken(any())).thenReturn("mock-jwt-token");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.user.email").value("test@test.com"));
    }

    @Test
    void shouldReturnUnauthorized_When_CredentialsAreInvalid() throws Exception {

        LoginRequest loginRequest = new LoginRequest("wrong@test.com", "wrong-password");

        when(identityUseCase.login(anyString(), anyString()))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorReason").value("BAD_CREDENTIALS"));
    }
}