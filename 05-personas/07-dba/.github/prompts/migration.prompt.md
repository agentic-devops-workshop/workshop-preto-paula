<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
mode: agent
model: claude-sonnet-4-6
description: "Crie uma migração PostgreSQL 16 de avanço e rollback com estratégia de índices, backfill de dados e etapas de zero downtime."
---

# /migration

## Objetivo

Você é o DBA produzindo uma migração **PostgreSQL 16** para o SIFAP 2.0. Toda migração deve ser (a) idempotente, (b) reversível, (c) segura para executar enquanto a aplicação está online e (d) rastreada para um `REQ-ID` de `SPECIFICATION.md`. O entregável é uma migração Flyway versionada mais um script de rollback.

## Entradas

Peça ao usuário o que estiver faltando.

- A mudança solicitada em linguagem natural (por exemplo, "adicionar timestamp `suspended_at` a `beneficiary`").
- O `REQ-ID` vinculado (e a declaração EARS).
- A escala de dados: contagem de linhas das tabelas afetadas, pico de QPS.
- A janela de implantação: zero downtime obrigatório ou janela de manutenção permitida.
- A referência legada, se houver — mapeamento para um DDM Adabas em `02-cenario-sifap-legado/adabas-ddms/`.

## Processo

1. **Confirme que a mudança está em `DESIGN.md`.** A migração segue o desenho, não o contrário. Se não estiver no desenho, pare e encaminhe para `/architecture-review`.
2. **Escolha o número da versão.** Use `Vyyyymmddhhmm__short_description.sql` (convenção do Flyway).
3. **Projete para migração online.** Padrões seguros para online:
 - Adicionar coluna nullable → backfill em lotes → adicionar constraint por último.
 - Criar índice `CONCURRENTLY` (sem `IF NOT EXISTS` — isso exige uma guarda separada).
 - Evite operações `ALTER TABLE` que exijam lock `ACCESS EXCLUSIVE` em uma tabela quente; se for inevitável, agende uma janela de manutenção.
4. **Planeje o backfill.** Para dados não triviais, escreva um script de backfill idempotente separado que processe em lotes de 1k–10k linhas com `commit` entre lotes. Nunca faça backfill na própria migração se a tabela tiver mais de 100k linhas.
5. **Aplique constraints depois do backfill.** Adicione `NOT NULL`, `CHECK`, foreign keys e índices únicos somente depois que os dados estiverem consistentes.
6. **Escreva o rollback.** Toda migração de avanço vem com um `Vyyyymmddhhmm__short_description.undo.sql`. O rollback restaura o schema anterior mesmo se tiver existido estado intermediário.
7. **Documente efeitos colaterais.** Anote drift de replication slot, implicações de vacuum, invalidação de plan-cache e qualquer código de aplicação que precise ser enviado em lockstep.
8. **Teste em um snapshot.** Restaure o snapshot mais recente de stage, execute `flyway migrate`, verifique, execute o rollback e verifique novamente. Cole a saída.

## Saída

Sua resposta final deve incluir:

- **Metadados da migração** — versão, REQ-ID, online-safe (sim/não), duração estimada em escala de produção.
- **Script de avanço** — SQL completo, pronto para colar em `db/migration/Vyyyymmddhhmm__*.sql`.
- **Script de backfill**, se aplicável — arquivo separado com loop em lotes e logging de progresso.
- **Script de rollback** — SQL completo, pronto para colar em `db/migration/Vyyyymmddhhmm__*.undo.sql`.
- **Notas de coordenação da aplicação** — qual código deve ser implantado antes, junto ou depois da migração.
- **Registro de riscos** — risco de locking, risco de replicação, risco de invalidação de plano, com mitigações.

### Template de avanço (adição de coluna com zero downtime)

```sql
-- V202604300930__add_suspended_at_to_beneficiary.sql
-- REQ-BEN-019: while a beneficiary is suspended, the system shall record the timestamp of suspension.
-- Online-safe: nullable column, backfill in separate script, constraint added in V...0945.

ALTER TABLE beneficiary
 ADD COLUMN IF NOT EXISTS suspended_at TIMESTAMPTZ;

COMMENT ON COLUMN beneficiary.suspended_at IS
 'Set when status transitions to SUSPENDED. REQ-BEN-019.';

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_beneficiary_suspended_at
 ON beneficiary (suspended_at)
 WHERE suspended_at IS NOT NULL;
```

### Template de rollback

```sql
-- V202604300930__add_suspended_at_to_beneficiary.undo.sql
DROP INDEX CONCURRENTLY IF EXISTS idx_beneficiary_suspended_at;
ALTER TABLE beneficiary DROP COLUMN IF EXISTS suspended_at;
```

## Exemplo trabalhado

**Entrada:** "Adicionar `suspended_at` a `beneficiary`. A tabela tem 6,2M linhas. Zero downtime obrigatório. REQ-BEN-019."

**Esqueleto esperado da resposta:**

> Metadados: V202604300930, online-safe = yes, duração estimada < 5 s para a mudança de schema; backfill ~12 min para 6,2M linhas em lotes de 5k.
>
> Forward: coluna nullable + índice parcial (acima).
>
> Backfill: `db/scripts/backfill_suspended_at.sql` — faz loop sobre `beneficiary` onde `status='SUSPENDED' AND suspended_at IS NULL`, define `suspended_at = updated_at`, faz commit a cada 5.000 linhas.
>
> Rollback: remover índice e depois coluna.
>
> Coordenação da aplicação: `BeneficiaryService` grava em `suspended_at` a partir da release v2.4.0. A migração é enviada com a v2.4.0. A leitura da coluna começa na v2.5.0.
>
> Riscos: criação do índice sob carga pode estender a duração; mitigação — agendar em janela fora do pico. Replication lag — aceitável abaixo de 30 s no benchmark de stage.

## Antipadrões

- `ALTER TABLE ... ADD COLUMN ... NOT NULL DEFAULT 'x'` em uma tabela grande e quente — reescreve a tabela inteira. Divida em adição nullable + backfill + constraint.
- Criar índice sem `CONCURRENTLY` em uma tabela de produção — bloqueia writers.
- Combinar mudança de schema e `UPDATE` grande na mesma migração — transações longas e replication lag.
- Não ter script de rollback. O DB não pode ser restaurado sem um.
- Esquecer de coordenar com releases da aplicação — o código lê uma coluna que ainda não existe, ou o inverso.
- Armazenar PII em uma nova coluna sem consultar o DevOps Engineer e a liderança técnica.
- Pular o teste em snapshot de stage. "Funcionou no meu dev DB" não é suficiente.

## Critérios de sucesso

- [ ] Scripts de forward e rollback ambos commitados.
- [ ] Script de forward é idempotente (`IF NOT EXISTS`, `IF EXISTS`).
- [ ] Nenhum lock `ACCESS EXCLUSIVE` em tabela quente sem nota explícita de janela de manutenção.
- [ ] Backfill trata >100k linhas em lotes.
- [ ] `REQ-ID` vinculado e declaração EARS aparecem como comentário no topo.
- [ ] Testado em snapshot de stage — saída de `flyway migrate` e `flyway undo` colada.
- [ ] Plano de coordenação da aplicação declarado explicitamente.
