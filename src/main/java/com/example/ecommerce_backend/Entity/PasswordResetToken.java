package com.example.ecommerce_backend.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
@Data
@NoArgsConstructor
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    public PasswordResetToken(String token, User user) {
        this.token = token;
        this.user = user;
        //Ma token co thoi gian hieu luc 15 phut
        this.expiryDate = LocalDateTime.now().plusMinutes(15);
    }
    //kiem tra token da bi qua han hay chua
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }
}
