package org.magicalpanda.projectmanagementbackend.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class VerifyEmailRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "verification code is required")
    @Size(min = 6, max = 6, message = "Code must be exactly 6-character long")
    private String verificationCode;
}
