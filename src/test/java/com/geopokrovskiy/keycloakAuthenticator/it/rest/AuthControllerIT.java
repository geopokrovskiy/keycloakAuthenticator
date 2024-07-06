package com.geopokrovskiy.keycloakAuthenticator.it.rest;

import com.geopokrovskiy.dto.auth.UserRegisterRequestDTO;
import com.geopokrovskiy.dto.auth.UserRegisterResponseDTO;
import com.geopokrovskiy.keycloakAuthenticator.config.KeycloakTestContainers;
import com.geopokrovskiy.keycloakAuthenticator.util.AuthUtils;
import com.geopokrovskiy.service.KeyCloakService;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.main.allow-bean-definition-overriding=true")
@Testcontainers
@AutoConfigureWebTestClient
@Import(KeycloakTestContainers.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class AuthControllerIT {
    @Autowired
    private KeycloakContainer keycloakContainer;
    @Autowired
    private WebTestClient webTestClient;

    @Test
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
}
