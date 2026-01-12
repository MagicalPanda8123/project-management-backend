package org.magicalpanda.projectmanagementbackend.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.magicalpanda.projectmanagementbackend.model.User;

@Getter
@Builder
public class RegisterResponse {

    private final Long id;
    private final String email;
    private final String username;
    private final String firstName;
    private final String lastName;
    private final boolean emailVerified;

    public static RegisterResponse from(User user) {
        return RegisterResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .emailVerified(user.isEmailVerified())
                .build();
    }
}