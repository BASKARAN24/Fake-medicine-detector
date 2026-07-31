-- Reference schema. Spring Boot (spring.jpa.hibernate.ddl-auto=update) will
-- create/update this table automatically on startup, but you can also run
-- this manually if you prefer explicit control.

CREATE DATABASE IF NOT EXISTS fake_medicine_db;
USE fake_medicine_db;

CREATE TABLE IF NOT EXISTS medicines (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicine_name     VARCHAR(255) NOT NULL,
    batch_number      VARCHAR(255) NOT NULL UNIQUE,
    manufacturer      VARCHAR(255) NOT NULL,
    manufacture_date  DATE NOT NULL,
    expiry_date       DATE NOT NULL,
    verification_code VARCHAR(128) NOT NULL UNIQUE,
    integrity_hash    VARCHAR(128) NOT NULL,
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    scan_count         INT DEFAULT 0
);

-- Optional: sample row for quick testing (manually generate a matching QR
-- via the app's /api/medicines endpoint instead of inserting rows by hand,
-- since verification_code and integrity_hash must be generated together).
