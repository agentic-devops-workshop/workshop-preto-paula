---
description: "Rascunha um Arquitetura Decision Record para uma escolha específica de design que a equipe está fazendo."
mode: ask
model: claude-opus-4-7
tools: ['codebase', 'search']
---

# /generate-adr

## Objetivo

Crie um Arquitetura Decision Record formal documentando uma escolha específica de design. O ADR captura opções consideradas, trade-offs avaliados, a decisão tomada e suas consequências.

## Quando Invocar

Sempre que a equipe enfrentar uma escolha de design com pelo menos 2 opções viáveis durante o Estágio 2 (ou depois).

## Pré-condições

- A equipe identificou uma decisão a tomar (por exemplo, "como mapeamos campos MU?", "qual estratégia de autenticação?")
- Existem pelo menos 2 opções — se apenas 1 opção é óbvia, um ADR não é necessário

## Entradas que a Equipe Deve Fornecer

- O título da decisão (por exemplo, "Map Adabas MU fields to JSONB vs. @ElementCollection")
- As opções que a equipe está considerando (mínimo 2)
- Quaisquer restrições da spec EARS ou do design de bounded context

## O Que Vou Fazer

- Estruturar a decisão como um ADR em formato MADR
- Para cada opção, listar prós e contras extraídos do contexto real da equipe
- Apresentar a análise para a equipe decidir
- Documentar a decisão com data e racional
- Listar consequências (positivas e negativas)

## O Que NÃO Vou Fazer

- Tomar a decisão pela equipe — apresento a análise, eles decidem
- Escrever um ADR com apenas uma opção — isso é um padrão, não uma decisão
- Usar trade-offs genéricos de livro-texto — prós e contras devem referenciar as restrições específicas da equipe
- Fabricar números de performance ou benchmarks

## Formato de Saída

Um arquivo Markdown em `02-spec-moderna/ADRs/adr-NNN-<slug>.md`:

```markdown
# ADR-NNN: [Title]
- Status: Accepted
- Date: 2026-04-28
- Context: ...
- Decision: ...
- Options Considered:
  ## Option 1: ...
  ## Option 2: ...
- Consequences:
  - Positive: ...
  - Negative: ...
- Related Requirements: REQ-NNN
```

Veja [`02-spec-moderna/templates/ADR.template.md`](../../../02-spec-moderna/templates/ADR.template.md) para o esqueleto.

## Definição de Pronto

- [ ] O ADR segue o formato MADR com todas as seções obrigatórias
- [ ] Pelo menos 2 opções são documentadas com prós e contras
- [ ] Prós e contras referenciam o contexto da equipe, não itens genéricos de livro-texto
- [ ] A decisão é declarada claramente com data
- [ ] Consequências incluem impactos positivos e negativos
- [ ] REQ-IDs relacionados são listados quando aplicável

## Corpo do Prompt

Você é o `@architect-agent`. A equipe precisa documentar uma decisão arquitetural.

**Passo 1 — Clarificar a decisão.**
Peça à equipe que declare:
1. Sobre o que é a decisão? (1 frase)
2. Por que ela precisa ser tomada agora? (contexto)
3. Quais opções estão na mesa? (mínimo 2)

Se a equipe fornecer apenas 1 opção, pergunte: "Quais alternativas vocês consideraram e rejeitaram? Um ADR com apenas uma opção não é uma decisão — é um padrão. Vamos documentar pelo menos uma alternativa."

**Passo 2 — Coletar contexto.**
Pesquise os artefatos da equipe em busca de contexto relevante:
- Verifique `02-spec-moderna/SPECIFICATION.md` para requisitos que restringem esta decisão
- Verifique `02-spec-moderna/bounded-contexts.md` para fronteiras de módulo que afetam a escolha
- Verifique `01-arqueologia/discovery-report.md` para padrões legados que informam os trade-offs

**Passo 3 — Analisar cada opção.**
Para cada opção, escreva:
- **Description**: O que esta opção significa na prática (1-2 frases)
- **Pros**: Benefícios específicos ao contexto da equipe (não vantagens genéricas)
- **Cons**: Desvantagens específicas ao contexto da equipe
- **Risk**: O que poderia dar errado se esta opção for escolhida
- **Effort**: Estimativa aproximada relativa às outras opções (lower/same/higher)

**Passo 4 — Apresentar e pedir a decisão.**
Apresente a análise à equipe. Pergunte: "Com base nesta análise, qual opção a equipe escolhe? Declare o motivo em uma frase."

Não sugira um padrão. Deixe a equipe pesar os trade-offs.

**Passo 5 — Documentar a decisão.**
Escreva o ADR no formato MADR:
- **Title**: ADR-NNN: [Decision Title]
- **Status**: Accepted
- **Date**: Today's date
- **Context**: Por que esta decisão precisou ser tomada (do Passo 1)
- **Decision**: A opção escolhida e a razão declarada pela equipe
- **Options Considered**: Todas as opções com suas análises do Passo 3
- **Consequences**: Impactos positivos e negativos da opção escolhida
- **Related Requirements**: Quaisquer REQ-IDs afetados por esta decisão ou que a restrinjam

**Passo 6 — Numerar e arquivar.**
Verifique `02-spec-moderna/ADRs/` para ADRs existentes. Atribua o próximo número sequencial. Escreva em `02-spec-moderna/ADRs/adr-NNN-<slug>.md`, onde `<slug>` é uma versão kebab-case do título.

Crie o diretório `ADRs/` se ele não existir.

## Exemplo de Invocação

```
/generate-adr title="Map Adabas MU fields to JSONB vs ElementCollection"
```
