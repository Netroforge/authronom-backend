package com.github.netroforge.authronom_backend.repository.db;

import com.github.netroforge.authronom_backend.repository.db.entity.UserEmailVerification;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface UserEmailVerificationRepository extends CrudRepository<UserEmailVerification, String> {
    UserEmailVerification findByEmailAndConfirmationCode(String email, String confirmationCode);

    @Modifying
    @Query("DELETE FROM users_email_verifications t WHERE t.email = :email AND t.confirmation_code = :confirmationCode")
    void deleteByEmailAndConfirmationCode(@Param("email") String email, @Param("confirmationCode") String confirmationCode);

    @Modifying
    @Query("DELETE FROM users_email_verifications t WHERE t.created_at < :createdAtThreshold")
    void deleteAllOldRecords(@Param("createdAtThreshold") LocalDateTime localDateTime);
}

