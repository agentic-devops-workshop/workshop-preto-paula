<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Decisões de Escopo — SIFAP 2.0

![ESTÁGIO 02 Spec](https://img.shields.io/badge/ESTÁGIO-02%20Spec-00A4EF?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S2](https://img.shields.io/badge/PREENCHA-Durante%20S2-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 2](README.md) → **Scope Decisions**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 2 (Spec Moderna).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento preenchido para sua feature
> 2. Rastreabilidade `source_legacy:` para cada REQ-ID
> 3. Sign-off do Product Owner antes da passagem H2
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Para cada funcionalidade encontrada no Estágio 1, decida: **Migrar**, **Descartar** ou **Evoluir**.
>
> - **Migrar**: trazer para o SIFAP 2.0 como está (mesma lógica, nova tecnologia)
> - **Descartar**: não trazer — funcionalidade obsoleta ou desnecessária
> - **Evoluir**: trazer E melhorar (nova UX, novo fluxo, nova capacidade)

**Time**: Equipe Visão (Par 1 — PO + RE)
**Data**: 2026-05-20
**Edição**: Workshop SIFAP / Maio 2026
**Par 1 (Product Owner) responsável**: Paula Silva

## Por que isso importa

O escopo é o que protege o time de chegar às 17h00 com 12 features pela metade. Se o Par 1 não cortar, o Estágio 3 não fecha. **Decisão difícil é tomada aqui, não no Estágio 3.**

## Como decidir

Pergunte de cada funcionalidade:

1. **Afeta o ciclo mensal de pagamento?** Sim → Migrar. Não → considere descartar.
2. **Tem uso documentado nos últimos 12 meses?** Não → descartar.
3. **Faz parte de um relatório regulatório obrigatório (TCU, CGU, BB)?** Sim → Migrar como está.
4. **Tem uma versão moderna mais barata de implementar?** Sim → Evoluir.

---

## Decisões por Funcionalidade

> Decisões derivadas do [`discovery-report.md`](../01-arqueologia/discovery-report.md) §5 e refinadas no [`baseline/requirements.md`](baseline/requirements.md) §5.

| # | Funcionalidade | Decisão | Justificativa | BR-XXX | Prioridade |
|---|---|---|---|---|---|
| 1 | Cadastro de Beneficiários (CADBENEF) | **Evoluir** | Entidade central. Manter regras (CPF Mod-11, status) + corrigir conflito semântico do status `S` (BONUS-01) + máscara LGPD única (ADR-007) | BR-001 a BR-005, BR-040 | Alta |
| 2 | Cadastro de Dependentes (CADDEPEND) | **Evoluir** | Migrar PE group para tabela filha (ADR-002) + resolver triple mismatch do limite (MYS-002 → parametrizável) | BR-006 a BR-009 | Alta |
| 3 | Cadastro de Programas (CADPROG) | **Evoluir** | Preservar FATOR-K legado como default + governança via parâmetro auditável (ADR-004) | BR-010 a BR-011 | Alta |
| 4 | Validação Cadastro (VALBENEF) | **Evoluir** | Inline no service de cadastro (não programa órfão) | BR-029 a BR-032 | Alta |
| 5 | Validação Documentos (VALDOCS) | **Evoluir** | Backdoor de 8 prefixos CPF disabled em prod via feature flag (ADR-008) | BR-028 | Alta |
| 6 | Validação Elegibilidade (VALELEG) | **Evoluir** | Região 99 vira `SpecialRegion` explícita (MYS-008) em vez de bypass implícito | BR-024 a BR-027 | Alta |
| 7 | Cálculo Benefício (CALCBENF) | **Migrar** | Preservar fórmula 5 fatores (BR-017) e regras de dezembro (MYS-004). Unificar com BATCHPGT em `PaymentCalculatorService` único (FR-PAY-018, BONUS-02) | BR-012 a BR-019 | Alta |
| 8 | Cálculo Correção (CALCCORR) | **Migrar** | Recálculo retroativo IPCA. Preservar tabelas IPCA + remover bloco morto Plano Verão (EGG-001) | BR-020 | Média |
| 9 | Cálculo Descontos (CALCDSCT) | **Migrar** | Preservar teto 30% + exceção Judicial (BR-013, MYS-006) | BR-013, BR-022, BR-023 | Alta |
| 10 | Batch Pagamentos (BATCHPGT) | **Evoluir** | Migrar para Spring Batch + Quartz (ADR-005). Preservar ordenação por CPF (MYS-009 → BR-033). Remover duplicação com CALCBENF (BONUS-02) | BR-033 a BR-035 | Alta |
| 11 | Batch Conciliação (BATCHCON) | **Evoluir** | Manter CNAB 240 BB (CON-004) + remover código morto Banco Real (EGG-003) | BR-036 a BR-038 | Alta |
| 12 | Batch Relatórios (BATCHREL) | **Evoluir** | Unificar método de arredondamento com CALC* (ADR-003 resolve INC-004) | BR-021, BR-063 | Média |
| 13 | Consulta Beneficiário (CONSBENF) | **Evoluir** | Corrigir vazamento de máscara CPF (BONUS-03 → ADR-007). Manter histórico 12 meses paginado | BR-039, BR-040 | Média |
| 14 | Relatório Pagamentos (RELPGT) | **Evoluir** | Corrigir vazamento de máscara CPF (BONUS-04 → ADR-007). Substituir output mainframe por PDF/CSV | BR-041 | Média |
| 15 | Relatório Auditoria (RELAUDIT) | **Evoluir** | Remover filtro silencioso de ação `EX` (MYS-010 → FR-AUD-002). Compliance IN-TCU 63/2010 | BR-042 | Alta |
| 16 | Trilha de Auditoria (AUDITORIA.ddm) | **Migrar** | Preservar retenção 10 anos. Schema imutável (NFR-COMP-003) | BR-038, BR-042 | Alta |

---

## Funcionalidades Novas (não existem no legado)

> Todas com `source_legacy: [GREENFIELD]` no Estágio 2. Detalhamento em [`baseline/requirements.md`](baseline/requirements.md) §2.5.

| # | Funcionalidade Nova | Justificativa | Prioridade | Complexidade |
|---|---|---|---|---|
| N1 | OAuth 2.0 + JWT (Spring Security) | Substitui sessão de terminal 3270; obrigatório para API REST e integração Entra ID | Alta | Média |
| N2 | RBAC com 4 perfis (ADM/OPR/CON/AUD) | Legacy `AUDITORIA.COD-PERFIL` virou modelo explícito; sem authorization granular hoje | Alta | Baixa |
| N3 | API REST `/api/v1/` + OpenAPI 3.1 | Workshop tem que expor endpoint para frontend Next.js + integrações futuras | Alta | Média |
| N4 | UI web responsiva (Next.js 15 + shadcn/ui) | Substitui telas 3270; mobile-first; WCAG 2.1 AA | Alta | Alta |
| N5 | Trilha de auditoria com `correlation_id` | Tracing distribuído de operações compostas; pré-requisito para observabilidade moderna | Média | Baixa |
| N6 | Endpoint LGPD (right of access / deletion) | Lei 13.709/2018 — sem mecanismo no legado | Média | Média |
| N7 | Rate limiting por endpoint público | OWASP defesa contra DoS; sem limite no legado (sessão terminal era física) | Média | Baixa |
| N8 | Application Insights dashboards por bounded context | Observabilidade moderna; legacy só tinha SMF logs do z/OS | Baixa | Baixa |

---

## Resumo de Escopo

| Decisão | Quantidade | Percentual |
|---|---|---|
| Migrar (preservar lógica + tecnologia nova) | 4 | 25% |
| Descartar (não traz, marcado em OUT) | 0 funcionalidades + 3 easter eggs + 5 itens out-of-scope | — |
| Evoluir (preservar + melhorar) | 12 | 75% |
| **Total funcionalidades legadas** | **16** | **100%** |
| Greenfield (novas) | 8 | — |

## Itens explicitamente descartados (OUT)

Detalhados em [`baseline/requirements.md`](baseline/requirements.md) §5:

| ID | Item | Razão |
|---|---|---|
| OUT-001 | Outros bancos além do BB | Não havia no legado; workshop tem 1 dia |
| OUT-002 | App mobile nativo | Frontend responsivo cobre |
| OUT-003 | Plano Verão (EGG-001) | Política econômica 1989-1991 — código morto |
| OUT-004 | Banco Real (EGG-003) | Adquirido pelo Santander em 2007 |
| OUT-005 | Multi-idioma além PT-BR | Sistema federal brasileiro |
| OUT-006 | Self-service beneficiário | Sistema administrativo SENARC |
| OUT-007 | Integração CadÚnico | Fica para v2.1 |
| OUT-008 | ETL único de 180M registros | Strategy = Strangler Fig (ADR-006) |

## Riscos de Escopo

| Risco | Probabilidade | Impacto | Mitigação |
|---|---|---|---|
| **MYS-003 FATOR-K não confirmado por SENARC** durante workshop | Alta | Alto | Preservar valor exato `0.347215` como default (ADR-004); marcar `[PENDING-SENARC]` em qualquer requisito dependente |
| **Triple mismatch dependentes (3/5/10)** sem decisão SENARC | Alta | Médio | Default 5 (valor atual em produção); parametrizável via `social_program.max_dependents` |
| **Semântica conflitante do status `S`** (Senior vs Suspenso) | Média | Alto | Modelo separado em 2 conceitos (`LifecycleStatus` + `AgeCategory`) — FR-BEN-010 |
| **Método de arredondamento muda em produção** (R$ 900K/mês) | Média | Crítico | Cut-over com flag `LEGACY_TRUNCATE` em M-1, half-even em M (ADR-003) |
| **Backdoors CPF em uso real desconhecido** | Baixa | Alto | Feature flag, prod = false, app refuses to start se = true em prod (ADR-008) |
| **Workshop não termina implementação de PaymentProcessing** | Alta | Médio | Priorizar BeneficiaryManagement primeiro (CADBENEF + dependentes), PaymentProcessing como stretch goal |
| **Migração CDC de Adabas não testada em workshop** | Alta | Médio | POC com 10K rows + runbook documentado para produção (ADR-006) |
| **LGPD compliance gap** se máscara de CPF inconsistente | Média | Crítico | Componente único `BrazilianCpfMasker`/`<MaskedCpf>` + log filter + CI gate (ADR-007) |

## Aprovação

- [x] Par 1 (Product Owner) aprovou as decisões de escopo — Paula Silva, 2026-05-20
- [x] Par 2 (Enterprise Architect) validou a viabilidade técnica — refletido nos 8 ADRs em `baseline/`
- [ ] Par 3 (Technical Lead) confirmou que cabe nas 3 horas do Estágio 3 — **pendente** (depende da decisão de priorização: BeneficiaryManagement first vs PaymentProcessing first)
- [x] Time concordou com as prioridades — refletido em `baseline/requirements.md` (MUST/SHOULD)

> **Sign-off**: Esta versão fecha a aprovação do Par 1 para a Passagem #2 (Estágio 2 → Estágio 3). Mudanças após este ponto exigem novo sign-off documentado.

---

## Próximos passos

1. **Par 3 (Tech Lead)** revisa a priorização e confirma o escopo executável em 3 horas
2. **`/speckit.specify`** para o primeiro bounded context (sugerido: `BeneficiaryManagement` por ser entidade central)
3. **C4 L1 + L2** desenhados pelo Par 2 (próximo deliverable do Estágio 2)
4. **Passagem #2** ao Par 3 + Par 4 quando specs EARS estiverem prontas com `source_legacy:` em 100%

— Paula


---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="GUIDE.md"><strong>GUIDE do Estágio 2</strong></a><br/>
<sub>Passo a passo do estágio.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="ADR-TEMPLATE.md"><strong>ADR-TEMPLATE</strong></a><br/>
<sub>Template de ADR.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>

