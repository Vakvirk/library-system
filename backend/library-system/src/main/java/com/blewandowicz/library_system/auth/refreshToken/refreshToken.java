package com.blewandowicz.library_system.auth.refreshToken;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.blewandowicz.library_system.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, unique = true, length = 500)
    private String token;
    @Column(nullable = false, name = "expiry_date")
    private LocalDateTime expiryDate;
    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    /**
     * JPA lifecycle callback invoked before the entity is persisted.
     *
     * Sets the createdAt timestamp to the current system date-time if not already set.
     */
    @PrePersist
    public void PrePersist() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Returns whether this refresh token is expired.
     *
     * Compares the current system time to the token's {@code expiryDate}.
     *
     * @return {@code true} if the current time is after {@code expiryDate}; {@code false} otherwise
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}
