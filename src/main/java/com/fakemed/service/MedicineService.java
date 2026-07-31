package com.fakemed.service;

import com.fakemed.dto.MedicineRequest;
import com.fakemed.dto.VerificationResponse;
import com.fakemed.model.Medicine;
import com.fakemed.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final IntegrityService integrityService;

    public Medicine registerMedicine(MedicineRequest request) {
        if (medicineRepository.existsByBatchNumber(request.getBatchNumber())) {
            throw new IllegalArgumentException("A medicine with this batch number already exists");
        }

        String verificationCode = integrityService.generateVerificationCode();
        String integrityHash = integrityService.computeIntegrityHash(
                request.getBatchNumber(),
                request.getManufacturer(),
                request.getManufactureDate(),
                request.getExpiryDate(),
                verificationCode);

        Medicine medicine = new Medicine();
        medicine.setMedicineName(request.getMedicineName());
        medicine.setBatchNumber(request.getBatchNumber());
        medicine.setManufacturer(request.getManufacturer());
        medicine.setManufactureDate(request.getManufactureDate());
        medicine.setExpiryDate(request.getExpiryDate());
        medicine.setVerificationCode(verificationCode);
        medicine.setIntegrityHash(integrityHash);
        medicine.setScanCount(0);

        return medicineRepository.save(medicine);
    }

    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }

    /**
     * Core verification flow used whenever a QR is scanned.
     *
     *  - No matching record found          -> FAKE_OR_NOT_FOUND
     *  - Record found but hash mismatches  -> TAMPERED  (data was altered/corrupted)
     *  - Record found, hash OK, expired    -> EXPIRED
     *  - Record found, hash OK, valid date -> GENUINE
     */
    public VerificationResponse verify(String scannedCode) {
        if (scannedCode == null || scannedCode.isBlank()) {
            return new VerificationResponse("FAKE_OR_NOT_FOUND",
                    "No QR data received. This product cannot be verified.",
                    null, null, null, null, null, null);
        }

        return medicineRepository.findByVerificationCode(scannedCode.trim())
                .map(this::buildResponseForExistingRecord)
                .orElseGet(() -> new VerificationResponse("FAKE_OR_NOT_FOUND",
                        "⚠ This QR code does not match any registered medicine. " +
                                "It is likely FAKE or COUNTERFEIT. Do not consume — report it.",
                        null, null, null, null, null, null));
    }

    private VerificationResponse buildResponseForExistingRecord(Medicine medicine) {
        String recomputedHash = integrityService.computeIntegrityHash(
                medicine.getBatchNumber(),
                medicine.getManufacturer(),
                medicine.getManufactureDate(),
                medicine.getExpiryDate(),
                medicine.getVerificationCode());

        medicine.setScanCount(medicine.getScanCount() == null ? 1 : medicine.getScanCount() + 1);
        medicineRepository.save(medicine);

        if (!Objects.equals(recomputedHash, medicine.getIntegrityHash())) {
            return new VerificationResponse("TAMPERED",
                    "⚠ This product's record appears to have been altered. " +
                            "The data does not match manufacturer records — treat as SUSPICIOUS.",
                    medicine.getMedicineName(), medicine.getBatchNumber(), medicine.getManufacturer(),
                    medicine.getManufactureDate(), medicine.getExpiryDate(), medicine.getScanCount());
        }

        if (medicine.getExpiryDate().isBefore(LocalDate.now())) {
            return new VerificationResponse("EXPIRED",
                    "This medicine is GENUINE but has EXPIRED on " + medicine.getExpiryDate() +
                            ". Do not use.",
                    medicine.getMedicineName(), medicine.getBatchNumber(), medicine.getManufacturer(),
                    medicine.getManufactureDate(), medicine.getExpiryDate(), medicine.getScanCount());
        }

        return new VerificationResponse("GENUINE",
                "✅ This medicine is verified GENUINE and safe to use.",
                medicine.getMedicineName(), medicine.getBatchNumber(), medicine.getManufacturer(),
                medicine.getManufactureDate(), medicine.getExpiryDate(), medicine.getScanCount());
    }
}
