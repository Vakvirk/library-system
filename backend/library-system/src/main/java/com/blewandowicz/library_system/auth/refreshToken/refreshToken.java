package com.blewandowicz.library_system.auth.refreshToken;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.cglib.core.Local;

import com.blewandowicz.library_system.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class refreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, unique = true, length = 500)
    private String token;
    @Column(nullable = false, name = "expiry_date")
    private LocalDateTime expiryDate;
    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Przy utworzeniu
    @PrePersist
    public void PrePersist() {
        createdAt = LocalDateTime.now();
    }

    // Utility klasy
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}
