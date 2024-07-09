package com.geopokrovskiy.service;

import com.geopokrovskiy.dto.auth.UserRegisterRequestDTO;
import com.geopokrovskiy.dto.auth.UserRegisterResponseDTO;
import com.geopokrovskiy.dto.info.UserInfoResponseDTO;
import com.geopokrovskiy.dto.login.UserLoginRequestDTO;
import com.geopokrovskiy.dto.login.UserLoginResponseDTO;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.Response;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@Data
public class KeyCloakService {
    private final Keycloak keycloak;
    private final String USER_CREATED = "Successfully created user";
    private final String INVALID_CREDENTIALS = "Invalid credentials";
    @Value("${keycloak.realm}")
    private String realm;
    @Value("${keycloak.server-url}")
    private String serverUrl;
    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private String clientSecret;

    public Keycloak keycloakForAuth(UserLoginRequestDTO request) {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType(OAuth2Constants.PASSWORD)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .username(request.getUsername())
                .password(request.getPassword())
                .build();
    }

    public Mono<UserRegisterResponseDTO> addUser(UserRegisterRequestDTO dto) {

        if (dto.getUsername() == null || dto.getPassword() == null) {
            return Mono.error(new RuntimeException("Username or password is null"));
        }

        if (dto.getFirstName() == null || dto.getLastName() == null) {
            return Mono.error(new RuntimeException("First name or last name is null"));
        }

        if (dto.getEmail() == null) {
            return Mono.error(new RuntimeException("Email is null"));
        }

        UserRepresentation user = new UserRepresentation();
        user.setUsername(dto.getUsername());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setEnabled(true);
        user.setEmailVerified(true);

        CredentialRepresentation credential = createPasswordCredentials(dto.getPassword());
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        user.setCredentials(Collections.singletonList(credential));

        UsersResource usersResource = getUsersResource();

        try (Response response = usersResource.create(user)) {
            if (response.getStatus() != 201) {
                return Mono.error(new RuntimeException("Failed to create user"));
            }
        } catch (Throwable throwable) {
            return Mono.error(new RuntimeException(throwable.getMessage()));
        }

        UserRegisterResponseDTO userRegisterResponseDTO = new UserRegisterResponseDTO();
        userRegisterResponseDTO.setEmail(dto.getEmail());
        userRegisterResponseDTO.setUsername(dto.getUsername());
        userRegisterResponseDTO.setMessage(USER_CREATED);
        return Mono.just(userRegisterResponseDTO);
    }

    public Mono<UserInfoResponseDTO> getUserInfo(Mono<Principal> principalMono) {
        return principalMono.map(principal -> {
            JwtAuthenticationToken jwtPrincipal = (JwtAuthenticationToken) principal;
            Jwt token = jwtPrincipal.getToken();
            Map<String, Object> claims = token.getClaims();

            return new UserInfoResponseDTO().toBuilder()
                    .username(principal.getName())
                    .email((String) claims.get("email"))
                    .firstName((String) claims.get("given_name"))
                    .lastName((String) claims.get("family_name"))
                    .build();
        });
    }

    public Mono<UserLoginResponseDTO> generateToken(UserLoginRequestDTO dto) {

        if (dto.getUsername() == null || dto.getPassword() == null) {
            return Mono.error(new RuntimeException(INVALID_CREDENTIALS));
        }

        try (Keycloak keycloak1 = keycloakForAuth(dto)) {
            AccessTokenResponse token = keycloak1.tokenManager().getAccessToken();

            UserLoginResponseDTO userLoginResponseDTO = new UserLoginResponseDTO();
            userLoginResponseDTO.setAccessToken(token.getToken());
            userLoginResponseDTO.setRefreshToken(token.getRefreshToken());
            userLoginResponseDTO.setExpiresIn(token.getExpiresIn());
            userLoginResponseDTO.setTokenType(token.getTokenType());

            return Mono.just(userLoginResponseDTO);

        } catch (NotAuthorizedException exception) {
            return Mono.error(new RuntimeException("Incorrect username or password"));
        } catch (Throwable throwable) {
            return Mono.error(new RuntimeException(throwable));
        }
    }


    private UsersResource getUsersResource() {
        RealmResource realm1 = keycloak.realm(realm);
        return realm1.users();
    }

    private static CredentialRepresentation createPasswordCredentials(String password) {
        CredentialRepresentation passwordCredentials = new CredentialRepresentation();
        passwordCredentials.setTemporary(false);
        passwordCredentials.setType(CredentialRepresentation.PASSWORD);
        passwordCredentials.setValue(password);
        return passwordCredentials;
    }
}
