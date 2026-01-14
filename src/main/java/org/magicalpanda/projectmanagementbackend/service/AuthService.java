package org.magicalpanda.projectmanagementbackend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.magicalpanda.projectmanagementbackend.dto.request.LoginRequest;
import org.magicalpanda.projectmanagementbackend.dto.request.RefreshTokenRequest;
import org.magicalpanda.projectmanagementbackend.dto.request.RegisterRequest;
import org.magicalpanda.projectmanagementbackend.dto.request.VerifyEmailRequest;
import org.magicalpanda.projectmanagementbackend.dto.response.LoginResponse;
import org.magicalpanda.projectmanagementbackend.exception.ResourceAlreadyExistsException;
import org.magicalpanda.projectmanagementbackend.exception.ResourceNotFoundException;
import org.magicalpanda.projectmanagementbackend.exception.VerificationCodeException;
import org.magicalpanda.projectmanagementbackend.model.RefreshToken;
import org.magicalpanda.projectmanagementbackend.model.User;
import org.magicalpanda.projectmanagementbackend.model.VerificationCode;
import org.magicalpanda.projectmanagementbackend.model.enumeration.Role;
import org.magicalpanda.projectmanagementbackend.model.enumeration.VerificationPurpose;
import org.magicalpanda.projectmanagementbackend.proxy.EmailProxy;
import org.magicalpanda.projectmanagementbackend.repository.RefreshTokenRepository;
import org.magicalpanda.projectmanagementbackend.repository.UserRepository;
import org.magicalpanda.projectmanagementbackend.repository.VerificationCodeRepository;
import org.magicalpanda.projectmanagementbackend.security.jwt.JwtService;
import org.magicalpanda.projectmanagementbackend.security.user.SecurityUser;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
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
        if (code.getExpiresAt().isBefore(Instant.now())) {
            throw VerificationCodeException.codeExpired();
        }

        // 5. Mark as used and verify user's email
        user.setEmailVerified(true);
        code.setUsed(true);

        userRepository.save(user);
        verificationCodeRepository.save(code);
    }

    public LoginResponse login(LoginRequest request) {

        // 1. Delegate authentication to Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // 2. Authentication succeeded -> extract principal
        SecurityUser principal = (SecurityUser) authentication.getPrincipal();

        // 3. Issue tokens (access and refresh JWTs)
        String accessToken = jwtService.generateAccessToken(principal);

        User user = userRepository.getReferenceById(principal.getId());
        String refreshToken = jwtService.generateRefreshToken(user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

    }

    public LoginResponse refresh(RefreshTokenRequest request) {

        String refreshToken = request.getRefreshToken();

        // 1. Validate refresh token (and retrieve claims if valid)
        Claims claims = jwtService.validateRefreshToken(refreshToken);

        String jti = (String) claims.get("jti");
        Long userId = Long.valueOf(claims.getSubject());

        // 2. Load refresh token from DB (by JTI)
        RefreshToken storedToken = refreshTokenRepository.findByJti(jti)
                .orElseThrow(() -> new JwtException("Refresh token not found"));

        // 3. Enforce server-side validity
        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            throw new JwtException("Refresh token expired");
        }

        if (storedToken.isRevoked()) {
            throw new JwtException("Refresh token revoked");
        }

        // 4. Rotate refresh token
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        User user = storedToken.getUser();
        SecurityUser securityUser = new SecurityUser(user);

        String newAccessToken = jwtService.generateAccessToken(securityUser);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();

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
                .expiresAt(Instant.now().plus(Duration.ofMinutes(15)))
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
