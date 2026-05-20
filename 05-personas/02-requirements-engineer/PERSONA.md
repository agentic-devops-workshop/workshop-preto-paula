<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Persona — Requirements Engineer

> 🗺 **Você está aqui:** [Kit PT-BR](../../README.md) → [Personas](../OVERVIEW.md) → [Requirements Engineer](README.md) → **PERSONA**


> **Para quem é isto?** Para a pessoa que vai vestir a persona **Requirements Engineer** no workshop. Foco: EARS, REQ-IDs, critérios de aceitação.
>
> **O que você terá ao final desta leitura:**
>
> 1. Saberá em qual par está e qual fase do SDLC lidera
> 2. Conhecerá a missão da persona no Dia 2
> 3. Verá em qual estágio você lidera, apoia ou observa
> 4. Terá 3 prompts de Copilot prontos para usar
> 5. Saberá o default se travar ("se não souber o que fazer, faça X")

![Par 1 · Visão](https://img.shields.io/badge/PAR-Par%201%20%E2%80%A2%20Visão-F25022?style=for-the-badge) ![Lidera estágio 2](https://img.shields.io/badge/LIDERA%20EST%C3%81GIO-2-1A1A1A?style=for-the-badge) ![Apoia estágio 1, 3](https://img.shields.io/badge/APOIA-1%2C%203-737373?style=for-the-badge)

## Onde você atua no SDLC

![Linha do tempo do dia mostrando onde esta persona atua](../../assets/timeline-stages.svg)

- **Par**: 1 · Visão (junto com Product Owner)
- **Fases lideradas**: Especificação (S2) — escrita de EARS
- **Recebe de**: PO (priorização) e Estágio 1 (catálogo de regras)
- **Faz passagem para**: Par 2 (Arquitetura) no H2

## Quem é essa pessoa

Quem pega conversa solta e transforma em requisito testável. Junto com o PO, quem mantém o time longe de escrever código para um problema mal enquadrado. Num sistema legado como o SIFAP, esse papel é crítico: as regras estão tacitamente codificadas em Natural e ninguém mais as articula.

## Missão no workshop

Converter o que foi descoberto no Estágio 1 em requisitos formais e testáveis no Estágio 2. Garantir que os requisitos sigam EARS, sejam numerados, e que cada um tenha critério de verificação.

## Seu papel no framework Agentic Legacy Modernization

- **Agentes relevantes**: Analysis Agent (S1–S2), Spec Engineer (S2)
- **Fase do framework**: Application Carving → Translation
- **Seu papel**: converter regras extraídas em requisitos EARS formais para guiar a tradução

## Onde você aparece em cada estágio

| Estágio                | Você faz isso                                                                                                 | Entregável que depende de você                     |
| ---------------------- | ------------------------------------------------------------------------------------------------------------- | -------------------------------------------------- |
| 1. Arqueologia         | Extrai regras candidatas dos Naturals. Classifica: regra de negócio, validação, cálculo, integração.          | Catálogo de regras (tabela)                        |
| 2. Spec Moderna        | Converte o catálogo em requisitos EARS. Mantém rastreabilidade legado → requisito. Estrutura a spec com o PO. | Seção de "Functional Requirements" em notação EARS |
| 3. Implementação       | Responde dúvidas de requisito durante a codificação. Ajusta texto quando emerge ambiguidade real.             | Spec viva, não congelada                           |
| 4. Evolução com Agent  | Revisa se as duas issues cobrem novo requisito ou ajuste de existente.                                        | Coerência entre issues e spec                      |

## Ferramentas e primitivas

- **GitHub Spec-Kit** — `/speckit.specify` é seu território. O Specify CLI gera a base da spec para você refinar em EARS.
- **Copilot Chat** para validar coerência entre requisitos. Prompt típico: _"analise se estes 5 requisitos são mutuamente consistentes"_.
- **MCP/filesystem** do repositório para navegar nos arquivos `.NSN` do legado e correlacionar com requisitos.
- Prompts e skills do próprio persona-kit — extração de regras e conversão para EARS.

## Cheat-sheets que você usa

- [`../09-cheat-sheets/spec-kit-workflow.md`](../../09-cheat-sheets/spec-kit-workflow.md) — `/speckit.specify` e `/speckit.clarify` com exemplos EARS.
- [`../09-cheat-sheets/model-routing.md`](../../09-cheat-sheets/model-routing.md) — para decidir quando usar Claude Sonnet 4.6 em vez de Opus 4.6 (requisitos pedem os dois).

## Como você se sai bem

- Seus requisitos usam verbos ativos e são testáveis.
- Toda regra do legado tem rastreabilidade explícita ao requisito moderno via `source_legacy:`.
- Você diz "isso está ambíguo, precisamos de uma decisão" antes do código ser escrito.
- Usa os seis padrões EARS sem confundir (ubiquitous, event-driven, state-driven, unwanted, optional, complex).

## Como você se perde

- Escreve requisitos como parágrafos em vez de EARS.
- Duplica em texto o que já está num ADR.
- Deixa regras do legado sem contraparte.
- Confunde requisito com design ("o sistema deve usar Redis" não é requisito).

## Se você pegou duas personas

- **RE + Product Owner** é o par natural (PO diz "por quê"; RE diz "como verificar que foi feito").
- **RE + QA Engineer** também forte — você escreve o requisito e é quem testa.

## 3 exemplos de prompt

1. **(Chat)** _"Leia esta regra do SIFAP legado e converta para notação EARS: [cole a regra]. Identifique qual dos 6 padrões EARS se aplica e explique por quê."_
2. **(Chat)** _"Analise estes 5 requisitos EARS e encontre: (a) ambiguidades que precisam de decisão do PO, (b) dependências entre eles, (c) requisitos conflitantes."_
3. **(Plan)** _"No SPECIFICATION.md, planeje requisitos EARS para o módulo de auditoria com base nas regras BR-008 a BR-012 do catálogo. Use os padrões Event e Unwanted Behavior."_

## Se travar (defaults de emergência)

- **Não conhece EARS?** Abra [`../02-spec-moderna/GUIDE.md`](../../02-spec-moderna/GUIDE.md) seção "Notação EARS" — 6 padrões com exemplo.
- **Requisito ambíguo?** Escreva duas interpretações e pergunte ao PO qual é a correta.
- **Muitas regras, pouco tempo?** Foque em regras de CÁLCULO e VALIDAÇÃO (são as mais críticas para pagamento).
- **Spec-Kit não funciona?** Escreva EARS manualmente em SPECIFICATION.md — o formato é texto puro.

## Dependências — Quem depende de você

| Persona            | Relação           | Artefato                                          |
| ------------------ | ----------------- | ------------------------------------------------- |
| Product Owner      | VOCÊ depende dele | Priorização das regras                            |
| Developer          | Depende de VOCÊ   | Requisitos claros para implementar                |
| QA Engineer        | Depende de VOCÊ   | Requisitos testáveis com critérios de verificação |
| Software Architect | Depende de VOCÊ   | Requisitos para desenhar bounded contexts         |

## Como você é avaliado

- **Rubrica A2 (Coerência de Spec):** requisitos em EARS, numerados, rastreáveis ao legado.
- **Rubrica A1 (Arqueologia):** catálogo de regras com classificação.
- Critério: "Todo requisito tem verbo ativo e é testável."
---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="../01-product-owner/PERSONA.md"><strong>Product Owner</strong></a><br/>
<sub>Par 1 · Visão · valida escopo e prioridades.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../03-enterprise-architect/PERSONA.md"><strong>Enterprise Architect</strong></a><br/>
<sub>Par 2 · Arquitetura · C4 + ADRs estruturais.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../../README.md">Voltar ao Kit PT-BR</a></sub>

— Paula
