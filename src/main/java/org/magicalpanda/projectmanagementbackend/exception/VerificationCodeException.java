package org.magicalpanda.projectmanagementbackend.exception;

public class VerificationCodeException extends RuntimeException {
    public VerificationCodeException(String message) {
        super(message);
    }

    public static VerificationCodeException codeMismatch() {
        return new VerificationCodeException("Verification code is invalid");
    }

    public static VerificationCodeException codeExpired() {
        return new VerificationCodeException("Verification code has expired");
    }
}
