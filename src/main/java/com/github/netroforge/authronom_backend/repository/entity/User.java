package com.github.netroforge.authronom_backend.repository.entity;

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
import java.util.Set;

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

    @Column("first_name")
    private String firstName;

    @Column("last_name")
    private String lastName;

    // Additional personal information
    @Column("phone_number")
    private String phoneNumber;

    @Column("roles")
    private Set<String> roles;

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
