CREATE TABLE tasks
(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(50) NOT NULL,
    description VARCHAR(255) NULL,
    task_status VARCHAR(25) NOT NULL,
    task_priority VARCHAR(25) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    due_date TIMESTAMP NOT NULL
);