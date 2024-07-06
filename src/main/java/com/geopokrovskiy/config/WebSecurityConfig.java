package com.geopokrovskiy.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtGrantedAuthoritiesConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Configuration
@EnableReactiveMethodSecurity
public class WebSecurityConfig {

    private final String[] publicRoutes = {"/api/v1/auth/**"};

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf().disable()
                .anonymous().disable()
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(publicRoutes).permitAll()
                        .anyExchange()
                        .authenticated()
                )
                .oauth2ResourceServer(customizer -> customizer.jwt(jwt -> {
                    ReactiveJwtAuthenticationConverter jwtAuthenticationConverter = new ReactiveJwtAuthenticationConverter();
                    jwtAuthenticationConverter.setPrincipalClaimName("preferred_username");
                    JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

                    // Custom converter for the nested roles
                    Converter<Jwt, Collection<GrantedAuthority>> customConverter = jwt2 -> {
                        Map<String, Object> realmAccess = (Map<String, Object>) jwt2.getClaims().get("realm_access");
                        if (realmAccess == null || realmAccess.isEmpty()) {
                            return new ArrayList<GrantedAuthority>();
                        }
                        Collection<String> roles = (Collection<String>) realmAccess.get("roles");
                        return roles.stream()
                                .map(roleName -> "ROLE_" + roleName) // Prefixing role name with "ROLE_"
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());
                    };

                    // Combine the default and custom converters
                    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
                            new ReactiveJwtGrantedAuthoritiesConverterAdapter(jwt3 -> {
                                Stream<GrantedAuthority> defaultAuthorities = jwtGrantedAuthoritiesConverter.convert(jwt3).stream();
                                Stream<GrantedAuthority> customAuthorities = customConverter.convert(jwt3).stream();
                                return Stream.concat(defaultAuthorities, customAuthorities).collect(Collectors.toList());
                            })
                    );

                    // Set the custom JWT authentication converter
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter);


                }))
                .exceptionHandling(exceptionHandlingConfigurer -> exceptionHandlingConfigurer
                        .authenticationEntryPoint((swe, e) -> {
                            log.error("IN securityWebFilterChain - unauthorized error: {}", e.getMessage());
                            return Mono.fromRunnable(() -> swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED));
                        })
                        .accessDeniedHandler((swe, e) -> {
                            log.error("IN securityWebFilterChain - access denied: {}", e.getMessage());

                            return Mono.fromRunnable(() -> swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN));
                        }))
                .build();
    }

    @Bean
    public ReactiveOAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        OidcReactiveOAuth2UserService oidcUserService = new OidcReactiveOAuth2UserService();
        return userRequest -> oidcUserService.loadUser(userRequest)
                .map(oidcUser -> {
                    List<GrantedAuthority> grantedAuthorities = Stream.concat(oidcUser.getAuthorities().stream(),
                                    oidcUser.getClaimAsStringList("roles").stream()
                                            .filter(authority -> authority.startsWith("ROLE_"))
                                            .map(SimpleGrantedAuthority::new))
                            .toList();
                    return new DefaultOidcUser(grantedAuthorities, oidcUser.getIdToken(), oidcUser.getUserInfo(),
                            "preferred_username");
                });
    }

}
