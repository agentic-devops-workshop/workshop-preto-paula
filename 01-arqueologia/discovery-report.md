<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Relatório de Descoberta — Estágio 1: Arqueologia Digital

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **discovery-report**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Este documento consolida todas as descobertas do Estágio 1.
> Preencha cada seção com as conclusões do time. **Este é o input principal do Estágio 2** — sem ele, a especificação vira chute.

**Time**: Equipe Visão (Par 1 — PO + RE)
**Data**: 2026-05-20
**Edição**: Workshop SIFAP / Maio 2026
**Participantes**: Par 1 (Product Owner + Requirements Engineer)

---

## 1. Sumário Executivo

O SIFAP é um sistema crítico de pagamento de benefícios sociais escrito em Natural/Adabas em 1997, com aproximadamente 4,2 milhões de beneficiários ativos e 180 milhões de registros de pagamento históricos. O código tem 15 programas e 4 DDMs (files 150–153), com extensivo uso de PE groups (grupos periódicos) para dependentes e descontos. **A análise completa extraiu 71 regras de negócio (18 críticas) e 24 achados (10/10 mistérios oficiais, 3/3 easter eggs, 4/4 inconsistências, 7 bônus).** Principais bloqueios para o Estágio 2: triple mismatch no limite de dependentes (manual=3, código=5, DDM=10), conflito semântico do status `S` (Senior em CADBENEF, Suspenso em CONSBENF/VALELEG), constante mágica `0.347215` sem documentação, e dois métodos de arredondamento diferentes para o mesmo valor (truncamento em CALC* vs half-up em BATCHREL).

---

## 2. Visão Geral do Sistema

### 2.1 Propósito do SIFAP

Sistema centralizado para administração e pagamento de benefícios sociais federais (Bolsa Família, BPC, PETI, etc.), substituindo o antigo SIPAG/DOS (Clipper 5.2, descentralizado em regionais). Gerencia cadastro nacional de beneficiários, geração de folha mensal, conciliação bancária CNAB 240 com o Banco do Brasil, integração SIAFI, e trilha de auditoria com retenção legal de 10 anos.

### 2.2 Arquitetura Legada

- **15 programas Natural** organizados por prefixo funcional: CAD (3), CALC (3), VAL (3), BATCH (3), REL (2), CONS (1)
- **4 DDMs Adabas** (files 150–153) — todos criados em maio/1997 pelo mesmo DBA (Roberto Carlos Ferreira)
- **Plataforma**: z/OS + Natural 6.3.12 + Adabas 7.4.3 + Com*plete 6.3.1 + JES2 + CICS
- **Volumetria atual**: ~4.2M beneficiários, 180M pagamentos históricos, crescimento 3.8M/mês
- **Dados em PE groups**: dependentes (max 10 no DDM, mas código limita 5), descontos (max 8), faixas de cálculo (max 5), parâmetros regionais (6)

### 2.3 Usuários e Perfis

3 perfis identificados no `AUDITORIA.ddm` (campo COD-PERFIL):
- **ADM** — administradores (CADBENEF, CADPROG, alterações)
- **OPR** — operadores (consultas, batch)
- **CON** — consulta (CONSBENF apenas)
- **AUD** / **SUP** — auditoria e supervisão (RELAUDIT)

Usuário especial **`BATCH`** usado pelos jobs JES2 noturnos.

---

## 3. Principais Descobertas

### 3.1 Regras de Negócio Críticas

1. **BR-017** — Fórmula core de cálculo com 5 fatores: `VLR = BASE × FREG × FFAM × FRND × FIDADE × (1+REAJ)` ([CALCBENF.NSN#L224-L231](01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN)). Duplicada em BATCHPGT — risco de divergência.
2. **BR-018** — Em dezembro, o 13º usa fórmula reduzida (sem FFAM e FRND) + abono natalino 15% só para programas tipo A ([CALCBENF.NSN#L239-L258](01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN)).
3. **BR-013** — Desconto total limitado a 30% do bruto, EXCETO desconto Judicial (`J`) que ignora o teto ([CALCDSCT.NSN#L125, L137-L141](01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN)).
4. **BR-024** — Região 99 ("INTERNACIONAL/DIPLOMATICO") pula TODAS as verificações de elegibilidade ([VALELEG.NSN#L106-L110](01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN)).
5. **BR-042** — Ação `EX` (Exclusão) é sempre filtrada dos relatórios de auditoria, independente dos parâmetros do usuário ([RELAUDIT.NSN#L104-L107](01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN)).

### 3.2 Dependências Complexas

- **`BENEFICIARIO.ddm` (file 150)** é hub central — lido/escrito por **9 dos 15 programas**. Mudanças neste DDM afetam toda a base.
- **`CALCBENF` e `BATCHPGT` duplicam a fórmula de cálculo** — não há chamada CALLNAT entre eles. Risco de drift.
- **`CALCBENF` chama `DET-FAIXA-RENDA` e `CALC-DESCONTOS`** internamente (sub-rotinas), mas BATCHPGT replica a lógica inline.
- **`BATCHCON` escreve em `AUDITORIA.ddm`** para cada conciliação e divergência (alto volume).
- **`VALELEG` cruza `BENEFICIARIO` (150) e `PROGRAMA-SOCIAL` (151)** — único programa que junta os dois bounded contexts.

### 3.3 Dívida Técnica Identificada

- [x] **Conflito semântico do status `S`** — 3 significados diferentes nos 3 programas (Senior em CADBENEF, Suspenso em CONSBENF, Suspenso em VALELEG). Modelagem de domínio incoerente.
- [x] **Duplicação de lógica de cálculo** — CALCBENF e BATCHPGT implementam a mesma fórmula independentemente
- [x] **Dois métodos de arredondamento divergentes** — truncamento em CALC* vs half-up em BATCHREL (INC-004 com comentário explícito)
- [x] **2 backdoors de validação de CPF** — VALDOCS (8 prefixos) + VALBENEF (CPFs 000.000.000-00). Risco de segurança.
- [x] **2 vazamentos de máscara de CPF** — CONSBENF e RELPGT expõem dígitos sensíveis (LGPD)
- [x] **3 tabelas hardcoded duplicadas** — fatores regionais aparecem em CALCBENF e BATCHPGT idênticos
- [x] **Constantes mágicas sem documentação** — `0.347215` (FATOR-K), `0.15` (abono natalino), `0.30` (teto desconto)
- [x] **Mismatch entre código, DDM e manual** — limite de dependentes (3/5/10)

### 3.4 Gaps de Documentação

- **Manual técnico (2008)** desatualizado: descreve 3 DDMs (hoje são 4) e não menciona programas CALC* nem RELAUDIT
- **Regras de negócio (2012)** marcado como DRAFT pela própria autora, com `[A COMPLETAR]` em vários campos
- **Arquitetura original (1997)** previa 11 programas em 4 módulos; hoje são 15 — 4 surgiram sem atualizar o documento
- **FATOR-K sem documentação alguma** — confirmado no DDM e no manual de regras
- **Sub-rotina `CHECK-DOC-ESPECIAL` (8 prefixos CPF)** não consta em nenhum documento
- **Filtro de exclusões em RELAUDIT** documentado apenas como NOTA2 dentro do próprio DDM AUDITORIA

---

## 4. Mistérios e Riscos

### 4.1 Mistérios Não Resolvidos

| ID | Descrição | Risco para Migração |
|---|---|---|
| MYS-001 | Status `S` automático para >75 anos | Beneficiários perdem status sem notificação se preservado; quebra fluxo se removido |
| MYS-002 | Limite dependentes 3/5/10 (triple mismatch) | Spec não pode definir contrato sem decisão SENARC |
| MYS-003 | Constante mágica `0.347215` (FATOR-K) | Valores divergem ~34.7% × reajuste se não preservada |
| MYS-004 | Dezembro: fórmula reduzida + abono | Falha no 13º se não modelado como tipo de pagamento separado |
| MYS-005 | Truncamento sistemático (perda de centavos) | Decisão impacta R$/mês em escala de 180M registros |
| MYS-006 | Judicial ignora teto 30% | Provavelmente correto (lei) — validar norma |
| MYS-007 | 8 prefixos CPF aceitos sem validação | Risco de segurança se backdoor permanecer |
| MYS-008 | Região 99 pula elegibilidade | Uso real desconhecido — pode ser legítimo (diplomáticos) |
| MYS-009 | Batch ordenado por CPF — dependência downstream | Arquivo de saída/API deve preservar ordem |
| MYS-010 | Exclusões ocultadas da auditoria | Provável violação compliance — corrigir na migração |

### 4.2 Riscos para o Estágio 2

1. **Decisão SENARC obrigatória sobre limite de dependentes** — sem isto, spec EARS para `CADDEPEND` é inviável
2. **Conflito semântico do status `S`** — modelo de domínio precisa decidir se é `Senior`, `Suspended` ou ambos (com tabela de transição)
3. **FATOR-K como caixa-preta** — Estágio 2 deve marcar como "constante histórica preservada" e tratar a fórmula como contrato imutável
4. **Decisão sobre método de arredondamento** — half-even (banking) ou truncamento legado? Impacto financeiro acumulado.
5. **Backdoors de CPF** — Estágio 2 precisa decidir: remover (risco para integração) ou modelar como `feature flag` parametrizável

---

## 5. Recomendações

### 5.1 O que migrar primeiro

| Prioridade | Funcionalidade | Justificativa |
|---|---|---|
| 1 | **Cadastro de Beneficiário (CADBENEF + VALBENEF + VALDOCS)** | Entidade central, todas as outras funções dependem dela |
| 2 | **Cadastro de Programa Social (CADPROG)** | Catálogo de programas necessário antes de cálculos |
| 3 | **Validação de Elegibilidade (VALELEG)** | Interface entre Beneficiary e Program — core do negócio |
| 4 | **Cálculo de Benefício (CALCBENF + CALCDSCT)** | Core financeiro, depende de itens 1-3 |
| 5 | **Geração de Pagamento (BATCHPGT)** | Pipeline batch — depende de cálculo |
| 6 | **Conciliação Bancária (BATCHCON)** | Fechamento do ciclo — pode ser fase posterior |
| 7 | **Relatórios e Auditoria (REL* + RELAUDIT)** | Read-only, baixo risco, modernização incremental |

### 5.2 O que descartar

- **Easter eggs EGG-001 (Plano Verão 1989-1991)**: código morto sem valor funcional — mover para documentação histórica
- **Easter eggs EGG-003 (Banco Real)**: integração desativada desde 2007 (Santander absorveu) — remover
- **EGG-002 / MYS-007 backdoors de CPF**: avaliar com segurança — se não houver uso real, remover; se houver, modelar como exception explícita
- **MYS-010 ocultação de exclusões em auditoria**: violação provável de compliance — remover na migração

### 5.3 O que evoluir

- **Status `S` ambíguo** → Modelo de domínio com dois conceitos separados: `LifecycleStatus` (Active/Suspended/Cancelled/Inactive/Disabled) e `AgeCategory` (Adult/Senior/Minor)
- **Limite de dependentes** → Constraint parametrizável no PostgreSQL (default 5, configurável por programa)
- **Arredondamento** → Migrar para half-even (banking) com flag de compatibilidade para preservar truncamento se necessário
- **Máscaras de CPF** → Padrão único conforme LGPD (`XXX.XXX.XXX-**`) em todas as exibições
- **Filtro de auditoria** → Manter todas as ações visíveis (incluindo `EX`) com filtro opcional explícito pelo usuário
- **Lógica duplicada CALCBENF/BATCHPGT** → Extrair `PaymentCalculatorService` único, chamado pelos dois fluxos

---

## 6. Métricas do Estágio

| Métrica | Valor |
|---|---|
| Programas analisados | **15 / 15** ✅ |
| DDMs mapeados | **4 / 4** ✅ |
| Documentos legados lidos | **3 / 3** ✅ |
| Regras de negócio encontradas | **71** (42 detalhadas neste catálogo + 29 no `.final.md`) |
| Regras escondidas encontradas | **10 / 10** ✅ |
| Easter eggs encontrados | **3 / 3** ✅ |
| Inconsistências confirmadas | **4 / 4** ✅ |
| Achados bônus | **7** |
| Termos no glossário | **44** |
| Mistérios catalogados | **24** (10 MYS + 3 EGG + 4 INC + 7 BONUS) |
| Tempo total gasto | ~4 horas |

---

## 7. Notas para o Próximo Estágio

**Para o Par 2 (Arquitetura) — Enterprise Architect + Software Architect:**

1. **4 bounded contexts propostos** (validar antes do C4):
   - **BeneficiaryManagement** (CADBENEF, CADDEPEND, VALBENEF, VALDOCS) → ARQ 150
   - **SocialProgramRegistry** (CADPROG, VALELEG) → ARQ 151
   - **PaymentProcessing** (BATCHPGT, BATCHCON, CALCBENF, CALCCORR, CALCDSCT) → ARQ 152
   - **ReportingAndAudit** (BATCHREL, RELPGT, RELAUDIT, CONSBENF) → ARQ 153 + reads dos outros

2. **3 ADRs obrigatórios no Estágio 2**:
   - ADR sobre **estratégia de PE groups** (tabelas filhas vs JSON vs arrays PostgreSQL)
   - ADR sobre **método de arredondamento** (preservar truncamento ou migrar half-even)
   - ADR sobre **tratamento do FATOR-K** (constante imutável vs configuração)

3. **Decisões pendentes que bloqueiam EARS** (todas em `mysteries-found.md`):
   - Limite real de dependentes (5/10/configurável?)
   - Significado do status `S` (Senior, Suspended, ambos?)
   - Política de máscara de CPF padronizada (LGPD)
   - Manter ou remover backdoors de CPF

4. **Todas as 71 regras já têm candidata EARS** em `output-requisitos/business-rules-catalog.final.md` — usar como base para `/speckit.specify`.

---

## Definição de Pronto deste relatório

- [x] Todas as seções acima preenchidas (sem placeholders).
- [x] Pelo menos 5 regras críticas listadas em §3.1, cada uma referenciando uma `BR-XXX` do catálogo.
- [x] Decisões de migrar/descartar/evoluir em §5 cobrem as 8+ funcionalidades principais.
- [x] Métricas de §6 conferem com os outros artefatos (`glossary.md` = 44 termos, `business-rules-catalog.md` = 71 regras, `mysteries-found.md` = 24 achados).

— Paula


---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="mysteries-found.md"><strong>mysteries-found.md</strong></a><br/>
<sub>Lista de mistérios.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../02-spec-moderna/GUIDE.md"><strong>Estágio 2 — Spec</strong></a><br/>
<sub>Próximo estágio: spec moderna.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>

