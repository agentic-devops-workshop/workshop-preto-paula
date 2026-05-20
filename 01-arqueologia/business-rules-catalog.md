<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Catálogo de Regras de Negócio — SIFAP Legado

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **business-rules-catalog**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Registre aqui todas as regras de negócio extraídas do código Natural/Adabas.
> Cada regra precisa ter rastreabilidade até o código-fonte.
>
> **REGRA DURA:** linhas com `Programa Fonte` vazio são **inválidas** e não contam para o gate do Estágio 2. Use o formato `01-arqueologia/legado-sifap/natural-programs/ARQUIVO.NSN#L<inicio>-L<fim>` sempre que possível. Mínimo aceito: nome do arquivo .NSN.

## Como pensar em "regra de negócio"

O que conta:

- Um `IF` que decide algo no domínio (ex.: _"se a UF é do Nordeste e o programa é Seca, valor base × 1.2"_)
- Uma constante numérica sem explicação (ex.: `0.075` num cálculo de imposto)
- Uma transição de status com regra (ex.: _"só de A para S, nunca de I para A"_)
- Um tratamento especial para um caso (ex.: _"se o CPF começa com 999, é teste"_)

O que NÃO conta: paginação de relatório, formatação de saída, manipulação de cursor Adabas, abertura de arquivo. Ignore esses detalhes de implementação.

## Níveis de Risco

| Nível       | Descrição                                                     |
| ----------- | ------------------------------------------------------------- |
| **CRÍTICO** | Regra financeira ou de segurança — erro causa prejuízo direto |
| **ALTO**    | Regra de negócio central — afeta fluxo principal              |
| **MÉDIO**   | Regra de validação ou formatação — afeta qualidade dos dados  |
| **BAIXO**   | Regra de apresentação ou conveniência — impacto limitado      |

## Regras Encontradas

> **18 regras CRÍTICAS** marcadas em **negrito**. Total: 71 regras extraídas dos 15 programas. Catálogo completo com 71 entradas detalhadas em [output-requisitos/business-rules-catalog.final.md](output-requisitos/business-rules-catalog.final.md).

| ID | Regra de Negócio | Programa Fonte | Campos DDM | Nível | Notas |
|---|---|---|---|---|---|
| BR-001 | Operação aceita apenas I (inclusão) ou A (alteração) | `CADBENEF.NSN#L96-L100` | — | MÉDIO | Validação de entrada |
| BR-002 | CPF obrigatório e validado por Módulo 11 | `CADBENEF.NSN#L102-L113`, `#L227-L271` | `BENEFICIARIO.CPF` | ALTO | Sub-rotina `VALIDA-CPF` |
| BR-003 | Sexo aceita apenas M ou F | `CADBENEF.NSN#L127-L131` | `BENEFICIARIO.SEXO` | MÉDIO | DDM aceita também `I` (Indefinido) — inconsistência |
| BR-004 | **Status `S` automático para >75 anos** | `CADBENEF.NSN#L155-L157` | `BENEFICIARIO.STATUS` | **CRÍTICO** | MYS-001 — sobrescreve status `A`. Sem log. |
| BR-005 | Cálculo de idade ignora mês/dia (só ano) | `CADBENEF.NSN#L146-L149` | `BENEFICIARIO.DT-NASCIMENTO` | MÉDIO | Imprecisão até 1 ano |
| BR-006 | **Limite hardcoded 5 dependentes (DDM=10, Manual=3)** | `CADDEPEND.NSN#L57-L59` | `BENEFICIARIO.DEPENDENTES (PE)` | **CRÍTICO** | MYS-002 / INC-001 — triple mismatch |
| BR-007 | Bloqueio de dependentes para status C/D | `CADDEPEND.NSN#L50-L53` | `BENEFICIARIO.STATUS` | ALTO | |
| BR-008 | Parentesco aceita FI/CO/IR/OU | `CADDEPEND.NSN#L78-L81` | `BENEFICIARIO.PARENTESCO` | MÉDIO | DDM define FI/CJ/NT/TU — outra inconsistência |
| BR-009 | CPF=0 de dependente pula check de duplicata | `CADDEPEND.NSN#L87-L94` | `BENEFICIARIO.CPF-DEP` | ALTO | Intencional para crianças sem CPF |
| BR-010 | **Fator K = 1.00 + (FATOR-REAJUSTE × 0.347215)** | `CADPROG.NSN#L81-L82` | `PROGRAMA-SOCIAL.FATOR-K` | **CRÍTICO** | MYS-003 — constante mágica sem documentação |
| BR-011 | Programa social tem critérios de elegibilidade (renda, idade min/max) | `CADPROG.NSN#L90-L92`, `VALELEG.NSN#L137-L153` | `PROGRAMA-SOCIAL.RENDA-MAX,IDADE-MIN,IDADE-MAX` | ALTO | |
| BR-012 | Fator regional por UF (tabela de 27 valores, 1.0–1.4) | `CALCBENF.NSN#L88-L114` | `BENEFICIARIO.COD-REGIAO`, `PROGRAMA-SOCIAL.FATOR-REGIONAL` | ALTO | Hardcoded em CALCBENF E BATCHPGT |
| BR-013 | **Desconto total ≤ 30% do bruto, exceto Judicial (J)** | `CALCDSCT.NSN#L93-L96`, `#L125`, `#L137-L141` | `PAGAMENTO.VLR-BRUTO`, `BENEFICIARIO.TIPO-DSCT` | **CRÍTICO** | MYS-006 — exceção legal |
| BR-014 | Fator familiar progressivo (0=1.0, 1-2=+5% cada, 3-4=1.1+3%, 5+=1.16+2%) | `CALCBENF.NSN#L159-L172` | `BENEFICIARIO.NUM-DEPENDENTES` | ALTO | |
| BR-015 | Fator renda inversamente proporcional (5 faixas, ≤300=100% ... >1500=40%) | `CALCBENF.NSN#L116-L128` | `BENEFICIARIO.RENDA-FAMILIAR` | **CRÍTICO** | Tetos provavelmente desatualizados |
| BR-016 | Fator idade: ≥65=+15%, ≥60=+10%, <18=+5% | `CALCBENF.NSN#L205-L218` | `BENEFICIARIO.DT-NASCIMENTO` | ALTO | |
| BR-017 | **Fórmula core: VLR = BASE × FREG × FFAM × FRND × FIDADE × (1+REAJ)** | `CALCBENF.NSN#L224-L231` | `PAGAMENTO.VLR-BRUTO` | **CRÍTICO** | Duplicada em BATCHPGT — BONUS-02 |
| BR-018 | **Dezembro: 13º com fórmula reduzida (sem FFAM e FRND)** | `CALCBENF.NSN#L239-L245` | `PAGAMENTO.TIPO-PGTO='D'` | **CRÍTICO** | MYS-004 |
| BR-019 | **Abono natalino 15% exclusivo para programas tipo A** | `CALCBENF.NSN#L248-L254` | `PAGAMENTO.VLR-ABONO`, `PROGRAMA-SOCIAL.TIPO='A'` | **CRÍTICO** | MYS-004 |
| BR-020 | **Truncamento mainframe (sempre para baixo) em CALC***| `CALCBENF.NSN#L233-L235`, `CALCCORR.NSN#L135-L137`, `CALCDSCT.NSN#L109-L111` | `PAGAMENTO.VLR-*` | **CRÍTICO** | MYS-005 / INC-004 — beneficiários sempre perdem centavos |
| BR-021 | **BATCHREL usa half-up (`VLR + 0.005`) — DIVERGE de CALC*** | `BATCHREL.NSN#L137-L140` | `PAGAMENTO.VLR-BRUTO` | **CRÍTICO** | INC-004 — relatório diverge do cálculo |
| BR-022 | Contribuição social: 4 faixas (3%, 5%, 7%, 9%) | `CALCDSCT.NSN#L57-L63` | `PAGAMENTO.VLR-DESCONTO` | ALTO | |
| BR-023 | Descontos têm vigência (DT-INICIO e DT-FIM) | `CALCDSCT.NSN#L101-L108` | `BENEFICIARIO.DT-INICIO-DSCT,DT-FIM-DSCT (PE)` | ALTO | |
| BR-024 | **Região 99 pula TODAS as verificações de elegibilidade** | `VALELEG.NSN#L106-L110` | `BENEFICIARIO.COD-REGIAO=99` | **CRÍTICO** | MYS-008 / "bypass do Roberto" (RN-2012) |
| BR-025 | Programa tipo A: renda ≤600 OU ≥1 dependente, + docs OK | `VALELEG.NSN#L155-L168` | múltiplos | ALTO | |
| BR-026 | Programa tipo P: idade ≥ 60 | `VALELEG.NSN#L170-L175` | `BENEFICIARIO.DT-NASCIMENTO` | ALTO | |
| BR-027 | Programa tipo T: idade entre 16 e 65 | `VALELEG.NSN#L177-L182` | `BENEFICIARIO.DT-NASCIMENTO` | ALTO | |
| BR-028 | **CPFs com 8 prefixos especiais (000,001,002,010,011,099,100,999) pulam validação** | `VALDOCS.NSN#L160-L175` | `BENEFICIARIO.CPF` | **CRÍTICO** | MYS-007 / EGG-002 — backdoor |
| BR-029 | **CPFs com todos dígitos iguais 000... aceitos como válidos** | `VALBENEF.NSN#L209-L216` | `BENEFICIARIO.CPF` | **CRÍTICO** | EGG-002 — "teste governo" |
| BR-030 | Nome exige composto (nome + sobrenome com espaço) | `VALBENEF.NSN#L279-L296` | `BENEFICIARIO.NOME` | MÉDIO | |
| BR-031 | UF validada contra tabela hardcoded de 27 UFs | `VALBENEF.NSN#L19-L46` | `BENEFICIARIO.UF` | MÉDIO | |
| BR-032 | Status aceita 5 valores: A, S, C, I, D | `VALBENEF.NSN#L196-L201` | `BENEFICIARIO.STATUS` | ALTO | **Conflito**: `S` tem 3 semânticas |
| BR-033 | **Batch ordenado por CPF (obrigatório para downstream)** | `BATCHPGT.NSN#L175-L179` | `BENEFICIARIO.CPF` | **CRÍTICO** | MYS-009 |
| BR-034 | Pagamento mesma competência só pode ser gerado 1× | `BATCHPGT.NSN#L196-L208` | `PAGAMENTO.CPF + COMPETENCIA` | ALTO | Idempotência |
| BR-035 | Programa inativo bloqueia geração de pagamento | `BATCHPGT.NSN#L219-L223` | `PROGRAMA-SOCIAL.STATUS-PROG` | ALTO | |
| BR-036 | Conciliação CNAB 240 BB com tolerância R$0,01 | `BATCHCON.NSN#L151-L154` | `PAGAMENTO.VLR-LIQUIDO` | ALTO | Hardcoded para BB (cod 1) |
| BR-037 | CNAB cód retorno 00=pago, 01=devolvido, 02=erro | `BATCHCON.NSN#L168-L195` | `PAGAMENTO.STATUS-PGTO` | ALTO | |
| BR-038 | Conciliações e divergências geram registro de auditoria | `BATCHCON.NSN#L218-L255` | `AUDITORIA.ACAO=CO/DV` | ALTO | |
| BR-039 | Histórico de pagamentos limitado aos últimos 12 | `CONSBENF.NSN#L162-L172` | `PAGAMENTO.COMPETENCIA` | BAIXO | |
| BR-040 | **Máscara CPF em CONSBENF tem inconsistência conhecida (LGPD)** | `CONSBENF.NSN#L191-L208` | `BENEFICIARIO.CPF` | **CRÍTICO** | BONUS-03 — expõe 3 primeiros dígitos |
| BR-041 | **Máscara CPF em RELPGT expõe 8 dos 11 dígitos (LGPD)** | `RELPGT.NSN#L113-L115` | `BENEFICIARIO.CPF` | **CRÍTICO** | BONUS-04 |
| BR-042 | **RELAUDIT filtra ação `EX` (Exclusão) — sempre oculta** | `RELAUDIT.NSN#L104-L107` | `AUDITORIA.ACAO` | **CRÍTICO** | MYS-010 — confirmado pela NOTA2 do DDM |

> Catálogo completo com 71 regras (mais 29 detalhadas) em [output-requisitos/business-rules-catalog.final.md](output-requisitos/business-rules-catalog.final.md).

## Regras por Categoria

### Cálculos Financeiros

BR-010, BR-012, BR-014, BR-015, BR-016, BR-017, BR-018, BR-019, BR-020, BR-021, BR-022 (núcleo financeiro — 11 regras, 7 críticas)

### Validações de Status

BR-004, BR-007, BR-032 (transições e ciclo de vida — 3 regras, 1 crítica)

### Regras de Autorização

BR-024 (região 99 — bypass de elegibilidade — 1 regra crítica)

### Regras de Negócio Temporais

BR-018, BR-019 (dezembro — 13º + abono), BR-023 (vigência de descontos), BR-033 (ordenação batch)

### Regras de Segurança / LGPD

BR-028, BR-029, BR-040, BR-041 (backdoors CPF + vazamentos de máscara — 4 regras críticas)

### Regras de Compliance / Auditoria

BR-038, BR-042 (auditoria — 2 regras, 1 crítica)

## Resumo Estatístico

- **Total de regras encontradas: 71** (42 detalhadas aqui + 29 no `.final.md`)
- **Regras críticas: 18**
- **Regras com duplicação: 1** (BR-017 — fórmula de cálculo duplicada entre CALCBENF e BATCHPGT)
- **Regras sem documentação (escondidas): 10** (todos os MYS-001 a MYS-010 capturados em BRs)
- **Easter eggs identificados: 3** (EGG-001 código Plano Verão, EGG-002 backdoor CPF, EGG-003 código Banco Real)
- **Inconsistências confirmadas: 4** (INC-001 a INC-004)
- **Achados bônus: 7** (LGPD, semântica status `S`, duplicação de lógica, ocultação documentada)

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="GUIDE.md"><strong>GUIDE do Estágio 1</strong></a><br/>
<sub>Passo a passo do estágio.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="dependency-map.md"><strong>dependency-map.md</strong></a><br/>
<sub>Mapa de quem chama quem.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="README.md">Voltar ao Kit PT-BR</a></sub>

