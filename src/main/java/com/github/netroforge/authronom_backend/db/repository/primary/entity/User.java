package com.github.netroforge.authronom_backend.db.repository.primary.entity;

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
@Table(name = "users")
public class User implements Persistable<String> {
    @Id
    @Column("uid")
    private String uid; // Firebase UID

    @Column("email")
    private String email;

    @Column("password")
    private String password;

    @Column("google_id")
    private String googleId;

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
