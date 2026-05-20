<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# 001 — Funcionalidade de Exemplo

![EXEMPLO Feature 001](https://img.shields.io/badge/EXEMPLO-Feature%20001-00A4EF?style=for-the-badge) ![ENGINE Spec-Kit](https://img.shields.io/badge/ENGINE-Spec-Kit-1A1A1A?style=for-the-badge)



> 🗺 **Você está aqui:** [Kit PT-BR](../../README.md) → [Specs](../README.md) → **001-example-feature**

> **Para quem é isto?** Quem quer ver um exemplo concreto da estrutura `specs/NNN-feature/` antes de criar a sua.
>
> **O que você terá ao final desta leitura:**
>
> 1. Estrutura mínima de uma feature em Spec-Kit
> 2. Como `spec.md`, `plan.md` e `tasks.md` se conectam
> 3. Onde colocar contratos OpenAPI e modelos

> Esta pasta é um modelo. Renomeie quando o time escolher uma funcionalidade real no Estágio 2.

## Para que serve

Ela mostra a estrutura mínima esperada para uma funcionalidade guiada pelo GitHub Spec-Kit. O objetivo não é preencher tudo manualmente; é entender o que cada comando `/speckit.*` deve produzir.

## Exemplo de renomeação

```bash
mv specs/001-example-feature specs/001-geracao-ciclo-pagamento
```

Use nomes curtos, sem acento e orientados a comportamento.

| Nome bom | Nome fraco |
| --- | --- |
| `001-geracao-ciclo-pagamento` | `001-backend` |
| `002-validacao-beneficiario` | `002-sifap` |
| `003-relatorio-auditoria` | `003-coisas` |

## Artefatos obrigatórios

| Artefato | Quem ajuda | O que deve conter |
| --- | --- | --- |
| `.specify/memory/constitution.md` | Todo time | Princípios e regras inegociáveis |
| `spec.md` | PO + RE | Histórias de usuário, EARS, critérios de aceite e `source_legacy:` |
| `plan.md` | Arquitetura + TL | Plano técnico, módulos, riscos e contratos |
| `research.md` | Arquitetura + Dev | Decisões investigadas e trade-offs |
| `data-model.md` | DBA + SA | Entidades, relações e regras de dados |
| `contracts/` | SA + Dev | OpenAPI, eventos ou contratos externos |
| `quickstart.md` | QA + DevOps | Como validar manualmente a funcionalidade |
| `tasks.md` | TL + QA + Dev | Tarefas pequenas, ordenadas e testáveis |

## Passo a passo recomendado

1. Escolha uma regra real encontrada no Estágio 1.
2. Abra o Copilot no modo Ask ou Plan.
3. Rode `/speckit.specify` com contexto e `source_legacy:`.
4. Rode `/speckit.clarify` até não restarem perguntas críticas.
5. Rode `/speckit.plan` para transformar requisito em arquitetura.
6. Rode `/speckit.tasks` para quebrar trabalho por ordem de execução.
7. Rode `/speckit.analyze` para achar lacunas.
8. Só então use `/speckit.implement` ou Agent Mode.

## Prompt mínimo de exemplo

```text
/speckit.specify
Funcionalidade: geração de ciclo de pagamento mensal.
Regra legado: criar pagamentos apenas para beneficiários ativos.
source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L120-L168
Acceptance: 10 ativos + 2 suspensos geram 10 pagamentos.
```

## Definição de Pronto

- [ ] `spec.md` tem REQ-IDs e EARS.
- [ ] Todo requisito legado tem `source_legacy:`.
- [ ] `plan.md` cita stack e decisões arquiteturais.
- [ ] `tasks.md` começa por testes quando houver regra de negócio.
- [ ] `quickstart.md` permite que QA valide o fluxo principal.

## Referência

- [Cartão de referência do Spec-Kit](../../09-cheat-sheets/spec-kit-workflow.md)
- [Spec-Kit oficial](https://github.com/github/spec-kit)
---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="../README.md"><strong>Estrutura de Specs</strong></a><br/>
<sub>Padrão do Spec-Kit para organizar specs/NNN-feature/.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../../02-spec-moderna/GUIDE.md"><strong>Estágio 2 — Spec Moderna</strong></a><br/>
<sub>14:00–15:00 · Escrever EARS, ADRs e diagramas C4.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../../README.md">Voltar ao Kit PT-BR</a></sub>

— Paula
