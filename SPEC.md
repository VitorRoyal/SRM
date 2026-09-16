# SPEC.md — SRM Credit Engine

# 1\. Premissas adotadas

O enunciado contém ambiguidades propositais. Abaixo, as decisões tomadas para cada uma:

|Ambiguidade|Premissa adotada|Justificativa|
|-|-|-|
|Origem e valor da taxa base|Taxa base é um parâmetro persistido (tabela `rates` ou `parameters`), não hardcoded. Para os golden cases, vale fixo em **1% a.m.**|Uma taxa hardcoded no código é um anti-padrão citado explicitamente no desafio; em cenário real ela muda por decisão da mesa.|
|Unidade do prazo|**Meses inteiros**, juros compostos mensais, conforme fixado nos golden cases.|Elimina ambiguidade de fração de mês/dias corridos.|
|Qual câmbio vale na liquidação|A taxa **vigente no momento em que a liquidação é processada** (a mais recente cadastrada com `data\_vigencia <= now()`), e não a da criação do recebível nem a do vencimento.|A liquidação é o evento financeiro real — é quando o valor é efetivamente convertido e pago. Essa taxa fica registrada no próprio registro de liquidação para fins de auditoria.|
|Política de arredondamento|**Half-even (banker's rounding)**, 2 casas decimais, aplicado **apenas no resultado final** de cada cálculo (valor presente e, se houver, valor convertido). Nenhum arredondamento em etapas intermediárias.|Conforme exigido pelos golden cases; evita erro acumulado de arredondamentos sucessivos.|
|Conversão cross-currency|Primeiro calcula-se o valor presente em BRL e arredonda-se; depois converte-se esse valor **já arredondado** pela taxa de câmbio.|Regra explícita da seção de golden cases (C3).|

## 2\. Perguntas que eu faria ao negócio (projeto real)

1. Com que frequência a taxa base muda, e quem tem autoridade para atualizá-la?
2. Existe SLA de atualização para a taxa de câmbio (ex.: quantos minutos de defasagem são toleráveis)?
3. O que deve acontecer se não houver taxa de câmbio cadastrada para a moeda solicitada no momento da liquidação — bloquear a operação ou usar a última conhecida com alerta?
4. Uma liquidação pode ser cancelada/estornada, ou é definitivamente imutável (exigindo lançamento de estorno como nova transação)?
5. Existe algum limite de valor de face ou de prazo que exija alçada de aprovação adicional?

## 3\. Decisões de precisão numérica

* **Tipo de dado na aplicação:** `BigDecimal` (Java) em toda a cadeia de cálculo — nunca `float`/`double`.
* **Tipo de dado no banco:** `NUMERIC(19,4)` para valores monetários (2 casas de folga além das 2 exigidas no resultado final, para não perder precisão em valores intermediários antes do arredondamento).
* **Taxas (base, spread, câmbio):** `NUMERIC(10,6)` — precisam de mais casas decimais que valores monetários.
* **Momento do arredondamento:** apenas no resultado final de cada cálculo (valor presente / valor convertido), com `RoundingMode.HALF\_EVEN`. Nenhum arredondamento em variáveis intermediárias.
* **Arredondamento é responsabilidade exclusiva da camada de negócio (Strategy)**, nunca da camada de apresentação ou de persistência.

## 4\. Critérios de aceite (definidos por mim)

**Usabilidade**

* O operador consegue simular o valor líquido de um recebível em tempo real, sem precisar salvar antes de ver o resultado.
* Mensagens de erro de validação (ex.: valor de face negativo, prazo zero) aparecem de forma clara no formulário, sem quebrar a tela.

**Segurança**

* Nenhuma query SQL concatenada diretamente com input do usuário (uso de queries parametrizadas / JPA).
* Erros inesperados no backend nunca retornam `200 OK` — sempre um status HTTP condizente (4xx/5xx) com corpo de erro estruturado.

**Desempenho**

* Endpoint de simulação de precificação responde em até \~300ms em ambiente local, sem I/O externo bloqueante desnecessário.
* Listagem de transações não carrega todos os registros de uma vez (paginação, ainda que simples para o nível júnior).

**Corretude**

* Os 3 golden cases (C1, C2, C3) passam com valores idênticos ao centavo, validados por teste automatizado.

