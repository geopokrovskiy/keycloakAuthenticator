package com.geopokrovskiy.keycloakAuthenticator.config;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class KeycloakTestContainers {

    @Bean
    public KeycloakContainer keycloakContainer() {
        KeycloakContainer keycloakContainer = new KeycloakContainer("quay.io/keycloak/keycloak:24.0.4")
                .withRealmImportFile("realm-export-test.json")
                .withEnv("DB_VENDOR", "h2")
                .withEnv("DB_URL", "jdbc:h2:mem:testdb")
                .withEnv("DB_USER", "sa")
                .withEnv("DB_PASSWORD", "");
        keycloakContainer.start();
        return keycloakContainer;
    }

}
