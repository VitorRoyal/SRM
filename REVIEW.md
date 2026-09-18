# REVIEW.md — Code review do Anexo A

Revisão do endpoint de liquidação apresentado no Anexo A do enunciado, escrita como se fosse o comentário de um PR.

**Recomendação: não aprovar.** O trecho tem um erro de cálculo que faz o fundo pagar centavos por títulos de milhares de reais, e uma falha de segurança que expõe o banco inteiro. Qualquer um dos dois, sozinho, já bloqueia o merge.

Os problemas estão em ordem de severidade: impacto financeiro direto primeiro, depois segurança, integridade, concorrência e, por último, qualidade.

---

## 1. Taxas em percentual usadas como fração: o valor pago fica absurdamente errado

```java
const BASE_RATE = 1.0;
const spread = receivable.type === "DUPLICATA" ? 1.5 : 2.5;
const presentValue = receivable.face_value / Math.pow(1 + BASE_RATE + spread, receivable.term);
```

`1.0` deveria representar 1% a.m., mas na fórmula ele vale 100%. Com o spread, o divisor vira `(1 + 1 + 1.5)^prazo`, ou seja, 3,5 elevado ao prazo, em vez de 1,025.

**Impacto em produção:** uma duplicata de R$ 100.000,00 em 3 meses deveria ser paga por R$ 92.859,94. Com esse código, sai por **R$ 2.332,36**. O cedente recebe 2,5% do que tem direito. É prejuízo contratual imediato, com risco jurídico e de reputação.

**Correção:** armazenar as taxas como fração (`0.01` e `0.015`) ou dividir por 100 no cálculo, e cobrir com um teste que reproduza os golden cases ao centavo. O teste é o que impede a volta do erro.

---

## 2. SQL injection nas três queries

```java
`SELECT * FROM receivables WHERE id = ${receivableId}`
`INSERT INTO settlements (...) VALUES (${receivableId}, ${finalAmount.toFixed(2)}, '${currency}')`
`UPDATE receivables SET status = 'SETTLED' WHERE id = ${receivableId}`
```

`receivableId` e `currency` vêm direto do corpo da requisição e são concatenados no SQL.

**Impacto em produção:** um `currency` como `BRL'); DROP TABLE settlements; --` executa comando arbitrário. Em um sistema que guarda operações de crédito, isso significa vazamento e destruição de dados, com violação de LGPD.

**Correção:** queries parametrizadas em todos os casos, sem exceção. Vale adicionar uma regra de lint ou SAST no pipeline que reprove concatenação em SQL, para não depender de revisão humana.

---

## 3. Ponto flutuante binário em valores monetários

`face_value` é lido como número JavaScript e todo o cálculo usa `double`, que não representa valores decimais exatamente.

**Impacto em produção:** diferenças de centavos que não fecham na conciliação contábil e se acumulam em volume. O enunciado trata isso como eliminatório a partir de pleno.

**Correção:** tipo decimal em toda a cadeia (`BigDecimal` em Java, `decimal.js` em Node) e `NUMERIC` no banco, com arredondamento half-even em um único ponto, no resultado final.

---

## 4. Duas escritas sem transação, com a falha ignorada

```java
try {
  await db.query(`INSERT INTO settlements ...`);
  await db.query(`UPDATE receivables SET status = 'SETTLED' ...`);
} catch (e) {
  // se falhar aqui, o insert já rodou, então segue o jogo
}
```

Não há transação, e o `catch` vazio engole qualquer erro. O comentário admite o problema e segue adiante.

**Impacto em produção:** se o `UPDATE` falhar, existe uma liquidação paga para um recebível que continua disponível para ser liquidado de novo. Pior: o cliente recebe `200 OK` mesmo quando nada foi gravado, então o operador acha que deu certo. O rombo só aparece na conciliação.

**Correção:** as duas escritas em uma transação única; em caso de erro, rollback e resposta 5xx com log estruturado. Nenhuma exceção pode ser silenciada.

---

## 5. Endpoint não é idempotente

Nada impede que a mesma requisição seja processada duas vezes. Um retry de rede, um duplo clique ou um reenvio do proxy criam duas liquidações do mesmo título.

**Impacto em produção:** pagamento em duplicidade. É exatamente o incidente descrito no Anexo B do enunciado.

**Correção:** header `Idempotency-Key` guardado junto com um hash da requisição; a mesma chave com o mesmo corpo devolve a liquidação original, e com corpo diferente é rejeitada. A garantia final precisa vir do banco, com `UNIQUE` na chave e no recebível, porque em concorrência a verificação na aplicação não basta.

---

## 6. Sem controle de concorrência entre requisições simultâneas

Duas requisições paralelas leem o mesmo recebível como disponível e ambas inserem.

**Impacto em produção:** duplicidade mesmo com chaves de idempotência distintas, e o problema aparece justamente nos picos de uso.

**Correção:** constraints de unicidade no banco e tratamento da violação, devolvendo `409 Conflict`. Como alternativa, bloqueio otimista com versão do registro.

---

## 7. Câmbio: taxa não registrada e sem tratamento de indisponibilidade

```java
const rate = await fxService.getLatestRate("USD");
finalAmount = presentValue / rate;
```

A taxa usada não é gravada na liquidação, e não há tratamento para o provedor fora do ar ou sem cotação.

**Impacto em produção:** dias depois, ninguém consegue explicar de onde saiu o valor pago, o que inviabiliza auditoria. Se o provedor cair, a exceção cai no `catch` vazio ou gera erro obscuro no meio da operação.

**Correção:** gravar a taxa efetivamente usada e a data de vigência no registro da liquidação; sem cotação, recusar a operação com `422`, em vez de liquidar com valor indefinido.

---

## 8. Registro mutável e sem trilha de auditoria

A liquidação pode ser alterada por qualquer `UPDATE`, e o recebível tem apenas um campo `status`, sem histórico.

**Impacto em produção:** não é possível provar o que foi pago nem quando. Em um FIDC, isso é problema regulatório.

**Correção:** tabela somente de inserção, com trigger bloqueando `UPDATE` e `DELETE`. Correções viram um novo lançamento de estorno, nunca uma edição.

---

## 9. Sem validação de entrada e sem verificação de existência

```java
const { receivableId, currency } = req.body;
const receivable = await db.queryOne(...);
const spread = receivable.type === "DUPLICATA" ? 1.5 : 2.5;
```

Se o recebível não existir, `receivable` é nulo e o acesso a `receivable.type` quebra com erro genérico. Nada valida `receivableId`, `currency` ou o estado do recebível (já liquidado, vencido).

**Impacto em produção:** `500` sem explicação para o operador e ruído no monitoramento.

**Correção:** validação de schema na entrada, `404` quando o recebível não existe e `409` quando já foi liquidado.

---

## 10. Regra de negócio espalhada e acoplada ao controller

O spread está num ternário dentro do endpoint, a taxa base é uma constante no arquivo e a fórmula está misturada com o código HTTP.

**Impacto em produção:** um tipo novo de recebível exige alterar o controller, o que é justamente o cenário de mudança mais comum. Também impede testar o cálculo sem subir a aplicação.

**Correção:** mover o cálculo para uma camada de domínio, com uma strategy por tipo de recebível e a taxa base vinda de configuração ou do banco. O controller apenas recebe a requisição e delega.

---

## 11. Semântica HTTP incorreta

`res.status(200).json({ ok: true, ... })` é usado inclusive quando a gravação falhou. Uma criação bem-sucedida deveria responder `201`.

**Impacto em produção:** clientes e monitoramento não conseguem distinguir sucesso de falha. Um alerta baseado em taxa de erro nunca dispara.

**Correção:** `201` na criação, `4xx` para erro do cliente e `5xx` para falha interna, com corpo de erro padronizado.

---

## Resumo

| # | Problema | Severidade | Efeito |
|---|---|---|---|
| 1 | Taxas percentuais usadas como fração | Crítica | Paga 2,5% do valor devido |
| 2 | SQL injection | Crítica | Vazamento e destruição de dados |
| 3 | Ponto flutuante em dinheiro | Alta | Erro de centavos acumulado |
| 4 | Sem transação e exceção engolida | Alta | Liquidação pela metade reportada como sucesso |
| 5 | Sem idempotência | Alta | Pagamento em duplicidade |
| 6 | Sem controle de concorrência | Alta | Duplicidade sob carga |
| 7 | Câmbio não registrado e sem fallback | Média | Cálculo não auditável |
| 8 | Registro mutável | Média | Risco regulatório |
| 9 | Sem validação de entrada | Média | Erros 500 genéricos |
| 10 | Regra de negócio no controller | Média | Custo alto de manutenção |
| 11 | Status HTTP incorreto | Baixa | Monitoramento cego |

**Ordem sugerida de correção:** os itens 1 e 2 exigem hotfix imediato. Os itens 3, 4, 5 e 6 entram na mesma refatoração, porque dependem do redesenho da transação. Os demais seguem em sequência.

Como o código já pode ter rodado em produção, a correção precisa vir acompanhada de uma auditoria das liquidações existentes: identificar valores calculados errado e tratar os pagamentos afetados.
