package com.geopokrovskiy.keycloakAuthenticator.config;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.apache.commons.compress.archivers.sevenz.CLI;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class KeycloakTestContainers {

    private static final String CLIENT_ID = "app-auth-client-id";
    private static final String CLIENT_SECRET = "FuXDordwvRlgaCasQ1xsqi6s9pkAuQPI";
    private static final String REALM = "appauth";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "adminpass";
    static final KeycloakContainer keycloak;

    static {
        keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:24.0.4")
                .withRealmImportFile("realm-export-test.json")
                .withEnv("DB_VENDOR", "h2")
                .withEnv("DB_URL", "jdbc:h2:mem:testdb")
                .withEnv("DB_USER", "sa")
                .withEnv("DB_PASSWORD", "");
        keycloak.start();
    }

    @DynamicPropertySource
    static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.client.registration.keycloak.client-id", () -> CLIENT_ID);
        registry.add("spring.security.oauth2.client.registration.keycloak.client-secret", () -> CLIENT_SECRET);
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> keycloak.getAuthServerUrl() + "/realms/appauth");
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () -> keycloak.getAuthServerUrl() + "/realms/appauth/protocol/openid-connect/certs");
        registry.add("spring.security.oauth2.client.provider.keycloak.issuer-uri", () -> keycloak.getAuthServerUrl() + "/realms/appauth");
        registry.add("keycloak-service.client.base-admin-url", () -> keycloak.getAuthServerUrl() + "/admin/realms/appauth");
        registry.add("keycloak.clientId", () -> CLIENT_ID);
        registry.add("keycloak.username", () -> USERNAME);
        registry.add("keycloak.password", () -> PASSWORD);
        registry.add("keycloak.clientSecret", () -> CLIENT_SECRET);
        registry.add("keycloak.server-url", () -> keycloak.getAuthServerUrl());
    }

    @Bean
    @Primary
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloak.getAuthServerUrl())
                .realm(REALM)
                .username(USERNAME)
                .password(PASSWORD)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .build();
    }

}
