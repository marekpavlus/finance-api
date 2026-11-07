
-- Account constraints
ALTER TABLE account
ALTER COLUMN name TYPE VARCHAR(100),
    ALTER COLUMN name SET NOT NULL,
    ALTER COLUMN currency TYPE VARCHAR(3),
    ALTER COLUMN currency SET NOT NULL;

ALTER TABLE account
    ADD CONSTRAINT ck_account_name_not_blank
        CHECK (LENGTH(TRIM(name)) > 0),
    ADD CONSTRAINT ck_account_currency_supported
        CHECK (currency IN ('EUR', 'USD', 'DKK', 'GBP', 'CZK', 'PLN')),
    ADD CONSTRAINT ck_account_balance_non_negative
        CHECK (balance >= 0);

-- Transaction constraints
ALTER TABLE transaction
    ALTER COLUMN type SET NOT NULL,
ALTER COLUMN description SET NOT NULL,
    ALTER COLUMN date SET NOT NULL,
    ALTER COLUMN account_id SET NOT NULL;

ALTER TABLE transaction
    ADD CONSTRAINT ck_transaction_amount_positive
        CHECK (amount > 0),
    ADD CONSTRAINT ck_transaction_description_not_blank
        CHECK (LENGTH(TRIM(description)) > 0);
