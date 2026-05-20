<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
mode: agent
model: claude-opus-4-6
description: "Escreva CONSTITUTION.md — as regras e princípios inegociáveis que governam a construção de uma feature do SIFAP 2.0."
---

# /create-constitution

## Objetivo

Você é o enterprise architect escrevendo `specs/<NNN>-<feature>/CONSTITUTION.md` — o documento que lista as **regras inegociáveis** com as quais todo contribuidor concorda antes de ler a spec. A constitution é curta, explícita e estável. ADRs tomam decisões; a constitution define os trilhos dos quais as decisões não podem sair.

## Entradas

Peça ao usuário o que estiver faltando.

- A pasta da feature (`specs/<NNN>-<feature>/`).
- As restrições organizacionais já em vigor (baseline de segurança, somente Azure, OWASP Top 10, LGPD).
- Quaisquer constitutions anteriores das quais herdar (CONSTITUTION de uma feature pai).
- O time e donos de persona designados para esta feature.

## Processo

1. **Herde e adapte.** Comece pela constitution em nível de projeto e aperte ou relaxe apenas para esta feature, com justificativa explícita.
2. **Agrupe regras por categoria.**
 - **Stack** — language, framework, runtime versions.
 - **Security** — identity, secrets, network, encryption, OWASP baseline.
 - **Data** — PII handling, retention, residency, encryption at rest.
 - **Operations** — observability, deployment, environments, SLOs.
 - **Process** — branching, code review, testing thresholds, ADR cadence.
 - **Compliance** — LGPD, regulatory, audit logging.
3. **Torne toda regra testável.** "Use Java 21" é testável (`mvnw --version`); "use modern Java" não é.
4. **Numere as regras.** `C1`, `C2` ... para que revisores possam citá-las.
5. **Declare a consequência de quebrar uma regra.** "Build fails," "PR rejected," "InfoSec exception required" — não silêncio.
6. **Marque mutável vs. imutável.** Algumas regras podem ser relaxadas via ADR com sign-off de InfoSec; outras exigem uma nova constitution.
7. **Date e assine.** Data do fórum de arquitetura, aprovadores nomeados, versão `1.0.0`. Bump apenas quando a própria constitution mudar.
8. **Mantenha curto.** Meta ≤ 80 linhas. Se o time não consegue lembrar a constitution, ela não funciona.

## Saída

O entregável é `specs/<NNN>-<feature>/CONSTITUTION.md`:

```markdown
# CONSTITUTION — <feature> (<feature-folder>)

- **Version**: 1.0.0
- **Date**: 2026-04-29
- **Approvers**: @paula (enterprise architect), @morgan (technical lead), @infosec-lead
- **Inherits from**: project-level CONSTITUTION.md

## 1. Stack
| ID | Regra | Consequência da violação |
|----|------|----------------------|
| C1 | Serviços de backend rodam somente em Java 21 (Temurin) e Spring Boot 3.3. | Build falha. |
| C2 | Frontend roda em Next.js 15 com TypeScript `strict: true`. Sem `any`. | Lint bloqueia merge. |
| C3 | PostgreSQL 16 é o único sistema de registro para dados do SIFAP. | Exige exceção de InfoSec. |
| C4 | Toda infraestrutura de nuvem é Azure. Sem multi-cloud nesta funcionalidade. | Novo ADR + fórum de arquitetura obrigatórios. |

## 2. Segurança
| ID | Regra | Consequência |
|----|------|-------------|
| C5 | Autenticação serviço-a-serviço usa Azure Managed Identity. Sem client secrets em código ou configuração. | PR bloqueado. |
| C6 | Todos os secrets são lidos do Key Vault em runtime. Nenhum `.env` commitado. | Gitleaks bloqueia merge. |
| C7 | Baseline OWASP Top 10 se aplica — validação de entrada, SQL parametrizado, sem queries montadas por string. | PR rejeitado. |
| C8 | Allowlist de CORS é explícita. Sem `*` na configuração de produção. | Promoção de estágio bloqueada. |

## 3. Data
| ID | Rule | Consequence |
|----|------|-------------|
| C9 | PII columns carry a `COMMENT` flagging them as PII. | DBA review blocks. |
| C10 | No production PII in `dev` or `stage`. Synthetic data only. | InfoSec finding, immediate revert. |
| C11 | Money is `NUMERIC(15,2)`; never `FLOAT`. | DBA review blocks. |
| C12 | Audit log entries are append-only; no `DELETE` or `UPDATE` on `audit_log`. | Migration rejected. |

## 4. Operations
| ID | Rule | Consequence |
|----|------|-------------|
| C13 | Every public endpoint emits a structured log line with `requestId`, `userId`, `latencyMs`. | Code review blocks. |
| C14 | SLO defined for every user-facing endpoint, in REQ-OPS-*. | Spec review blocks. |
| C15 | Deployments to prod require two reviewers and a linked change ticket. | Pipeline blocks. |

## 5. Processo
| ID | Regra | Consequência |
|----|------|-------------|
| C16 | One branch per spec — `spec/<NNN>-<name>` from `develop`. No direct commits to `develop`, `stage`, or `main`. | PR rejected. |
| C17 | Every requirement uses EARS notation; every test cites a `REQ-ID`. | Spec review blocks. |
| C18 | Every architectural decision is captured as an ADR before code lands. | Code review blocks. |

## 6. Compliance
| ID | Rule | Consequence |
|----|------|-------------|
| C19 | LGPD subject-rights endpoints (read, delete, export) covered by REQ-COMP-*. | Compliance review blocks release. |
| C20 | TCU/SISP audit trail completeness verified before each release. | Release manager blocks. |

## 7. Mutable vs immutable
- Mutable (relaxable via ADR + InfoSec sign-off): C13–C18.
- Immutable (constitutional change required): C1, C3, C4, C5, C6, C7, C9, C10, C19, C20.

## 8. Processo de emenda
Abra um PR para este arquivo. O fórum de arquitetura revisa, a versão é atualizada (`1.0.0` → `1.1.0` menor, → `2.0.0` maior). É obrigatória a assinatura de novos aprovadores.
```

## Exemplo trabalhado

**Entrada:** Nova feature `004-uat-portal`, herda a constitution do projeto, adiciona duas regras extras: acessibilidade WCAG 2.1 AA, suporte a SSO via Entra ID.

**Resposta esperada:** a estrutura acima, com duas linhas adicionadas na seção Security (`C8a` SSO required) e Operations (`C14a` axe-core checks pass), e "Inherits from" nomeando a CONSTITUTION do projeto.

## Antipadrões

- Constitutions longas que ninguém lê. Corte para ≤ 80 linhas.
- Regras sem consequências. Regras suaves viram opcionais.
- Regras sem IDs. Revisores não conseguem citá-las.
- Misturar princípios ("we value quality") com regras ("Java 21 only"). Princípios pertencem a outro lugar; a constitution é feita de regras.
- Redação de "Best practices". Declare a regra.
- Sem distinção mutável/imutável. Toda regra parece igualmente rígida; times sobrecumprem ou se rebelam.
- Sem processo de emenda. Constitutions obsoletas são ignoradas.

## Critérios de sucesso

- [ ] Comprimento do arquivo ≤ 80 linhas (excluindo assinaturas).
- [ ] Toda regra tem ID e consequência em caso de violação.
- [ ] Pelo menos uma regra por categoria (Stack, Security, Data, Operations, Process, Compliance).
- [ ] Distinção mutável vs. imutável declarada.
- [ ] Processo de emenda documentado.
- [ ] Herda de uma constitution pai quando houver.
- [ ] Aprovadores e data registrados.
- [ ] Versão segue semver.
