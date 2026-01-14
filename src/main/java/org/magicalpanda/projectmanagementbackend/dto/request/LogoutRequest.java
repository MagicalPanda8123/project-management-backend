package org.magicalpanda.projectmanagementbackend.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
public class LogoutRequest {
    private String refreshToken;
}
