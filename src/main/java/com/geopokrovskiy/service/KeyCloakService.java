package com.geopokrovskiy.service;

import com.geopokrovskiy.dto.UserRequestDTO;
import com.geopokrovskiy.dto.UserResponseDTO;
import jakarta.ws.rs.core.Response;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Data
public class KeyCloakService {
    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    public Mono<UserResponseDTO> addUser(UserRequestDTO dto) {
        String username = dto.getUsername();
        CredentialRepresentation credential = createPasswordCredentials(dto.getPassword());
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setCredentials(Collections.singletonList(credential));
        user.setEnabled(true);

        UsersResource usersResource = getUsersResource();
        Response response = usersResource.create(user);

        if (response.getStatus() != 201) {
            return Mono.error(new RuntimeException("Failed to create user"));
        }

        return generateToken(dto);
    }

    private Mono<UserResponseDTO> generateToken(UserRequestDTO dto) {
        String username = dto.getUsername();
        String password = dto.getPassword();

        return Mono.fromCallable(() -> {
            AccessTokenResponse tokenResponse = keycloak.tokenManager().grantToken();
            UserResponseDTO userResponseDTO = new UserResponseDTO();
            userResponseDTO.setAccessToken(tokenResponse.getToken());
            userResponseDTO.setExpiresIn(tokenResponse.getExpiresIn());
            userResponseDTO.setRefreshToken(tokenResponse.getRefreshToken());
            userResponseDTO.setTokenType(tokenResponse.getTokenType());
            return userResponseDTO;
        });
    }

    private UsersResource getUsersResource() {
        return keycloak.realm(realm).users();
    }

    private static CredentialRepresentation createPasswordCredentials(String password) {
        CredentialRepresentation passwordCredentials = new CredentialRepresentation();
        passwordCredentials.setTemporary(false);
        passwordCredentials.setType(CredentialRepresentation.PASSWORD);
        passwordCredentials.setValue(password);
        return passwordCredentials;
    }
}
