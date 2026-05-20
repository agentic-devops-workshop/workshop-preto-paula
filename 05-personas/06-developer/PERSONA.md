<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Persona — Developer

> 🗺 **Você está aqui:** [Kit PT-BR](../../README.md) → [Personas](../OVERVIEW.md) → [Developer](README.md) → **PERSONA**


> **Para quem é isto?** Para a pessoa que vai vestir a persona **Developer** no workshop. Foco: código Java + TypeScript, testes, integração.
>
> **O que você terá ao final desta leitura:**
>
> 1. Saberá em qual par está e qual fase do SDLC lidera
> 2. Conhecerá a missão da persona no Dia 2
> 3. Verá em qual estágio você lidera, apoia ou observa
> 4. Terá 3 prompts de Copilot prontos para usar
> 5. Saberá o default se travar ("se não souber o que fazer, faça X")

![Par 3 · Implementação](https://img.shields.io/badge/PAR-Par%203%20%E2%80%A2%20Implementação-7FBA00?style=for-the-badge) ![Lidera estágio 3](https://img.shields.io/badge/LIDERA%20EST%C3%81GIO-3-1A1A1A?style=for-the-badge) ![Apoia estágio 4](https://img.shields.io/badge/APOIA-4-737373?style=for-the-badge)

## Onde você atua no SDLC

![Linha do tempo do dia mostrando onde esta persona atua](../../assets/timeline-stages.svg)

- **Par**: 3 · Implementação (junto com Technical Lead)
- **Fases lideradas**: Implementação (S3) + Evolução (S4)
- **Recebe de**: Par 2 (Arquitetura) no H2 — REQ-IDs + estrutura de pacotes
- **Faz passagem para**: Par 4 (Qualidade) — código testável; Par 5 (Operações) no H3 — build estável

## Quem é essa pessoa

Você escreve o código. Mais que isso: é quem usa o Copilot o dia inteiro nos três modos e traduz ideias em diff. No Estágio 3 carrega o peso pesado da produção.

## Missão no workshop

Transformar spec em código rodando. Usar o Copilot deliberadamente — Ask para entender, Plan para desenhar mudanças, Agent para delegar. Commitar todo dia.

## Seu papel no framework Agentic Legacy Modernization

- **Agentes relevantes**: Translation Agent (S3), Review Agent (S3)
- **Fase do framework**: Tradução e geração de testes
- **Seu papel**: implementar a tradução Natural → Java guiada pela spec EARS

## Onde você aparece em cada estágio

| Estágio                | Você faz isso                                                                                 | Entregável que depende de você   |
| ---------------------- | --------------------------------------------------------------------------------------------- | -------------------------------- |
| 1. Arqueologia         | Lê programas Natural com Copilot Chat. Produz resumo legível para o resto do time.            | Resumos narrativos dos programas |
| 2. Spec Moderna        | Pareia com o Requirements Engineer para antecipar problemas de implementação.                 | Sinais preventivos na spec       |
| 3. Implementação       | Implementa, testa, abre PR, revisa PR, implementa de novo.                                    | Backend + frontend da sua fatia  |
| 4. Evolução com Agent | Acompanha o Agent trabalhando. Intervém quando ele se perde. Termina o que ele não completou. | PR do Agent em estado mergeável  |

## Ferramentas e primitivas

- **Copilot Chat** — entendimento e discussão de design.
- **Copilot Plan** — sua principal ferramenta no Estágio 3 para mudanças multi-arquivo.
- **Copilot Agent** — no Estágio 4, você dirige o Agent pelo time ou junto com o TL.
- **Spec-Kit** — consome artefatos do SA e do RE; produz código guiado pela spec.
- **GitHub MCP** para trabalhar com issues e PRs sem sair do VS Code.

## Cheat-sheets que você usa

- [`../09-cheat-sheets/copilot-3-modes.md`](../../09-cheat-sheets/copilot-3-modes.md) — este é seu mapa do dia.
- [`../09-cheat-sheets/spec-kit-workflow.md`](../../09-cheat-sheets/spec-kit-workflow.md) — `/speckit.tasks`, `/speckit.implement` e `/speckit.analyze`.
- [`../09-cheat-sheets/model-routing.md`](../../09-cheat-sheets/model-routing.md) — Haiku 4.5 para snippets simples, Sonnet 4.6 como default, Opus 4.6 para design.

## Como você se sai bem

- Usa os três modos do Copilot deliberadamente — nem sempre é Chat.
- Commits pequenos e pull requests pequenos.
- Escreve testes ao mesmo tempo que o código.
- Não se apaixona por uma abstração no meio do Estágio 3.

## Como você se perde

- Trabalha oito horas numa única branch gigante.
- Usa o Agent para uma tarefa que Ask ou Plan resolveriam em 5 minutos.
- Escreve código sem teste e descobre às 16:30 que nada funciona.
- Vai sempre para Opus 4.6 — vai gastar tempo demais esperando.

## Se você pegou duas personas

- **Developer + Technical Lead** — muito comum.
- **Developer + QA Engineer** — você escreve a feature e os testes na mesma cabeça.
- **Developer + DevOps Engineer** em time pequeno — você empacota e entrega.

## 3 prompts de exemplo

1. **(Chat)** _"Explique o programa CALCDSCT.NSN do SIFAP legado e identifique a regra de teto de desconto. Depois me ajude a implementar o equivalente em Java seguindo o padrão do `PaymentService` existente."_
2. **(Plan)** _"Selecione BeneficiaryEntity.java, BeneficiaryService.java e BeneficiaryController.java. Planeje a adição de um campo 'email' ao beneficiário: entity, service, controller, migration e teste."_
3. **(Agent)** _"Implemente a feature descrita nesta Issue: [cole a issue]. Respeite a arquitetura de 3 camadas e inclua testes."_

## Se travar (defaults de emergência)

- Código não compila? `mvn test-compile` para ver o erro exato. Geralmente é um import faltando.
- Não conhece a estrutura de pacotes? Olhe `beneficiary/` como referência: domain/ → application/ → infrastructure/.
- Copilot gerando código ruim? Mude de Ask para Plan — selecione os arquivos relevantes e descreva a mudança.
- Teste falhando? Leia a mensagem de erro. Se for NPE, provavelmente falta um mock. Se for assertion, o valor esperado está errado.

## Dependências — Quem depende de você

| Persona               | Relação           | Artefato                                |
| --------------------- | ----------------- | --------------------------------------- |
| Software Architect    | VOCÊ depende dele | Estrutura de pacotes e bounded contexts |
| Requirements Engineer | VOCÊ depende dele | Requisitos claros para implementar      |
| Technical Lead        | Depende de VOCÊ   | PRs para revisar                        |
| QA Engineer           | Depende de VOCÊ   | Código testável                         |
| DBA                   | VOCÊ depende dele | Migrações e modelo de dados             |

## Como você é avaliado

- Rubrica A3 (Integridade Técnica): endpoints funcionais, testes passando
- Rubrica A4 (Uso Consciente do Copilot): troca deliberada entre Ask, Plan e Agent
- Critério: "Commits pequenos, PRs revisáveis, testes escritos junto do código"
---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="../05-technical-lead/PERSONA.md"><strong>Technical Lead</strong></a><br/>
<sub>Par 3 · Implementação · padrões e revisão.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../07-dba/PERSONA.md"><strong>DBA</strong></a><br/>
<sub>Par 4 · Qualidade · migrações Flyway + queries.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../../README.md">Voltar ao Kit PT-BR</a></sub>

— Paula
