package com.geopokrovskiy.controller;

import com.geopokrovskiy.dto.auth.UserRegisterRequestDTO;
import com.geopokrovskiy.dto.auth.UserRegisterResponseDTO;
import com.geopokrovskiy.dto.info.UserInfoResponseDTO;
import com.geopokrovskiy.dto.login.UserLoginRequestDTO;
import com.geopokrovskiy.dto.login.UserLoginResponseDTO;
import com.geopokrovskiy.service.KeyCloakService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final KeyCloakService keyCloakService;

    @PostMapping("/registration")
    public Mono<UserRegisterResponseDTO> register(@RequestBody UserRegisterRequestDTO userRequestDTO) {
        return keyCloakService.addUser(userRequestDTO);
    }

    @PostMapping("/login")
    public Mono<UserLoginResponseDTO> login(@RequestBody UserLoginRequestDTO userRequestDTO) {
        return keyCloakService.generateToken(userRequestDTO);
    }

    @GetMapping("/info/me")
    public Mono<UserInfoResponseDTO> getInfo(Mono<Principal> principalMono) {
        return keyCloakService.getUserInfo(principalMono);
    }
}
