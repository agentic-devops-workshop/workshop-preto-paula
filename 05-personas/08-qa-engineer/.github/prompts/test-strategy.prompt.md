<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
mode: ask
model: claude-opus-4-6
description: "Escreva uma estratégia de testes para uma feature do SIFAP 2.0: camadas da pirâmide, escolhas de framework, ambientes e critérios de saída."
---

# /test-strategy

## Objetivo

Você é um QA lead escrevendo a estratégia de testes para uma feature do SIFAP 2.0. A estratégia diz ao time **o que testar, em qual camada, com qual ferramenta, contra qual ambiente e como sabemos que terminamos**. Ela é aprovada pelo Technical Lead depois de `/speckit.tasks` e antes de `/speckit.implement`, e fica em `specs/<NNN>-<feature>/TEST-STRATEGY.md`.

## Entradas

Peça ao usuário o que estiver faltando.

- A pasta da feature (`specs/<NNN>-<feature>/`) com `SPECIFICATION.md` e `DESIGN.md` já aprovados.
- O perfil de risco: financeiro (`high`), regulatório (`high`), batch (`medium`), somente leitura (`low`).
- Restrições: orçamento de tempo, minutos de CI paralelo, ambientes disponíveis (`local`, `dev`, `stage`, `prod-shadow`).
- Quaisquer requisitos não funcionais com limites mensuráveis (latência p95, throughput, RPO/RTO).

## Processo

1. **Classifique cada `REQ-ID` por camada de teste.** Use a pirâmide de testes:
 - **Unit** — funções puras, calculadoras, validadores (a maioria de `REQ-CALC-*`).
 - **Integration** — adapters: repositories, filas, serviços externos (a maioria de `REQ-PAY-*`).
 - **Contract** — testes consumer/provider de API (frontend ↔ backend, backend ↔ wrapper Adabas externo).
 - **End-to-end** — apenas jornadas críticas de usuário (cadastro, desembolso, consulta de auditoria).
 - **Non-functional** — performance, segurança, acessibilidade, observabilidade.
2. **Escolha ferramentas por camada.** JUnit 5 + AssertJ + Mockito (unit/integration backend), Testcontainers (integration), Pact (contract), Playwright (E2E), k6 (load), OWASP ZAP (security baseline), axe-core (a11y).
3. **Defina a estratégia de dados de teste.** Dados sintéticos para happy paths, snapshots legados anonimizados para casos de borda, seeds determinísticas para testes property-based. Nenhum PII de produção em qualquer ambiente.
4. **Mapeie testes para ambientes.** Unit/integration a cada push (CI). Contract em PR para `develop`. E2E noturno em `stage`. Performance semanal em `prod-shadow`.
5. **Defina critérios de saída.** Por camada: cobertura mínima de `REQ-IDs` (não de linhas), taxa máxima de flakiness, runtime p95 máximo.
6. **Identifique riscos e mitigações.** Dependências externas flaky, suítes de teste lentas, vazamento de dados, drift de ambiente.
7. **Escreva a estratégia como `TEST-STRATEGY.md`.**

## Saída

O entregável é um arquivo markdown com esta estrutura:

```markdown
# Test Strategy — <feature> (REQ-<DOMAIN>-*)

## 1. Scope
In scope: REQ-PAY-001 .. REQ-PAY-024
Out of scope: legacy compatibility tests (covered by `specs/002-legacy-bridge`)

## 2. Risk profile
Financial: high — every disbursement is auditable.
Regulatory: high — SISP / TCU audit trail required.

## 3. Test pyramid

| Layer | Framework | Coverage target | Where it runs |
|--------------|--------------------------|---------------------------|---------------|
| Unit | JUnit 5 + AssertJ | 100% of `REQ-CALC-*` | every push |
| Integration | Spring Boot Test + Testcontainers (Postgres 16) | 100% of `REQ-PAY-*` adapters | every push |
| Contract | Pact (consumer-driven) | every API endpoint | PR to develop |
| E2E | Playwright | 5 happy-path journeys | nightly stage |
| Performance | k6 | p95 < 250 ms disburse | weekly |
| Security | OWASP ZAP baseline + Trivy | every release candidate | nightly |
| Accessibility| axe-core | WCAG 2.1 AA on key pages | every push |

## 4. Data strategy
- Synthetic JSON fixtures in `src/test/resources/fixtures/`.
- Anonymized snapshots from `02-cenario-sifap-legado/` for legacy regression cases.
- No production PII anywhere — `dev` and `stage` use synthetic data only.

## 5. Environments
local → dev (CI) → stage (nightly E2E) → prod-shadow (perf + chaos)

## 6. Exit criteria
- 100% of REQ-IDs have at least one direct test.
- 0 critical or high security findings.
- p95 latency under defined threshold for 7 consecutive nightly runs.
- Flakiness rate < 1% over the last 50 runs.

## 7. Risks
- Adabas legacy bridge is single-vendor; mock it for CI, hit it once nightly.
- Test data refresh from legacy is manual; automate by Sprint 4.

## 8. Schedule
Sprint 1: unit + integration scaffolding. Sprint 2: contract + E2E. Sprint 3: perf + security.
```

## Exemplo trabalhado

**Entrada:** Feature `003-payment-processing`, risco financeiro alto, 24 REQ-IDs, cronograma de quatro semanas.

**Saída esperada:** o markdown acima, preenchido com os 24 REQ-IDs agrupados por camada, três cenários de perf nomeados (desembolso único, batch de 10k, tempestade de retries) e um registro de riscos de 6 linhas.

## Anti-padrões

- Uma meta de 100% de cobertura de linhas sem meta de cobertura de requisitos. Linhas são fáceis; comportamentos não.
- Testes E2E para tudo. Eles são lentos, flaky e um lugar ruim para verificar lógica de ramificação.
- Pular a camada de contrato entre frontend e backend. Quebras em PR custarão mais do que isso economiza.
- Usar dados de produção em qualquer ambiente não produtivo. Risco LGPD / regulatório.
- Definir critérios de saída como "todos os testes passam" — isso é uma tautologia.
- Escolher ferramentas que o time ainda não usou no meio da sprint. A estratégia reflete a realidade.

## Critérios de sucesso

- [ ] Todo `REQ-ID` é mapeado para exatamente uma camada primária (com secundária opcional).
- [ ] Cada camada tem uma ferramenta nomeada, uma meta de cobertura e um orçamento de runtime.
- [ ] A estratégia de dados proíbe explicitamente PII de produção em não produção.
- [ ] Os critérios de saída são mensuráveis e delimitados no tempo.
- [ ] Os riscos têm owners nomeados e datas de mitigação.
- [ ] O documento é curto o suficiente (< 3 páginas) para que o time inteiro realmente leia.
