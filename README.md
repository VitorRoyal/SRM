# SRM Credit Engine

> Plataforma de cessão de crédito multimoedas: precifica recebíveis (duplicatas e cheques pré-datados), converte o pagamento para USD quando necessário e registra liquidações auditáveis.

---

## Tecnologias

- Java 21
- Spring Boot 4.1.1
- PostgreSQL 17 (migrations com Flyway)
- React 19 + TypeScript 6
- Node.js 22 (Vite 8)
- Docker / Docker Compose

**Por que essa stack:** Java com `BigDecimal` e Postgres com `NUMERIC` dão aritmética decimal exata, que é requisito em sistema financeiro. Spring Boot entrega transações, validação, tratamento de erros e OpenAPI sem código extra, e o Flyway versiona o schema com as constraints e os triggers que sustentam idempotência e auditoria. No frontend, TypeScript em modo estrito evita erro de tipo em tela que lida com dinheiro.

---

## Estrutura de Pastas

```
credit-engine/
├── backend/
│   └── src/main/java/com/srm/creditengine/
│       ├── config/                   # Relógio da aplicação (fuso de negócio)
│       ├── shared/                   # Tratamento global de erros e DTO de página
│       └── core/
│           ├── pricing/              # Motor de cálculo (strategies) e simulação
│           ├── settlement/           # Liquidação idempotente e extrato
│           ├── receivable/           # Recebível
│           ├── assignor/             # Cedente
│           ├── baserate/             # Taxa base
│           └── exchangerate/         # Câmbio
├── backend/src/main/resources/db/migration/   # Migrations Flyway
├── frontend/
│   └── src/
│       ├── component/                # Layout compartilhado
│       ├── modules/                  # OperatorPanel, Settlements, ExchangeRates
│       ├── services/                 # Cliente HTTP e chamadas compartilhadas
│       ├── hooks/ utils/ types/      # Código reutilizável entre módulos
│       ├── styles/                   # Paleta e estilos globais
│       └── i18n/                     # Traduções pt-BR e inglês
├── compose.yaml
└── SPEC.md
```

Cada domínio em `core/` segue a mesma divisão de camadas:

| Camada | Responsabilidade |
|--------|-----------------|
| `controller/` | Recebe requisições HTTP e delega para o Service |
| `service/` | Contém as regras de negócio |
| `repository/` | Acesso ao banco de dados |
| `entity/` | Entidades JPA |
| `dto/` | Objetos de entrada e saída da API |
| `mapper/` | Conversão entre entidade e DTO |
| `strategy/` | Regra de precificação por tipo de recebível |

---

## Como Instalar e Rodar

### Pré-requisitos

- Docker e Docker Compose
- Java 21+ (o Maven Wrapper já está no projeto)
- Node.js 20+

### Banco de dados

```bash
docker compose up -d
```

O Postgres sobe na porta 5434. As migrations e os dados iniciais (taxa base de 1% a.m., cotação USD 5,4321 e três cedentes) são aplicados quando o backend inicia.

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

A API fica em http://localhost:8080 e a documentação OpenAPI em http://localhost:8080/swagger-ui.html.

Para rodar os testes (o Docker precisa estar ativo, porque os testes de integração sobem um Postgres próprio):

```bash
cd backend
./mvnw test
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

A interface fica em http://localhost:5173 e acessa o backend por proxy. Os comandos `npm test`, `npm run lint` e `npm run typecheck` completam a verificação.

No Windows PowerShell, use `.\mvnw.cmd` no lugar de `./mvnw`.

---

## Principais Funcionalidades

- Precificação de recebíveis com spread por tipo, arredondamento half-even e conversão cambial do valor presente já arredondado.
- Simulação do valor líquido sem persistência, usada pelo painel do operador em tempo real.
- Liquidação idempotente e transacional, que registra recebível e liquidação com as taxas efetivamente aplicadas.
- Extrato de liquidações com filtros por período, cedente e moeda, e paginação no servidor.
- Gestão de câmbio com histórico por data de vigência e consulta da taxa vigente.

---

## Endpoints

### Precificação — `/pricing`

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/pricing/simulations` | Simula o valor presente e o valor a pagar, sem gravar |

### Liquidações — `/settlements`

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/settlements` | Registra a liquidação (exige o header `Idempotency-Key`) |
| `GET` | `/settlements` | Extrato paginado, com filtros de período, cedente e moeda |

### Câmbio — `/exchange-rates`

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/exchange-rates` | Cadastra uma cotação com data de vigência |
| `GET` | `/exchange-rates/current` | Retorna a cotação vigente da moeda informada |

### Taxa base — `/base-rates`

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/base-rates/current` | Retorna a taxa base vigente |

### Cedentes — `/assignors`

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/assignors` | Lista os cedentes cadastrados |

---

## Decisões de Design

- **Precisão decimal:** `BigDecimal` em toda a cadeia de cálculo e `NUMERIC` no banco. Valores intermediários usam `MathContext.DECIMAL128` e o arredondamento half-even acontece apenas no resultado final, num único ponto (`MonetaryPolicy`).
- **Strategy por tipo de recebível:** a fórmula fica em uma classe base e cada tipo define apenas o seu spread. Um tipo com regra diferente implementa a interface diretamente.
- **Idempotência em duas camadas:** a aplicação compara a chave e um hash da requisição; o banco garante o resto com constraints `UNIQUE`, inclusive quando duas requisições chegam simultaneamente.
- **Auditoria imutável:** liquidações, recebíveis e taxas são somente de inserção, com triggers no banco bloqueando `UPDATE` e `DELETE`. Cada liquidação guarda as taxas usadas.
- **Relatórios com SQL nativo:** o extrato usa consulta parametrizada com `JOIN`, evitando N+1 e devolvendo só as colunas exibidas.

Detalhamento das premissas em [SPEC.md](SPEC.md), do que foi cortado em [DECISIONS.md](DECISIONS.md), do modelo de dados em [docs/er-diagram.md](docs/er-diagram.md), do uso de IA em [AI_USAGE.md](AI_USAGE.md) e da revisão do Anexo A em [REVIEW.md](REVIEW.md).
