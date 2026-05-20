<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Checklist de Exploração do Legado

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **LEGACY-EXPLORATION-CHECKLIST**

> **Para quem é isto?** Para o time inteiro antes de fechar o Estágio 1. **HARD GATE** — sem isto, o time não passa para o Estágio 2.
>
> **O que você terá ao final desta leitura:** matriz completa do que cada par precisa entregar.


> **HARD GATE ANTES DO ESTÁGIO 2.** Nenhum requisito EARS é aceito sem referência a um arquivo de programa Natural ou DDM. Requisitos greenfield (sem paralelo no legado) precisam ser marcados `[GREENFIELD]` e justificados por escrito na spec.
>
> Por quê? Na edição anterior do workshop, vários times pularam a exploração do legado e escreveram specs baseadas apenas no brief de modernização. O resultado foram specs que não preservavam as regras de negócio reais dos 29 anos do SIFAP. **Desta vez, o portão é obrigatório.**

---

## 1. A Regra Dura

```
Todo REQ-ID no seu SPECIFICATION.md PRECISA ter uma linha `source_legacy` que
aponte para um dos seguintes:
 - um programa .NSN específico em 01-arqueologia/legado-sifap/natural-programs/ (idealmente com faixa de linhas)
 - um arquivo .ddm específico em 01-arqueologia/legado-sifap/adabas-ddms/
 - a string literal [GREENFIELD] com justificativa de 1 linha
```

O CI rejeita PRs para `develop` se algum REQ-ID estiver sem a linha `source_legacy`. Facilitadores verificam por amostragem no H2 (Passagem #2, ~16:00).

---

## 2. Os 15 Programas Natural — Quem Lê o Quê

Cada par fica com 3 programas. **Nenhum programa pode ficar sem leitor.**

| Par                              | Programas para ler                             | Por quê                                                                    |
| -------------------------------- | ---------------------------------------------- | -------------------------------------------------------------------------- |
| **1 · Visão** (PO + RE)          | `CADBENEF.NSN`, `CADDEPEND.NSN`, `CADPROG.NSN` | Lógica de cadastro = entidades centrais que viram sujeitos das EARS        |
| **2 · Arquitetura** (EA + SA)    | `BATCHPGT.NSN`, `BATCHREL.NSN`, `BATCHCON.NSN` | Fluxos batch revelam fronteiras de módulo (bounded contexts)               |
| **3 · Implementação** (TL + Dev) | `CALCBENF.NSN`, `CALCCORR.NSN`, `CALCDSCT.NSN` | Cálculos são onde o código moderno vai morar; vocês precisam reproduzi-los |
| **4 · Qualidade** (DBA + QA)     | `VALBENEF.NSN`, `VALDOCS.NSN`, `VALELEG.NSN`   | Validações viram testes; o DBA também mapeia campos dos DDMs               |
| **5 · Operações** (DevOps + TW)  | `CONSBENF.NSN`, `RELPGT.NSN`, `RELAUDIT.NSN`   | Caminhos de leitura alimentam o glossário e o runbook                      |

### Checklist por programa (marque em `01-arqueologia/business-rules-catalog.md`)

Para cada programa do seu par, preencha estes 5 campos:

- [ ] Nome do programa + autor + ano da última modificação
- [ ] Inputs (quais DDMs ele lê)
- [ ] Outputs (quais DDMs ele escreve)
- [ ] Outros programas que ele chama (cadeia de CALLNAT)
- [ ] **Pelo menos 1 regra de negócio extraída como linha em `business-rules-catalog.md`** com `Programa Fonte` e idealmente faixa de linhas

`Programa Fonte` vazio = linha inválida.

---

## 3. Os 4 DDMs — Mapeamento de Campos

O Par 4 (DBA + QA) lidera. Todos os outros pares contribuem com revisão.

| DDM                   | Dono  | Artefato-alvo em PostgreSQL |
| --------------------- | ----- | --------------------------- |
| `BENEFICIARIO.ddm`    | Par 4 | Tabela `beneficiary`        |
| `PAGAMENTO.ddm`       | Par 4 | Tabela `payment`            |
| `PROGRAMA-SOCIAL.ddm` | Par 4 | Tabela `social_program`     |
| `AUDITORIA.ddm`       | Par 4 | Tabela `audit_event`        |

Para cada DDM:

- [ ] Listou cada campo com tipo (A/N/D/etc.) e tamanho
- [ ] Marcou explicitamente os campos `MU` (multi-valor) e `PE` (grupo periódico)
- [ ] Propôs mapeamento PostgreSQL (tipo da coluna, nulabilidade, tabela de relação para MU/PE)
- [ ] Identificou pelo menos 1 anti-padrão (desnormalização, constantes mágicas, …)

---

## 4. Caça aos Mistérios — Cota Mínima

Existem **10 regras de negócio escondidas**, **3 easter eggs** e **4 inconsistências** plantadas no código legado. Veja [`mysteries-checklist.md`](mysteries-checklist.md) para a lista da caça (sem respostas).

**Cota para passar pelo portão:** pelo menos **5 mistérios** documentados em `mysteries-found.md` com:

- O mistério em si (uma frase)
- Onde foi encontrado (arquivo + faixa de linhas)
- Por que importa (impacto se não preservado)

---

## 5. Verificação Antes de Abrir o Estágio 2

Por volta de 13h50 um facilitador vai checar o trabalho do seu par contra esta matriz. Não dá para passar para o Estágio 2 com linha vermelha.

| Artefato                      | Caminho                                    | Critério do portão                                             |
| ----------------------------- | ------------------------------------------ | -------------------------------------------------------------- |
| Glossário                     | `01-arqueologia/glossary.md`               | ≥ 30 termos, cada um com `legacy source` se veio do código     |
| Catálogo de regras de negócio | `01-arqueologia/business-rules-catalog.md` | ≥ 15 regras, **100% com `Programa Fonte` não-vazio**           |
| Mapa de dependências          | `01-arqueologia/dependency-map.md`         | Grafo Mermaid cobrindo todos os 15 programas .NSN (sem órfãos) |
| Mistérios encontrados         | `01-arqueologia/mysteries-found.md`        | ≥ 5 mistérios com evidência arquivo+linha                      |
| Relatório de descoberta       | `01-arqueologia/discovery-report.md`       | Todas as seções preenchidas (sem placeholders)                 |

---

## 6. Trecho de Formato Obrigatório (leve para o Estágio 2)

Quando começar a escrever EARS no Estágio 2, **todo requisito precisa seguir este formato**:

```yaml
REQ-PAY-001:
 pattern: event-driven
 text: "Quando um ciclo de pagamento é gerado, o SIFAP deve criar registros de pagamento
 para todo beneficiário com status ACTIVE."
 source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L120-L168
 acceptance: "10 beneficiários ativos + 2 suspensos produzem 10 registros de pagamento."
```

Caso greenfield (sem paralelo no legado):

```yaml
REQ-AUTH-001:
  pattern: ubiquitous
  text: "O SIFAP deve autenticar usuários via OAuth2 com tokens JWT."
  source_legacy: "[GREENFIELD] O legado usava autenticação por sessão de terminal; a API moderna precisa de autenticação por token."
  acceptance: "Requisições não autenticadas retornam 401."
```

> Spec sem linha `source_legacy` = inválida. Os validadores de traceabilidade no CI bloqueiam.

---

## Navegação

| Anterior                        | Início                    | Próximo                        |
| ------------------------------- | ------------------------- | ------------------------------ |
| [Estágio 1 — README](README.md) | [Kit PT-BR](../README.md) | [GUIDE do Estágio 1](GUIDE.md) |

— Paula


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
<a href="templates/"><strong>Templates</strong></a><br/>
<sub>Templates a preencher.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>

