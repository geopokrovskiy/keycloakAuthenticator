package com.geopokrovskiy.controller;

import com.geopokrovskiy.dto.UserRequestDTO;
import com.geopokrovskiy.dto.UserResponseDTO;
import com.geopokrovskiy.service.KeyCloakService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final KeyCloakService keyCloakService;

    @PostMapping("/registration")
    public Mono<UserResponseDTO> register(@RequestBody UserRequestDTO userRequestDTO) {
        return keyCloakService.addUser(userRequestDTO);
    }
}
