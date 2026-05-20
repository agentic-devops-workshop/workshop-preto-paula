<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
name: ADR Drafting
description: 'Use ao rascunhar Arquitetura Decision Records, avaliar alternativas ou documentar trade-offs técnicos. Aciona com "ADR", "architecture decision", "trade-off", "pick between", "why did we choose".'
---

# Rascunho de ADR

## Quando invocar
- "Draft an ADR for choosing PostgreSQL over MongoDB."
- "Document our decision to adopt event-driven architecture."
- "Revisit ADR-007 - we need to supersede it."

## Quando escrever um ADR

Escreva um ADR quando uma decisão:
- É difícil ou cara de reverter.
- Afeta mais de um time.
- Restringe escolhas futuras (technology lock-in).
- Provavelmente será questionada em 6 meses.

Não escreva um ADR para um refactor local ou ajuste de configuração reversível.

## Estrutura

```markdown
# ADR-NNN: <Decision title in imperative>

**Status**: proposed | accepted | superseded by ADR-NNN | deprecated
**Date**: YYYY-MM-DD
**Deciders**: <names>
**Context tags**: security, performance, cost

## Context
2-4 paragraphs. What is the forcing function? What constraints apply?

## Decision
One paragraph. "We will <decision>."

## Alternatives considered
- **Option A**: <summary>. Pros: ... Cons: ...
- **Option B**: <summary>. Pros: ... Cons: ...
- **Option C (chosen)**: <summary>. Pros: ... Cons: ...

## Consequences
### Positive
- ...
### Negative
- ...
### Neutral
- ...

## Follow-ups
- [ ] Update REQ-NNN
- [ ] Migrate <system>
- [ ] Revisit in Q<N>

## References
- Source 1
- Source 2
```

## Dicas de escrita
- Escreva no tempo presente ("We use X").
- Inclua pelo menos 2 alternativas rejeitadas.
- Nomeie consequências que você sabe que vão doer - seu eu futuro vai agradecer.
- Substitua, nunca delete. A história é o valor.

## Antipadrões
- ADRs escritos depois do fato para justificar decisão já tomada.
- Um ADR que agrupa 5 decisões não relacionadas.
- Sem seção de alternativas (sinaliza que não houve análise de trade-off).
- Status preso em "proposed" por meses.

## Gate de qualidade
Rejeite qualquer ADR sem seções Context, Decision, Alternatives e Consequences.
