# SPEC.md — SRM Credit Engine

Nível-alvo: **Júnior** (corretude e clareza), com itens de **Pleno** (idempotência testada, tratamento global de erros, paginação server-side, Docker Compose).

## 1. Premissas adotadas

O enunciado contém ambiguidades propositais. Abaixo, as decisões tomadas para cada uma:

| Ambiguidade | Premissa adotada | Justificativa |
|-|-|-|
| Origem e valor da taxa base | Parâmetro persistido (tabela `base_rate`, com data de vigência), não hardcoded. Armazenada como **fração**: `0.010000` = 1% a.m. Para os golden cases, vale **1% a.m.** | Em cenário real a taxa muda por decisão da mesa. Guardar como fração evita o erro de usar `1.0` (100%) no lugar de `0.01`, como acontece no código do Anexo A. |
| Unidade do prazo | **Meses inteiros**, juros compostos mensais, conforme os golden cases. | Elimina a ambiguidade de fração de mês ou dias corridos. |
| Conversão de vencimento em prazo | O operador informa a **data de vencimento**. O prazo é o número de meses de calendário entre a data da operação (fuso `America/Sao_Paulo`) e o vencimento, **arredondado para cima**. O vencimento precisa ser posterior à data da operação, logo o prazo mínimo é 1 mês. | Arredondar para cima é conservador para o fundo (gera deságio maior). Vencimento no passado ou no mesmo dia não é uma operação de desconto válida. |
| Moeda do título | O valor de face é sempre em **BRL**. A moeda de pagamento pode ser BRL ou USD. | É o cenário descrito no enunciado ("título em BRL, pagamento em USD"). |
| Qual câmbio vale na liquidação | A taxa **vigente no momento em que a liquidação é processada** (a mais recente com `effective_at <= now()`), e não a da criação do recebível nem a do vencimento. | A liquidação é o evento financeiro real: é quando o valor é convertido e pago. |
| Direção da cotação | O câmbio é armazenado como **BRL por unidade da moeda** (`5.4321` = 1 USD vale 5,4321 BRL). A conversão é uma **divisão**: `valor em USD = valor em BRL / taxa`. | Deixa explícita a direção da cotação e evita inversão. |
| Câmbio indisponível | Se não houver taxa vigente para a moeda, a operação é **bloqueada** com `422 Unprocessable Content`. | Premissa provisória até a resposta do negócio (pergunta 3). Liquidar com taxa desconhecida é pior do que não liquidar. |
| Política de arredondamento | **Half-even (banker's rounding)**, 2 casas decimais, aplicado **apenas no resultado final** de cada cálculo (valor presente e, se houver, valor convertido). | Exigido pelos golden cases. Evita erro acumulado de arredondamentos sucessivos. |
| Conversão cross-currency | Primeiro calcula-se o valor presente em BRL e arredonda-se. Depois converte-se esse valor **já arredondado**. O deságio é sempre expresso em BRL. | Regra explícita dos golden cases (C3). |
| Idempotência da liquidação | O cliente envia o header `Idempotency-Key`. A mesma chave com o mesmo corpo devolve a liquidação original (`200` + `Idempotent-Replayed: true`). A mesma chave com corpo diferente retorna `422`. Um recebível já liquidado (mesmo cedente, tipo e número de documento) retorna `409`. As garantias finais são constraints `UNIQUE` no banco. | Protege contra retry de rede e duplo clique, inclusive com requisições simultâneas. |
| Auditabilidade | Liquidações e recebíveis são **somente inserção**: não existe endpoint de alteração e um trigger no banco rejeita `UPDATE` e `DELETE`. Cada liquidação guarda a taxa base, o spread e o câmbio efetivamente usados, com referência à taxa de origem e timestamps em UTC. | O registro precisa continuar explicando o cálculo mesmo depois que as taxas mudarem. Uma correção seria um novo lançamento (estorno), não uma edição. |
| Cedente | Cada recebível pertence a um **cedente** (`assignor` no código), cadastrado previamente. | O extrato precisa filtrar por cedente. |

## 2. Perguntas que eu faria ao negócio (projeto real)

1. Com que frequência a taxa base muda, e quem tem autoridade para atualizá-la?
2. Existe SLA de atualização para a taxa de câmbio (ex.: quantos minutos de defasagem são toleráveis)?
3. O que deve acontecer se não houver taxa de câmbio cadastrada para a moeda solicitada no momento da liquidação: bloquear a operação ou usar a última conhecida com alerta?
4. Uma liquidação pode ser cancelada/estornada, ou é definitivamente imutável (exigindo lançamento de estorno como nova transação)?
5. Existe algum limite de valor de face ou de prazo que exija alçada de aprovação adicional?
6. O prazo deve considerar meses de calendário ou dias corridos/úteis convertidos em meses?

## 3. Decisões de precisão numérica

* **Tipo de dado na aplicação:** `BigDecimal` em toda a cadeia de cálculo, nunca `float`/`double`.
* **Valores monetários no banco:** `NUMERIC(19,2)`. Só resultados finais são persistidos, e todos já estão arredondados em 2 casas.
* **Taxa base e spread:** `NUMERIC(9,6)`, como fração.
* **Câmbio:** `NUMERIC(18,8)`.
* **Precisão intermediária:** `MathContext.DECIMAL128` (34 dígitos significativos). A divisão `face / (1 + taxa)^prazo` normalmente gera uma dízima, e o `BigDecimal` exige uma precisão definida para não lançar exceção. Os valores intermediários não são arredondados para 2 casas.
* **Momento do arredondamento:** apenas no resultado final de cada cálculo (valor presente e valor convertido), com `RoundingMode.HALF_EVEN`.
* **Responsável pelo arredondamento:** a camada de negócio, **centralizada em `MonetaryPolicy`** e aplicada pelo `PricingEngine`. As strategies calculam o valor sem arredondar. Apresentação e persistência nunca arredondam. Mudar a política de arredondamento altera um único arquivo.

## 4. Critérios de aceite (definidos por mim)

**Usabilidade**

* O operador consegue simular o valor líquido de um recebível em tempo real, sem precisar salvar antes de ver o resultado.
* Mensagens de erro de validação (ex.: valor de face negativo, vencimento que não seja futuro) aparecem de forma clara no formulário, sem quebrar a tela.

**Segurança**

* Nenhuma query SQL concatenada com input do usuário (queries parametrizadas / JPA).
* Erros nunca retornam `200 OK`: `400` para entrada inválida, `404` para recurso inexistente, `409` para conflito, `422` para regra de negócio violada e `500` para erro inesperado, sempre com corpo estruturado (Problem Details, RFC 9457).

**Desempenho**

* O endpoint de simulação responde em até ~300ms em ambiente local, sem I/O externo bloqueante desnecessário.
* A listagem de transações não carrega todos os registros de uma vez (paginação server-side).

**Corretude**

* Os 3 golden cases (C1, C2, C3) passam com valores idênticos ao centavo, validados por teste automatizado.
* Repetir a mesma requisição de liquidação, em sequência ou em paralelo, gera exatamente uma liquidação, validado por teste automatizado.
