package org.magicalpanda.projectmanagementbackend.service;

import lombok.RequiredArgsConstructor;
import org.magicalpanda.projectmanagementbackend.dto.request.RegisterRequest;
import org.magicalpanda.projectmanagementbackend.dto.request.VerifyEmailRequest;
import org.magicalpanda.projectmanagementbackend.exception.ResourceAlreadyExistsException;
import org.magicalpanda.projectmanagementbackend.exception.ResourceNotFoundException;
import org.magicalpanda.projectmanagementbackend.exception.VerificationCodeException;
import org.magicalpanda.projectmanagementbackend.model.User;
import org.magicalpanda.projectmanagementbackend.model.VerificationCode;
import org.magicalpanda.projectmanagementbackend.model.enumeration.Role;
import org.magicalpanda.projectmanagementbackend.model.enumeration.VerificationPurpose;
import org.magicalpanda.projectmanagementbackend.proxy.EmailProxy;
import org.magicalpanda.projectmanagementbackend.repository.UserRepository;
import org.magicalpanda.projectmanagementbackend.repository.VerificationCodeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailProxy emailProxy;

    public User register(RegisterRequest request){

        // 1. Enforce uniqueness
        if (userRepository.existsByEmail(request.getEmail())){
            throw new ResourceAlreadyExistsException(
                    "User",
                    "email",
                    request.getEmail()
            );
        }

        if (userRepository.existsByUsername(request.getUsername())){
            throw new ResourceAlreadyExistsException(
                    "User",
                    "username",
                    request.getUsername()
            );
        }

        // 2. Create user
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .isEmailVerified(false)
                .role(Role.USER)
                .build();

        userRepository.save(user);

        // 3. Generate email verification code
        VerificationCode verificationCode = createEmailVerificationCode(user);

        verificationCodeRepository.save(verificationCode);

        // 4. Send verification code via email
        sendVerificationEmail(user, verificationCode.getCode());

        return user;
    }

    public void verifyEmail(VerifyEmailRequest request) {

        // 1. Find user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User",
                        request.getEmail()
                ));

        // 2. Find latest unused verification code for email verification
        VerificationCode code = verificationCodeRepository.findTopByUserAndPurposeAndIsUsedFalseOrderByCreatedAtDesc(
                user, VerificationPurpose.EMAIL
        ).orElseThrow(() -> new ResourceNotFoundException("No valid email verification code found"));

        // 3. Check if code matches
        if (!code.getCode().equals(request.getVerificationCode())) {
            throw VerificationCodeException.codeMismatch();
        }

        // 4. Check if code has expired
        if (code.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw VerificationCodeException.codeExpired();
        }

        // 5. Mark as used and verify user's email
        user.setEmailVerified(true);
        code.setUsed(true);

        userRepository.save(user);
        verificationCodeRepository.save(code);
    }

    /**
     * Generates a random 6-digit numeric code.
     */
    private String generateCode() {
        int value = 100000 + new Random().nextInt(900000);
        return String.valueOf(value);
    }

    /**
     * Creates a time-bound (15min) verification code for email verification.
     */
    private VerificationCode createEmailVerificationCode(User user) {

        String code = generateCode();

        return VerificationCode.builder()
                .code(code)
                .purpose(VerificationPurpose.EMAIL)
                .user(user)
                .isUsed(false)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();
    }

    private void sendVerificationEmail(User user, String code) {

        String subject = "Verify your email";
        String body = """
            Hi %s,

            Welcome aboard.

            Your email verification code is:

            %s

            This code expires in 15 minutes.

            — Project Management Team
            """.formatted(user.getFirstName(), code);

        emailProxy.sendEmail(user.getEmail(), subject, body);
    }


}
