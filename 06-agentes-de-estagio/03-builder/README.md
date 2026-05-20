<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# @builder — Estágio 3: Implementação

![MUNDO 3 de 4](https://img.shields.io/badge/MUNDO-3%20de%204-7FBA00?style=for-the-badge) ![AGENTE @builder](https://img.shields.io/badge/AGENTE-@builder-1A1A1A?style=for-the-badge) ![ESTÁGIO 3](https://img.shields.io/badge/ESTÁGIO-3-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../../README.md) → [Agentes](../README.md) → **@builder**


> **Para quem é isto?** Para os Pares 3 e 4 durante o Estágio 3.
>
> **O que você terá ao final desta leitura:**
>
> 1. Como ativar o agente `@builder` no Copilot Chat
> 2. Prompts para tradução Natural → Java, JPA, testes
> 3. Regra: não escreve código sem REQ-ID e teste

> Use este agente quando a spec já existe e o time precisa construir. Ele não substitui design: ele executa `spec.md`, `plan.md` e `tasks.md` com código, testes e rastreabilidade.

## Objetivo da etapa

Transformar requisitos em software funcionando: serviços Java, entidades JPA, migrations, endpoints REST, páginas Next.js e testes que provem equivalência com as regras do legado.

## Quando usar

- **Horário:** 15:00–16:10.
- **Protagonista:** Developer.
- **Suporte forte:** Technical Lead, DBA, QA Engineer e Software Architect.
- **Pré-requisito:** Passagem #2 com spec, ADRs, C4, modelo de dados e tasks priorizadas.

## Passo a passo com o agente

1. Selecione o agente `@builder` no Copilot Chat.
2. Cole o prompt de abertura abaixo.
3. Escolha uma task pequena de `tasks.md`.
4. Escreva ou gere teste primeiro quando tocar regra de negócio.
5. Implemente a menor mudança coerente com o design.
6. Rode testes e build.
7. Faça commit mencionando REQ-ID.

```text
Estou iniciando o Estágio 3 — Implementação.
Temos spec.md, plan.md, tasks.md, ADRs e modelo de dados.
Ajude a implementar a próxima task rastreável para Java 21 + Spring Boot,
PostgreSQL/JPA e Next.js, começando pelos testes quando houver regra de negócio.
```

## O que perguntar

| Situação | Prompt útil |
| --- | --- |
| Entidade JPA | "Gere a entidade a partir deste DDM e explique cada mapeamento." |
| Regra Natural | "Traduza esta regra para Java mantendo nomes claros e teste de equivalência." |
| Controller REST | "Crie controller `/api/v1/...` com DTOs, validação e OpenAPI." |
| Frontend | "Crie página Next.js App Router consumindo este endpoint, sem expor segredo." |
| Testes | "Escreva teste JUnit para REQ-X e marque o comentário de rastreabilidade." |

## Definição de Pronto

- [ ] Backend compila e `mvn test` ou comando equivalente passa.
- [ ] Frontend compila e `npm test` ou comando equivalente passa quando houver frontend.
- [ ] Pelo menos 3 endpoints REST principais funcionam.
- [ ] Pelo menos 2 telas Next.js consomem a API quando o escopo incluir UI.
- [ ] Migrations Flyway aplicam limpas.
- [ ] Testes citam REQ-IDs.
- [ ] Commits mencionam `Implements REQ-...` quando implementam comportamento.

## Anti-padrões

| Não faça | Faça |
| --- | --- |
| Codar sem REQ-ID | Volte à spec e encontre o requisito |
| Criar arquitetura nova no Estágio 3 | Siga ADRs e plano técnico |
| Pular testes por falta de tempo | Faça teste mínimo da regra crítica |
| Logar CPF ou valores sensíveis | Mascarar dados sensíveis sempre |

## Navegação

| Anterior | Início | Próximo |
| --- | --- | --- |
| [@architect](../02-architect/README.md) | [Kit PT-BR](../../README.md) | [@evolution](../04-evolution/README.md) |

— Paula


---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="../02-architect/"><strong>@architect</strong></a><br/>
<sub>Mundo anterior.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../04-evolution/"><strong>@evolution</strong></a><br/>
<sub>Próximo mundo: evolução.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../../README.md">Voltar ao Kit PT-BR</a></sub>

