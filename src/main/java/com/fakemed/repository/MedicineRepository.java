package com.fakemed.repository;

import com.fakemed.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    Optional<Medicine> findByVerificationCode(String verificationCode);
    Optional<Medicine> findByBatchNumber(String batchNumber);
    boolean existsByBatchNumber(String batchNumber);
}
