<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
mode: ask
model: claude-sonnet-4-6
description: "Gere uma tabela de roteamento de tarefa para modelo"
---

# /routing-table

## Tarefa
Produza uma tabela de roteamento que mapeia tarefas do SDLC para o modelo Copilot correto (Opus / Sonnet / Haiku) e explica o trade-off custo/qualidade.

## Passos
1. Leia TASKS.md (ou o backlog) e categorize cada tarefa como: Descoberta, Design, Implementação, Refactor, Review, Mechanical.
2. Para cada categoria, recomende um modelo usando estes defaults:
 - Descoberta / ambiguous design: Opus
 - Implementação / code review: Sonnet
 - Mechanical edits / bulk renames / reformatting: Haiku
3. Para cada tarefa, estime o custo de tokens (ordem de grandeza aproximada) e justifique a escolha do modelo em uma frase.
4. Sinalize qualquer tarefa em que Sonnet possa substituir Opus com perda aceitável de qualidade para economia de custo.

## Saída
Tabela Markdown: `Task ID | Category | Recommended Model | Rationale | Est. Cost Tier`.

## Gate de Qualidade
- [ ] Toda tarefa tem modelo e justificativa
- [ ] Pelo menos um candidato a Haiku identificado (ou anotado como "none applicable")
- [ ] Níveis de custo são consistentes (a mesma categoria raramente usa níveis diferentes)
- [ ] A justificativa referencia o conteúdo da tarefa, não linguagem genérica
