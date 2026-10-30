-- =============================================================================
-- Migration: V1__init_schema.sql
-- Description: Initial schema for Hostel Management System
-- =============================================================================

-- 1. HOSTELS (Organizations / Branches)
CREATE TABLE IF NOT EXISTS hostels (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    contact_email VARCHAR(100) NULL,
    contact_phone VARCHAR(20) NULL,
    address TEXT NULL,
    city VARCHAR(100) NULL,
    state VARCHAR(100) NULL,
    postal_code VARCHAR(20) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. USERS (Super Admin, Hostel Admin, Warden, Staff, Student)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hostel_id BIGINT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone_number VARCHAR(20) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    must_change_password BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_hostel FOREIGN KEY (hostel_id) REFERENCES hostels(id) ON DELETE CASCADE,
    INDEX idx_users_hostel_role (hostel_id, role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. STUDENT PROFILES (Extended details for resident students)
CREATE TABLE IF NOT EXISTS student_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    hostel_id BIGINT NOT NULL,
    admission_number VARCHAR(50) NOT NULL,
    college_name VARCHAR(150) NULL,
    department VARCHAR(100) NULL,
    year_of_study INT NULL,
    guardian_name VARCHAR(150) NULL,
    guardian_phone VARCHAR(20) NULL,
    guardian_relation VARCHAR(50) NULL,
    emergency_contact VARCHAR(20) NULL,
    permanent_address TEXT NULL,
    blood_group VARCHAR(10) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_students_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_students_hostel FOREIGN KEY (hostel_id) REFERENCES hostels(id) ON DELETE CASCADE,
    UNIQUE KEY uk_student_hostel_admission (hostel_id, admission_number),
    INDEX idx_students_hostel (hostel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. HOSTEL BLOCKS (Wings / Buildings)
CREATE TABLE IF NOT EXISTS hostel_blocks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hostel_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    gender_type VARCHAR(20) NOT NULL DEFAULT 'COED',
    total_floors INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_blocks_hostel FOREIGN KEY (hostel_id) REFERENCES hostels(id) ON DELETE CASCADE,
    UNIQUE KEY uk_block_hostel_name (hostel_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. ROOMS
CREATE TABLE IF NOT EXISTS rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hostel_id BIGINT NOT NULL,
    block_id BIGINT NOT NULL,
    room_number VARCHAR(30) NOT NULL,
    floor INT NOT NULL DEFAULT 0,
    capacity INT NOT NULL DEFAULT 1,
    room_type VARCHAR(30) NOT NULL DEFAULT 'NON_AC',
    monthly_rent DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_rooms_hostel FOREIGN KEY (hostel_id) REFERENCES hostels(id) ON DELETE CASCADE,
    CONSTRAINT fk_rooms_block FOREIGN KEY (block_id) REFERENCES hostel_blocks(id) ON DELETE CASCADE,
    UNIQUE KEY uk_room_block_number (block_id, room_number),
    INDEX idx_rooms_hostel (hostel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. BEDS
CREATE TABLE IF NOT EXISTS beds (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hostel_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    bed_label VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_beds_hostel FOREIGN KEY (hostel_id) REFERENCES hostels(id) ON DELETE CASCADE,
    CONSTRAINT fk_beds_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    UNIQUE KEY uk_bed_room_label (room_id, bed_label),
    INDEX idx_beds_hostel_status (hostel_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. ROOM ALLOCATIONS
CREATE TABLE IF NOT EXISTS room_allocations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hostel_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    bed_id BIGINT NOT NULL,
    allocated_by BIGINT NULL,
    start_date DATE NOT NULL,
    end_date DATE NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    notes TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_allocations_hostel FOREIGN KEY (hostel_id) REFERENCES hostels(id) ON DELETE CASCADE,
    CONSTRAINT fk_allocations_student FOREIGN KEY (student_id) REFERENCES student_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_allocations_bed FOREIGN KEY (bed_id) REFERENCES beds(id) ON DELETE RESTRICT,
    CONSTRAINT fk_allocations_user FOREIGN KEY (allocated_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_allocations_student_status (student_id, status),
    INDEX idx_allocations_bed_status (bed_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. INVOICES (Fees & Rent)
CREATE TABLE IF NOT EXISTS invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hostel_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    invoice_number VARCHAR(50) NOT NULL,
    title VARCHAR(150) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    paid_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    due_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_invoices_hostel FOREIGN KEY (hostel_id) REFERENCES hostels(id) ON DELETE CASCADE,
    CONSTRAINT fk_invoices_student FOREIGN KEY (student_id) REFERENCES student_profiles(id) ON DELETE CASCADE,
    UNIQUE KEY uk_invoice_hostel_number (hostel_id, invoice_number),
    INDEX idx_invoices_student_status (student_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. PAYMENTS (Transactions against invoices)
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hostel_id BIGINT NOT NULL,
    invoice_id BIGINT NOT NULL,
    receipt_number VARCHAR(50) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(30) NOT NULL DEFAULT 'CASH',
    transaction_reference VARCHAR(100) NULL,
    recorded_by BIGINT NULL,
    payment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_payments_hostel FOREIGN KEY (hostel_id) REFERENCES hostels(id) ON DELETE CASCADE,
    CONSTRAINT fk_payments_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE,
    CONSTRAINT fk_payments_recorder FOREIGN KEY (recorded_by) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE KEY uk_payment_receipt (receipt_number),
    INDEX idx_payments_invoice (invoice_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. LEAVE REQUESTS (Gate pass / Outpasses)
CREATE TABLE IF NOT EXISTS leave_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hostel_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    leave_type VARCHAR(30) NOT NULL DEFAULT 'DAY_OUTPASS',
    departure_time DATETIME NOT NULL,
    expected_return_time DATETIME NOT NULL,
    actual_return_time DATETIME NULL,
    reason TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approved_by BIGINT NULL,
    rejection_reason VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_leaves_hostel FOREIGN KEY (hostel_id) REFERENCES hostels(id) ON DELETE CASCADE,
    CONSTRAINT fk_leaves_student FOREIGN KEY (student_id) REFERENCES student_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_leaves_approver FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_leaves_student_status (student_id, status),
    INDEX idx_leaves_hostel_status (hostel_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. COMPLAINTS (Maintenance ticketing)
CREATE TABLE IF NOT EXISTS complaints (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hostel_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    room_id BIGINT NULL,
    category VARCHAR(50) NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    assigned_to BIGINT NULL,
    resolution_notes TEXT NULL,
    resolved_at DATETIME NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_complaints_hostel FOREIGN KEY (hostel_id) REFERENCES hostels(id) ON DELETE CASCADE,
    CONSTRAINT fk_complaints_student FOREIGN KEY (student_id) REFERENCES student_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_complaints_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE SET NULL,
    CONSTRAINT fk_complaints_assignee FOREIGN KEY (assigned_to) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_complaints_hostel_status (hostel_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
