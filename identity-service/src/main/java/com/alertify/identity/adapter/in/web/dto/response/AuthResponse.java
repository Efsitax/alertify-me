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

package com.alertify.identity.adapter.in.web.dto.response;

public record AuthResponse(
        String accessToken,
        String tokenType,
        UserResponse user,
        long expiresIn
) {
    public static AuthResponse of(
            String token,
            UserResponse userResponse,
            long expirationMillis
    ) {
        return new AuthResponse(token, "Bearer", userResponse, expirationMillis);
    }
}