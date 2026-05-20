---
description: "Traduz regras de negócio confirmadas em requisitos em notação EARS para o sistema moderno."
mode: ask
model: claude-opus-4-7
tools: ['codebase', 'search']
---

# /write-ears-spec

## Objetivo

Transforme regras de negócio confirmadas do Estágio 1 em requisitos EARS formais com REQ-IDs únicos, classificação de padrão, critérios de aceitação e rastreabilidade completa à fonte. Mistérios e regras inferred são excluídos da spec, mas documentados separadamente.

## Quando Invocar

Depois que os bounded contexts forem decididos (`/carve-bounded-contexts`), para que os requisitos possam ser organizados por contexto.

## Pré-condições

- `01-arqueologia/business-rules-catalog.md` existe com regras classificadas
- `02-spec-moderna/bounded-contexts.md` existe com contextos finalizados

## Entradas que a Equipe Deve Fornecer

- Confirmação de quais regras incluir (somente regras confirmed, ou também regras inferred selecionadas que a equipe quer promover)
- Quaisquer requisitos adicionais que não venham do código legado (por exemplo, "o sistema deve ter uma REST API" — novas capacidades)

## O Que Vou Fazer

- Filtrar o catálogo de regras de negócio para somente regras confirmadas (a menos que a equipe promova regras inferidas específicas)
- Para cada regra, escrever 1-3 declarações de requisito EARS
- Atribuir REQ-IDs únicos no formato `REQ-NNN`
- Mapear cada requisito para seu bounded context
- Escrever critérios de aceitação para cada requisito
- Separar mistérios e perguntas abertas em uma seção dedicada

## O Que NÃO Vou Fazer

- Criar requisitos sem rastreabilidade de fonte — todo REQ rastreia para uma regra confirmed
- Promover mistérios a requisitos — mistérios ficam na seção de open questions
- Fabricar critérios de aceitação que não podem ser testados — cada critério deve ser verificável
- Pular validação de padrão EARS — todo requisito deve corresponder a um dos 6 padrões

## Formato de Saída

Um arquivo Markdown em `02-spec-moderna/SPECIFICATION.md`:

```markdown
# SPECIFICATION — Modern System
## Bounded Context: [Name]
### REQ-001: [Statement]
- EARS Pattern: [pattern]
- Source: [rule #, file, line]
- Critérios de Aceite:
  - [ ] ...
...
## Open Questions (Not Requirements Yet)
## Traceability Matrix
```

## Definição de Pronto

- [ ] Existem pelo menos 10 requisitos EARS com REQ-IDs únicos
- [ ] Todo requisito cita sua regra-fonte e arquivo legado
- [ ] Todo requisito tem pelo menos 2 critérios de aceitação
- [ ] Requisitos são agrupados por bounded context
- [ ] Mistérios aparecem somente em "Open Questions", nunca como requisitos
- [ ] Uma matriz de rastreabilidade conecta todo REQ à sua regra-fonte

## Corpo do Prompt

Você é o `@architect-agent`. A equipe precisa escrever a especificação EARS para o sistema moderno com base no que foi descoberto no Estágio 1.

**Passo 1 — Carregar regras confirmed.**
Leia `01-arqueologia/business-rules-catalog.md`. Filtre somente regras classificadas como "confirmed". Liste-as com suas referências de fonte.

Se a equipe quiser promover regras "inferred" específicas, peça confirmação explícita por regra. Cada regra promovida deve incluir uma nota: "Promovida de inferred — decisão da equipe, [motivo]."

**Passo 2 — Mapear regras para bounded contexts.**
Leia `02-spec-moderna/bounded-contexts.md`. Para cada regra confirmed, determine qual bounded context é dono dela com base nos dados e programas envolvidos. Se uma regra atravessar múltiplos contextos, sinalize para discussão — talvez precise ser dividida ou atribuída a uma camada coordenadora.

**Passo 3 — Escrever declarações de requisito EARS.**
Para cada regra, escreva 1-3 declarações de requisito seguindo padrões EARS:

- **Ubiquitous**: "O sistema deverá [ação]." — Sempre se aplica, sem trigger.
- **Event-driven**: "Quando [evento], o sistema deverá [ação]." — Acionado por um evento específico.
- **State-driven**: "Enquanto [estado], o sistema deverá [ação]." — Ativo durante um estado.
- **Optional**: "Onde [condição], o sistema deverá [ação]." — Somente quando a condição vale.
- **Unwanted**: "Se [condição indesejada], então o sistema deverá [ação]." — Tratamento de erro/rejeição.
- **Complex**: "Enquanto [estado], quando [evento], o sistema deverá [ação]." — Combinação.

Valide cada declaração contra o padrão. Se uma declaração não se encaixar claramente em nenhum padrão, reformule até encaixar ou divida em múltiplos requisitos.

**Passo 4 — Atribuir REQ-IDs.**
Numere requisitos sequencialmente: REQ-001, REQ-002 etc. Agrupe por bounded context.

**Passo 5 — Escrever critérios de aceitação.**
Para cada requisito, escreva pelo menos 2 critérios de aceitação testáveis. Cada critério deve ser:
- Específico (não "o sistema funciona corretamente")
- Mensurável (tem condição de pass/fail)
- Rastreável (faz link de volta ao requisito)

Formato: `Given [precondition], when [action], then [expected result]`.

**Passo 6 — Tratar open questions.**
Para todo mistério de `01-arqueologia/mysteries-found.md` classificado como "blocks-stage-2", adicione-o à seção "Open Questions". Não o converta em requisito. Anote qual informação é necessária para resolvê-lo.

**Passo 7 — Construir matriz de rastreabilidade.**
Crie uma tabela conectando: `| REQ-ID | EARS Pattern | Source Rule # | Source File | Bounded Context |`.

**Passo 8 — Escrever no arquivo.**
Gere a saída em `02-spec-moderna/SPECIFICATION.md`. Se o arquivo já existir, pergunte à equipe se deve sobrescrever ou anexar.

## Exemplo de Invocação

```
/write-ears-spec rules=01-arqueologia/business-rules-catalog.md contexts=02-spec-moderna/bounded-contexts.md
```
