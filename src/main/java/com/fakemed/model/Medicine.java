package com.fakemed.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "medicines")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String medicineName;

    @Column(nullable = false, unique = true)
    private String batchNumber;

    @Column(nullable = false)
    private String manufacturer;

    @Column(nullable = false)
    private LocalDate manufactureDate;

    @Column(nullable = false)
    private LocalDate expiryDate;

    /**
     * A unique, unguessable verification code (UUID + HMAC hash) embedded in the
     * QR code. This is what makes the record "tamper proof" — the code is checked
     * against the DB record's stored hash on every scan, so a copied/altered QR
     * won't match.
     */
    @Column(nullable = false, unique = true, length = 128)
    private String verificationCode;

    @Column(nullable = false, length = 128)
    private String integrityHash; // HMAC-SHA256 of key fields, used to detect tampering

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private Integer scanCount = 0;
}
