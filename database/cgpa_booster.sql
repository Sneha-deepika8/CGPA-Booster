-- =========================================================
-- CGPA Booster - Database Schema
-- Run with: mysql -u root -p < cgpa_booster.sql
-- =========================================================

DROP DATABASE IF EXISTS cgpa_booster;
CREATE DATABASE cgpa_booster CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cgpa_booster;

-- =========================================================
-- Table: users
-- =========================================================
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password VARCHAR(255) NOT NULL,
    college VARCHAR(150) NOT NULL,
    department VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE INDEX idx_users_email ON users(email);

-- =========================================================
-- Table: resources
-- =========================================================
CREATE TABLE resources (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(60) NOT NULL,
    resource_url VARCHAR(500) NOT NULL,
    uploaded_by INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_resources_user FOREIGN KEY (uploaded_by)
        REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_resources_category ON resources(category);
CREATE INDEX idx_resources_uploaded_by ON resources(uploaded_by);

-- =========================================================
-- Table: posts
-- =========================================================
CREATE TABLE posts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_posts_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_posts_user_id ON posts(user_id);

-- =========================================================
-- Sample data (optional - safe to delete)
-- =========================================================
-- Password below is a bcrypt hash of "password123"
INSERT INTO users (full_name, email, password, college, department)
VALUES ('Demo Student', 'demo@cgpabooster.com',
        '$2a$10$O0kR2Z3v0r2WugC2jFQjHeQ0m8qz2m0m2YV9K1qkq2p1QaP3G7GXG',
        'Demo Institute of Technology', 'Computer Science');
