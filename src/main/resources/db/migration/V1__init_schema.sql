-- Initial schema setup for Enterprise Playwright Framework
CREATE TABLE IF NOT EXISTS customers (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert dummy seed data for testing
INSERT INTO customers (first_name, last_name, email, phone, status)
VALUES ('John', 'Doe', 'john.doe@example.com', '1234567890', 'ACTIVE')
ON CONFLICT (email) DO NOTHING;

INSERT INTO customers (first_name, last_name, email, phone, status)
VALUES ('Jane', 'Smith', 'jane.smith@example.com', '0987654321', 'ACTIVE')
ON CONFLICT (email) DO NOTHING;
