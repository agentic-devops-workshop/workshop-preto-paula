<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Tech Stack — SIFAP 2.0

![ESTÁGIO 02 Spec Moderna](https://img.shields.io/badge/ESTÁGIO-02%20Spec%20Moderna-00A4EF?style=for-the-badge) ![TIPO Reference](https://img.shields.io/badge/TIPO-Reference-1A1A1A?style=for-the-badge) ![STATUS Aprovado](https://img.shields.io/badge/STATUS-Aprovado-7FBA00?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 2](README.md) → **Tech Stack**

> **Para quem é isto?** Para o Par 2 (EA + SA) antes de escrever ADRs e EARS. Para o Par 3 (TL + Dev) antes de codificar. Para o Par 5 (DevOps) antes de provisionar infra.
>
> **O que este documento define:** as escolhas tecnológicas do SIFAP 2.0, com justificativa, versão fixada, mapeamento ao legado e alternativas descartadas.

> Este é um **contrato técnico**. Toda dependência adicionada fora desta lista exige ADR (`02-spec-moderna/ADR-NNN-*.md`).

## 1. Princípios da Stack

1. **Modular Monolith, não microservices** — orçamento do workshop (8h) não cabe sistemas distribuídos. Bounded contexts internos com fronteiras claras permitem evolução futura.
2. **JVM moderna + Web moderno** — Java 21 (LTS) + Next.js 15 são estáveis, têm forte ecossistema e cobrem 100% dos casos de uso do SIFAP.
3. **Open-source first, cloud-managed quando vale a pena** — PostgreSQL gerenciado no Azure, mas SDK PG padrão (driver portável).
4. **IaC obrigatório** — Terraform para tudo. Sem mudanças manuais via portal Azure.
5. **CI/CD declarativo** — GitHub Actions. Pipelines como código.
6. **Testes obrigatórios** — JUnit 5 + Testcontainers (backend), Vitest + Testing Library (frontend), cobertura ≥ 70% (linhas) / ≥ 60% (frontend).
7. **Sem segredos no código** — Azure Key Vault + Managed Identity para tudo.

## 2. Stack-Alvo Consolidada

| Camada | Tecnologia | Versão | Justificativa |
|---|---|---|---|
| **Linguagem backend** | Java | **21 (LTS)** | Records, sealed interfaces, pattern matching, virtual threads. Mesma família do legado mainframe (JVM no z/OS via OpenJDK), reduz risco cultural. |
| **Framework backend** | Spring Boot | **3.3.x** | Maturidade, Spring Security/Data/Validation/Actuator out-of-the-box. Suporte oficial a Java 21 + virtual threads. |
| **ORM** | Spring Data JPA + Hibernate | **6.x** (via Spring Boot 3.3) | Mapeamento direto de PE groups Adabas → entidades `@OneToMany`. Suporte nativo a JSONB PostgreSQL via `@JdbcTypeCode(SqlTypes.JSON)`. |
| **Banco de dados** | PostgreSQL | **16** | JSONB para campos MU legados, GIN indexes, particionamento por hash (para tabela `payment` com 180M+ registros), `numeric(precision,scale)` para `BigDecimal` (compatível com packed decimal Adabas). |
| **Migrações** | Flyway | **10.x** | Migrações versionadas em SQL puro, idempotentes, com rollback documentado. Compatível com CI/CD. |
| **Linguagem frontend** | TypeScript | **5.x** | `strict: true` obrigatório. Sem `any`. |
| **Framework frontend** | Next.js (App Router) | **15** | Server Components reduzem JS no client (importante para usuários em conexões variadas). Server Actions substituem boilerplate de API routes para forms. |
| **UI components** | shadcn/ui + Tailwind CSS | shadcn latest, Tailwind **3.4+** | Componentes copiados (não dependência npm), totalmente customizáveis, A11y por padrão. Mobile-first. |
| **State (client)** | Zustand | **4.x** | Quando precisar (a maior parte fica em Server Components). API simples, sem boilerplate. |
| **State (server cache)** | TanStack Query (React Query) | **5.x** | Para fluxos que dependem de cache de servidor em client components. |
| **Validação backend** | Bean Validation (Jakarta) | **3.x** | `@Valid` + annotations declarativas na camada de controller. Mensagens i18n. |
| **Validação frontend** | Zod + react-hook-form | latest | Tipos derivados (`z.infer`), compatível com Server Actions. |
| **API spec** | OpenAPI | **3.1** | Geração automática via springdoc-openapi. Versionamento via `/api/v1/`. |
| **Auth** | OAuth 2.0 + JWT (Spring Security) | Spring Security **6.x** | Substitui sessão de terminal 3270 do legado. Compatível com Azure AD / Entra ID. |
| **Containers** | Docker + Docker Compose | Docker **24+**, Compose v2 | Paridade dev/prod. Imagens base oficiais (eclipse-temurin:21-jre-alpine, node:20-alpine). |
| **IaC** | Terraform | **1.7+** com Azure provider `~> 3.x` | Módulos por área (network, compute, database, monitoring). State remote em Azure Storage. |
| **Cloud** | Azure | — | App Service (web app) ou Container Apps, PostgreSQL Flexible Server, Key Vault, Application Insights, Storage Account. |
| **CI/CD** | GitHub Actions | — | Workflows reutilizáveis. OIDC para auth com Azure (sem secrets). Actions fixadas por SHA. |
| **Testes backend** | JUnit 5 + Testcontainers + AssertJ + Mockito | JUnit **5.10+** | Testcontainers para PostgreSQL real em testes de integração. ≥70% cobertura linhas / ≥60% branches. |
| **Testes frontend** | Vitest + Testing Library + Playwright | Vitest **1.x**, Playwright **1.40+** | Vitest para unit, Playwright para E2E. |
| **Observabilidade** | Spring Boot Actuator + Application Insights | — | `/actuator/health`, `/actuator/metrics`. OpenTelemetry para tracing distribuído (mesmo no monólito, prepara migração futura). |
| **Logging** | SLF4J + Logback (JSON structured) | — | JSON logs para Application Insights. **Nunca** logar CPF/RG/valores de benefício sem mascaramento. |
| **Secrets** | Azure Key Vault + Managed Identity | — | Sem secrets em commits, env files, ou variáveis Terraform. |

## 3. Mapeamento Legado → Moderno

| Legado SIFAP 1.0 | SIFAP 2.0 | Decisão |
|---|---|---|
| **Natural 6.3.12** (linguagem) | Java 21 + Spring Boot 3.3 | Substituição direta — mantém JVM no servidor (vs C# ou Go) |
| **Adabas 7.4.3** (SGBD) | PostgreSQL 16 | Migração de SGBD invertido para relacional |
| **PE groups** (max 10 dependentes, 8 descontos) | Tabelas filhas com FK + index | ADR-NNN pendente (PE → tabela vs JSONB) |
| **MU fields** (multi-value) | JSONB ou `text[]` | Caso a caso conforme padrão de consulta |
| **Super-descriptors** (chaves compostas) | `@Index(columnList = "...")` | Mapeamento direto |
| **Packed decimal `P9.2`** | `numeric(11,2)` + `BigDecimal` | Preservar precisão financeira |
| **MAP 3270** (telas) | Next.js 15 App Router + shadcn/ui | Substituição completa de UI |
| **Com\*plete** (TP monitor) | Spring MVC + Tomcat embarcado | Substituição |
| **JES2 jobs** (batch noturno) | Spring `@Scheduled` ou Azure Functions (Timer trigger) | ADR-NNN pendente |
| **CNAB 240 BB** (conciliação) | Job Spring Batch + parser dedicado | Manter formato — banco não vai mudar |
| **SIAFI** (integração federal) | REST client com mTLS | Manter integração — sistema externo |
| **Trilha de auditoria** (`AUDITORIA.ddm`) | Tabela `audit_event` + Spring Data JPA Audit | Preservar retenção 10 anos (IN-TCU 63/2010) |
| **Sessão de terminal** (auth) | OAuth 2.0 + JWT (Spring Security) | Modernização — `[GREENFIELD]` |

## 4. Cross-Cutting Concerns

### 4.1 Segurança

- **OWASP Top 10**: validação em toda fronteira, prepared statements (JPA), CORS explícito, sem wildcards `*` em produção
- **CPF/RG**: nunca em logs sem mascaramento (`***.***.XXX-XX`) — política LGPD única (resolve BONUS-03, BONUS-04 do legado)
- **Secrets**: somente via `azurerm_key_vault_secret`, nunca em `locals` ou `variables`
- **Auth serviço-a-serviço**: Managed Identity para tudo no Azure
- **Rate limiting**: Spring Security + bucket4j para endpoints públicos

### 4.2 Observabilidade

- **Logs**: JSON structured (Logback), enviados para Application Insights via OpenTelemetry
- **Métricas**: `/actuator/metrics` expostas via Micrometer → App Insights
- **Tracing**: OpenTelemetry com sampling 10% (configurável)
- **Health checks**: `/actuator/health` para liveness + readiness probes
- **Dashboards**: Application Insights Workbooks por bounded context

### 4.3 Internacionalização

- **i18n**: Spring `MessageSource` (backend), `next-intl` (frontend)
- **Locale padrão**: `pt-BR`
- **Datas/moedas**: `java.time` + `BigDecimal`, sempre com timezone explícito (`UTC` no banco, `America/Sao_Paulo` na UI)

### 4.4 Acessibilidade

- **WCAG 2.1 AA** mínimo
- **Componentes shadcn/ui**: já vêm com ARIA labels e navegação por teclado
- **Testes**: Playwright com axe-core para auditoria automatizada de A11y

## 5. O Que NÃO Vamos Usar (e por quê)

| Tecnologia | Por quê NÃO |
|---|---|
| **Microservices** | 8h não é tempo para sistemas distribuídos. Modular Monolith resolve 80% dos benefícios com 20% do custo. |
| **GraphQL** | REST + OpenAPI cobre todos os casos de uso. GraphQL adicionaria complexidade sem ganho proporcional. |
| **MongoDB / NoSQL** | Dados do SIFAP são relacionais. PostgreSQL com JSONB cobre os 20% não-relacionais (PE groups). |
| **Kafka / RabbitMQ** | Sem necessidade de event streaming no escopo do workshop. Eventos internos via Spring Application Events. |
| **Kubernetes** | App Service ou Container Apps cobrem o caso de uso sem o custo operacional de K8s. |
| **React Native / Flutter** | Frontend é web-only (era terminal 3270, agora browser). Sem mobile nativo no escopo. |
| **Lombok** | Java 21 records eliminam a maior parte do uso. Constructor injection do Spring resolve o resto. |
| **OpenFeign** | Sem chamadas service-to-service significativas no monólito. `RestClient` do Spring resolve casos pontuais. |
| **Hibernate Envers** | `audit_event` é tabela de negócio explícita (`AUDITORIA.ddm` legada), não auditoria de revisões de entidade. |

## 6. Decisões Abertas (precisam de ADR no Estágio 2)

| # | Decisão | Opções | ADR previsto |
|---|---|---|---|
| 1 | **Mapeamento PE groups Adabas** | (a) tabela filha + FK; (b) coluna JSONB; (c) `text[]` PostgreSQL | `ADR-002-pe-groups-mapping` |
| 2 | **Método de arredondamento financeiro** | (a) truncamento legado (MYS-005, preserva compatibilidade); (b) half-even bancário (correto); (c) configurável por bounded context | `ADR-003-financial-rounding` |
| 3 | **Tratamento do FATOR-K (`0.347215`)** | (a) constante imutável em código; (b) parâmetro em tabela `program_config`; (c) feature flag | `ADR-004-fator-k-handling` |
| 4 | **Orquestração de batch** | (a) Spring `@Scheduled` no monólito; (b) Azure Functions Timer; (c) Spring Batch + Quartz | `ADR-005-batch-orchestration` |
| 5 | **Estratégia de migração de dados** | (a) ETL único + cutover; (b) CDC com Strangler Fig; (c) coexistência permanente | `ADR-006-data-migration` |
| 6 | **Política de máscara de CPF** | Padrão LGPD único (resolve BONUS-03, BONUS-04, INC-004 do legado) | `ADR-007-pii-masking` |
| 7 | **Backdoors de CPF do legado** | (a) remover (risco de quebrar integrações); (b) flag `allowTestCpfs` por env; (c) preservar com audit log | `ADR-008-cpf-backdoors` |

## 7. Decisões Já Tomadas (não-negociáveis)

| Decisão | Source |
|---|---|
| Modular Monolith (não microservices) | [`modular-monolith.instructions.md`](../.github/instructions/modular-monolith.instructions.md) |
| Java 21 + Spring Boot 3.3 | [`backend.instructions.md`](../.github/instructions/backend.instructions.md) |
| Next.js 15 + TypeScript strict | [`frontend-spec.instructions.md`](../.github/instructions/frontend-spec.instructions.md) |
| PostgreSQL 16 + Flyway | [`database.instructions.md`](../.github/instructions/database.instructions.md) |
| Terraform + Azure | [`infrastructure.instructions.md`](../.github/instructions/infrastructure.instructions.md) |
| OWASP Top 10 + Managed Identity | [`security.instructions.md`](../.github/instructions/security.instructions.md) |
| GitHub Actions com OIDC + SHA-pinned | [`cicd.instructions.md`](../.github/instructions/cicd.instructions.md) |
| Testes: AAA, mocks só de externos, ≥70% cobertura linhas | [`tests.instructions.md`](../.github/instructions/tests.instructions.md) |

## 8. Versionamento

Este documento é fonte da verdade para a stack. Mudanças exigem:

1. ADR justificando a mudança em `02-spec-moderna/ADR-NNN-*.md`
2. Atualização deste arquivo com referência ao ADR
3. Atualização do `.github/instructions/*.instructions.md` correspondente, se aplicável
4. PR com review do Par 2 (EA + SA) mais Par 3 (TL)

---

## Referências

- [Project Constitution](/.specify/memory/constitution.md) (a ser criada via `/speckit.constitution`)
- [Backend conventions](../.github/instructions/backend.instructions.md)
- [Frontend conventions](../.github/instructions/frontend-spec.instructions.md)
- [Database conventions](../.github/instructions/database.instructions.md)
- [Modular Monolith patterns](../.github/instructions/modular-monolith.instructions.md)
- [Security conventions](../.github/instructions/security.instructions.md)
- [Infrastructure conventions](../.github/instructions/infrastructure.instructions.md)
- [CI/CD conventions](../.github/instructions/cicd.instructions.md)
- [Tests conventions](../.github/instructions/tests.instructions.md)
- [Discovery report Estágio 1](../01-arqueologia/output-requisitos/discovery-report.final.md) — input que justifica escolhas
- [Business rules catalog](../01-arqueologia/output-requisitos/business-rules-catalog.final.md) — 71 regras com `source_legacy`

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="README.md"><strong>Estágio 2 — README</strong></a><br/>
<sub>Visão geral do estágio.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="GUIDE.md"><strong>GUIDE do Estágio 2</strong></a><br/>
<sub>Passo a passo para EARS, ADRs e C4.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>
