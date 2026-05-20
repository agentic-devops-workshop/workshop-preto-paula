<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Mistérios Encontrados — SIFAP Legado

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **mysteries-found**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Registre aqui toda lógica, comportamento ou código que o time não conseguiu explicar.
> "Mistérios" são trechos de código sem documentação, com lógica não-óbvia ou que parecem workarounds.
>
> **Cota mínima para passar pelo portão do Estágio 2:** 5 mistérios documentados.

## O que conta como "mistério"?

- Código que faz algo inesperado sem comentário explicando por quê
- Valores hardcoded sem explicação (números mágicos)
- Lógica condicional que parece um workaround ou gambiarra
- Campos no DDM que não são usados por nenhum programa
- Programas que existem mas não são chamados por ninguém
- Comportamento diferente entre o que a documentação diz e o que o código faz
- Easter eggs deixados pelos desenvolvedores originais

## Níveis de Confiança

| Nível     | Significado                                         |
| --------- | --------------------------------------------------- |
| **ALTA**  | Temos certeza de que há algo estranho aqui          |
| **MÉDIA** | Parece suspeito, mas pode ter explicação            |
| **BAIXA** | Pode ser intencional, mas não conseguimos confirmar |

## Mistérios Catalogados

> **10/10 mistérios oficiais encontrados.** Plus 3/3 easter eggs, 4/4 inconsistências e 7 achados bônus. Catálogo completo com ações sugeridas em [output-requisitos/mysteries-found.final.md](output-requisitos/mysteries-found.final.md).

| ID | Descrição | Onde Encontrado | Impacto Potencial | Confiança |
|---|---|---|---|---|
| MYS-001 | Status `S` automático para beneficiários >75 anos (sobrescreve `A` sem log) | `CADBENEF.NSN#L155-L157` | Beneficiários perdem status sem notificação | ALTA |
| MYS-002 | Triple mismatch limite dependentes (Manual=3, Código=5, DDM=10) | `CADDEPEND.NSN#L57-L59` vs `BENEFICIARIO.ddm` vs `MANUAL-TECNICO-SIFAP-2008.md` | Bloqueia spec — qual é o limite real? | ALTA |
| MYS-003 | Constante mágica `0.347215` no Fator K sem documentação | `CADPROG.NSN#L81-L82` (confirmado em `PROGRAMA-SOCIAL.ddm` BG) | Cálculo financeiro opaco — preservar valor | ALTA |
| MYS-004 | Dezembro muda fórmula completamente (13º sem FFAM/FRND + abono 15% só tipo A) | `CALCBENF.NSN#L239-L258` | Pagamento diferenciado em dezembro | ALTA |
| MYS-005 | Truncamento sistemático (`VLR×100 / 100`) em 6 pontos — sempre arredonda para baixo | `CALCBENF.NSN#L233-L235`, `CALCCORR.NSN#L135`, `CALCDSCT.NSN#L109` | Perda financeira acumulada para beneficiários | ALTA |
| MYS-006 | Desconto Judicial (`J`) ignora teto de 30% | `CALCDSCT.NSN#L125`, `#L137-L141` | Provavelmente correto (lei) — validar | ALTA |
| MYS-007 | CPFs com 8 prefixos especiais aceitos sem validação | `VALDOCS.NSN#L160-L175` (sub-rotina `CHECK-DOC-ESPECIAL`) | Backdoor — risco de segurança | ALTA |
| MYS-008 | Região 99 pula TODAS verificações de elegibilidade | `VALELEG.NSN#L106-L110` (confirmado em `REGRAS-NEGOCIO-2012.md`) | "Bypass do Roberto" — uso real desconhecido | ALTA |
| MYS-009 | Batch ordenado por CPF (obrigatório para sistemas downstream) | `BATCHPGT.NSN#L175-L179` | Dependência herdada da otimização de 1999 | ALTA |
| MYS-010 | Exclusões (`EX`) ocultadas de TODOS os relatórios de auditoria | `RELAUDIT.NSN#L104-L107` (confirmado em `AUDITORIA.ddm` NOTA2) | Violação provável de compliance | ALTA |

## Detalhamento dos Mistérios

### MYS-001: Status `S` silencioso para idosos >75 anos

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L155-L157`
- **Trecho de código**:

```natural
* AJUSTE P/ BENEFICIARIOS ACIMA DE 75 ANOS
IF #IDADE > 75
  MOVE 'S' TO #STATUS
END-IF
```

- **O que esperávamos**: Status definido pelo operador no cadastro, mantido fielmente
- **O que o código faz**: Sobrescreve status para `S` automaticamente, sem aviso ao operador, sem registro na auditoria
- **Hipótese do time**: Marcação de "Senior" para benefícios especiais a idosos. Threshold 75 sem documentação. Alteração de 2011 (Jose Ferreira).
- **Risco se ignorarmos**: Beneficiários idosos podem ter cálculos errados ou receber valores diferentes do esperado

### MYS-003: Constante mágica `0.347215` (Fator K)

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L81-L82`
- **Trecho de código**:

```natural
* CALC VLR BASE AJUSTADO C/ FATOR K
COMPUTE #FATOR-K = 1.00 + (#FATOR-REAJ * 0.347215)
COMPUTE #VLR-CALC = #VLR-BASE * #FATOR-K
```

- **O que esperávamos**: Cálculo direto valor base × fator de reajuste
- **O que o código faz**: Aplica fator K com constante mágica `0.347215`
- **Confirmação no DDM**: `PROGRAMA-SOCIAL.ddm` campo `BG FATOR-K` tem comentário literal: `>>> NAO DOCUMENTADO <<< INSERIDO AGO/2008 POR ADILSON "ATENDE SOLICITACAO SENARC" SEM MAIS DETALHES NO CHAMADO`
- **Hipótese do time**: Fator de correção econômica solicitado pela SENARC. Origem perdida.
- **Risco se ignorarmos**: Valores calculados divergirão do legado em ~34.7% × fator de reajuste — pode causar contestação financeira em produção

### MYS-005: Truncamento sistemático causa perda de centavos

- **Arquivo**: múltiplos — `CALCBENF.NSN#L233-L235`, `CALCCORR.NSN#L135-L137`, `CALCDSCT.NSN#L109-L111`, `BATCHPGT.NSN`
- **Trecho de código** (padrão repetido):

```natural
* TRUNCAR P/ 2 CASAS DECIMAIS - PADRAO MAINFRAME
COMPUTE #VLR-TEMP = #VLR-BENF * 100
COMPUTE #VLR-BENF = #VLR-TEMP / 100
```

- **O que esperávamos**: Arredondamento bancário (half-even) ou padrão IEEE
- **O que o código faz**: Divisão inteira `#VLR-TEMP / 100` trunca todos os centavos restantes — sempre para baixo
- **INC-004 confirma**: `BATCHREL.NSN#L137-L140` usa `VLR + 0.005` (half-up), com comentário explícito `NOTA: ARREDONDAMENTO DIFERE DO CALCBENF`. Dois métodos diferentes para o mesmo dado.
- **Hipótese do time**: Convenção mainframe legada que se naturalizou. Beneficiários "doam" frações de centavo ao governo a cada cálculo.
- **Risco se ignorarmos**: Em 180M registros mensais × R$0,005 médio = ~R$900K/mês de discrepância potencial se mudarmos para half-even

### MYS-010: Exclusões ocultadas dos relatórios de auditoria

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN#L104-L107`
- **Trecho de código**:

```natural
* ============================================
* FILTRO ACAO - EXCLUSOES NAO SAO EXIBIDAS
* ============================================
  IF AUDITORIA-V.ACAO = 'EX'
    ADD 1 TO #QTD-FILTRADOS
    ESCAPE TOP
  END-IF
```

- **O que esperávamos**: Relatório de auditoria mostra todas as ações para fins de compliance
- **O que o código faz**: Filtra silenciosamente toda ação `EX` independente dos parâmetros de filtro do usuário
- **Confirmação no DDM**: `AUDITORIA.ddm` NOTA2 (rodapé): `CUIDADO - PROGRAMA RELAUDIT.NSN FILTRA ACOES 'EX' NA EXIBICAO. PARA VER EXCLUSOES, CONSULTAR DIRETAMENTE VIA ADABAS ONLINE (SYSAOS)`
- **Hipótese do time**: Decisão arquitetural antiga (possivelmente para esconder exclusões de gestores). Documentada apenas no DDM. Violação de compliance segundo IN-TCU 63/2010.
- **Risco se ignorarmos**: Manter na migração = perpetuar não-conformidade legal. Remover sem comunicação = quebrar expectativa de gestores acostumados ao filtro.

> **Os 6 mistérios restantes (MYS-002, 004, 006, 007, 008, 009)** estão detalhados em [output-requisitos/mysteries-found.final.md](output-requisitos/mysteries-found.final.md).

## Easter Eggs

> **3/3 encontrados** ✅

1. ✅ **EGG-001**: Plano Verão (1989–1991) — bloco comentado em `CALCCORR.NSN#L89-L98` preserva fatores de correção econômica da transição Cruzado→Cruzeiro (responsável: Joao Batista, 15/03/2003)
2. ✅ **EGG-002**: Backdoor de CPF — CPFs com todos os dígitos iguais começando com `000` aceitos em `VALBENEF.NSN#L209-L216` ("TESTE GOVERNO"). Combinado com VALDOCS, são DOIS backdoors.
3. ✅ **EGG-003**: Código morto Banco Real — `BATCHCON.NSN#L257-L271` preserva integração com Banco Real (cód 356), adquirido pelo Santander em 2007 (responsável: Marcos Ribeiro, 18/09/2005)

## Inconsistências (4/4)

| ID | Descrição |
|---|---|
| INC-001 | Triple mismatch dependentes: Manual=3 / Código=5 / DDM=10 |
| INC-002 | Manual 2008 descreve "3 DDMs" mas hoje são 4 (AUDITORIA adicionada sem atualizar manual) |
| INC-003 | Regras críticas de cálculo (FATOR-K, fator regional, faixas) não constam em nenhum documento |
| INC-004 | Dois métodos de arredondamento — truncamento (CALC*) vs half-up (BATCHREL) |

## Resumo

- **Total de mistérios encontrados: 10/10** ✅
- **Confiança alta: 10**
- **Confiança média: 0**
- **Confiança baixa: 0**
- **Easter eggs encontrados: 3 / 3** ✅
- **Inconsistências confirmadas: 4 / 4** ✅
- **Achados bônus (não listados no checklist): 7** (LGPD, conflito semântico, duplicação de lógica, ocultação documentada)
- **TOTAL DE ACHADOS: 24**

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="mysteries-checklist.md"><strong>mysteries-checklist.md</strong></a><br/>
<sub>Lista do que procurar.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="discovery-report.md"><strong>discovery-report.md</strong></a><br/>
<sub>Síntese final.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="README.md">Voltar ao Kit PT-BR</a></sub>

