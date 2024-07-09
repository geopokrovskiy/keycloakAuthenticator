package com.geopokrovskiy.keycloakAuthenticator.it.rest;

import com.geopokrovskiy.dto.auth.UserRegisterRequestDTO;
import com.geopokrovskiy.dto.auth.UserRegisterResponseDTO;
import com.geopokrovskiy.dto.info.UserInfoResponseDTO;
import com.geopokrovskiy.dto.login.UserLoginRequestDTO;
import com.geopokrovskiy.dto.login.UserLoginResponseDTO;
import com.geopokrovskiy.keycloakAuthenticator.config.KeycloakTestContainers;
import com.geopokrovskiy.keycloakAuthenticator.util.AuthUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.main.allow-bean-definition-overriding=true")
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(locations = "classpath:application-test.yaml")
public class AuthControllerIT extends KeycloakTestContainers {
    @Autowired
    private WebTestClient webTestClient;


    @Test
    @Order(0)
    @DisplayName("Load context")
    void loadContext() {
    }

    @Test
    @Order(1)
    @DisplayName("Test user registration with a valid request DTO")
    public void test_registerUser_correctDTO() {
        // Given
        UserRegisterRequestDTO userRegisterRequestDTO = AuthUtils.validUserRegisterRequestDTO();
        UserRegisterResponseDTO userRegisterResponseDTO = AuthUtils.validUserRegisterResponseDTO();

        // When
        webTestClient.post().uri("/api/v1/auth/registration")
                .bodyValue(userRegisterRequestDTO)
                .exchange()

                // Then
                .expectStatus().isOk()
                .expectBody(UserRegisterResponseDTO.class)
                .value(response -> {
                    assertNotNull(response);

                    assertEquals(userRegisterResponseDTO.getMessage(), response.getMessage());
                    assertEquals(userRegisterResponseDTO.getUsername(), response.getUsername());
                    assertEquals(userRegisterResponseDTO.getEmail(), response.getEmail());
                });
    }

    @Test
    @Order(2)
    @DisplayName("Test user registration with null password")
    public void test_registerUser_incorrectDTO_nullPassword() {
        // Given
        UserRegisterRequestDTO userRegisterRequestDTO = AuthUtils.userRegisterRequestDTOWithNullPassword();

        // When
        webTestClient.post().uri("/api/v1/auth/registration")
                .bodyValue(userRegisterRequestDTO)
                .exchange()

                // Then
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .isEqualTo("Username or password is null");

    }

    @Test
    @Order(3)
    @DisplayName("Test user registration with null last name")
    public void test_registerUser_incorrectDTO_nullLastName() {
        // Given
        UserRegisterRequestDTO userRegisterRequestDTO = AuthUtils.userRegisterRequestDTOWithNullLastName();

        // When
        webTestClient.post().uri("/api/v1/auth/registration")
                .bodyValue(userRegisterRequestDTO)
                .exchange()

                // Then
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .isEqualTo("First name or last name is null");

    }

    @Test
    @Order(4)
    @DisplayName("Test user registration with null email")
    public void test_registerUser_incorrectDTO_nullEmail() {
        // Given
        UserRegisterRequestDTO userRegisterRequestDTO = AuthUtils.userRegisterRequestDTOWithNullEmail();

        // When
        webTestClient.post().uri("/api/v1/auth/registration")
                .bodyValue(userRegisterRequestDTO)
                .exchange()

                // Then
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .isEqualTo("Email is null");

    }

    @Test
    @Order(5)
    @DisplayName("Test user login with invalid login DTO")
    public void test_login_invalidDTO() {
        // Given
        UserLoginRequestDTO userRegisterLoginDTO = AuthUtils.userLoginRequestDTOWithNullPassword();

        // When
        webTestClient.post().uri("/api/v1/auth/login")
                .bodyValue(userRegisterLoginDTO)
                .exchange()

                // Then
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .isEqualTo("Invalid credentials");

    }

    @Test
    @Order(6)
    @DisplayName("Test user login with login DTO having an incorrect password")
    public void test_login_DTO_with_incorrectPassword() {
        // Given
        UserLoginRequestDTO userRegisterLoginDTO = AuthUtils.userLoginRequestDTOWithIncorrectPassword();

        // When
        webTestClient.post().uri("/api/v1/auth/login")
                .bodyValue(userRegisterLoginDTO)
                .exchange()

                // Then
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .isEqualTo("Incorrect username or password");

    }

    @Test
    @Order(7)
    @DisplayName("Test user login with valid login DTO")
    public void test_login_validDTO() {
        // Given
        UserLoginRequestDTO userLoginRequestDTO = AuthUtils.validUserLoginRequestDTO();

        // When
        webTestClient.post().uri("/api/v1/auth/login")
                .bodyValue(userLoginRequestDTO)
                .exchange()

                // Then
                .expectStatus().isOk()
                .expectBody(UserLoginResponseDTO.class)
                .value(response -> {

                    assertNotNull(response.getAccessToken());
                    assertNotNull(response.getExpiresIn());
                    assertNotNull(response.getTokenType());
                    assertNotNull(response.getRefreshToken());

                    System.setProperty("access_token", response.getAccessToken());
                });



    }

    @Test
    @Order(8)
    @DisplayName("Test user info")
    public void test_userInfo() {
        // Given
        UserInfoResponseDTO expectedUserInfoResponseDTO = AuthUtils.userInfoResponseDTO();

        // When
        webTestClient.get().uri("/api/v1/auth/info/me")
                .headers(http -> http.setBearerAuth(System.getProperty("access_token")))
                .exchange()

                // Then
                .expectStatus().isOk()
                .expectBody(UserInfoResponseDTO.class)
                .value(response -> {

                    assertNotNull(response);

                    assertEquals(expectedUserInfoResponseDTO.getFirstName(), response.getFirstName());
                    assertEquals(expectedUserInfoResponseDTO.getLastName(), response.getLastName());
                    assertEquals(expectedUserInfoResponseDTO.getEmail(), response.getEmail());
                    assertEquals(expectedUserInfoResponseDTO.getUsername(), response.getUsername());
                });

    }


}
