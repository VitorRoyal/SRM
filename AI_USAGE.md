# AI_USAGE.md — Engenharia da colaboração com IA

Usei IA (Claude) como par de programação durante todo o projeto. Este documento descreve como dirigi esse trabalho, onde a IA errou, como os erros foram detectados e o que decidi não delegar.

---

## 1. Como conduzi o trabalho

**Spec antes de código.** Antes de qualquer implementação, escrevi o `SPEC.md` com as premissas para as ambiguidades do enunciado. Usei a IA para explorar as opções de cada ambiguidade, mas a escolha foi minha, e foi ela que passou a valer como regra durante a implementação. Depois pedi que a IA revisasse o `SPEC.md` contra o enunciado; dessa revisão saíram lacunas que eu não tinha coberto, como a conversão de vencimento em meses e o comportamento quando não existe cotação vigente.

**Fatiamento por etapas.** Em vez de pedir "construa o sistema", dividi em sete etapas, cada uma em uma branch com PR:

1. motor de precificação;
2. banco e taxas;
3. simulação;
4. cedente, recebível e liquidação;
5. extrato;
6. frontend;
7. documentação.

A ordem não foi arbitrária: comecei pelo motor porque ele é o critério de maior peso do desafio e não depende de banco, então os golden cases podiam ser validados como teste unitário puro desde o primeiro dia. O cedente, por exemplo, só entrou na etapa 4, na mesma migration em que passou a ser necessário, para não criar código sem uso.

**Padrões antes do código.** Mantenho um conjunto de regras de estilo que aplico em todo projeto (inglês, sem operador ternário, injeção por construtor, DTOs de Request e Response separados, estilos fora dos `.tsx`, sem `any`, sem texto fixo na tela). Passei essas regras à IA como contexto obrigatório, então elas valeram desde o primeiro arquivo, em vez de virarem correção depois.

**Exemplo de instrução usada na etapa do motor:**

> Implemente o motor de precificação com Strategy por tipo de recebível. O cálculo não pode acessar banco: recebe taxa base e câmbio como parâmetros. Concentre precisão e arredondamento em um único ponto, com half-even em duas casas apenas no resultado final e `DECIMAL128` nos valores intermediários. Escreva os testes dos golden cases C1, C2 e C3 e me mostre falhando antes de considerar pronto.

---

## 2. Três casos em que a IA errou

### 2.1 Bug de concorrência na idempotência, encontrado pelo teste

**O erro.** O código gerado para a liquidação tratava a corrida entre requisições assim: consultava a chave de idempotência, e se não existisse, abria a transação; se o banco recusasse por violação de `UNIQUE`, consultava a chave de novo e devolvia a liquidação original. Parecia correto, e passava em todos os testes sequenciais.

Só que existe uma terceira janela: uma requisição pode passar pela consulta inicial **antes** de a primeira ter commitado e chegar à verificação do recebível **depois**. Nesse caso ela não bate no `UNIQUE`; ela encontra o recebível já cadastrado e lança conflito. Resultado: um retry legítimo recebia `409` em vez da liquidação original. Na prática, o operador que desse duplo clique veria erro vermelho na tela, mesmo com a liquidação registrada.

**Como detectei.** Escrevi um teste com 8 threads disparando a mesma chave ao mesmo tempo, sincronizadas por um `CountDownLatch`, contra um Postgres real via Testcontainers. O teste exige uma única linha no banco, uma criação e sete replays. Ele falhou com:

```
ConflictException: Receivable DUP-CONCURRENT has already been settled for this assignor
```

**A correção.** O tratamento de conflito passou a cobrir os dois caminhos: tanto a violação de constraint quanto o conflito detectado pela aplicação levam a uma nova consulta pela chave. Se a chave existir, a resposta é o replay; se não existir, aí sim é `409`.

**Verificação da correção.** Como teste de concorrência depende de timing, confirmei que ele realmente pega o problema: removi a correção de propósito, rodei de novo e vi a falha reaparecer. Só então restaurei o código.

### 2.2 Valor esperado errado dentro de um teste

**O erro.** Ao escrever o teste do extrato, a IA colocou US$ 4.296,34 como valor esperado da conversão de R$ 23.337,77 pela cotação 5,4321.

**Como detectei.** Não confiei no número e calculei por fora, com `Decimal` em Python, antes de rodar o teste:

```
23337.77 / 5.4321 = 4296.27
```

O valor correto é **US$ 4.296,27**. Esse é o caso mais perigoso dos três: um valor esperado errado dentro de um teste faz o teste "passar" validando o comportamento errado, e o erro se disfarça de cobertura. Por isso todo número esperado em teste financeiro foi conferido por um cálculo independente, e não pelo próprio código que ele deveria validar.

### 2.3 Parser de valores aceitando entrada ambígua

**O erro.** O parser do campo de valor de face precisa aceitar `100.000,00` e `100,000.00`, porque a interface tem dois idiomas. A primeira versão gerada removia os separadores de forma permissiva e aceitava `1.2.3,456`, lendo como 123456.

**Como detectei.** Escrevendo os casos de rejeição do teste, percebi que a entrada malformada passava. Um erro desses em campo de dinheiro é erro de ordem de grandeza.

**A correção.** O separador de milhar só é aceito em grupos de três dígitos, e o parser rejeita qualquer outra combinação.

---

## 3. Outros erros encontrados por verificação automática

| Erro | Como apareceu |
|---|---|
| `400` sem a lista de campos inválidos quando o endpoint valida header e body juntos (o Spring lança outra exceção nesse caso) | Teste de integração que checava `fieldErrors` |
| `settledAt` devolvido com nanossegundos, enquanto o Postgres guarda microssegundos, fazendo a resposta original e o replay divergirem | Chamada HTTP real contra o banco, depois dos testes passarem |
| Entidade mapeada como `VARCHAR` para uma coluna `CHAR(64)` | Validação de schema do Hibernate na inicialização |
| `TRUNCATE` em teste antigo quebrando após a criação de uma nova chave estrangeira | Execução da suíte completa após a etapa da liquidação |

O padrão é o mesmo nos quatro: nenhum foi encontrado lendo o código. Todos apareceram porque alguma verificação automática rodava contra o comportamento real.

---

## 4. O que não deleguei

**As premissas do `SPEC.md`.** Arredondamento half-even apenas no final, câmbio vigente no momento da liquidação, prazo em meses arredondado para cima, taxas armazenadas como fração. São decisões de negócio que eu preciso defender, e a IA não tem contexto para escolher entre alternativas igualmente válidas.

**O desenho das garantias no banco.** Quais colunas formam cada `UNIQUE`, o que o `CHECK` do câmbio precisa impedir e quais tabelas são somente de inserção. A IA propôs o esqueleto, mas a decisão de onde fica a garantia final (banco, não aplicação) é arquitetural e define o comportamento sob concorrência.

**Os valores esperados dos testes.** Conferidos por cálculo independente, como no caso 2.2.

**A priorização do `REVIEW.md`.** Listar defeitos é o que qualquer IA faz bem. Ordenar por impacto de negócio, decidir o que exige hotfix imediato e concluir que o código já em produção exige auditoria das liquidações existentes depende de entender o que dói no negócio.

**O corte de escopo.** O que ficou de fora e por quê está no `DECISIONS.md`, e é decisão minha de priorização, não sugestão de ferramenta.

---

## 5. O que esse processo me ensinou sobre trabalhar com IA

A IA acertou rápido a estrutura: camadas, Strategy, DTOs, migrations, esqueleto dos testes. Os erros dela não foram de sintaxe nem de organização; foram de **semântica em situações de borda**: uma janela de concorrência, um valor esperado, uma entrada ambígua. São exatamente os erros que passam em revisão de código feita na leitura.

A conclusão prática é que o valor do meu trabalho nesse arranjo está menos em escrever o código e mais em três coisas: decidir as premissas, projetar as verificações que provam o comportamento real e desconfiar dos números. Os testes de concorrência e os golden cases não foram burocracia; foram o que separou "parece certo" de "está certo".
