-- mysql-init/02-create-customers.sql
CREATE TABLE IF NOT EXISTS customers (
    id          VARCHAR(36)  NOT NULL,
    email       VARCHAR(255) NOT NULL,
    first_name  VARCHAR(255) NOT NULL,
    last_name   VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;