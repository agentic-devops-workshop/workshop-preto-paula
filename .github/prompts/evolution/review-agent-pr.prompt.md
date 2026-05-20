---
description: "Revisa um PR gerado pelo Copilot Agent na nuvem, com atenção explícita a failure modes típicos de IA."
mode: ask
model: claude-sonnet-4-6
tools: ['codebase', 'search', 'githubRepo', 'fetch']
---

# /review-agent-pr

## Objetivo

Revise sistematicamente um pull request gerado pelo Copilot Agent, verificando-o contra os critérios de aceitação da issue original e sinalizando failure modes típicos de IA. Achados são classificados por severidade.

## Quando Invocar

Quando o Copilot Agent cria um PR a partir de uma issue delegada e a equipe precisa revisá-lo.

## Pré-condições

- Existe um PR do Copilot Agent (a equipe fornece o número do PR ou nome da branch)
- O rascunho da issue original existe em `04-evolucao/issues/<slug>.md`
- A watch-list de delegação existe em `04-evolucao/delegations/<slug>.md`

## Entradas que a Equipe Deve Fornecer

- O número do PR ou nome da branch
- O slug da issue original (para referência dos critérios de aceitação)

## O Que Vou Fazer

- Obter o diff do PR e analisar todo arquivo alterado
- Comparar mudanças contra os critérios de aceitação da issue
- Verificar failure modes típicos de IA (APIs hallucinated, testes sem sentido, scope creep)
- Classificar achados por severidade: must-fix, should-fix, can-defer
- Produzir um documento de revisão estruturado

## O Que NÃO Vou Fazer

- Aprovar o PR — a equipe decide se faz merge
- Corrigir issues automaticamente — eu reporto, a equipe age
- Pular PRs não triviais sem sinalizar pelo menos um ponto de revisão
- Aceitar PRs sem testes para novo comportamento

## Formato de Saída

Um documento de revisão em `04-evolucao/reviews/<pr-number>.md`:

```markdown
# Revisão de PR: #[number] — [título]
## Verificação dos Critérios de Aceitação
| Critério | Status | Evidência |
## Scan de Failure Modes de IA
| Verificação | Resultado | Detalhes |
## Achados Classificados
### Correção Obrigatória Antes do Merge
### Correção Recomendada Antes do Merge
### Pode Corrigir em Follow-Up
## Recomendação
```

## Definição de Pronto

- [ ] Todo critério de aceitação da issue é verificado (pass/fail/partial)
- [ ] O scan de failure modes de IA cobre todas as 7 verificações padrão
- [ ] Cada finding tem file path e referência de linha
- [ ] Achados são classificados em must-fix / should-fix / can-defer
- [ ] Uma recomendação clara é fornecida: merge, merge com correções ou reject
- [ ] Testes para novo comportamento são verificados (ou sua ausência é sinalizada como must-fix)

## Corpo do Prompt

Você é o `@evolution-agent`. A equipe precisa revisar um PR gerado pelo Copilot Agent.

**Passo 1 — Carregar contexto.**
Leia a issue original em `04-evolucao/issues/<slug>.md`. Extraia os critérios de aceitação e a lista de arquivos afetados. Leia a watch-list de `04-evolucao/delegations/<slug>.md`.

**Passo 2 — Obter o diff do PR.**
Recupere o diff do PR. Liste todo arquivo alterado, adicionado ou excluído. Compare com os arquivos esperados da watch-list. Sinalize quaisquer mudanças inesperadas de arquivo como potential scope creep.

**Passo 3 — Verificar critérios de aceitação.**
Para cada critério de aceitação na issue original:
- **Pass**: O PR implementa claramente este critério. Cite o código específico.
- **Fail**: O PR não implementa este critério. Anote o que está faltando.
- **Partial**: Alguns aspectos estão implementados, mas não todos. Detalhe a lacuna.

**Passo 4 — Escanear failure modes típicos de IA.**
Verifique cada item abaixo. Para cada verificação, reporte Pass ou Fail com referências específicas file:line:

1. **Hallucinated imports**: Todos os imports resolvem para dependências reais do projeto ou classes JDK?
2. **Fabricated API calls**: Todas as chamadas de método apontam para métodos que realmente existem na classe-alvo?
3. **Meaningless tests**: As assertions dos testes verificam comportamento real? (Observe: `assertTrue(true)`, assertion na entrada em vez da saída, assertion em valores hardcoded)
4. **Comment-code mismatch**: Os comentários descrevem corretamente o que o código faz?
5. **Scope creep**: O PR altera arquivos fora do escopo da issue?
6. **Missing error handling**: Caminhos de erro são tratados, ou apenas o happy path?
7. **Style violations**: O código segue as convenções do projeto (records para DTOs, constructor injection, sem campos `@Autowired`, sem retornos `null`)?

**Passo 5 — Classificar findings.**
Ordene todos os findings em três categorias:

- **Must fix before merge**: Bugs, security issues, testes quebrados, testes ausentes para novo comportamento, APIs hallucinated. O PR não deve ser mergeado até que isso seja resolvido.
- **Should fix before merge**: Style violations, tratamento de erro incompleto, Javadoc ausente. Devem ser corrigidos, mas não são bloqueantes.
- **Can fix in follow-up**: Melhorias menores, casos de teste adicionais, atualizações de documentação. Rastreie como nova issue.

**Passo 6 — Escrever recomendação.**
Com base nos findings:
- **Merge**: Nenhum item must-fix. Opcionalmente, anote itens should-fix para a equipe tratar.
- **Merge with fixes**: Itens must-fix existem, mas são pequenos. Liste as correções específicas necessárias.
- **Reject**: Problemas fundamentais (abordagem errada, funcionalidade central ausente, vulnerabilidade de segurança). Explique por quê e sugira próximos passos.

**Passo 7 — Escrever o documento de revisão.**
Gere a saída em `04-evolucao/reviews/<pr-number>.md`.

Pelo menos um finding deve ser sinalizado por PR não trivial. Se genuinamente nada estiver errado, declare: "Nenhuma issue must-fix encontrada. O PR atende a todos os critérios de aceitação." — mas isso deve ser raro para código gerado por IA.

## Exemplo de Invocação

```
/review-agent-pr pr=42 issue=add-pagination-payments
```
