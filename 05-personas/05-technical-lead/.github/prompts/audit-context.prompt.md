<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
mode: ask
model: claude-sonnet-4-6
description: "Audite arquivos de context engineering do repositório"
---

# /audit-context

## Tarefa
Audite a superfície de context engineering do repositório (AGENTS.md, CODEMAP.md, `.github/instructions/*`, `.github/prompts/*`, `.github/agents/*`) e retorne uma lista priorizada de correções.

## Passos
1. Liste todos os arquivos em `.github/instructions/`, `.github/prompts/`, `.github/agents/` e reporte contagem de linhas.
2. Verifique o escopo `applyTo:` em todo arquivo de instructions. Sinalize qualquer arquivo com `applyTo: "**"` ou escopo ausente.
3. Leia CODEMAP.md. Sinalize como obsoleto se não tiver sido atualizado nos últimos 30 dias ou referenciar arquivos deletados.
4. Verifique o frontmatter de todo prompt/agent para: `description` presente e informativa (não "TBD"), `model` definido, `mode` correto.
5. Use grep para referências obsoletas de pastas (rastreamento de renames) e links relativos quebrados.
6. Resuma como tabela: `Arquivo | Problema | Severidade (Alta/Média/Baixa) | Correção`.

## Saída
- Uma tabela Markdown com uma linha por achado, ordenada por severidade.
- Um resumo curto "Top 3 correções" no final.

## Gate de Qualidade
- [ ] Sem falsos positivos (todo item sinalizado é de fato um problema)
- [ ] Todo item de severidade Alta tem uma correção concreta, não uma sugestão vaga
- [ ] Frescor de CODEMAP.md reportado explicitamente
- [ ] Nenhuma sugestão para editar código, apenas arquivos de contexto
