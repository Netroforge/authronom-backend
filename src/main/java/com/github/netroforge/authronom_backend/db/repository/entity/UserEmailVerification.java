package com.github.netroforge.authronom_backend.db.repository.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users_email_verifications")
public class UserEmailVerification implements Persistable<String> {
    @Id
    @Column(value = "uid")
    private String uid;

    @Column(value = "email")
    private String email;

    @Column("confirmation_code")
    private String confirmationCode;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    // Needed to have possibility to use save method of repository for new entities that have id set to not null
    // Inspired by https://github.com/spring-projects/spring-data-relational/issues/507
    @Transient
    @JsonIgnore
    private boolean isNew;

    @Override
    public String getId() {
        return uid;
    }
}
