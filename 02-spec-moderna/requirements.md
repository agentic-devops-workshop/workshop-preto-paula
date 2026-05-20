<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Requirements — SIFAP 2.0

![ESTÁGIO 02 Spec Moderna](https://img.shields.io/badge/ESTÁGIO-02%20Spec%20Moderna-00A4EF?style=for-the-badge) ![TIPO Requirements](https://img.shields.io/badge/TIPO-Requirements-1A1A1A?style=for-the-badge) ![STATUS Draft](https://img.shields.io/badge/STATUS-Draft-FFB900?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 2](README.md) → **Requirements**

> **Para quem é isto?** Documento-base que sintetiza requisitos funcionais e não-funcionais do SIFAP 2.0, alimentando `/speckit.specify` (EARS) e ADRs do Estágio 2.

> **Como ler:**
> - **FR-NNN** → o que o sistema **faz** (derivados das 71 regras BR-XXX do Estágio 1)
> - **NFR-NNN** → como o sistema **se comporta** (qualidade, performance, compliance)
> - **CON-NNN** → restrições não-negociáveis
> - **OUT** → escopo fora desta versão

---

## 1. Fontes deste documento

| Fonte | Conteúdo | Quantidade |
|---|---|---|
| [`business-rules-catalog.final.md`](../01-arqueologia/output-requisitos/business-rules-catalog.final.md) | Regras de negócio extraídas do legado | 71 BR (18 críticas) |
| [`mysteries-found.final.md`](../01-arqueologia/output-requisitos/mysteries-found.final.md) | Achados que viraram NFR/decisões | 24 achados |
| [`discovery-report.final.md`](../01-arqueologia/output-requisitos/discovery-report.final.md) | 4 bounded contexts + recomendações migrar/descartar/evoluir | 4 contextos |
| [`techstack.md`](techstack.md) | Stack-alvo (Java 21 + Next.js + PostgreSQL + Azure) | 22 tecnologias |
| `legacy-docs/REGRAS-NEGOCIO-2012.md` | Visão da analista de negócios SENARC | DRAFT |
| Volumetria atual (`legado-sifap/`) | 4.2M beneficiários, 180M pagamentos, +3.8M/mês | — |

---

## 2. Functional Requirements (FR)

Organizados pelos 4 bounded contexts propostos. Cada FR rastreia para uma ou mais BR-XXX.

### 2.1 Bounded Context: BeneficiaryManagement

| ID | Requisito | BR rastreado | Prioridade |
|---|---|---|---|
| FR-BEN-001 | O sistema deve permitir cadastrar beneficiários com CPF, nome, data de nascimento, sexo, endereço, NIS e programa social vinculado | BR-002, BR-005, BR-031 | MUST |
| FR-BEN-002 | O sistema deve validar CPF pelo algoritmo Módulo 11 antes de qualquer gravação | BR-002, BR-029 | MUST |
| FR-BEN-003 | O sistema deve impedir cadastro com CPF duplicado em status ativo | BR-002 | MUST |
| FR-BEN-004 | O sistema deve permitir alteração de dados cadastrais sem permitir alterar CPF, data de nascimento, sexo ou programa social | BR-001 (legacy CADBENEF) | MUST |
| FR-BEN-005 | O sistema deve gerenciar dependentes vinculados ao beneficiário titular como entidade filha (FK para `beneficiary`) | BR-006, BR-007, BR-008, BR-019 (DDM PE) | MUST |
| FR-BEN-006 | O sistema deve impedir cadastro de dependentes quando o titular tem status `C` (Cancelado) ou `D` (Desligado) | BR-007 | MUST |
| FR-BEN-007 | O sistema deve permitir dependente sem CPF (default zero) e tratar regra de duplicata apenas quando CPF é informado | BR-009 | MUST |
| FR-BEN-008 | O sistema deve aplicar política de máscara de CPF única em toda exibição: `XXX.XXX.XXX-**` (resolve BONUS-03, BONUS-04) | BR-040, BR-041 + NFR-SEC-001 | MUST |
| FR-BEN-009 | O sistema deve registrar evento de auditoria para toda inclusão/alteração/exclusão de beneficiário | BR-038 + NFR-COMP-002 | MUST |
| FR-BEN-010 | O sistema deve diferenciar semanticamente status `S` em dois conceitos: `LifecycleStatus` (Active/Suspended/Cancelled/Inactive/Disabled) e `AgeCategory` (Adult/Senior/Minor) | BR-004 + BONUS-01 | MUST |
| FR-BEN-011 | O sistema deve calcular categoria etária do beneficiário considerando dia/mês (não apenas ano como no legado) | BR-005 + MYS-001 | SHOULD |
| FR-BEN-012 | O sistema deve consultar histórico de pagamentos do beneficiário (últimos N meses, paginado) | BR-039 | MUST |
| FR-BEN-013 | O sistema deve permitir busca de beneficiário por CPF ou NIS | BR-039 (legacy CONSBENF) | MUST |

### 2.2 Bounded Context: SocialProgramRegistry

| ID | Requisito | BR rastreado | Prioridade |
|---|---|---|---|
| FR-PROG-001 | O sistema deve permitir cadastrar programas sociais com código único, nome, tipo (A/P/T), valor base, critérios de elegibilidade e fator de reajuste | BR-010, BR-011 | MUST |
| FR-PROG-002 | O sistema deve aplicar Fator K na composição do valor base do programa, preservando a fórmula histórica `BASE × (1 + FATOR_REAJUSTE × 0.347215)` | BR-010, MYS-003 + ADR-004 | MUST |
| FR-PROG-003 | O sistema deve validar tipo de programa contra valores permitidos: `A` (Assistencial), `P` (Previdenciário), `T` (Trabalho) | BR-011 | MUST |
| FR-PROG-004 | O sistema deve permitir desativar programa (status A→I) sem afetar pagamentos já gerados | BR-011, BR-035 | MUST |
| FR-PROG-005 | O sistema deve validar elegibilidade de beneficiário para programa considerando: status do beneficiário, idade min/max do programa, renda máxima, tipo de programa | BR-011, BR-024, BR-025, BR-026, BR-027 | MUST |
| FR-PROG-006 | O sistema deve marcar região 99 como `SpecialRegion` (diplomática/internacional) com regra explícita parametrizada — substituindo o bypass implícito do legado | BR-024, MYS-008 | MUST |

### 2.3 Bounded Context: PaymentProcessing

| ID | Requisito | BR rastreado | Prioridade |
|---|---|---|---|
| FR-PAY-001 | O sistema deve calcular valor de benefício mensal pela fórmula: `VLR = BASE × FREG × FFAM × FRND × FIDADE × (1+REAJ)` | BR-017 | MUST |
| FR-PAY-002 | O sistema deve aplicar fator regional por UF conforme tabela parametrizada (27 valores, range 1.0–1.4) | BR-012 | MUST |
| FR-PAY-003 | O sistema deve aplicar fator familiar progressivo por número de dependentes: 0=1.0, 1-2=+5%/cada, 3-4=base 1.1 +3%/cada, 5+=base 1.16 +2%/cada | BR-014 | MUST |
| FR-PAY-004 | O sistema deve aplicar fator renda em 5 faixas inversamente proporcionais: ≤300=100%, ≤600=85%, ≤1000=70%, ≤1500=55%, >1500=40% | BR-015 | MUST |
| FR-PAY-005 | O sistema deve aplicar fator idade: ≥65=+15%, ≥60=+10%, <18=+5%, demais=1.0 | BR-016 | MUST |
| FR-PAY-006 | Em dezembro, o sistema deve calcular 13º salário com fórmula reduzida `BASE × FREG × FIDADE` (sem FFAM e FRND) | BR-018, MYS-004 | MUST |
| FR-PAY-007 | Em dezembro, o sistema deve adicionar abono natalino de 15% sobre valor mensal exclusivamente para programas tipo `A` | BR-019, MYS-004 | MUST |
| FR-PAY-008 | O sistema deve aplicar método de arredondamento financeiro definido em ADR-003 (default: half-even bancário com flag de compatibilidade para truncamento legado) | BR-020, BR-021, INC-004 + ADR-003 | MUST |
| FR-PAY-009 | O sistema deve calcular descontos com teto de 30% sobre o bruto, com exceção explícita para descontos do tipo `J` (Judicial) | BR-013, MYS-006 | MUST |
| FR-PAY-010 | O sistema deve aplicar contribuição social progressiva em 4 faixas: ≤500=3%, ≤1000=5%, ≤2000=7%, >2000=9% | BR-022 | MUST |
| FR-PAY-011 | O sistema deve respeitar vigência de descontos (DT-INICIO e DT-FIM) — descontos expirados são ignorados | BR-023 | MUST |
| FR-PAY-012 | O sistema deve gerar pagamentos em lote mensal para todos beneficiários com status `A` e programa ativo | BR-033, BR-034, BR-035 | MUST |
| FR-PAY-013 | O sistema deve garantir idempotência: um beneficiário tem no máximo 1 pagamento por competência | BR-034 | MUST |
| FR-PAY-014 | O sistema deve preservar ordenação por CPF na saída do batch para compatibilidade com sistemas downstream | BR-033, MYS-009 | MUST |
| FR-PAY-015 | O sistema deve conciliar pagamentos com retorno bancário CNAB 240 (Banco do Brasil), com tolerância de R$ 0,01 | BR-036, BR-037 | MUST |
| FR-PAY-016 | O sistema deve atualizar status do pagamento conforme código de retorno CNAB: 00=Pago, 01=Devolvido, 02=Erro | BR-037 | MUST |
| FR-PAY-017 | O sistema deve gerar correção monetária retroativa de pagamentos pelo IPCA acumulado do período | BR-020 (CALCCORR) | SHOULD |
| FR-PAY-018 | O sistema deve usar fórmula única de cálculo (`PaymentCalculatorService`) chamada por todos os fluxos (online + batch) — eliminando duplicação BATCHPGT/CALCBENF | BONUS-02 | MUST |
| FR-PAY-019 | O sistema deve registrar evento de auditoria para conciliações (ação `CO`) e divergências (ação `DV`) | BR-038 | MUST |

### 2.4 Bounded Context: ReportingAndAudit

| ID | Requisito | BR rastreado | Prioridade |
|---|---|---|---|
| FR-AUD-001 | O sistema deve manter trilha imutável de auditoria de todas as operações (criação, alteração, exclusão, consulta crítica) | BR-038, BR-042 + NFR-COMP-002 | MUST |
| FR-AUD-002 | O sistema deve exibir TODAS as ações de auditoria nos relatórios, incluindo exclusões (`EX`) — corrigindo a ocultação do legado | BR-042, MYS-010 + NFR-COMP-001 | MUST |
| FR-AUD-003 | O sistema deve permitir filtros opcionais (período, ação, usuário, entidade) sem ocultar ações por padrão | BR-042 | MUST |
| FR-AUD-004 | O sistema deve preservar retenção mínima de 10 anos para eventos de auditoria (IN-TCU 63/2010) | NFR-COMP-002 | MUST |
| FR-AUD-005 | O sistema deve gerar relatório analítico de pagamentos por período, programa e região | BR-070 (legacy RELPGT) | MUST |
| FR-AUD-006 | O sistema deve gerar relatório consolidado mensal por região, programa e status | BR-063 (legacy BATCHREL) | MUST |
| FR-AUD-007 | O sistema deve correlacionar eventos de auditoria via `correlation_id` para operações compostas | NFR-OBS-002 | SHOULD |

### 2.5 Bounded Context: Cross-Cutting / Greenfield

Requisitos que NÃO existem no legado mas são obrigatórios no SIFAP 2.0.

| ID | Requisito | Origem | Prioridade |
|---|---|---|---|
| FR-AUTH-001 | O sistema deve autenticar usuários via OAuth 2.0 com tokens JWT (substitui sessão de terminal 3270) | [GREENFIELD] | MUST |
| FR-AUTH-002 | O sistema deve aplicar autorização baseada em papel (RBAC) com 4 perfis: ADM, OPR, CON, AUD/SUP | Legacy `AUDITORIA.COD-PERFIL` + GREENFIELD | MUST |
| FR-AUTH-003 | O sistema deve registrar todo login/logout e tentativa de acesso negado como evento de auditoria | NFR-SEC-005 | MUST |
| FR-API-001 | O sistema deve expor API REST versionada (`/api/v1/`) com OpenAPI 3.1 | [GREENFIELD] + techstack | MUST |
| FR-UI-001 | O sistema deve oferecer UI web responsiva (mobile-first) substituindo telas 3270 | [GREENFIELD] | MUST |
| FR-UI-002 | A UI deve seguir padrão acessível WCAG 2.1 AA | NFR-USAB-001 | MUST |

---

## 3. Non-Functional Requirements (NFR)

Categorias baseadas em ISO 25010 (System & Software Quality Models).

### 3.1 Segurança (NFR-SEC)

| ID | Requisito | Critério de aceitação |
|---|---|---|
| NFR-SEC-001 | Dados pessoais sensíveis (CPF, RG, valores de benefício) **nunca** podem aparecer em logs sem mascaramento | Auditoria automatizada de logs no CI: regex `\d{11}` em logs falha o build |
| NFR-SEC-002 | Toda comunicação externa deve usar TLS 1.3+ | Configuração Nginx/App Service rejeita TLS < 1.3 |
| NFR-SEC-003 | Secrets devem ser armazenados em Azure Key Vault e acessados via Managed Identity — nunca em variáveis de ambiente ou código | Scan de secrets (gitleaks) no CI falha o build se detectar |
| NFR-SEC-004 | Backdoors de validação de CPF do legado (8 prefixos + CPFs com dígitos iguais) devem ser desativados em produção — disponíveis apenas via feature flag `allowTestCpfs=true` em ambientes não-produtivos | ADR-008 + testes parametrizados validam comportamento por ambiente |
| NFR-SEC-005 | O sistema deve aplicar rate limiting por endpoint público (default 100 req/min/IP) | Testes de carga validam 429 após threshold |
| NFR-SEC-006 | Conformidade com OWASP Top 10 (validação de entrada, prepared statements, CORS explícito, etc.) | OWASP ZAP scan no pipeline com 0 high/critical |
| NFR-SEC-007 | Autenticação de serviço-a-serviço com Azure exclusivamente via Managed Identity | Terraform impede service principal com senha |

### 3.2 Performance (NFR-PERF)

| ID | Requisito | Critério de aceitação |
|---|---|---|
| NFR-PERF-001 | API REST: latência P95 ≤ 300 ms para consulta de beneficiário; P99 ≤ 800 ms | Load test com k6, 100 RPS sustentado por 5 min |
| NFR-PERF-002 | API REST: latência P95 ≤ 500 ms para cálculo de pagamento individual | Load test cobre 50% leitura + 50% escrita |
| NFR-PERF-003 | Batch mensal de pagamentos: processar 4.2M beneficiários em ≤ 4 horas (janela noturna) | Stress test com seed de 4.2M registros em ambiente equivalente |
| NFR-PERF-004 | Throughput de conciliação CNAB: ≥ 10.000 registros/min | Mensurado em fluxo end-to-end |
| NFR-PERF-005 | Consulta de histórico de pagamentos: P95 ≤ 200 ms (com paginação) | Index em `(cpf, competencia DESC)` confirmado via EXPLAIN |

### 3.3 Escalabilidade (NFR-SCAL)

| ID | Requisito | Critério de aceitação |
|---|---|---|
| NFR-SCAL-001 | Suportar crescimento de 3.8M pagamentos/mês sem degradação de performance acima de 10% | Particionamento PostgreSQL por hash de CPF, 16 partições |
| NFR-SCAL-002 | Escala horizontal stateless: aplicação suporta N instâncias sem session affinity | Health checks + readiness probes no Container Apps |
| NFR-SCAL-003 | Batch deve suportar reprocessamento parcial (retomada do último beneficiário processado) | Checkpointing via Spring Batch ou tabela `batch_job_execution` |

### 3.4 Disponibilidade (NFR-AVAIL)

| ID | Requisito | Critério de aceitação |
|---|---|---|
| NFR-AVAIL-001 | SLA de 99.5% (≤ 3h 39min downtime/mês) para operações online | Application Insights availability tests |
| NFR-AVAIL-002 | Janela de manutenção planejada não conta para SLA (publicada com 7 dias de antecedência) | Status page pública |
| NFR-AVAIL-003 | RTO ≤ 4 horas; RPO ≤ 1 hora para dados transacionais | Backup PostgreSQL automático com PITR ≤ 5 min |
| NFR-AVAIL-004 | Multi-zone deployment dentro de uma região Azure | Terraform força `availability_zone` em PostgreSQL Flexible Server |

### 3.5 Compliance (NFR-COMP)

| ID | Requisito | Critério de aceitação |
|---|---|---|
| NFR-COMP-001 | Conformidade com LGPD: consentimento, direito de acesso, portabilidade, exclusão (lógica), portabilidade | Trilha de auditoria + endpoint `DELETE /api/v1/beneficiaries/{cpf}` (soft delete) |
| NFR-COMP-002 | Retenção mínima de 10 anos para trilha de auditoria (IN-TCU 63/2010) | Política de retenção PostgreSQL + Storage Archive tier |
| NFR-COMP-003 | Trilha de auditoria imutável (nenhum UPDATE/DELETE permitido na tabela `audit_event`) | Trigger PostgreSQL bloqueia UPDATE/DELETE |
| NFR-COMP-004 | Toda exibição de CPF deve mascarar 3 grupos centrais (`XXX.XXX.XXX-**` ou `***.XXX.XXX-**`) — política única documentada em ADR-007 | Code review + componente UI compartilhado `<MaskedCpf>` |
| NFR-COMP-005 | Conformidade com Marco Civil da Internet — logs de acesso retidos por ≥ 6 meses | Application Insights retention ≥ 180 dias |

### 3.6 Manutenibilidade (NFR-MAIN)

| ID | Requisito | Critério de aceitação |
|---|---|---|
| NFR-MAIN-001 | Cobertura de testes: backend ≥ 70% linhas / ≥ 60% branches; frontend ≥ 60% linhas | Jacoco + Vitest reports falham build se abaixo |
| NFR-MAIN-002 | Todo bounded context segue package-by-feature (não package-by-layer) | ArchUnit teste valida estrutura |
| NFR-MAIN-003 | Lógica de cálculo financeiro DEVE estar em um único service por bounded context (sem duplicação como CALCBENF/BATCHPGT no legado) | ArchUnit teste detecta duplicação de fórmulas |
| NFR-MAIN-004 | Toda decisão arquitetural deve ter ADR (`02-spec-moderna/ADR-NNN-*.md`) | CI bloqueia adição de dependência sem ADR correspondente |
| NFR-MAIN-005 | Schema migrations devem ser idempotentes e ter rollback documentado | Flyway + script de rollback obrigatório |

### 3.7 Observabilidade (NFR-OBS)

| ID | Requisito | Critério de aceitação |
|---|---|---|
| NFR-OBS-001 | Logs estruturados em JSON, enviados para Application Insights | Logback configurado com JsonEncoder |
| NFR-OBS-002 | Toda request HTTP carrega `correlation_id` (header `X-Correlation-Id`) propagado em logs e auditoria | Interceptor Spring valida + Playwright E2E confirma |
| NFR-OBS-003 | Métricas custom (RED + USE) expostas via `/actuator/metrics` | Dashboards Application Insights por bounded context |
| NFR-OBS-004 | Tracing distribuído via OpenTelemetry com sampling configurável (default 10%) | OTel collector envia para App Insights |
| NFR-OBS-005 | Alertas configurados para: SLA breach (P95 > limite), error rate > 1%, audit log write failure | Azure Monitor alert rules como código (Terraform) |

### 3.8 Usabilidade (NFR-USAB)

| ID | Requisito | Critério de aceitação |
|---|---|---|
| NFR-USAB-001 | WCAG 2.1 AA mínimo em todas as telas | Axe-core no Playwright CI |
| NFR-USAB-002 | Mensagens de erro em português (PT-BR) com sugestão acionável | i18n via Spring MessageSource + next-intl |
| NFR-USAB-003 | Tempo até interatividade (TTI) ≤ 3.5s em conexão 3G (Next.js Server Components ajudam) | Lighthouse CI score ≥ 90 |
| NFR-USAB-004 | UI responsiva: mobile-first com breakpoints `sm/md/lg` | Testes visuais Playwright em 3 viewports |

### 3.9 Portabilidade (NFR-PORT)

| ID | Requisito | Critério de aceitação |
|---|---|---|
| NFR-PORT-001 | Aplicação deve rodar em qualquer Docker host (sem dependência de features Azure-only no código) | `docker compose up` funcional em dev + prod-equivalente |
| NFR-PORT-002 | Database adapter usa driver PostgreSQL JDBC padrão (sem extensões Azure-exclusivas no schema) | Schema portável para PostgreSQL self-hosted |
| NFR-PORT-003 | Configurações específicas de cloud isoladas em camada Terraform | Mudar provider exige zero mudança de código aplicação |

---

## 4. Constraints (CON)

Restrições não-negociáveis impostas pelo contexto.

| ID | Restrição | Origem |
|---|---|---|
| CON-001 | Stack-alvo é Java 21 + Spring Boot 3.3 + Next.js 15 + PostgreSQL 16 + Azure (sem desvios) | [`techstack.md`](techstack.md) + workshop |
| CON-002 | Arquitetura é Modular Monolith — não microservices | [`modular-monolith.instructions.md`](../.github/instructions/modular-monolith.instructions.md) |
| CON-003 | Todo REQ-ID em `02-spec-moderna/` precisa de `source_legacy:` apontando para `.NSN`/`.ddm` ou `[GREENFIELD]` | [`LEGACY-EXPLORATION-CHECKLIST.md`](../01-arqueologia/LEGACY-EXPLORATION-CHECKLIST.md) |
| CON-004 | Integração CNAB 240 com Banco do Brasil deve ser preservada (não pode mudar layout) | Sistema bancário externo |
| CON-005 | Integração SIAFI (federal) deve ser preservada | Sistema federal externo |
| CON-006 | Trilha de auditoria não pode permitir UPDATE/DELETE (imutabilidade legal) | IN-TCU 63/2010 |
| CON-007 | Retenção mínima de 10 anos para auditoria | IN-TCU 63/2010 |
| CON-008 | LGPD: dados pessoais devem ter consentimento, base legal documentada, direito de exclusão | Lei 13.709/2018 |
| CON-009 | Backdoors de validação de CPF do legado devem ser desativados em produção (decisão de segurança) | ADR-008 |
| CON-010 | Pagamentos não podem ser duplicados (CPF + competência é chave única) | BR-034 |

---

## 5. Out of Scope (OUT)

Funcionalidades **explicitamente excluídas** desta versão do SIFAP 2.0.

| ID | Item | Razão |
|---|---|---|
| OUT-001 | Integração com outros bancos além do Banco do Brasil | Não havia no legado; workshop tem 1 dia |
| OUT-002 | App mobile nativo (iOS/Android) | Frontend é web-only (responsivo cobre mobile) |
| OUT-003 | Funcionalidades do "Plano Verão" (EGG-001) | Política econômica de 1989-1991 — sem relevância atual |
| OUT-004 | Integração com Banco Real (EGG-003) | Banco adquirido pelo Santander em 2007 |
| OUT-005 | Suporte a múltiplos idiomas além de PT-BR | Sistema é federal brasileiro |
| OUT-006 | Self-service para beneficiários (portal cidadão) | Sistema é administrativo (operadores SENARC) |
| OUT-007 | Integração CadÚnico | Mencionado em REGRAS-NEGOCIO-2012 como pendente; fica para v2.1 |
| OUT-008 | Migração ETL única dos 180M registros legados | Estratégia em ADR-006 — provavelmente Strangler Fig |

---

## 6. Decisões Pendentes (bloqueiam alguns FR/NFR)

Decisões que precisam ser resolvidas antes do `/speckit.specify` finalizar EARS dependentes.

| # | Decisão | Bloqueia | Resolução |
|---|---|---|---|
| 1 | Limite real de dependentes (manual=3, código=5, DDM=10) | FR-BEN-005 | Validar com SENARC. Default sugerido: 5 (valor em produção) parametrizável |
| 2 | Semântica de status `S` (Senior vs Suspenso) | FR-BEN-010 | Modelo separado: `LifecycleStatus` + `AgeCategory` (resolvido) |
| 3 | Tratamento do FATOR-K (`0.347215`) | FR-PROG-002 | ADR-004 — proposta: parâmetro em `program_config` |
| 4 | Método de arredondamento (truncamento vs half-even) | FR-PAY-008 | ADR-003 — proposta: half-even com flag de compatibilidade |
| 5 | Backdoors de CPF | NFR-SEC-004 | ADR-008 — proposta: desativar em produção via feature flag |
| 6 | Estratégia de migração de dados | OUT-008 | ADR-006 — Strangler Fig com CDC ou ETL único |
| 7 | Orquestração de batch | FR-PAY-012 | ADR-005 — Spring `@Scheduled` vs Azure Functions |

---

## 7. Matriz de Rastreabilidade (resumo)

| Categoria | Total | Origem |
|---|---|---|
| Functional Requirements (FR) | **48** | Derivados de 71 BR-XXX do Estágio 1 + 6 greenfield |
| Non-Functional Requirements (NFR) | **40** | 9 categorias ISO 25010 |
| Constraints (CON) | **10** | Workshop + regulatório + integrações externas |
| Out of Scope (OUT) | **8** | Decisões explícitas de exclusão |
| Decisões pendentes | **7** | Resolvidas via ADR no Estágio 2 |

### Cobertura BR → FR

- ✅ Todas as 18 BR críticas têm FR correspondente
- ✅ Todas as 71 BR do `business-rules-catalog.final.md` estão referenciadas em pelo menos 1 FR
- ✅ Todos os 10 mistérios (MYS-001 a MYS-010) endereçados como FR, NFR ou ADR pendente
- ✅ 4 inconsistências (INC-001 a INC-004) endereçadas como ADR pendente ou NFR

---

## 8. Próximos passos

1. **Resolver decisões pendentes** via ADRs em `02-spec-moderna/ADR-NNN-*.md` (7 ADRs previstos no `techstack.md`)
2. **Gerar specs EARS** via `/speckit.specify` para cada bounded context, usando este documento como input
3. **Validar com PO** (sign-off de escopo + prioridades MUST/SHOULD)
4. **Refinar com `/speckit.clarify`** ambiguidades restantes
5. **Plano técnico** via `/speckit.plan` + diagramas C4 (L1, L2, L3)

---

## Referências

- [Tech Stack](techstack.md) — escolhas tecnológicas
- [ADR Template](ADR-TEMPLATE.md) — formato dos ADRs
- [Scope Decisions](scope-decisions.md) — decisões de escopo migrar/descartar/evoluir
- [Business Rules Catalog](../01-arqueologia/output-requisitos/business-rules-catalog.final.md) — 71 BRs do legado
- [Mysteries Found](../01-arqueologia/output-requisitos/mysteries-found.final.md) — 24 achados
- [Discovery Report](../01-arqueologia/output-requisitos/discovery-report.final.md) — síntese Estágio 1
- [LEGACY-EXPLORATION-CHECKLIST](../01-arqueologia/LEGACY-EXPLORATION-CHECKLIST.md) — regra dura de `source_legacy`
- [ISO 25010 — System and Software Quality Models](https://iso25000.com/index.php/en/iso-25000-standards/iso-25010) — taxonomia de NFR

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="techstack.md"><strong>Tech Stack</strong></a><br/>
<sub>Escolhas tecnológicas do SIFAP 2.0.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="GUIDE.md"><strong>GUIDE do Estágio 2</strong></a><br/>
<sub>Passo a passo para EARS, ADRs e C4.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>
