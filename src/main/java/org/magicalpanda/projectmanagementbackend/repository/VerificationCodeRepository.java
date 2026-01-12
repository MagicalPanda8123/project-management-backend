package org.magicalpanda.projectmanagementbackend.repository;

import org.magicalpanda.projectmanagementbackend.model.User;
import org.magicalpanda.projectmanagementbackend.model.VerificationCode;
import org.magicalpanda.projectmanagementbackend.model.enumeration.VerificationPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    /**
     * Finds the latest unused verification code for a given user and purpose.
     *
     * @param user the user who owns the verification code
     * @param purpose the purpose of the verification code (e.g., EMAIL)
     * @return an Optional containing the latest unused VerificationCode, or empty if none found
     */
    Optional<VerificationCode> findTopByUserAndPurposeAndIsUsedFalseOrderByCreatedAtDesc(
            User user,
            VerificationPurpose purpose
    );

}
