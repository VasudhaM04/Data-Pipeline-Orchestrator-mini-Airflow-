-- Data Pipeline Orchestrator - Database Schema
-- MySQL Database

-- Create database
CREATE DATABASE IF NOT EXISTS pipeline_db 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE pipeline_db;

-- ============================================
-- TABLE: users
-- Stores user account information
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email)
) ENGINE=InnoDB;

-- ============================================
-- TABLE: pipelines
-- Stores pipeline definitions
-- ============================================
CREATE TABLE IF NOT EXISTS pipelines (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    schedule_time VARCHAR(10), -- Format: HH:MM (e.g., "02:00")
    is_active BOOLEAN DEFAULT TRUE,
    status VARCHAR(20) DEFAULT 'IDLE', -- IDLE, RUNNING, SUCCESS, FAILED, PARTIAL_FAILURE
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_run TIMESTAMP NULL,
    user_id INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB;

-- ============================================
-- TABLE: tasks
-- Stores individual tasks within pipelines
-- ============================================
CREATE TABLE IF NOT EXISTS tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL, -- API, DATABASE, CODE, TRANSFORM, LOAD
    config TEXT, -- Configuration string (e.g., "type=API;url=https://example.com")
    priority INT DEFAULT 0, -- Higher = executed first
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, RUNNING, COMPLETED, FAILED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    pipeline_id INT NOT NULL,
    FOREIGN KEY (pipeline_id) REFERENCES pipelines(id) ON DELETE CASCADE,
    INDEX idx_pipeline (pipeline_id),
    INDEX idx_status (status)
) ENGINE=InnoDB;

-- ============================================
-- TABLE: dependencies
-- Stores task dependencies for DAG structure
-- ============================================
CREATE TABLE IF NOT EXISTS dependencies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    task_id INT NOT NULL, -- The task that has a dependency
    depends_on INT NOT NULL, -- The task it depends on
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (depends_on) REFERENCES tasks(id) ON DELETE CASCADE,
    UNIQUE KEY unique_dependency (task_id, depends_on),
    INDEX idx_task (task_id),
    INDEX idx_depends_on (depends_on)
) ENGINE=InnoDB;

-- ============================================
-- TABLE: execution_logs
-- Stores execution history for tasks
-- Uses JDBC for fine-grained control
-- ============================================
CREATE TABLE IF NOT EXISTS execution_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    task_id INT NOT NULL,
    status VARCHAR(20) NOT NULL, -- RUNNING, COMPLETED, FAILED
    message TEXT,
    start_time TIMESTAMP NULL,
    end_time TIMESTAMP NULL,
    duration_ms BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    INDEX idx_task (task_id),
    INDEX idx_status (status),
    INDEX idx_created (created_at)
) ENGINE=InnoDB;

-- ============================================
-- SAMPLE DATA (Optional - for testing)
-- ============================================

-- Insert test user (password: 'password' - in production, use hashed passwords)
-- INSERT INTO users (email, password, full_name) 
-- VALUES ('test@example.com', 'password', 'Test User');

-- ============================================
-- VIEWS (Optional - for reporting)
-- ============================================

-- View: Pipeline execution summary
CREATE OR REPLACE VIEW pipeline_execution_summary AS
SELECT 
    p.id AS pipeline_id,
    p.name AS pipeline_name,
    u.email AS owner_email,
    p.status AS pipeline_status,
    p.last_run,
    COUNT(DISTINCT t.id) AS total_tasks,
    COUNT(DISTINCT CASE WHEN t.status = 'COMPLETED' THEN t.id END) AS completed_tasks,
    COUNT(DISTINCT CASE WHEN t.status = 'FAILED' THEN t.id END) AS failed_tasks
FROM pipelines p
JOIN users u ON p.user_id = u.id
LEFT JOIN tasks t ON p.id = t.pipeline_id
GROUP BY p.id, p.name, u.email, p.status, p.last_run;

-- View: Recent execution activity
CREATE OR REPLACE VIEW recent_execution_activity AS
SELECT 
    el.id AS log_id,
    p.name AS pipeline_name,
    t.name AS task_name,
    el.status,
    el.message,
    el.start_time,
    el.end_time,
    el.duration_ms
FROM execution_logs el
JOIN tasks t ON el.task_id = t.id
JOIN pipelines p ON t.pipeline_id = p.id
WHERE el.created_at >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
ORDER BY el.created_at DESC;

-- ============================================
-- STORED PROCEDURES (Optional)
-- ============================================

-- Procedure: Clean old execution logs
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS clean_old_logs(IN days_to_keep INT)
BEGIN
    DELETE FROM execution_logs 
    WHERE created_at < DATE_SUB(NOW(), INTERVAL days_to_keep DAY);
END //
DELIMITER ;

-- ============================================
-- TRIGGERS (Optional)
-- ============================================

-- Trigger: Update pipeline last_run when status changes to completed
DELIMITER //
CREATE TRIGGER IF NOT EXISTS update_pipeline_last_run
AFTER UPDATE ON pipelines
FOR EACH ROW
BEGIN
    IF OLD.status != 'SUCCESS' AND NEW.status = 'SUCCESS' THEN
        SET NEW.last_run = NOW();
    END IF;
END //
DELIMITER ;
