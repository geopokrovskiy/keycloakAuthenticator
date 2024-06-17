package com.geopokrovskiy.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class UserRequestDTO {
    private String username;
    private String password;
}
