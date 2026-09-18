# DECISIONS.md — O que foi cortado, simplificado e por quê

Nível-alvo: **júnior**, com itens de **pleno** onde o custo era baixo e o ganho grande. Este documento registra o que ficou de fora de propósito.

---

## 1. O que entrei além do nível júnior, e por quê

| Item | Nível | Por que valeu o custo |
|---|---|---|
| Idempotência na liquidação, implementada e testada | Pleno | O enunciado exige idempotência para todos os níveis no item 4.1.3, e é o coração do problema de negócio (pagamento em duplicidade) |
| Tratamento global de erros com Problem Details | Pleno | Sem isso, erro vira `500` genérico, que o enunciado lista como anti-padrão |
| Paginação server-side no extrato | Pleno | Um extrato que carrega tudo não serve para um fundo com volume |
| SQL nativo no relatório | Pleno | Diferencial citado no item 4.1.6, e evita N+1 |
| Testes de integração com Testcontainers | Pleno | Triggers, constraints e `NUMERIC` só podem ser validados em Postgres real |
| Teste de concorrência com 8 threads | Sênior | Foi ele que encontrou um bug real de idempotência (detalhado no `AI_USAGE.md`) |

---

## 2. O que cortei

### 2.1 Observabilidade (sênior)
**Cortado:** métricas de negócio (liquidações por minuto, latência do motor) e logs estruturados em JSON.
**Por quê:** é item de nível sênior e não muda a corretude do que entreguei. Mantive logs nos pontos que importam para investigar um incidente: criação e replay de liquidação, com id e chave de idempotência.
**Se fosse fazer:** Actuator com Micrometer, um contador de liquidações por moeda e um timer no motor de precificação.

### 2.2 Resiliência na integração de câmbio (sênior)
**Cortado:** timeout, retry e circuit breaker.
**Por quê:** as cotações vêm do próprio banco, e não de um provedor externo, então não existe a falha que o circuit breaker protegeria. Criar um provedor mockado só para adicionar resiliência seria complexidade artificial.
**O que existe hoje:** se não houver cotação vigente, a operação é recusada com `422`, em vez de liquidar com valor indefinido.

### 2.3 Bloqueio otimista (sênior)
**Cortado:** `@Version` nas entidades.
**Por quê:** as tabelas são somente de inserção, então não existe atualização concorrente para proteger. A corrida real é entre duas inserções, e ela é resolvida pelas constraints `UNIQUE`, com teste que comprova.

### 2.4 CI, diagrama C4 e ADRs (sênior e staff)
**Cortado:** pipeline no GitHub Actions, diagramas C4 e ADRs formais.
**Por quê:** são exigências de níveis acima do alvo. As decisões estruturais estão registradas no `SPEC.md`, nas descrições dos PRs e neste arquivo.
**Observação:** o CI seria o próximo passo, porque é barato: um workflow rodando `./mvnw test` e o lint do frontend.

### 2.5 Dockerfile da aplicação
**Cortado:** imagem da API e do frontend.
**Por quê:** o Compose sobe o banco, que é a dependência que realmente atrapalha a instalação. Backend e frontend rodam com um comando cada, e o Maven Wrapper dispensa instalar Maven.
**Custo assumido:** quem clonar o projeto precisa de Java 21 e Node instalados.

### 2.6 Autenticação e autorização
**Cortado:** login, perfis e controle de acesso.
**Por quê:** o enunciado não pede, e a rubrica não pontua. Em um sistema real, liquidar seria uma operação com alçada.

---

## 3. Simplificações conscientes

### 3.1 Taxa base sem endpoint de cadastro
Existe `GET /base-rates/current`, mas não há `POST`. A taxa muda por migration. O enunciado pede endpoint de atualização apenas para câmbio, e o modelo já guarda histórico por vigência, então adicionar a escrita depois é trivial.

### 3.2 Apenas USD como moeda estrangeira
O enum tem BRL e USD, e o `CHECK` no banco aceita só USD em `exchange_rate`. É o escopo do enunciado. Uma moeda nova exige migration e uma entrada no enum; o motor de precificação não muda, porque a conversão é genérica.

### 3.3 O recebível nasce junto com a liquidação
Não existe cadastro separado de recebíveis. O `POST /settlements` cria o recebível e a liquidação na mesma transação.
**Por quê:** o fluxo do painel do operador é esse (ele digita os dados do título e liquida), e não há tela nem endpoint que precise de um recebível ainda não liquidado.
**Limite:** para registrar um lote de recebíveis antes de decidir quais liquidar, seria preciso separar os dois cadastros.

### 3.4 Valores monetários trafegam como número no JSON
**Risco conhecido:** JavaScript representa números como ponto flutuante binário.
**Mitigação:** o frontend não calcula nada, apenas formata; e a entrada é limitada a 13 dígitos inteiros com 2 decimais, faixa em que a ida e volta é exata.
**Alternativa mais rígida:** serializar como string, ao custo de perder a leitura direta do JSON.

### 3.5 Mensagens de erro do backend em inglês
As mensagens de negócio (por exemplo, recebível já liquidado) vêm prontas do backend e aparecem no toast em inglês, mesmo com a interface em português.
**Por que não resolvi:** a solução correta é o backend devolver um código de erro estável e o frontend traduzir. Isso mexe no contrato de erro de todos os endpoints, e preferi manter o escopo.

### 3.6 Frontend com dois idiomas e testes focados
A interface tem português e inglês, e os testes automatizados do frontend cobrem a leitura de valores digitados, que é onde mora o risco de erro de ordem de grandeza. Não escrevi testes de componente nem end-to-end: o fluxo completo foi verificado manualmente, e a regra de negócio, que é o que não pode quebrar, está coberta no backend.

### 3.7 Mappers escritos à mão
Sem MapStruct. São poucos DTOs, e uma dependência com geração de código custaria mais do que as poucas linhas que ela economizaria.

---

## 4. Limitação conhecida da imutabilidade

Os triggers bloqueiam `UPDATE` e `DELETE`, mas **não bloqueiam `TRUNCATE`**, que é justamente o que os testes usam para limpar o banco entre cenários. Em produção, o usuário da aplicação não deve ter permissão de `TRUNCATE`, e essa restrição pertence ao provisionamento do banco, não à migration.

---

## 5. Se eu tivesse mais tempo, nesta ordem

1. **CI** rodando testes e lint em cada PR: barato e protege tudo o que já existe.
2. **Códigos de erro traduzíveis**, resolvendo a mistura de idiomas na interface.
3. **Métricas de negócio**, para enxergar volume e latência das liquidações.
4. **Cadastro de recebíveis separado da liquidação**, abrindo espaço para operação em lote.
