<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
mode: ask
model: claude-opus-4-6
description: "Revise um DESIGN.md contra os pilares Well-Architected"
---

# /architecture-review

## Tarefa
Revise DESIGN.md (ou uma mudança arquitetural proposta) contra os pilares Microsoft Azure Well-Architected e produza uma lista priorizada de achados.

## Passos
1. Carregue DESIGN.md e quaisquer ADRs relevantes.
2. Pontue o design contra cada pilar com evidências concretas:
 - Reliability: SLO, redundancy, failure modes, retry policies
 - Security: identity, network, data, secrets, threat model
 - Cost: right-sizing, reserved capacity, idle resources
 - Operational Excellence: IaC, observability, runbooks
 - Performance Efficiency: scaling, caching, data access patterns
3. Classifique cada achado como: Critical (bloqueia go-live), Major (corrigir antes de GA), Minor (backlog).
4. Referencie a decisão arquitetural ou diagrama específico que dispara o achado.
5. Proponha uma remediação concreta por achado, com estimativa de esforço (S/M/L).

## Saída
- Tabela scorecard: `Pillar | Score (1-5) | Top Finding | Remediation`
- Lista priorizada de achados agrupada por severidade
- Três opções alternativas para o achado mais crítico

## Gate de Qualidade
- [ ] Todos os pilares revisados, nenhum pulado
- [ ] Todo achado cita um artefato específico (diagrama, ADR, parágrafo)
- [ ] Remediações são específicas, não declarações genéricas de boas práticas
- [ ] Pelo menos um achado de otimização de custo identificado (ou marcado como "already optimal")
