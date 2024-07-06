package com.geopokrovskiy.keycloakAuthenticator.util;

import java.util.Map;

public class TokenUtils {
    public static Map validClaims() {
        return Map.of("email", "test@valid.com", "first_name", "First", "last_name", "Last");
    }
}
