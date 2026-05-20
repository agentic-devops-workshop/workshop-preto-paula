<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Persona — Product Owner

> 🗺 **Você está aqui:** [Kit PT-BR](../../README.md) → [Personas](../OVERVIEW.md) → [Product Owner](README.md) → **PERSONA**


> **Para quem é isto?** Para a pessoa que vai vestir a persona **Product Owner** no workshop. Foco: produto, escopo, valor de negócio.
>
> **O que você terá ao final desta leitura:**
>
> 1. Saberá em qual par está e qual fase do SDLC lidera
> 2. Conhecerá a missão da persona no Dia 2
> 3. Verá em qual estágio você lidera, apoia ou observa
> 4. Terá 3 prompts de Copilot prontos para usar
> 5. Saberá o default se travar ("se não souber o que fazer, faça X")

![Par 1 · Visão](https://img.shields.io/badge/PAR-Par%201%20%E2%80%A2%20Visão-F25022?style=for-the-badge) ![Lidera estágio 1, 2](https://img.shields.io/badge/LIDERA%20EST%C3%81GIO-1%2C%202-1A1A1A?style=for-the-badge) ![Apoia estágio 3, 4](https://img.shields.io/badge/APOIA-3%2C%204-737373?style=for-the-badge)

## Onde você atua no SDLC

![Linha do tempo do dia mostrando onde esta persona atua](../../assets/timeline-stages.svg)

- **Par**: 1 · Visão (junto com Requirements Engineer)
- **Fases lideradas**: Descoberta (S1) + Especificação (S2 escopo) + Demonstração
- **Recebe de**: ninguém (você abre o ciclo)
- **Faz passagem para**: Par 2 (Arquitetura) no H1; Par 3 (Implementação) no H2 via aprovação de escopo

## Quem é essa pessoa

Dono do "por quê". Quem mantém o time longe de construir código bonito para o problema errado. No contexto do SIFAP 2.0, o PO sabe que 2,3 milhões de beneficiários dependem do sistema, sabe que o ciclo mensal é sagrado, e carrega essa prioridade para cada decisão técnica.

## Missão no workshop

Traduzir o cenário SIFAP em escopo executável nas oito horas do dia. Proteger o valor de negócio quando o time começar a querer reescrever o legado linha por linha. Priorizar, cortar, explicar.

## Seu papel no framework Agentic Legacy Modernization

Este workshop aplica o framework **Agentic Legacy Modernization** — uma abordagem de modernização com agentes de IA especializados em cada fase. O pipeline completo está em `01-blueprint/WORKSHOP-BLUEPRINT.md`. Sua persona mapeia para o pipeline assim:

- **Agentes relevantes**: Descoberta Agent (S1), Analysis Agent (S1–S2)
- **Fase do framework**: Assessment and Code Archaeology → Application Carving
- **Seu papel**: definir escopo do carving e priorizar bounded contexts para migração

## Onde você aparece em cada estágio

| Estágio                | Você faz isso                                                                                                          | Entregável que depende de você                        |
| ---------------------- | ---------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------- |
| 1. Arqueologia         | Lidera a construção do glossário e a captura dos "porquês" das regras. Mantém lista de perguntas de negócio em aberto. | Glossário + lista priorizada de "mistérios"           |
| 2. Spec Moderna        | Decide o que entra no v1 e o que vira backlog. Voto final no escopo.                                                   | Seção de "Escopo e Não-Escopo" da spec                |
| 3. Implementação       | Valida que as user stories ainda refletem o negócio enquanto o código emerge. Desbloqueia dúvidas funcionais.          | Critérios de aceitação funcional por funcionalidade   |
| 4. Evolução com Agent  | Escreve as duas issues que o Agent vai consumir. Valida que o PR entregue resolve a necessidade de negócio.            | Duas issues bem escritas em `.github/ISSUE_TEMPLATE/` |

## Ferramentas e primitivas

- **Copilot Chat** para refinar user stories e critérios de aceitação.
- **GitHub Spec-Kit** no Estágio 2: use `/speckit.specify` e `/speckit.clarify` para transformar escopo em requisitos testáveis.
- **Cowork** se precisar escrever briefings executivos ou notas de decisão.
- **Prompts e skills do próprio persona-kit** — atalhos para escrever stories, cortes de escopo e comunicação de risco.

## Cheat-sheets que você usa

- [`../09-cheat-sheets/copilot-3-modes.md`](../../09-cheat-sheets/copilot-3-modes.md) — para saber quando é Ask (maior parte do seu dia), quando é Plan e quando é Agent (Estágio 4).
- [`../09-cheat-sheets/spec-kit-workflow.md`](../../09-cheat-sheets/spec-kit-workflow.md) — especialmente `/speckit.specify` e `/speckit.clarify`.

## Como você se sai bem

- Dizer "isso fica fora do v1" três vezes ao dia sem vacilar.
- Conectar cada ADR a um impacto concreto no beneficiário ou no operador.
- Proteger o foco do time quando alguém sugere refatorar algo que já funciona.
- Escrever as duas issues do Estágio 4 com contexto suficiente para o Agent trabalhar sozinho.

## Como você se perde

- Travar em detalhes técnicos que não são seus.
- Deixar o time reconstruir o legado programa por programa.
- Escrever issues vagas e o Agent produzir lixo.
- Não cortar escopo e o Estágio 3 terminar pela metade.

## Se você pegou duas personas

- PO + **Requirements Engineer** é a combinação natural. Você escreve as regras; o RE estrutura e testa.
- PO + **Tech Writer** também funciona para times de perfil mais comunicacional.

## 3 exemplos de prompt

1. **(Chat)** _"Analise o programa CALCBENF.NSN do SIFAP legado e liste as 5 regras de negócio com maior impacto no beneficiário. Para cada uma, diga se deve ser migrada, descartada ou evoluída."_
2. **(Chat)** _"Revise estas 3 user stories e reescreva como GitHub issues no formato que o Copilot Agent consome. Inclua contexto, requisitos funcionais como checklist e critérios de aceitação."_
3. **(Chat)** _"O time quer implementar 8 funcionalidades em 3 horas. Com base em complexidade, ajude-me a cortar para as 3 mais críticas para o ciclo mensal de pagamento."_

## Se travar (defaults de emergência)

- **Travou na priorização?** Aplique a regra: "Afeta o ciclo mensal de pagamento? → v1. Não? → backlog."
- **Não sabe escrever uma issue?** Copie o template de [`../04-evolucao/GUIDE.md`](../../04-evolucao/GUIDE.md) e adapte.
- **O time quer tudo no escopo?** Diga: "Temos 3 horas de implementação; escolham 3 funcionalidades."
- **Pergunta de negócio sem resposta?** Documente como premissa e siga.

## Dependências — Quem depende de você

| Persona               | Relação              | Artefato                                    |
| --------------------- | -------------------- | ------------------------------------------- |
| Requirements Engineer | Depende de VOCÊ      | Priorização das regras para virar EARS      |
| Technical Lead        | Depende de VOCÊ      | Escopo definido para calibrar Estágio 3     |
| Developer             | Depende de VOCÊ (S4) | Issues bem escritas para o Agent            |
| Enterprise Architect  | VOCÊ depende dele    | Mapa de integrações para decisões de escopo |

## Como você é avaliado

- **Rubrica A2 (Coerência de Spec):** escopo claro, não-escopo documentado.
- **Rubrica A7 (Agent Experience):** issues com contexto suficiente para o Agent produzir PR útil.
- Avaliado indiretamente em **A6 (Colaboração):** PO que protege o foco do time.
---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="../OVERVIEW.md"><strong>OVERVIEW das 10 personas</strong></a><br/>
<sub>Tabela comparativa: par, líder de estágio, defaults de emergência.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../02-requirements-engineer/PERSONA.md"><strong>Requirements Engineer</strong></a><br/>
<sub>Par 1 · Visão · escreve EARS com source_legacy.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../../README.md">Voltar ao Kit PT-BR</a></sub>

— Paula
