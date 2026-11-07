CREATE UNIQUE INDEX uq_category_name_type
    ON category (LOWER(name), type);

CREATE INDEX idx_transaction_account_date
    ON transaction (account_id, date);

CREATE INDEX idx_transaction_category
    ON transaction (category_id);