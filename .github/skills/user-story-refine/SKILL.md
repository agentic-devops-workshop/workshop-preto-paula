<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
name: User Story Refinement
description: 'Use ao refinar itens de backlog, dividir épicos ou validar critérios INVEST. Aciona com "refine story", "split epic", "critérios de aceite", "user story", "INVEST".'
---

# Refinamento de User Story

## Quando invocar
- "This story is too big. Help me split it."
- "Turn this feature description into user stories with critérios de aceite."
- "Check if these stories are INVEST-compliant."

## Entradas necessárias
- Descrição da feature ou épico
- Persona / tipo de usuário
- Objetivo de negócio atendido pela feature
- Quaisquer restrições conhecidas (regulatórias, técnicas, UX)

## Passos de refinamento
1. **Confirme o resultado**. Toda story deve responder: qual persona, qual resultado, por que importa.
2. **Aplique INVEST** (Independent, Negotiable, Valuable, Estimable, Small, Testable) a cada rascunho.
3. **Divida verticalmente**, nunca horizontalmente. Prefira divisões por: etapa do workflow, variação de dados, operação CRUD, caminho feliz vs. caminho de borda, regra de negócio, critério de aceitação.
4. **Escreva critérios de aceitação em Given/When/Then**. Inclua um caminho feliz, um caso de borda e um caso de falha.
5. **Rastreie até REQ-ID**. Toda story se vincula a pelo menos um requisito.

## Template de saída
```markdown
### US-NNN: <short title>
**As a** <persona>
**I want** <capability>
**So that** <business outcome>

**Acceptance criteria**
- Given <context>, when <action>, then <result>
- Given <edge>, when <action>, then <result>

**Traces to**: REQ-001, REQ-042
**Effort**: S / M / L
**Dependencies**: US-NNN (if any)
```

## Antipadrões
- Stories escritas como tarefas ("Add a button").
- Critérios de aceitação que descrevem UI em vez de comportamento.
- Divisões horizontais ("backend story" + "frontend story" para a mesma feature).
- Link de REQ-ID ausente.

## Gate de qualidade
Rejeite qualquer story que falhe no INVEST ou não tenha critérios de aceitação Given/When/Then.
