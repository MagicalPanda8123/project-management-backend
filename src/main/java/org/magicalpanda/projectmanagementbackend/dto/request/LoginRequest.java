package org.magicalpanda.projectmanagementbackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LoginRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
