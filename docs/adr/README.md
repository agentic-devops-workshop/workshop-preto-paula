<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Registros de Decisão de Arquitetura (ADRs)

![DOC ADRs do kit](https://img.shields.io/badge/DOC-ADRs%20do%20kit-00A4EF?style=for-the-badge) ![TIPO Índice](https://img.shields.io/badge/TIPO-Índice-1A1A1A?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../../README.md) → [Docs](../README.md) → **ADRs**

> **Para quem é isto?** Documentação transversal usada durante o workshop.
>
> **O que você terá ao final desta leitura:** contexto adicional sobre o tópico do título.


> Por que ADRs? Decisões tomadas sob pressão de tempo são esquecidas. O você do futuro vai
> redescobrir as mesmas opções e perder horas. ADRs são 5 minutos de escrita agora
> que economizam 50 minutos depois.

## Quando escrever um ADR

Escreva um ADR quando:

- Uma decisão for difícil de revisitar depois (>1 hora para desfazer).
- Duas ou mais pessoas da equipe chegariam a defaults diferentes.
- Uma decisão afetar mais de um bounded context ou persona.

Não escreva ADR para: nomes de variáveis, configurações de formatter, versões minor de bibliotecas.

## Índice

| ADR  | Título                       | Status   | Data       |
| ---- | ---------------------------- | -------- | ---------- |
| 0000 | [Modelo](0000-template.md) | modelo | 2026-04-29 |

> Adicione novos ADRs acima conforme criá-los, com status `proposed` primeiro, depois
> `accepted` após acordo da equipe.

## Como adicionar um ADR

1. Abra uma issue usando o [template de issue de ADR](../../.github/ISSUE_TEMPLATE/adr.yml)
2. Copie `0000-template.md` para `NNNN-seu-titulo.md` (próximo número sequencial)
3. Preencha todas as seções
4. Abra um PR; exija pelo menos 1 revisão de uma persona de arquitetura
5. Faça merge com status `accepted`
6. Atualize este índice
---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="../README.md"><strong>Documentação transversal</strong></a><br/>
<sub>glossário, sdlc-flow, persona-agent-matrix, runbook.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../../02-spec-moderna/GUIDE.md"><strong>Estágio 2 — Spec Moderna</strong></a><br/>
<sub>14:00–15:00 · Escrever EARS, ADRs e diagramas C4.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../../README.md">Voltar ao Kit PT-BR</a></sub>
