CREATE TABLE base_rate (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    monthly_rate NUMERIC(9, 6) NOT NULL CHECK (monthly_rate >= 0),
    effective_at TIMESTAMPTZ   NOT NULL,
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_base_rate_effective_at ON base_rate (effective_at DESC);

COMMENT ON COLUMN base_rate.monthly_rate IS 'Monthly rate as a fraction: 0.010000 means 1% per month';

CREATE TABLE exchange_rate (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    currency     VARCHAR(3)     NOT NULL CHECK (currency IN ('USD')),
    brl_per_unit NUMERIC(18, 8) NOT NULL CHECK (brl_per_unit > 0),
    effective_at TIMESTAMPTZ    NOT NULL,
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX idx_exchange_rate_currency_effective_at ON exchange_rate (currency, effective_at DESC);

COMMENT ON COLUMN exchange_rate.brl_per_unit IS 'BRL paid for one unit of the currency: 5.4321 means 1 USD = 5.4321 BRL';

CREATE FUNCTION reject_mutation() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'Table % is append-only: % is not allowed', TG_TABLE_NAME, TG_OP;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_base_rate_append_only BEFORE UPDATE OR DELETE ON base_rate
    FOR EACH ROW EXECUTE FUNCTION reject_mutation();

CREATE TRIGGER trg_exchange_rate_append_only BEFORE UPDATE OR DELETE ON exchange_rate
    FOR EACH ROW EXECUTE FUNCTION reject_mutation();
