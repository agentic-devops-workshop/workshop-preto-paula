<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
name: Migração segura de schema
description: "Use ao planejar uma mudança de schema online, migração com zero downtime ou rollback de um deploy que alterou uma tabela. Acionadores: 'migration', 'ALTER TABLE', 'zero-downtime', 'expand-contract', 'backfill'."
---

# Migração segura de schema

## Quando invocar
- "Planeje a migração para adicionar a coluna X."
- "Podemos renomear esta coluna sem downtime?"
- "Como removemos esta tabela com segurança?"

## Padrão expand / migrate / contract
Toda mudança de schema que toca tráfego vivo passa por **três deploys**, nunca um só.

1. **Expand** - adicione a nova forma ao lado da antiga (nova coluna nullable, nova tabela, novo índice). Nenhuma leitura/escrita a usa ainda.
2. **Migrate** - faça dual-write na forma antiga e na nova, backfill das linhas históricas, alterne leituras para a nova forma atrás de uma flag.
3. **Contract** - remova a forma antiga somente depois que a nova forma for autoritativa por pelo menos um ciclo de release.

## Regras práticas
- **Aditivo é sempre seguro**: nova coluna nullable, novo índice (CONCURRENTLY / ONLINE), nova tabela.
- **Destrutivo nunca é um deploy só**: drop column, rename column, change type, drop table, adicionar NOT NULL.
- **Backfills rodam em lotes** com LIMIT, pausa entre lotes, idempotentes. Nunca execute `UPDATE whole_table SET …` de uma vez.
- **Construção de índices**: `CREATE INDEX CONCURRENTLY` (Postgres), `ONLINE=ON` (MySQL 8 / SQL Server). Observe lock escalation.
- **Renomeações**: NÃO renomeie in place. Adicione nova coluna → dual-write → backfill → alterne leituras → remova a antiga.

## Checklist de pre-flight
- [ ] Migration tem plano **forward** e **rollback** por escrito.
- [ ] Duração estimada em uma **cópia de produção** (nunca estime em dev).
- [ ] Impacto de lock avaliado (`pg_locks`, `SHOW ENGINE INNODB STATUS`, `sys.dm_tran_locks`).
- [ ] Tamanho de lote do backfill escolhido com base no orçamento de replication lag.
- [ ] Monitoramento instalado para replica lag, transações longas e deadlocks.
- [ ] Feature flag ou caminho de dual-read instalado antes da etapa migrate.

## Alertas vermelhos - não envie
- `ALTER TABLE` único que toma lock completo em tabela grande.
- Migration acoplada ao deploy da aplicação (não consegue fazer rollback independentemente).
- Etapa irreversível sem backup.
- Backfill que reescreve todas as linhas em uma transação.

## Referências
- [Braintree - PostgreSQL at Scale: Safe Migrations](https://medium.com/paypal-tech/postgresql-at-scale-database-schema-changes-without-downtime-20d3749ed680)
- [GitHub - gh-ost online schema migration](https://github.com/github/gh-ost)
- [Martin Fowler - Evoluçãoary Database Design](https://martinfowler.com/articles/evodb.html)
