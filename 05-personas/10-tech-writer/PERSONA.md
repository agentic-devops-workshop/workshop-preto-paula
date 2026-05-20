<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Persona — Tech Writer

> 🗺 **Você está aqui:** [Kit PT-BR](../../README.md) → [Personas](../OVERVIEW.md) → [Tech Writer](README.md) → **PERSONA**


> **Para quem é isto?** Para a pessoa que vai vestir a persona **Tech Writer** no workshop. Foco: glossário, ADRs, runbook, README evolutivo.
>
> **O que você terá ao final desta leitura:**
>
> 1. Saberá em qual par está e qual fase do SDLC lidera
> 2. Conhecerá a missão da persona no Dia 2
> 3. Verá em qual estágio você lidera, apoia ou observa
> 4. Terá 3 prompts de Copilot prontos para usar
> 5. Saberá o default se travar ("se não souber o que fazer, faça X")

![Par 5 · Operações](https://img.shields.io/badge/PAR-Par%205%20%E2%80%A2%20Operações-1A1A1A?style=for-the-badge) ![Lidera estágio 4](https://img.shields.io/badge/LIDERA%20EST%C3%81GIO-4-1A1A1A?style=for-the-badge) ![Apoia estágio transversal](https://img.shields.io/badge/APOIA-transversal-737373?style=for-the-badge)

## Onde você atua no SDLC

![Linha do tempo do dia mostrando onde esta persona atua](../../assets/timeline-stages.svg)

- **Par**: 5 · Operações (junto com DevOps Engineer)
- **Fases lideradas**: transversal (todas as fases) + Evolução (S4) — relatório do Agent
- **Recebe de**: todos os pares — decisões e código para documentar
- **Faz passagem para**: facilitadores (Paula) — relatório final

## Quem é essa pessoa

Quem transforma uma decisão em memória durável. Sem um Tech Writer deliberado, ADRs viram arquivos vazios, o README fica em "instale as dependências", e nada do que foi descoberto sobrevive à última hora do workshop.

## Missão no workshop

Manter documentação viva durante o dia inteiro — não no final. README que cresce, ADRs escritos no momento da decisão, change log presente. Escrever o relatório de experiência com o Agent no Estágio 4.

## Seu papel no framework Agentic Legacy Modernization

- **Agentes relevantes**: Documentation Agent (transversal)
- **Fase do framework**: todas as fases (documentação contínua)
- **Seu papel**: manter rastreabilidade e documentar decisões para a trilha de auditoria

## Onde você aparece em cada estágio

| Estágio                | Você faz isso                                                                                                               | Entregável que depende de você |
| ---------------------- | --------------------------------------------------------------------------------------------------------------------------- | ------------------------------ |
| 1. Arqueologia         | Mantém glossário e catálogo em formato legível. Escreve o relatório de descoberta no fim do estágio.                        | Relatório do Estágio 1         |
| 2. Spec Moderna        | Revisa a spec por consistência, terminologia e clareza. Formata ADRs com o template.                                        | Spec e ADRs em formato padrão  |
| 3. Implementação       | O README do projeto vira real, não placeholder. Documenta decisões em `docs/` conforme emergem.                             | README populado + `docs/`      |
| 4. Evolução com Agent | Acompanha o Agent trabalhando e escreve relatório honesto da experiência (o que foi bom, o que foi ruim, o que aprenderam). | Relatório final do Estágio 4   |

## Ferramentas e primitivas

- **Copilot Chat** para revisão de estilo e clareza.
- **Cowork** se precisar escrever documento mais longo.
- Skill **markdown-writer** para READMEs e ADRs estruturados.
- **GitHub MCP** para commits no `docs/` enquanto outros mexem no código.

## Cheat-sheets que você usa

- [`../09-cheat-sheets/spec-kit-workflow.md`](../../09-cheat-sheets/spec-kit-workflow.md) — o Specify CLI gera `spec.md`, `plan.md`, `tasks.md` e você mantém consistência com a documentação do time.
- [`../09-cheat-sheets/model-routing.md`](../../09-cheat-sheets/model-routing.md) — Haiku 4.5 para revisão de estilo, Sonnet 4.6 para escrita.

## Como você se sai bem

- Cada ADR tem: contexto, decisão, consequências. Não mais, não menos.
- O README evolui a cada hora, não só no final.
- Terminologia do projeto é consistente (se foi "ciclo", não vira "rodada" no parágrafo seguinte).
- O relatório do Estágio 4 é honesto sobre o Agent — não tenta vender.

## Como você se perde

- Espera o Estágio 3 terminar para começar a escrever.
- ADRs de uma linha ("decidimos usar X").
- README que continua dizendo "TODO: add instructions" no final.
- Relatório do Agent que só diz "funcionou bem".

## Se você pegou duas personas

- **Tech Writer + Product Owner** — você escreve o "por quê" do projeto.
- **Tech Writer + DevOps Engineer** — você documenta enquanto o pipeline roda.
- **Tech Writer + Requirements Engineer** é forte para time pequeno — você estrutura e escreve.

## 3 prompts de exemplo

1. **(Chat)** _"Revise este README e identifique: seções com TODO, terminologia inconsistente, informação desatualizada (portas, credenciais, endpoints). Proponha correções."_
2. **(Plan)** _"No arquivo ADR-001.md, planeje como completar as seções Contexto, Decisão e Consequências usando o template em 02-spec-moderna/ADR-TEMPLATE.md."_
3. **(Chat)** _"Crie um relatório honesto da experiência com Copilot Agent: o que funcionou, o que surpreendeu, o que falhou. Base no template em 04-evolucao/agent-experience-report.md."_

## Checkpoints horários (você não fica perdida(o))

Tech Writer é a persona mais transversal do time — você participa de todos os estágios. Para não ficar "esperando algo para documentar", siga estes checkpoints:

| Hora | O que você está fazendo | Entregável visível |
|---|---|---|
| **13:00–13:30** | Lê comentários e cabeçalhos dos 15 `.NSN` em paralelo com Par 5 (programas de Consulta/Relatório) | Primeiras 10 entradas do `glossary.md` |
| **13:30–14:30** | Consolida glossário com termos vindos de **todos os pares** (peça contribuições a cada 20 min) | Glossário ≥30 termos com `Fonte:` em cada um |
| **14:00–15:30** | Formata os ADRs do Par 2 usando `02-spec-moderna/ADR-TEMPLATE.md`; revisa terminologia da spec | ADRs sem `TODO`, terminologia consistente |
| **15:30–16:00** | Escreve o `README.md` do projeto evoluído — o que é, como rodar, endpoints | README real (não placeholder) |
| **15:00–16:10** | Acompanha Estágio 3 do lado do Par 5; documenta endpoints novos no `docs/` conforme aparecem | `docs/api-endpoints.md` evoluindo |
| **16:10–16:50** | Observa o Agent trabalhando, escreve `agent-experience-report.md` em tempo real | Relatório honesto preenchido até o fim do dia |

> **Regra:** se em 30 minutos você "não tem nada para documentar", pergunte ao par líder do estágio: _"O que você decidiu nos últimos 30 minutos que ainda não está escrito?"_. Quase sempre tem coisa.

## Se travar (defaults de emergência)

- Não conhece o formato ADR? Abra [`../02-spec-moderna/ADR-TEMPLATE.md`](../../02-spec-moderna/ADR-TEMPLATE.md) — copie e preencha.
- README vazio? Comece com: (1) o que o sistema é, (2) como rodar, (3) endpoints disponíveis. O resto pode crescer.
- Glossário travado? Pergunte ao Copilot: _"Liste todas as abreviações encontradas nos arquivos `.NSN` do SIFAP e expanda cada uma."_
- Relatório do Agent vazio? Abra [`../04-evolucao/agent-experience-report.md`](../../04-evolucao/agent-experience-report.md) — o template tem seções prontas para preencher.

## Dependências — Quem depende de você

| Persona             | Relação                    | Artefato                          |
| ------------------- | -------------------------- | --------------------------------- |
| Todo mundo          | VOCÊ depende deles         | Decisões e código para documentar |
| Product Owner       | Depende de VOCÊ            | Glossário e relatórios legíveis   |
| QA Engineer         | Depende de VOCÊ (indireto) | Terminologia consistente na spec  |
| Facilitador (Paula) | Depende de VOCÊ            | Relatório final do Estágio 4      |

## Como você é avaliado

- Rubrica A2 (Spec): documentação consistente, terminologia padronizada
- Rubrica A7 (Agent): relatório honesto e detalhado
- Critério: "README evoluiu a cada hora. ADRs têm contexto, decisão e consequências. Nada diz TODO."
---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="../09-devops-engineer/PERSONA.md"><strong>DevOps Engineer</strong></a><br/>
<sub>Par 5 · Operações · Terraform + CI/CD + runbook.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../../01-arqueologia/GUIDE.md"><strong>Estágio 1 — Arqueologia</strong></a><br/>
<sub>11:00–12:00 + 13:30–14:00 · Ler o legado e catalogar regras de negócio.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../../README.md">Voltar ao Kit PT-BR</a></sub>

— Paula
