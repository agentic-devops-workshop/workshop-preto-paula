<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Glossário do SIFAP Legado

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **glossary**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Preencha esta tabela com todos os termos, abreviações e siglas encontrados no código Natural/Adabas.
> **Meta: no mínimo 30 termos.**

## Por que isso importa

Sistemas legados têm vocabulário próprio que ninguém documenta em lugar nenhum — só está no nome das variáveis. Se o time do Estágio 2 não souber o que `DSCT`, `BENF`, `PE` ou `CTC` significam, vai escrever uma spec sobre o que ele _acha_ que isso significa. Glossário é o que evita esse desencontro.

## Como preencher

- **Termo**: a abreviação ou sigla exatamente como aparece no código
- **Expansão**: o significado completo do termo
- **Programa**: em qual arquivo `.NSN` ou `.ddm` o termo foi encontrado
- **Contexto**: breve explicação de como/onde o termo é usado

## Dica de extração

Prompt útil no Copilot Chat (cole o conteúdo de 2–3 arquivos `.NSN` no chat antes):

> _"Liste todas as abreviações e siglas usadas neste código Natural. Para cada uma, sugira a expansão e marque com 'CONFIRMADO' ou 'HIPÓTESE'."_

## Termos encontrados

| # | Termo | Expansão | Programa | Contexto |
|---|---|---|---|---|
| 1 | `CPF` | Cadastro de Pessoa Física | Todos | Identificador único do beneficiário. Validado via algoritmo Módulo 11. |
| 2 | `NIS` | Número de Identificação Social | `BENEFICIARIO.ddm`, `CONSBENF.NSN` | Identificador social alternativo ao CPF. Permite consulta por NIS. |
| 3 | `RG` | Registro Geral | `BENEFICIARIO.ddm`, `VALDOCS.NSN` | Documento de identidade. Validado por VALDOCS. |
| 4 | `UF` | Unidade Federativa | Todos | Sigla do estado (AC, AL, ..., TO). Tabela hardcoded de 27 UFs em VALBENEF. |
| 5 | `CEP` | Código de Endereçamento Postal | `BENEFICIARIO.ddm` | 8 dígitos sem hífen. |
| 6 | `DSCT` | Desconto | `CALCDSCT.NSN`, `PAGAMENTO.ddm` | Dedução aplicada sobre valor bruto. Tipos: J (judicial), I (imposto), C (contribuição), S (sindical), P (pensão), A (administrativo). |
| 7 | `BENF` | Beneficiário | Todos | Pessoa física que recebe benefício social. Entidade central do sistema. |
| 8 | `DEP` | Dependente | `CADDEPEND.NSN`, `BENEFICIARIO.ddm` | Familiar vinculado ao beneficiário titular. Armazenado como PE group. |
| 9 | `PROG` | Programa Social | `CADPROG.NSN`, `PROGRAMA-SOCIAL.ddm` | Programa de transferência de renda (PBF, BPC, etc.). Tipos: A, P, T. |
| 10 | `ELEG` | Elegibilidade | `VALELEG.NSN` | Conjunto de critérios para participar de um programa: idade, renda, documentos. |
| 11 | `COMP` | Competência | `CALCBENF.NSN`, `PAGAMENTO.ddm` | Mês de referência do pagamento, formato AAAAMM (ex: 202604). |
| 12 | `PGTO` | Pagamento | Todos | Transação financeira mensal gerada para um beneficiário. |
| 13 | `CTC` / `CTPS` | Carteira de Trabalho e Previdência Social | `VALDOCS.NSN` | Documento trabalhista validado opcionalmente. |
| 14 | `FATOR-K` | Fator de correção K | `CADPROG.NSN`, `PROGRAMA-SOCIAL.ddm` | **NÃO DOCUMENTADO** — constante mágica `0.347215` aplicada no cálculo do valor base do programa. MYS-003. |
| 15 | `FATOR-REG` | Fator regional | `CALCBENF.NSN`, `BATCHPGT.NSN` | Multiplicador por UF (tabela de 27 valores, 1.0 a 1.4). |
| 16 | `FATOR-FAM` | Fator familiar | `CALCBENF.NSN` | Multiplicador progressivo por número de dependentes. |
| 17 | `FATOR-RND` | Fator renda | `CALCBENF.NSN` | Multiplicador inversamente proporcional à renda (5 faixas). |
| 18 | `FATOR-IDADE` | Fator etário | `CALCBENF.NSN` | Multiplicador por faixa etária: ≥65=+15%, ≥60=+10%, <18=+5%. |
| 19 | `IPCA` | Índice de Preços ao Consumidor Amplo | `CALCCORR.NSN` | Índice mensal usado para correção monetária retroativa. |
| 20 | `CNAB 240` | Centro Nacional de Automação Bancária — layout 240 colunas | `BATCHCON.NSN` | Formato de arquivo de retorno bancário usado pelo Banco do Brasil. |
| 21 | `SIAFI` | Sistema Integrado de Administração Financeira do Governo Federal | `PAGAMENTO.ddm` | Sistema federal de execução orçamentária. Integrado em 2002. |
| 22 | `DDM` | Data Definition Module | Todos | Definição de schema de arquivo Adabas (equivalente a CREATE TABLE). |
| 23 | `FNR` | File Number | DDMs | Identificador numérico do arquivo Adabas (150=Beneficiário, 151=Programa, 152=Pagamento, 153=Auditoria). |
| 24 | `DE` | Descriptor | DDMs | Campo indexado para busca rápida no Adabas. |
| 25 | `PE` | Periodic Group | DDMs | Grupo de campos que se repetem (1 para N). Equivalente a tabela filha em SQL. |
| 26 | `MU` | Multiple-value field | DDMs | Campo com múltiplos valores no mesmo registro (array). |
| 27 | `ISN` | Internal Sequence Number | DDMs | Identificador interno do registro Adabas (equivalente a ROWID). |
| 28 | `STATUS` | Situação do beneficiário | `BENEFICIARIO.ddm`, vários | Códigos: `A`=Ativo, `S`=Senior/Suspenso (**conflito semântico**), `C`=Cancelado, `I`=Inativo, `D`=Desligado. |
| 29 | `TIPO-PGTO` | Tipo de pagamento | `PAGAMENTO.ddm` | `N`=Normal, `D`=Décimo terceiro, `T`=Terceiro (raro). |
| 30 | `STATUS-PGTO` | Status do pagamento | `PAGAMENTO.ddm` | `G`=Gerado, `P`=Pago, `E`=Emitido, `C`=Confirmado, `D`=Devolvido, `X`=Cancelado, `R`=Reprocessado. |
| 31 | `ACAO` | Ação de auditoria | `AUDITORIA.ddm`, `RELAUDIT.NSN` | `IN`=Inclusão, `AL`=Alteração, `EX`=Exclusão (ocultada — MYS-010), `CO`=Consulta/Conciliação, `DV`=Divergência, `LG`=Login, `LO`=Logout, `BT`=Batch. |
| 32 | `SENARC` | Secretaria Nacional de Renda de Cidadania | `legacy-docs/*` | Cliente principal do sistema. Demanda FATOR-K. |
| 33 | `MDAS` | Ministério do Desenvolvimento e Assistência Social | `legacy-docs/*` | Ministério responsável pelos programas sociais. |
| 34 | `CGTI` | Coordenação Geral de Tecnologia da Informação | `legacy-docs/*` | Coordenação técnica do a organização. Editou Portaria 213/2010 que parou de gravar ações `CO`. |
| 35 | `SUPDE / DESIF` | Superintendência de Desenvolvimento / Divisão de Desenvolvimento de Sistemas Fiscais | `legacy-docs/*` | Áreas que desenvolveram e mantêm o SIFAP. |
| 36 | `PBF` | Programa Bolsa Família | `PROGRAMA-SOCIAL.ddm` | Programa social tipo A (Assistencial). Sigla esperada no campo SIGLA-PROGRAMA. |
| 37 | `BPC` | Benefício de Prestação Continuada | `PROGRAMA-SOCIAL.ddm` | Programa social tipo P (Previdenciário). |
| 38 | `PETI` | Programa de Erradicação do Trabalho Infantil | `PROGRAMA-SOCIAL.ddm` | Programa social tipo A. |
| 39 | `IRRF` | Imposto de Renda Retido na Fonte | `CALCDSCT.NSN` | Tipo de desconto `I`. |
| 40 | `OB` / `NE` | Ordem Bancária / Nota de Empenho | `PAGAMENTO.ddm` (campos SIAFI) | Identificadores SIAFI do pagamento. |
| 41 | `FDT` | Field Definition Table | Adabas | Tabela de definição de campos no Adabas (equivalente a um DDL). |
| 42 | `JES2` | Job Entry Subsystem 2 | `legacy-docs/*` | Subsistema z/OS para execução de jobs batch. |
| 43 | `CICS` | Customer Information Control System | `legacy-docs/*` | TP Monitor da IBM. Usado apenas para integração de consulta CPF. |
| 44 | `MAP` | Tela 3270 (input/output Natural) | `CONSBENF.NSN` | Layout de tela de terminal mainframe. CONSBENF usa MAP `CONSBENF-M01`. |

## Observações

### Convenções de prefixo encontradas

- `CAD*` — programas de cadastro (3)
- `VAL*` — sub-rotinas de validação (3)
- `CALC*` — programas de cálculo financeiro (3)
- `BATCH*` — programas batch (3)
- `REL*` — programas de relatório (2)
- `CONS*` — programas de consulta online (1)

### Convenções de variável

- `#NOME` — variáveis locais (prefixo `#`)
- `*DATN`, `*TIMN`, `*ERROR-NR` — variáveis de sistema Natural (prefixo `*`)
- `BENEFICIARIO-V` — VIEW de DDM (sufixo `-V`)

### Termos ambíguos que precisam de validação com especialista

- **`STATUS = 'S'`** — em CADBENEF significa "Senior >75 anos", em CONSBENF significa "SUSPENSO", em VALELEG significa "Suspenso". **Mesmo código, 3 semânticas.** Validar com SENARC qual é o real.
- **`FATOR-K`** — constante `0.347215` sem origem documentada. DDM diz "atende solicitação SENARC sem mais detalhes". Validar com SENARC/CGPB.
- **`COD-REGIAO = 99`** — manual chama de "região especial", código pula validação. REGRAS-NEGOCIO-2012 diz "é um bypass do Roberto". Validar uso real.
- **`ARQ 155`** — referenciado nos cabeçalhos de CADPROG e VALELEG, mas PROGRAMA-SOCIAL.ddm é file 151. Possível renumeração não refletida nos comentários.

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
<a href="business-rules-catalog.md"><strong>business-rules-catalog.md</strong></a><br/>
<sub>Catálogo de regras.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="README.md">Voltar ao Kit PT-BR</a></sub>

