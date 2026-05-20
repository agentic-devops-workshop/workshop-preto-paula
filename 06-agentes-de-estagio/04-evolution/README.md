<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# @evolution — Estágio 4: Evolução

![MUNDO 4 de 4](https://img.shields.io/badge/MUNDO-4%20de%204-FFB900?style=for-the-badge) ![AGENTE @evolution](https://img.shields.io/badge/AGENTE-@evolution-1A1A1A?style=for-the-badge) ![ESTÁGIO 4](https://img.shields.io/badge/ESTÁGIO-4-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../../README.md) → [Agentes](../README.md) → **@evolution**


> **Para quem é isto?** Para o Par 5 e Par 3 durante o Estágio 4.
>
> **O que você terá ao final desta leitura:**
>
> 1. Como ativar o agente `@evolution` no Copilot Chat
> 2. Como escrever Issues que o Agent implementa bem
> 3. Critérios de revisão do PR do Agent

> Use este agente quando o protótipo já existe e o time precisa transformar trabalho local em entrega revisável: issues, PRs, CI/CD, IaC, runbook e relatório final.

## Objetivo da etapa

Operacionalizar o que foi construído: criar issues claras para o modo Agent, revisar PRs gerados por IA, preparar pipeline, organizar Terraform e documentar como rodar e evoluir o sistema.

## Quando usar

- **Horário:** 16:10–16:50.
- **Protagonista:** Technical Lead.
- **Suporte forte:** DevOps Engineer, Tech Writer, Developer e QA Engineer.
- **Pré-requisito:** Passagem #3 com backend/frontend funcionando, testes relevantes e pendências conhecidas.

## Passo a passo com o agente

1. Selecione o agente `@evolution` no Copilot Chat.
2. Cole o prompt de abertura abaixo.
3. Transforme pendências em GitHub Issues pequenas e revisáveis.
4. Use o modo Agent somente para tasks com critérios de aceite claros.
5. Revise o PR como se fosse de uma pessoa do time.
6. Atualize CI/CD, Terraform e runbook.
7. Feche com relatório de experiência do Agent.

```text
Estou iniciando o Estágio 4 — Evolução.
Temos um protótipo com backend, frontend e testes.
Ajude a criar issues para Copilot Agent, revisar PRs, preparar CI/CD,
organizar Terraform e escrever o runbook final do SIFAP 2.0.
```

## O que perguntar

| Situação | Prompt útil |
| --- | --- |
| Issue para modo Agent | "Escreva uma issue pequena, com contexto, arquivos relevantes, critérios de aceite e fora de escopo." |
| Revisão de PR | "Revise este PR priorizando bug, risco, regressão e teste faltante." |
| CI/CD | "Crie workflow GitHub Actions para build, test e validação de Terraform." |
| Runbook | "Transforme estes comandos em runbook para pessoa on-call iniciante." |
| Relatório final | "Escreva o agent-experience-report com o que funcionou, falhou e aprendemos." |

## Definição de Pronto

- [ ] Pelo menos 2 issues prontas para o modo Agent ou backlog pós-workshop.
- [ ] PRs gerados por IA revisados com critérios humanos.
- [ ] Workflow CI/CD documentado ou criado.
- [ ] Terraform validado ou pendências registradas.
- [ ] Runbook atualizado com comandos de execução e troubleshooting.
- [ ] Relatório de experiência do Agent preenchido.

## Anti-padrões

| Não faça | Faça |
| --- | --- |
| Delegar issue vaga para o modo Agent | Escreva contexto, escopo e critérios de aceite |
| Aceitar PR de IA sem revisão | Revise como PR humano |
| Criar feature nova no final | Coloque no backlog |
| Esconder pendência do demo | Documente risco e workaround |

## Navegação

| Anterior | Início | Próximo |
| --- | --- | --- |
| [@builder](../03-builder/README.md) | [Kit PT-BR](../../README.md) | [04-evolucao](../../04-evolucao/README.md) |

— Paula


---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="../03-builder/"><strong>@builder</strong></a><br/>
<sub>Mundo anterior.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../README.md"><strong>Visão dos agentes</strong></a><br/>
<sub>Voltar à visão.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../../README.md">Voltar ao Kit PT-BR</a></sub>

