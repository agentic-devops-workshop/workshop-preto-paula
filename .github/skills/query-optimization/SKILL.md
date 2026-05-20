<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
name: Otimização de consultas
description: "Use ao investigar consultas lentas, desenhar índices ou revisar planos de execução. Acionadores: 'slow query', 'explain plan', 'index', 'query tuning', 'N+1', 'table scan'."
---

# Otimização de consultas

## Quando invocar
- "Esta consulta está lenta."
- "Por que ela não está usando o índice?"
- "Devo adicionar um índice em...?"
- "Revise esta saída de EXPLAIN."

## Fluxo diagnóstico
1. **Meça antes de otimizar** - capture a baseline (latência p50/p95, linhas examinadas, linhas retornadas, leituras lógicas).
2. **Obtenha o plano**: `EXPLAIN (ANALYZE, BUFFERS)` no PostgreSQL, `EXPLAIN ANALYZE FORMAT=JSON` no MySQL 8, `SET STATISTICS IO, TIME ON` no SQL Server.
3. **Procure os suspeitos comuns**:
 - **Seq Scan / Table Scan** em tabela grande com predicado seletivo → índice ausente
 - **Estimativa de linhas errada por >10×** → estatísticas obsoletas, execute `ANALYZE`
 - **Nested Loop com muitas linhas externas** → deveria ser Hash/Merge join
 - **Sort derramado para disco** → `work_mem` baixo demais ou índice ausente no ORDER BY
 - **Filtro depois do join** em vez de pushdown → reescreva ou adicione índice de predicado
4. **Proponha a menor mudança**: índice, reescrita, atualização de estatísticas, ajuste de parâmetro.
5. **Valide**: execute novamente com ANALYZE, confirme que o plano mudou e a latência caiu. Nunca faça "ship and hope".

## Heurísticas de design de índices
- **Colunas de igualdade primeiro**, depois range, depois sort (a regra ESR).
- **Covering index** (colunas INCLUDE) para consultas read-heavy evita heap lookups.
- **Índice parcial** para filtros altamente seletivos em dados enviesados (`WHERE status = 'pending'`).
- Todo índice custa escritas - justifique cada um.

## Antipadrões
- `SELECT *` em caminhos quentes - força acesso ao heap, quebra covering indexes.
- `WHERE func(col) = x` - impede uso de índice; armazene coluna computada ou use índice de expressão.
- N+1 vindo do ORM - corrija no ORM (eager load), não com um índice.
- "Adicionar índice em toda coluna" - desperdiça armazenamento e desacelera escritas.

## Referências
- [Use The Index, Luke!](https://use-the-index-luke.com/)
- [PostgreSQL - Performance Tips](https://www.postgresql.org/docs/current/performance-tips.html)
- [SQL Server - Query Store](https://learn.microsoft.com/sql/relational-databases/performance/monitoring-performance-by-using-the-query-store)
