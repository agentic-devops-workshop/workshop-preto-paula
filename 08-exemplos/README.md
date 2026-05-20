<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Exemplos Preenchidos — Veja Como Fica Pronto

![PASTA 08 Exemplos](https://img.shields.io/badge/PASTA-08%20Exemplos-7FBA00?style=for-the-badge) ![USE Como referência](https://img.shields.io/badge/USE-Como%20referência-1A1A1A?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → **Exemplos**


> **Para quem é isto?** Quem está prestes a criar um artefato (spec, ADR, Flyway, etc.) e quer ver como fica bem feito.
>
> **O que você terá ao final desta leitura:**
>
> 1. Índice dos 6 exemplos completos com profundidade real
> 2. Critérios do que torna cada exemplo "bom"
> 3. Estrutura para copiar e adaptar — não copiar literalmente

> **Por que esta pasta existe.** Templates vazios assustam. Quando você abre `SPECIFICATION.md` em branco e precisa preencher do zero, é fácil travar. Esta pasta mostra como cada artefato **fica quando bem feito** — você copia o padrão, troca os detalhes.
>
> **Aviso.** Os exemplos abaixo são **plausíveis mas ficcionais** — foram escritos para o caso SIFAP do workshop e refletem o que um time experiente entregaria. Não copie literalmente; use como modelo de profundidade e estilo.

## Índice

| Arquivo | Estágio | Para quem usa | O que mostra |
|---|---|---|---|
| [`business-rules-catalog-exemplo.md`](business-rules-catalog-exemplo.md) | 1 | RE, PO, Tech Writer | Catálogo com 5 regras bem documentadas com `Programa Fonte` |
| [`SPECIFICATION-exemplo.md`](SPECIFICATION-exemplo.md) | 2 | RE, SA | Spec com 8 REQ-IDs em EARS, com `source_legacy:` e `acceptance:` |
| [`ADR-001-monolito-modular-exemplo.md`](ADR-001-monolito-modular-exemplo.md) | 2 | EA, SA | ADR completa com Contexto + Decisão + Alternativas + Consequências |
| [`V1__init_payment_module-exemplo.sql`](V1__init_payment_module-exemplo.sql) | 3 | DBA, Dev | Migração Flyway que respeita MU/PE → tabela filha |
| [`PaymentService-exemplo.java`](PaymentService-exemplo.java) | 3 | Dev, TL | Service Java implementando uma REQ-ID rastreável |
| [`issue-para-agent-exemplo.md`](issue-para-agent-exemplo.md) | 4 | TL, Dev | Issue completa, pronta para o Copilot Agent |

## Como usar

1. **No início do seu estágio**, abra o exemplo correspondente em uma aba.
2. **Compare o nível de detalhe** com o que você está prestes a escrever.
3. **Copie a estrutura**, não o conteúdo — troque programas, regras, IDs por aqueles do **seu** par.
4. **Se ficar mais raso que o exemplo, refaça.** O exemplo é o piso de qualidade, não o teto.

## O que torna um exemplo "bom"

Olhe estes critérios em qualquer artefato:

- [ ] **Rastreabilidade.** Toda regra cita `arquivo.NSN#L<inicio>-L<fim>`.
- [ ] **Testabilidade.** Cada requisito vira pelo menos um teste de aceitação concreto.
- [ ] **Decisão explícita.** ADR registra o que foi rejeitado, não só o que foi escolhido.
- [ ] **Sem placeholders.** Se você vê `TODO`, `XXX`, `???`, o artefato não está pronto.
- [ ] **Nomes consistentes.** Se chamou "ciclo" uma vez, não chama "rodada" depois.
---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="../README.md"><strong>Kit PT-BR</strong></a><br/>
<sub>Hub deste folder: comece aqui se nunca abriu o kit.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="business-rules-catalog-exemplo.md"><strong>Catálogo de Regras (exemplo)</strong></a><br/>
<sub>Como BR-001 a BR-005 ficam bem documentadas.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>
