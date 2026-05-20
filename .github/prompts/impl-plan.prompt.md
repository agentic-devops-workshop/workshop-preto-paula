<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
mode: ask
model: claude-sonnet-4-6
description: "Crie IMPLEMENTATION_PLAN.md com tarefas por fases"
---

# /impl-plan

## Tarefa
Produza IMPLEMENTATION_PLAN.md que sequencia tarefas em fases, marca tarefas paralelizáveis, seleciona um modelo por tarefa e define critérios de saída.

## Passos
1. Leia SPECIFICATION.md, DESIGN.md e TASKS.md.
2. Agrupe tarefas em fases com base na ordem de dependências (fundação → features → hardening).
3. Dentro de cada fase, marque tarefas como `[P]` paralelizáveis se tocarem arquivos disjuntos e não tiverem dependência de runtime.
4. Atribua um modelo por tarefa: Opus (arquitetural), Sonnet (implementação), Haiku (mecânico).
5. Defina uma Definição de Pronto por fase: testes passando, docs atualizadas, code review completo.

## Saída
Um arquivo IMPLEMENTATION_PLAN.md com:
- Títulos de fase, cada um com objetivo, estimativa de duração e critérios de saída
- Tabela de tarefas por fase: `Task ID | Title | [P] | Model | Est. Effort | Traces To (REQ-ID)`
- Seção de riscos globais com mitigações

## Gate de Qualidade
- [ ] Toda tarefa rastreia para pelo menos um REQ-ID
- [ ] Tarefas `[P]` realmente tocam arquivos independentes (verificado por grep)
- [ ] Critérios de saída de fase são mensuráveis
- [ ] Nenhuma tarefa é maior que 1 dia de esforço sem decomposição
