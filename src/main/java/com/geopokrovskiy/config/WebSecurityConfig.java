package com.geopokrovskiy.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Slf4j
@Configuration
@EnableReactiveMethodSecurity
public class WebSecurityConfig {

    private final String[] publicRoutes = {"/api/v1/auth/**"};

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf().disable()
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(publicRoutes).permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2Login(withDefaults());
        return http.build();
    }

}
