package com.geopokrovskiy.dto.info;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserInfoResponseDTO {
    private String username;
    private String firstName;
    private String lastName;
    private String email;

}
