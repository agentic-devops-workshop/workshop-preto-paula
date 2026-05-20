<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
mode: ask
model: claude-sonnet-4-6
description: "Inicialize um novo projeto habilitado para Copilot"
---

# /setup-project

## Tarefa
Inicialize um projeto com o scaffold de context engineering do Copilot: AGENTS.md, CODEMAP.md, `.github/instructions/`, `.github/prompts/`, `.github/agents/`, `.github/copilot-instructions.md`.

## Passos
1. Detecte a stack técnica a partir dos arquivos existentes (package.json, pom.xml, requirements.txt, *.csproj).
2. Crie AGENTS.md com: resumo da stack, convenções de código, comando de teste, comando de lint, comando de build.
3. Crie esqueleto de CODEMAP.md com seções `## Modules`, `## Data Flow`, `## External Integrations`.
4. Crie `.github/copilot-instructions.md` com linguagem, tom e regras de segurança do padrão do time.
5. Adicione arquivos de instruction baseline com escopo por `applyTo:` (ex.: `**/*.java`, `**/*.ts`).
6. Stage as mudanças, mas não faça commit. Imprima a lista de arquivos criados.

## Saída
- Lista de arquivos criados (paths absolutos)
- Mensagem sugerida para o primeiro commit
- Três ações de follow-up que o usuário deve executar manualmente

## Gate de Qualidade
- [ ] AGENTS.md é específico para a stack detectada, não genérico
- [ ] Todo arquivo de instruction tem escopo `applyTo:`
- [ ] Sem secrets, credenciais ou placeholders como TODO
- [ ] `.gitignore` atualizado se novas pastas precisarem ser versionadas
