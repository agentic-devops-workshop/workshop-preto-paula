<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
title: "Inventário do Estágio 1"
description: "Esqueleto para o inventário do codebase legado produzido por /archaeology-kickoff"
author: "Paula Silva, AI-Native Software Engineer, Americas Global Black Belt at Microsoft"
date: "2026-04-29"
version: "1.0.0"
status: "approved"
tags: ["template", "inventory", "archaeology", "stage-1"]
---

<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Inventário Legado — Equipe Visão (Par 1 · PO + RE)

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![STATUS Preenchido](https://img.shields.io/badge/STATUS-Preenchido-7FBA00?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../../README.md) → [Estágio 1](../README.md) → **output-requisitos** → **inventory**

> Inventário gerado por `/archaeology-kickoff` em 2026-05-20. Primeira passada — a ser revisado conforme a equipe lê arquivos individuais.

**Data:** 2026-05-20
**Caminho escaneado:** `01-arqueologia/legado-sifap/`

## Estrutura de Pastas

```text
01-arqueologia/legado-sifap/
├── COMO-LER-NATURAL.md          ← Guia de leitura para não-naturalistas
├── README.md                     ← Visão geral do legado SIFAP
├── adabas-ddms/                  ← 4 DDMs (schema Adabas)
│   ├── AUDITORIA.ddm             (Arquivo 153)
│   ├── BENEFICIARIO.ddm          (Arquivo 150)
│   ├── PAGAMENTO.ddm             (Arquivo 152)
│   ├── PROGRAMA-SOCIAL.ddm       (Arquivo 151)
│   └── README.md
├── legacy-docs/                  ← Documentação histórica (3 docs)
│   ├── ARQUITETURA-ORIGINAL-1997.md
│   ├── MANUAL-TECNICO-SIFAP-2008.md
│   ├── REGRAS-NEGOCIO-2012.md
│   └── README.md
└── natural-programs/             ← 15 programas Natural (.NSN)
    ├── BATCHCON.NSN
    ├── BATCHPGT.NSN
    ├── BATCHREL.NSN
    ├── CADBENEF.NSN
    ├── CADDEPEND.NSN
    ├── CADPROG.NSN
    ├── CALCBENF.NSN
    ├── CALCCORR.NSN
    ├── CALCDSCT.NSN
    ├── CONSBENF.NSN
    ├── RELAUDIT.NSN
    ├── RELPGT.NSN
    ├── VALBENEF.NSN
    ├── VALDOCS.NSN
    ├── VALELEG.NSN
    └── README.md
```

**Total:** 3 diretórios, 27 arquivos (15 `.NSN` + 4 `.ddm` + 5 `.md` docs + 3 `.md` READMEs)

## Contagem de Arquivos por Tipo

| Extensão | Contagem | Propósito Provável |
| -------- | -------- | ------------------- |
| `.NSN`   | 15       | Programas fonte Natural (convenção SIFAP: extensão `.NSN` em vez do padrão `.nat`) |
| `.ddm`   | 4        | Data Definition Modules — schema dos arquivos Adabas |
| `.md`    | 8        | Documentação (READMEs, guia de leitura, docs legados) |
| `.cpy`   | 0        | Copycodes — nenhum encontrado (possível código inline ou perda histórica) |
| `.map`   | 0        | MAP screens — nenhum encontrado (provável perda ou embutido nos `.NSN`) |

## Padrões de Convenção de Nomes

| Prefixo   | Contagem | Hipótese |
| --------- | -------- | -------- |
| `BATCH*`  | 3        | **Programas batch** — processamento em lote (pagamento, relatório consolidado, conciliação bancária) |
| `CAD*`    | 3        | **Cadastro** — manutenção de entidades principais (beneficiário, dependente, programa social) |
| `CALC*`   | 3        | **Cálculo** — lógica financeira/matemática (benefício, correção monetária, descontos) |
| `VAL*`    | 3        | **Validação** — sub-rotinas chamadas antes de gravação (beneficiário, documentos, elegibilidade) |
| `REL*`    | 2        | **Relatório** — geração de relatórios impressos/flat file (pagamentos analítico, auditoria) |
| `CONS*`   | 1        | **Consulta** — tela online 3270 para consulta (beneficiário) |

> **Observação:** Distribuição simétrica — 3+3+3+3+2+1 = 15 programas. Os prefixos revelam uma arquitetura funcional clara: cadastro → validação → cálculo → batch → relatório → consulta.

## Catálogo de Programas (15 .NSN)

| # | Programa | Prefixo | Autor Original | Data | Última Alteração | Objetivo (do cabeçalho) |
|---|----------|---------|----------------|------|------------------|------------------------|
| 1 | `CADBENEF.NSN` | CAD | Carlos Roberto da Silva | 15/03/1997 | 10/01/2011 | Cadastro de beneficiário — inclusão/alteração, ARQ 150 |
| 2 | `CADDEPEND.NSN` | CAD | Ana Lucia Pereira | 20/06/1998 | 14/03/2008 | Cadastro de dependentes do beneficiário |
| 3 | `CADPROG.NSN` | CAD | Marcos Antonio Ribeiro | 10/09/1997 | 18/11/2012 | Cadastro de programa social |
| 4 | `VALBENEF.NSN` | VAL | Marcia Helena Oliveira | 08/01/1998 | 30/11/2010 | Validação dados cadastrais — chamada antes de gravação, ARQ 150 |
| 5 | `VALDOCS.NSN` | VAL | Ana Lucia Pereira | 14/05/1998 | 07/06/2011 | Validação de documentos (CPF, RG, complementares), ARQ 150 |
| 6 | `VALELEG.NSN` | VAL | Jose Ferreira dos Santos | 03/02/1999 | 05/04/2013 | Validação elegibilidade beneficiário p/ programa, ARQ 150/155 |
| 7 | `CALCBENF.NSN` | CALC | Carlos Roberto da Silva | 18/04/1997 | 15/06/2004 | Cálculo de benefício (13º, fator regional) |
| 8 | `CALCCORR.NSN` | CALC | Patricia Gomes de Souza | 12/07/2001 | 15/08/2014 | Correção monetária (índices IPCA, período) |
| 9 | `CALCDSCT.NSN` | CALC | Roberto Mendes Junior | 25/08/1999 | 30/09/2015 | Cálculo de descontos (judicial, alíquotas) |
| 10 | `BATCHPGT.NSN` | BATCH | Carlos Roberto da Silva | 22/06/1997 | 10/07/2015 | Batch de pagamento (mais alterado: 5 revisões) |
| 11 | `BATCHREL.NSN` | BATCH | Patricia Gomes de Souza | 10/11/1999 | 14/02/2013 | Relatórios consolidados mensais (região/programa/status) |
| 12 | `BATCHCON.NSN` | BATCH | Marcos Antonio Ribeiro | 05/03/2000 | 30/01/2014 | Conciliação pagamentos × retorno bancário CNAB 240 |
| 13 | `CONSBENF.NSN` | CONS | Marcia Helena Oliveira | 28/09/1998 | 05/08/2012 | Consulta online 3270 beneficiário + histórico pagamentos |
| 14 | `RELPGT.NSN` | REL | Ana Lucia Pereira | 17/12/1999 | 10/08/2010 | Relatório analítico pagamentos por período (impressora 66 lin/pag) |
| 15 | `RELAUDIT.NSN` | REL | Roberto Mendes Junior | 20/08/2002 | 15/09/2014 | Relatório trilha de auditoria (filtros período/ação) |

## Catálogo de DDMs (4 arquivos Adabas)

| DDM | Arquivo Adabas | Autor | Data | Última Alteração |
|-----|---------------|-------|------|------------------|
| `BENEFICIARIO.ddm` | 150 | Roberto Carlos Ferreira (DBA) | 12/05/1997 | 18/07/2005 — campos biometria |
| `PROGRAMA-SOCIAL.ddm` | 151 | Roberto Carlos Ferreira (DBA) | 12/05/1997 | 22/03/2002 — novos programas MDS |
| `PAGAMENTO.ddm` | 152 | Roberto Carlos Ferreira (DBA) | 15/05/1997 | 10/01/1999 — campos batch |
| `AUDITORIA.ddm` | 153 | Roberto Carlos Ferreira (DBA) | 20/05/1997 | 15/04/2005 — campos detalhamento |

> **Nota:** Todos DDMs criados pelo mesmo DBA (Roberto C. Ferreira) em maio/1997. File numbers sequenciais (150–153).

## Itens Incomuns (Top 3)

| # | Caminho do Arquivo | O Que o Torna Incomum | Investigação Sugerida |
|---|---|---|---|
| 1 | `BATCHPGT.NSN` | **Programa mais alterado** — 5 revisões entre 2000–2015. Toca pagamento (core financeiro), 13º salário, abono, novas faixas e auditoria. | Ler com atenção redobrada: acumula 18 anos de patches. Provável fonte de regras escondidas e constantes mágicas. |
| 2 | `VALELEG.NSN` | **Referencia 2 arquivos Adabas** (ARQ 150 + ARQ 155). Nenhum DDM com arquivo 155 existe em `adabas-ddms/`. | Investigar: ARQ 155 é um DDM perdido? Um alias? Possível mistério para `mysteries-found.md`. |
| 3 | `REGRAS-NEGOCIO-2012.md` | **Status DRAFT** — levantamento por analista de negócios (SENARC), não pelo time técnico. Pode ter visão complementar ou conflitante com o código. | Ler antes de extrair regras dos `.NSN` — pode confirmar ou contradizer o que o código realmente faz. |

## Ordem de Leitura Proposta

> **Hipótese de leitura.** A ordem real mudará conforme a equipe rastreia dependências com `/map-dependencies`.

### Fase A — Entender os dados (DDMs primeiro)
1. `BENEFICIARIO.ddm` (ARQ 150) — entidade central
2. `PROGRAMA-SOCIAL.ddm` (ARQ 151) — programas que o beneficiário participa
3. `PAGAMENTO.ddm` (ARQ 152) — registros financeiros
4. `AUDITORIA.ddm` (ARQ 153) — trilha de auditoria

### Fase B — Ler os pontos de entrada (cadastros) — **Par 1 · Visão**
5. `CADBENEF.NSN` — cadastro do beneficiário (ponto de entrada principal, mais antigo)
6. `CADPROG.NSN` — cadastro de programa social
7. `CADDEPEND.NSN` — cadastro de dependentes

### Fase C — Ler as validações (sub-rotinas chamadas pelos cadastros) — **Par 4 · Qualidade**
8. `VALBENEF.NSN` — validação de dados cadastrais
9. `VALDOCS.NSN` — validação de documentos
10. `VALELEG.NSN` — validação de elegibilidade (⚠️ referencia ARQ 155 desconhecido)

### Fase D — Ler os cálculos (lógica financeira) — **Par 3 · Implementação**
11. `CALCBENF.NSN` — cálculo do valor do benefício
12. `CALCCORR.NSN` — correção monetária
13. `CALCDSCT.NSN` — descontos

### Fase E — Ler os batch e relatórios (orquestração) — **Par 2 · Arquitetura** + **Par 5 · Operações**
14. `BATCHPGT.NSN` — processamento de pagamento em lote (programa mais complexo)
15. `BATCHCON.NSN` — conciliação bancária CNAB 240
16. `BATCHREL.NSN` — relatórios consolidados mensais
17. `CONSBENF.NSN` — consulta online 3270
18. `RELPGT.NSN` — relatório analítico de pagamentos
19. `RELAUDIT.NSN` — relatório de auditoria

### Documentação de apoio (ler em paralelo)
- `REGRAS-NEGOCIO-2012.md` — contrastar com regras extraídas do código
- `MANUAL-TECNICO-SIFAP-2008.md` — referência técnica
- `ARQUITETURA-ORIGINAL-1997.md` — visão original do sistema
- `COMO-LER-NATURAL.md` — guia de leitura para não-naturalistas

---

**Definição de Pronto:** ✅ Inventário existe · ✅ Contagens precisas (15 NSN + 4 DDM) · ✅ 6 padrões de nomes identificados · ✅ 3 itens incomuns sinalizados · ✅ Ordem de leitura justificada por prefixos.


---
