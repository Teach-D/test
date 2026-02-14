CREATE TABLE daily_budgets (
    id           BIGINT    AUTO_INCREMENT PRIMARY KEY,
    budget_date  DATE      NOT NULL UNIQUE,
    total_budget INT       NOT NULL DEFAULT 100000,
    used_budget  INT       NOT NULL DEFAULT 0,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
