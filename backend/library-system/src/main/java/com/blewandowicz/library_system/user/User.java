package com.blewandowicz.library_system.user;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
})
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID id;
    @Column(name = "first_name", nullable = false, length = 100)
    private String name;
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    @Column(nullable = false, unique = true, length = 255)
    private String email;
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String role = "client";
    @Column(name = "is_active")
    @Builder.Default
    private boolean isActive = true;
    @Column(name = "is_student")
    @Builder.Default
    private boolean isStudent = false;
    @Column(name = "student_proof", length = 255)
    private String studentProof;
    @Column(name = "student_institiution", length = 255)
    private String studentInstitiution;
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Indicates whether the user account is enabled for authentication.
     *
     * @return true if the user's account is active (eligible for authentication); false otherwise
     */

    @Override
    public boolean isEnabled() {
        return isActive;
    }

    /**
     * Returns the authorities granted to the user.
     *
     * <p>The collection contains a single SimpleGrantedAuthority constructed from this user's role.
     *
     * @return an immutable collection with one authority representing the user's role
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    /**
     * Returns the stored password used for authentication.
     *
     * <p>Returns the password hash associated with this user as required by
     * Spring Security's {@code UserDetails}.</p>
     *
     * @return the user's password hash
     */
    @Override
    public String getPassword() {
        return passwordHash;
    }

    /**
     * Returns the principal username for authentication.
     *
     * <p>Uses the user's email as the username (principal) for Spring Security.
     *
     * @return the user's email used as the username/principal
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * JPA lifecycle callback invoked before the entity is persisted.
     *
     * Sets both {@code createdAt} and {@code updatedAt} to the current local date‑time
     * so new entities have creation and last-modified timestamps.
     */
    @PrePersist
    public void PrePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * JPA lifecycle callback invoked before the entity is updated; sets {@code updatedAt}
     * to the current local date-time.
     */
    @PreUpdate
    public void PreUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
