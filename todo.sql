CREATE DATABASE IF NOT EXISTS todo;

USE todo;

CREATE TABLE IF NOT EXISTS tasks (
    task VARCHAR(400) NOT NULL,
    due_date DATE,
    completed TINYINT(1) DEFAULT 0
    CONSTRAINT task_unique UNIQUE (task, due_date)
);
