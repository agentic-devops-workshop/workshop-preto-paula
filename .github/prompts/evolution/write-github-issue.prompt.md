---
description: "Escreve uma GitHub issue de alta qualidade pronta para ser assumida pelo Copilot Agent na nuvem."
mode: ask
model: claude-haiku-4-5
tools: ['codebase', 'search', 'githubRepo']
---

# /write-github-issue

## Objetivo

Crie uma GitHub Issue bem estruturada, otimizada para execução autônoma pelo Copilot Agent (cloud). A issue tem critérios de aceitação claros, dicas de file path e rastreabilidade de REQ-ID.

## Quando Invocar

No início do Estágio 4, quando a equipe identifica trabalho que pode ser delegado ao Copilot Agent.

## Pré-condições

- A equipe tem um protótipo funcional do Estágio 3
- `02-spec-moderna/SPECIFICATION.md` existe com requisitos EARS
- A equipe identificou uma peça específica de trabalho para delegar

## Entradas que a Equipe Deve Fornecer

- Uma descrição da feature ou correção desejada
- REQ-IDs relacionados (se houver)
- O bounded context e arquivos provavelmente afetados

## O Que Vou Fazer

- Estruturar a issue com 5 seções obrigatórias: Contexto, Critérios de Aceite, Arquivos Afetados, Abordagem de Testes, Fora de Escopo
- Escrever critérios de aceitação em notação EARS quando aplicável
- Referenciar REQ-IDs existentes ou declarar explicitamente novo comportamento
- Sugerir labels e assignee

## O Que NÃO Vou Fazer

- Postar a issue diretamente — a equipe revisa e posta manualmente
- Escrever issues vagas — toda issue tem critérios de aceitação específicos
- Criar issues para trabalho que a equipe deve fazer por conta própria (decisões arquiteturais, correções de segurança)
- Pular a seção de abordagem de testes — o Copilot Agent precisa saber como verificar seu trabalho

## Formato de Saída

Um arquivo de rascunho em `04-evolucao/issues/<slug>.md`:

```markdown
# Issue: [Título]
## Contexto
## Critérios de Aceite
## Arquivos Provavelmente Afetados
## Abordagem de Testes
## Fora de Escopo
## Labels
## Requisitos Relacionados
```

## Definição de Pronto

- [ ] O rascunho da issue tem todas as 5 seções de conteúdo
- [ ] Critérios de aceitação são específicos e testáveis
- [ ] Pelo menos um REQ-ID é referenciado, ou "novo comportamento" é declarado com racional
- [ ] Arquivos provavelmente afetados são listados com paths relativos
- [ ] A abordagem de testes descreve quais testes adicionar ou modificar
- [ ] A issue é pequena o suficiente para um único PR (se for grande demais, divida)

## Corpo do Prompt

Você é o `@evolution-agent`. A equipe quer delegar trabalho ao Copilot Agent por meio de uma GitHub Issue.

**Passo 1 — Entender o pedido.**
Pergunte à equipe:
1. O que vocês querem que seja feito? (1-2 frases)
2. Qual bounded context isso afeta?
3. Isto implementa um `REQ-NNN` existente ou novo comportamento?
4. Quais arquivos provavelmente estão envolvidos?

**Passo 2 — Escrever a seção Contexto.**
Descreva por que este trabalho é necessário. Referencie o estado atual da codebase (o que existe) e o estado desejado (o que deve existir depois). Faça link para a spec EARS se relevante.

**Passo 3 — Escrever Critérios de Aceite.**
Escreva 3-5 critérios específicos e testáveis. Use notação EARS quando apropriado:
- "Quando [evento], o sistema deverá [comportamento]"
- "O sistema deverá [comportamento sempre verdadeiro]"

Cada critério deve ser verificável por teste ou checagem manual.

**Passo 4 — Listar Arquivos Afetados.**
Com base no input da equipe e em uma busca na codebase, liste:
- Arquivos a modificar (com paths relativos)
- Arquivos a criar (com paths sugeridos seguindo a estrutura de packages)
- Arquivos a referenciar, mas não modificar (por exemplo, a spec OpenAPI, interfaces existentes)

**Passo 5 — Definir Abordagem de Testes.**
Descreva quais testes o Copilot Agent deve escrever:
- Unit tests para novos métodos de service
- Integration tests para novos endpoints
- Testes existentes que talvez precisem de atualização

Se o bounded context já tiver padrões de teste, referencie-os para que o Copilot Agent siga o mesmo estilo.

**Passo 6 — Marcar Out of Scope.**
Declare explicitamente o que esta issue NÃO cobre. Isso evita scope creep no PR gerado por IA. Exemplos:
- "Não altera o schema do banco de dados"
- "Não modifica o fluxo de autenticação"
- "Mudanças de frontend são rastreadas em uma issue separada"

**Passo 7 — Adicionar metadata.**
Sugira labels: `enhancement` ou `bug`, o nome do bounded context, `copilot-agent`.

**Passo 8 — Escrever o rascunho.**
Gere a saída em `04-evolucao/issues/<slug>.md`, onde `<slug>` é uma versão kebab-case do título. A equipe revisa este rascunho antes de postá-lo como GitHub Issue real.

Lembre a equipe: isto é um rascunho. Revise, ajuste o escopo se necessário, depois poste manualmente pela GitHub UI ou `gh issue create`.

## Exemplo de Invocação

```
/write-github-issue feature="Add pagination to the payment list endpoint" context=payment reqs=REQ-015
```
