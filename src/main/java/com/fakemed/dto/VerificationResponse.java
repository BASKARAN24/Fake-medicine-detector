package com.fakemed.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationResponse {

    // one of: GENUINE, EXPIRED, TAMPERED, FAKE_OR_NOT_FOUND
    private String status;

    private String message;

    private String medicineName;
    private String batchNumber;
    private String manufacturer;
    private LocalDate manufactureDate;
    private LocalDate expiryDate;
    private Integer scanCount;
}
