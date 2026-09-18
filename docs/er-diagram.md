# Modelo de dados

```mermaid
erDiagram
    ASSIGNOR ||--o{ RECEIVABLE : "cede"
    RECEIVABLE ||--|| SETTLEMENT : "é liquidado por"
    BASE_RATE ||--o{ SETTLEMENT : "taxa base aplicada"
    EXCHANGE_RATE ||--o{ SETTLEMENT : "câmbio aplicado"

    ASSIGNOR {
        bigint id PK
        varchar_150 name
        varchar_14 tax_id UK
        timestamptz created_at
    }

    RECEIVABLE {
        bigint id PK
        bigint assignor_id FK
        varchar_50 document_number
        varchar_30 type "TRADE_BILL ou POST_DATED_CHECK"
        numeric_19_2 face_value "sempre em BRL"
        date due_date
        timestamptz created_at
    }

    SETTLEMENT {
        bigint id PK
        bigint receivable_id FK "UK: uma liquidação por recebível"
        varchar_100 idempotency_key UK
        char_64 request_fingerprint "SHA-256 da requisição"
        int term_in_months
        bigint base_rate_id FK
        numeric_9_6 monthly_base_rate "fração: 0.010000 = 1%"
        numeric_9_6 monthly_spread
        numeric_19_2 present_value_brl
        numeric_19_2 discount_brl
        varchar_3 payment_currency
        numeric_19_2 payment_amount
        bigint exchange_rate_id FK "nulo quando pago em BRL"
        numeric_18_8 exchange_rate_brl_per_unit
        timestamptz settled_at
    }

    BASE_RATE {
        bigint id PK
        numeric_9_6 monthly_rate
        timestamptz effective_at
        timestamptz created_at
    }

    EXCHANGE_RATE {
        bigint id PK
        varchar_3 currency
        numeric_18_8 brl_per_unit "1 USD = 5.4321 BRL"
        timestamptz effective_at
        timestamptz created_at
    }
```

## Como ler o modelo

**Fluxo do negócio.** Um **cedente** (`assignor`) vende seus **recebíveis** ao fundo. Cada recebível é liquidado uma única vez, e essa **liquidação** registra quanto o fundo pagou e com quais taxas.

**Normalização.** Cada informação aparece em um lugar só: o nome do cedente fica em `assignor`, os dados do título ficam em `receivable` e os valores calculados ficam em `settlement`. As tabelas de taxas guardam o histórico.

## Regras que o banco garante

| Regra | Como é garantida |
|---|---|
| Um recebível é liquidado no máximo uma vez | `UNIQUE (receivable_id)` em `settlement` |
| O mesmo título não é cadastrado duas vezes | `UNIQUE (assignor_id, type, document_number)` em `receivable` |
| Uma chave de idempotência gera uma liquidação | `UNIQUE (idempotency_key)` em `settlement` |
| Pagamento em moeda estrangeira sempre tem câmbio registrado, e pagamento em BRL nunca tem | `CHECK ck_settlement_exchange_rate` |
| Valores positivos e prazo de pelo menos um mês | `CHECK` em `face_value` e `term_in_months` |
| Nenhum registro é alterado ou apagado | Trigger `reject_mutation()` bloqueia `UPDATE` e `DELETE` nas cinco tabelas |

## Por que a liquidação copia as taxas

A liquidação guarda `monthly_base_rate`, `monthly_spread` e `exchange_rate_brl_per_unit`, além das chaves estrangeiras para as taxas de origem. Sem essa cópia, uma liquidação antiga deixaria de explicar o próprio cálculo assim que as taxas mudassem. As chaves estrangeiras mantêm a rastreabilidade de qual registro de taxa foi usado.

## Índices

| Índice | Para que serve |
|---|---|
| `idx_settlement_settled_at (settled_at DESC, id DESC)` | Ordem do extrato e paginação estável |
| `idx_settlement_currency_settled_at` | Filtro por moeda |
| `idx_receivable_assignor_id` | Filtro por cedente |
| `idx_base_rate_effective_at`, `idx_exchange_rate_currency_effective_at` | Busca da taxa vigente em uma data |

## Migrations

| Arquivo | Conteúdo |
|---|---|
| `V1__create_reference_rates.sql` | `base_rate`, `exchange_rate`, função `reject_mutation()` e triggers |
| `V2__seed_reference_rates.sql` | Taxa base de 1% a.m. e cotação USD 5,4321 |
| `V3__create_assignor_receivable_settlement.sql` | `assignor`, `receivable`, `settlement`, constraints, índices e triggers |
| `V4__seed_assignors.sql` | Três cedentes de exemplo |
