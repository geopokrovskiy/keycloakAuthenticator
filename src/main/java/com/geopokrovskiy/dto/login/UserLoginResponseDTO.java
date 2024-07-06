package com.geopokrovskiy.dto.login;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserLoginResponseDTO {
    private String accessToken;
    private Long expiresIn;
    private String refreshToken;
    private String tokenType;
}
