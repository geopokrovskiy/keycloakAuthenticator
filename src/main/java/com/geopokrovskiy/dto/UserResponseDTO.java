package com.geopokrovskiy.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
public class UserResponseDTO {
    private String accessToken;
    private long expiresIn;
    private String refreshToken;
    private String tokenType;
}
