package com.geopokrovskiy.keycloakAuthenticator.util;

import com.geopokrovskiy.dto.auth.UserRegisterRequestDTO;
import com.geopokrovskiy.dto.auth.UserRegisterResponseDTO;
import com.geopokrovskiy.dto.login.UserLoginRequestDTO;

public class AuthUtils {

    public static final String USER_CREATED = "Successfully created user";
    public static final String INVALID_CREDENTIALS = "Invalid credentials";

    public static UserRegisterRequestDTO validUserRegisterRequestDTO() {
        return new UserRegisterRequestDTO()
                .toBuilder()
                .email("test@valid.com")
                .password("Password$!")
                .firstName("firstName")
                .lastName("lastName")
                .username("username")
                .build();
    }

    public static UserRegisterResponseDTO validUserRegisterResponseDTO() {
        return new UserRegisterResponseDTO()
                .toBuilder()
                .email("test@valid.com")
                .username("username")
                .message(USER_CREATED)
                .build();
    }

    public static UserRegisterRequestDTO userRegisterRequestDTOWithNullPassword() {
        return new UserRegisterRequestDTO()
                .toBuilder()
                .email("test@valid.com")
                .firstName("firstName")
                .lastName("lastName")
                .username("username")
                .build();
    }

    public static UserRegisterRequestDTO userRegisterRequestDTOWithNullEmail() {
        return new UserRegisterRequestDTO()
                .toBuilder()
                .firstName("firstName")
                .lastName("lastName")
                .username("username")
                .password("Password$!")
                .build();
    }

    public static UserRegisterRequestDTO userRegisterRequestDTOWithNullLastName() {
        return new UserRegisterRequestDTO()
                .toBuilder()
                .email("test@valid.com")
                .firstName("firstName")
                .username("username")
                .password("Password$!")
                .build();
    }

    public static UserLoginRequestDTO validUserLoginRequestDTO() {
        return new UserLoginRequestDTO()
                .toBuilder()
                .username("username")
                .password("Password$")
                .build();
    }

    public static UserLoginRequestDTO userLoginRequestDTOWithNullPassword() {
        return new UserLoginRequestDTO()
                .toBuilder()
                .username("username")
                .build();
    }
}
