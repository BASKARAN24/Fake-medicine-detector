package com.fakemed.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;

/**
 * Generates the "tamper-proof" identity for a medicine record.
 *
 * How it works:
 * 1. Every medicine gets a random UUID as its public verificationCode (this is
 *    what gets encoded into the QR image).
 * 2. We compute an HMAC-SHA256 hash over the medicine's core fields + a server
 *    secret key. This hash is stored in the DB (integrityHash), never in the QR.
 * 3. On scan, the backend recomputes the hash from the current DB row and
 *    compares it to the stored hash. If someone edits the DB directly (bypassing
 *    the app) or a counterfeiter fabricates a similar-looking QR/record, the
 *    hash won't match -> flagged as tampered/fake.
 *
 * NOTE: For a real production deployment, move `secretKey` out of source code
 * into an environment variable / secrets manager.
 */
@Service
public class IntegrityService {

    @Value("${app.integrity.secret:ChangeThisSecretKeyInProduction123!}")
    private String secretKey;

    public String generateVerificationCode() {
        return UUID.randomUUID().toString();
    }

    public String computeIntegrityHash(String batchNumber, String manufacturer,
                                        LocalDate manufactureDate, LocalDate expiryDate,
                                        String verificationCode) {
        String payload = String.join("|",
                batchNumber,
                manufacturer,
                manufactureDate.toString(),
                expiryDate.toString(),
                verificationCode);
        return hmacSha256(payload, secretKey);
    }

    private String hmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Could not compute integrity hash", e);
        }
    }
}
