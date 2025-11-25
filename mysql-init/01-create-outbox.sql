-- mysql-init/01-create-outbox.sql

CREATE DATABASE IF NOT EXISTS customerdb;
USE customerdb;

CREATE TABLE IF NOT EXISTS outbox_event (
    id            VARCHAR(36)   NOT NULL,
    aggregatetype VARCHAR(255)  NOT NULL,
    aggregateid   VARCHAR(255)  NOT NULL,
    type          VARCHAR(255)  NOT NULL,
    payload       JSON          NOT NULL,
    created_at    DATETIME(6)   NOT NULL,
    PRIMARY KEY (id)
    ) ENGINE=InnoDB;
