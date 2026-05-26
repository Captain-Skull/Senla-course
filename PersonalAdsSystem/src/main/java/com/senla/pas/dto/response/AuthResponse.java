package com.senla.pas.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private UserResponse user;

    public AuthResponse(String accessToken, UserResponse userResponse) {
        this.accessToken = accessToken;
        this.user = userResponse;
    }
}
