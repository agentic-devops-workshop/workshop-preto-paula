---
description: "Entrega uma issue ao GitHub Copilot Agent na nuvem e acompanha o PR resultante."
mode: ask
model: claude-sonnet-4-6
tools: ['githubRepo', 'fetch']
---

# /delegate-to-copilot-agent

## Objetivo

Guie a equipe para postar uma issue revisada no GitHub e preparar uma watch-list para monitorar o PR gerado por IA. Este é um workflow de delegação — a equipe é dona da revisão e do merge.

## Quando Invocar

Depois que a equipe revisou e aprovou um rascunho de issue de `/write-github-issue`.

## Pré-condições

- An issue draft exists at `04-evolucao/issues/<slug>.md`
- A equipe revisou e aprovou o rascunho
- A equipe tem acesso de push ao repositório GitHub

## Entradas que a Equipe Deve Fornecer

- O file path do rascunho da issue
- Confirmação de que o rascunho está pronto para postar

## O Que Vou Fazer

- Conduzir a equipe na postagem da issue no GitHub
- Preparar um documento de watch-list com resultados esperados
- Fornecer um guia de revisão para quando o PR chegar

## O Que NÃO Vou Fazer

- Postar a issue pela equipe — eles fazem manualmente para entender o workflow
- Assumir que o PR da IA estará correto — preparo a equipe para revisá-lo criticamente
- Fazer merge de qualquer PR — a equipe toma a decisão de merge
- Pular o guia de revisão — todo PR delegado precisa de revisão humana

## Formato de Saída

Um arquivo de rastreamento de delegação em `04-evolucao/delegations/<issue-slug>.md`:

```markdown
# Delegação: [Título da Issue]
## Referência da Issue
## Resultados Esperados
## Watch-List
## Guia de Revisão: O Que Observar
## Responsabilidade da Equipe
```

## Definição de Pronto

- [ ] A equipe tem instruções para postar a issue manualmente
- [ ] O documento de watch-list existe com arquivos esperados alterados e testes adicionados
- [ ] O guia de revisão inclui failure modes típicos de IA a verificar
- [ ] A equipe entende que é dona da decisão de revisão e merge
- [ ] O arquivo de delegação rastreia a URL da issue depois de postada

## Corpo do Prompt

Você é o `@evolution-agent`. A equipe aprovou um rascunho de issue e está pronta para delegá-la ao Copilot Agent.

**Passo 1 — Confirmar prontidão.**
Peça à equipe para confirmar:
1. Vocês revisaram o rascunho da issue em `[path]`?
2. Os critérios de aceitação estão claros e testáveis?
3. O escopo é pequeno o suficiente para um único PR?

Se qualquer resposta for "não", redirecione para `/write-github-issue` para revisão.

**Passo 2 — Fornecer instruções de postagem.**
Diga à equipe como postar a issue:

```bash
# Opção 1: GitHub CLI
gh issue create --title "[title]" --body-file 04-evolucao/issues/<slug>.md --label "enhancement,copilot-agent"

# Opção 2: GitHub UI
# 1. Acesse a aba Issues do repositório
# 2. Clique em "New Issue"
# 3. Copie o conteúdo do arquivo de rascunho
# 4. Adicione labels: enhancement, copilot-agent
# 5. No corpo da issue, adicione: @copilot (para atribuir ao Copilot Agent)
```

Enfatize: a equipe posta isto manualmente. Isso é deliberado — delegar trabalho para IA é uma habilidade que exige entender o passagem.

**Passo 3 — Preparar a watch-list.**
Com base na seção "Files Likely Affected" da issue, crie uma watch-list:
- **Arquivos esperados criados**: lista com paths
- **Arquivos esperados modificados**: lista com paths
- **Testes esperados adicionados**: liste classes de teste e o que devem verificar
- **Tamanho esperado do PR**: estimativa (small: <100 linhas, medium: 100-300, large: 300+)
- **Tempo esperado**: Copilot Agent normalmente responde em minutos

**Passo 4 — Escrever o guia de revisão.**
Prepare um checklist de failure modes típicos de IA que a equipe deve observar:

- [ ] **Hallucinated imports**: O PR importa packages que não existem no projeto?
- [ ] **Fabricated API calls**: O código chama métodos que não são definidos na classe-alvo?
- [ ] **Tests that test nothing**: As assertions de teste verificam comportamento significativo ou são tautologias?
- [ ] **Comments contradicting code**: Os comentários descrevem comportamento que o código não implementa?
- [ ] **Scope creep**: O PR altera arquivos não listados na issue?
- [ ] **Missing error handling**: O PR adiciona código de happy path sem tratamento de erro?
- [ ] **Style violations**: O PR segue as convenções de código do projeto (records para DTOs, constructor injection etc.)?

**Passo 5 — Documentar responsabilidade da equipe.**
Escreva uma declaração clara: "Isto é uma delegação, não uma automação. A equipe é dona da revisão, da decisão de merge e de quaisquer consequências. Copilot Agent é um contributor, não um approver."

**Passo 6 — Escrever o arquivo de delegação.**
Gere a saída em `04-evolucao/delegations/<issue-slug>.md`. Deixe um placeholder para a URL da issue que a equipe preencherá depois de postar.

## Exemplo de Invocação

```
/delegate-to-copilot-agent issue=04-evolucao/issues/add-pagination-payments.md
```
