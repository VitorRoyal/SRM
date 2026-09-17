CREATE TABLE assignor (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name       VARCHAR(150) NOT NULL,
    tax_id     VARCHAR(14)  NOT NULL UNIQUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

COMMENT ON TABLE assignor IS 'Company that sells (assigns) its receivables to the fund: the cedente';

CREATE TABLE receivable (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    assignor_id     BIGINT         NOT NULL REFERENCES assignor (id),
    document_number VARCHAR(50)    NOT NULL,
    type            VARCHAR(30)    NOT NULL CHECK (type IN ('TRADE_BILL', 'POST_DATED_CHECK')),
    face_value      NUMERIC(19, 2) NOT NULL CHECK (face_value > 0),
    due_date        DATE           NOT NULL,
    created_at      TIMESTAMPTZ    NOT NULL,
    CONSTRAINT uk_receivable_assignor_type_document UNIQUE (assignor_id, type, document_number)
);

CREATE INDEX idx_receivable_assignor_id ON receivable (assignor_id);

CREATE TABLE settlement (
    id                         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    receivable_id              BIGINT         NOT NULL UNIQUE REFERENCES receivable (id),
    idempotency_key            VARCHAR(100)   NOT NULL UNIQUE,
    request_fingerprint        CHAR(64)       NOT NULL,
    term_in_months             INTEGER        NOT NULL CHECK (term_in_months >= 1),
    base_rate_id               BIGINT         NOT NULL REFERENCES base_rate (id),
    monthly_base_rate          NUMERIC(9, 6)  NOT NULL,
    monthly_spread             NUMERIC(9, 6)  NOT NULL,
    present_value_brl          NUMERIC(19, 2) NOT NULL,
    discount_brl               NUMERIC(19, 2) NOT NULL,
    payment_currency           VARCHAR(3)     NOT NULL CHECK (payment_currency IN ('BRL', 'USD')),
    payment_amount             NUMERIC(19, 2) NOT NULL,
    exchange_rate_id           BIGINT REFERENCES exchange_rate (id),
    exchange_rate_brl_per_unit NUMERIC(18, 8),
    settled_at                 TIMESTAMPTZ    NOT NULL,
    CONSTRAINT ck_settlement_exchange_rate CHECK (
        (payment_currency = 'BRL' AND exchange_rate_id IS NULL AND exchange_rate_brl_per_unit IS NULL)
        OR (payment_currency <> 'BRL' AND exchange_rate_id IS NOT NULL AND exchange_rate_brl_per_unit IS NOT NULL)
    )
);

CREATE INDEX idx_settlement_settled_at ON settlement (settled_at DESC, id DESC);
CREATE INDEX idx_settlement_currency_settled_at ON settlement (payment_currency, settled_at DESC);

COMMENT ON COLUMN settlement.request_fingerprint IS 'SHA-256 of the settlement request, used to detect idempotency key reuse with a different payload';

CREATE TRIGGER trg_receivable_append_only BEFORE UPDATE OR DELETE ON receivable
    FOR EACH ROW EXECUTE FUNCTION reject_mutation();

CREATE TRIGGER trg_settlement_append_only BEFORE UPDATE OR DELETE ON settlement
    FOR EACH ROW EXECUTE FUNCTION reject_mutation();
