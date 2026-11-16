CREATE DATABASE IF NOT EXISTS hospital_db;
USE hospital_db;

CREATE TABLE IF NOT EXISTS patients (
    patient_id     INT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    age            INT          NOT NULL,
    gender         VARCHAR(30)  NOT NULL,
    phone          VARCHAR(20)  NOT NULL,
    diagnosis      VARCHAR(255) NOT NULL,
    admission_date DATE         NOT NULL,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

SELECT * FROM patients;

-- sample rows
INSERT INTO patients (name, age, gender, phone, diagnosis, admission_date) VALUES
('Jane Doe',   34, 'Female', '416-555-0101', 'Seasonal flu',    '2026-11-10'),
('John Smith', 52, 'Male',   '416-555-0102', 'Routine checkup', '2026-11-12');
