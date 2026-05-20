<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Persona — Enterprise Architect

> 🗺 **Você está aqui:** [Kit PT-BR](../../README.md) → [Personas](../OVERVIEW.md) → [Enterprise Architect](README.md) → **PERSONA**


> **Para quem é isto?** Para a pessoa que vai vestir a persona **Enterprise Architect** no workshop. Foco: contexto do sistema, ADRs estruturais, integrações.
>
> **O que você terá ao final desta leitura:**
>
> 1. Saberá em qual par está e qual fase do SDLC lidera
> 2. Conhecerá a missão da persona no Dia 2
> 3. Verá em qual estágio você lidera, apoia ou observa
> 4. Terá 3 prompts de Copilot prontos para usar
> 5. Saberá o default se travar ("se não souber o que fazer, faça X")

![Par 2 · Arquitetura](https://img.shields.io/badge/PAR-Par%202%20%E2%80%A2%20Arquitetura-FFB900?style=for-the-badge) ![Lidera estágio 2](https://img.shields.io/badge/LIDERA%20EST%C3%81GIO-2-1A1A1A?style=for-the-badge) ![Apoia estágio 1, 4](https://img.shields.io/badge/APOIA-1%2C%204-737373?style=for-the-badge)

## Onde você atua no SDLC

![Linha do tempo do dia mostrando onde esta persona atua](../../assets/timeline-stages.svg)

- **Par**: 2 · Arquitetura (junto com Software Architect)
- **Fases lideradas**: Especificação (S2) — C4 L1 + ADRs de topologia
- **Recebe de**: Par 1 (Visão) no H1 — catálogo de regras e escopo
- **Faz passagem para**: Par 3 (Implementação) e Par 4 (Qualidade) no H2; Par 5 (Operações) para Terraform

## Quem é essa pessoa

Quem enxerga o sistema dentro do seu ecossistema. No SIFAP, isso significa: SIAFI, Banco do Brasil, INCRA, MDA e outros internos do governo. O EA sabe onde estão os contratos, quais são frágeis, e quais podem ser tocados sem disparar a cadeia inteira.

## Missão no workshop

Garantir que o SIFAP 2.0 não quebre o mundo ao redor. Desenhar o mapa de dependências. Validar que a arquitetura-alvo respeita contratos externos (síncrono com SIAFI, assíncrono com BB) e que a estratégia de coexistência com o legado é viável.

## Seu papel no framework Agentic Legacy Modernization

- **Agentes relevantes**: Descoberta Agent (S1), Deployment Agent (S4)
- **Fase do framework**: Assessment → Coexistence and Traffic Migration
- **Seu papel**: mapear dependências externas e definir estratégia de coexistência (Strangler Fig)

## Onde você aparece em cada estágio

| Estágio                | Você faz isso                                                                                                            | Entregável que depende de você             |
| ---------------------- | ------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------ |
| 1. Arqueologia         | Constrói mapa de dependências e integrações (C4 nível 1 — sistema em contexto). Identifica contratos externos.           | Diagrama C4 L1 + inventário de integrações |
| 2. Spec Moderna        | Define decisões de topologia (onde o sistema vive na cloud, quem é cliente de quem, quais APIs são síncronas e por quê). | ADRs de topologia e integração (1–2)       |
| 3. Implementação       | Valida que a implementação respeita os contratos desenhados. Ajuda DevOps com Terraform de alto nível.                   | Validação do layout deployado              |
| 4. Evolução com Agent  | Avalia se as issues do Estágio 4 têm implicações arquiteturais que precisam de revisão prévia.                           | Avaliação de impacto                       |

## Ferramentas e primitivas

- **Mermaid** e **C4** para diagramas.
- **Copilot Chat** para validar decisões de topologia com prompts de pressão (_"que risco esse design carrega se o SIAFI cair?"_).
- **GitHub Spec-Kit** com `/speckit.plan` — transforma a spec em plano técnico, decisões e contratos revisáveis.
- Skills do próprio persona-kit — prompts estruturados para análise de dependências.

## Cheat-sheets que você usa

- [`../09-cheat-sheets/spec-kit-workflow.md`](../../09-cheat-sheets/spec-kit-workflow.md) — especialmente `/speckit.plan` e `/speckit.analyze`.
- [`../09-cheat-sheets/model-routing.md`](../../09-cheat-sheets/model-routing.md) — use **Opus 4.6** para análise de impacto arquitetural.

## Como você se sai bem

- O C4 nível 1 é legível por qualquer pessoa não-técnica do time em 30 segundos.
- Seus ADRs nomeiam o "caminho não tomado" e explicam por quê.
- Você ancora Modular Monolith no argumento — não como moda, mas por fit com o contexto cliente.
- Você alinha com o Software Architect onde seu escopo termina e onde o dele começa.

## Como você se perde

- Desenha diagrama que só você entende.
- Propõe microsserviços (arquitetura é fixa — um ADR explicando é legítimo; insistir é desperdício).
- Esquece integrações reais (SIAFI, BB) e o Estágio 3 descobre tarde.
- Duplica o trabalho do Software Architect em vez de desenhar uma fronteira clara.

## Se você pegou duas personas

- **EA + Software Architect** é a combinação mais comum em time pequeno. Você cuida do C4 L1; seu par cuida dos níveis 2 e 3.
- **EA + Technical Lead** também funciona se quiser envolvimento mais hands-on.

## 3 exemplos de prompt

1. **(Chat)** _"Crie um diagrama C4 Nível 1 em Mermaid para o SIFAP 2.0 mostrando: 3 tipos de usuário, o sistema central, e 4 sistemas externos (SIAFI, Receita Federal, Banco do Brasil, CadÚnico)."_
2. **(Chat)** _"Se o SIAFI ficar offline por 2 horas durante o ciclo mensal de pagamento, qual o impacto? Proponha 3 estratégias de fallback e recomende uma."_
3. **(Chat)** _"Compare estas 3 opções de integração com o Banco do Brasil: batch CNAB, REST síncrono, mensageria assíncrona. Escreva um ADR recomendando uma."_

## Se travar (defaults de emergência)

- **Não conhece C4?** Use um Mermaid flowchart simples: caixas = sistemas, setas = integrações. Rotule as setas.
- **Queimou tempo em C4 Nível 3?** Pare. Nível 1 + Nível 2 são suficientes. Times raramente precisam de L3.
- **Não conhece Mermaid?** Pergunte ao Copilot: _"Crie um diagrama C4 nível 1 em Mermaid para um sistema de pagamento que integra com SIAFI e Banco do Brasil."_
- **Discordância com o Software Architect?** Escreva um ADR com as duas opções e peça votação ao time.

## Dependências — Quem depende de você

| Persona               | Relação                    | Artefato                  |
| --------------------- | -------------------------- | ------------------------- |
| Software Architect    | Depende de VOCÊ            | C4 L1 para desenhar L2/L3 |
| DevOps Engineer       | Depende de VOCÊ            | Topologia para Terraform  |
| Developer             | Depende de VOCÊ (indireto) | Contratos de integração   |
| Requirements Engineer | VOCÊ depende dele          | Requisitos de integração  |

## Como você é avaliado

- **Rubrica A1 (Arqueologia):** mapa de dependências legível por não-técnicos.
- **Rubrica A2 (Coerência de Spec):** ADRs nomeiam o "caminho não tomado".
- Critério: "C4 L1 entendido em 30 segundos por qualquer pessoa do time."
---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="../02-requirements-engineer/PERSONA.md"><strong>Requirements Engineer</strong></a><br/>
<sub>Par 1 · Visão · escreve EARS com source_legacy.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../04-software-architect/PERSONA.md"><strong>Software Architect</strong></a><br/>
<sub>Par 2 · Arquitetura · bounded contexts e módulos.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../../README.md">Voltar ao Kit PT-BR</a></sub>

— Paula
