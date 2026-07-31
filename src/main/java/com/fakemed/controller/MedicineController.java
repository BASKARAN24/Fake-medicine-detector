package com.fakemed.controller;

import com.fakemed.dto.MedicineRequest;
import com.fakemed.dto.VerificationResponse;
import com.fakemed.model.Medicine;
import com.fakemed.service.MedicineService;
import com.fakemed.service.QRCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medicines")
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineService medicineService;
    private final QRCodeService qrCodeService;

    // ---- Admin / Manufacturer side: register a new medicine batch ----
    @PostMapping
    public ResponseEntity<?> registerMedicine(@Valid @RequestBody MedicineRequest request) {
        try {
            Medicine saved = medicineService.registerMedicine(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public List<Medicine> getAllMedicines() {
        return medicineService.getAllMedicines();
    }

    // ---- Returns a scannable QR PNG image for a given medicine ----
    @GetMapping(value = "/{id}/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQrCode(@PathVariable Long id) {
        Medicine medicine = medicineService.getAllMedicines().stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (medicine == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            byte[] png = qrCodeService.generateQRCodePng(medicine.getVerificationCode());
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(png);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ---- Public side: verify a scanned QR code ----
    @GetMapping("/verify/{code}")
    public ResponseEntity<VerificationResponse> verify(@PathVariable String code) {
        VerificationResponse response = medicineService.verify(code);
        return ResponseEntity.ok(response);
    }

    // Some QR scanner libraries hand back full raw text via POST; support that too
    @PostMapping("/verify")
    public ResponseEntity<VerificationResponse> verifyPost(@RequestBody Map<String, String> body) {
        VerificationResponse response = medicineService.verify(body.get("code"));
        return ResponseEntity.ok(response);
    }
}
