package com.geopokrovskiy.keycloakAuthenticator.unit;

import com.geopokrovskiy.dto.auth.UserRegisterResponseDTO;
import com.geopokrovskiy.dto.info.UserInfoResponseDTO;
import com.geopokrovskiy.dto.login.UserLoginResponseDTO;
import com.geopokrovskiy.keycloakAuthenticator.util.AuthUtils;
import com.geopokrovskiy.keycloakAuthenticator.util.TokenUtils;
import com.geopokrovskiy.service.KeyCloakService;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.admin.client.token.TokenManager;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.security.Principal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class KeycloakServiceTest {
    private final String INVALID_CREDENTIALS = "Invalid credentials";

    private final Keycloak keycloak = mock(Keycloak.class);
    private final UsersResource usersResource = mock(UsersResource.class);
    private final RealmResource realmResource = mock(RealmResource.class);
    @InjectMocks
    private final KeyCloakService keyCloakService = new KeyCloakService(keycloak);

    @Test
    public void testAddUser() {
        try {
            Mockito.when(keycloak.realm(anyString()))
                    .thenReturn(this.realmResource);

            Mockito.when(this.realmResource.users())
                    .thenReturn(usersResource);

            URI uri = new URI("mock.uri");
            Mockito.when(usersResource.create(any(UserRepresentation.class)))
                    .thenReturn(Response.created(uri).build());

            // Valid Request DTO
            Mono<UserRegisterResponseDTO> result = keyCloakService.addUser(AuthUtils.validUserRegisterRequestDTO());
            result.subscribe((created) -> {
                verify(usersResource, times(1)).create(any());
                verify(keycloak, times(1)).realm(anyString());
            });

            // Request DTO with null password
            result = keyCloakService.addUser(AuthUtils.userRegisterRequestDTOWithNullPassword());
            StepVerifier.create(result)
                    .expectErrorMatches(throwable -> throwable instanceof RuntimeException && throwable.getMessage().equals("Username or password is null"))
                    .verify();

            verify(usersResource, times(0)).create(any());
            verify(keycloak, times(0)).realm(anyString());

            // Request DTO with null email
            result = keyCloakService.addUser(AuthUtils.userRegisterRequestDTOWithNullEmail());
            StepVerifier.create(result)
                    .expectErrorMatches(throwable -> throwable instanceof RuntimeException && throwable.getMessage().equals("Email is null"))
                    .verify();

            verify(usersResource, times(0)).create(any());
            verify(keycloak, times(0)).realm(anyString());

            // Request DTO with null last name
            result = keyCloakService.addUser(AuthUtils.userRegisterRequestDTOWithNullLastName());
            StepVerifier.create(result)
                    .expectErrorMatches(throwable -> throwable instanceof RuntimeException && throwable.getMessage().equals("First name or last name is null"))
                    .verify();

            verify(usersResource, times(0)).create(any());
            verify(keycloak, times(0)).realm(anyString());


        } catch (Exception ignored) {

        }
    }

    @Test
    public void testGenerateToken() {
        try {
            Keycloak keycloakForAuthMock = mock(Keycloak.class);
            TokenManager tokenManagerMock = mock(TokenManager.class);
            AccessTokenResponse accessTokenResponse = mock(AccessTokenResponse.class);

            when(keyCloakService.keycloakForAuth(AuthUtils.validUserLoginRequestDTO())).thenReturn(keycloakForAuthMock);
            when(keycloakForAuthMock.tokenManager()).thenReturn(tokenManagerMock);
            when(tokenManagerMock.getAccessToken()).thenReturn(accessTokenResponse);

            // Valid request DTO
            Mono<UserLoginResponseDTO> result = keyCloakService.generateToken(AuthUtils.validUserLoginRequestDTO());
            result.subscribe(token -> {
                verify(keycloakForAuthMock, times(1)).tokenManager();
                verify(tokenManagerMock, times(1)).getAccessToken();
            });

            // Request DTO with null password
            result = keyCloakService.generateToken(AuthUtils.userLoginRequestDTOWithNullPassword());
            StepVerifier.create(result)
                    .expectErrorMatches(throwable -> throwable instanceof RuntimeException && throwable.getMessage().equals(INVALID_CREDENTIALS))
                    .verify();

        } catch (Exception ignored) {

        }
    }

    @Test
    public void testGetUserInfo() {
        try {
            Jwt jwt = mock(Jwt.class);
            when(jwt.getClaims()).thenReturn(TokenUtils.validClaims());

            JwtAuthenticationToken jwtAuthToken = mock(JwtAuthenticationToken.class);
            when(jwtAuthToken.getToken()).thenReturn(jwt);
            when(jwtAuthToken.getName()).thenReturn("username");

            Mono<Principal> principalMono = Mono.just(jwtAuthToken);

            // Call the method and verify the results
            Mono<UserInfoResponseDTO> result = keyCloakService.getUserInfo(principalMono);

            result.subscribe(info -> {
                verify(jwtAuthToken, times(1)).getToken();
                verify(jwt, times(1)).getClaims();

                assertEquals(info.getUsername(), "username");
                assertEquals(info.getEmail(), "test@valid.com");
                assertEquals(info.getFirstName(), "First");
                assertEquals(info.getLastName(), "Last");

            });
        } catch (Exception ignored) {

        }
    }
}


