<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
title: "Exemplo: agente Architect em ação"
description: "Transcrição didática de sessão mostrando como o agente Architect recorta bounded contexts a partir da análise do legado"
tags: [exemplo, transcricao, architect, hackathon]
author: "Paula Silva, AI-Native Software Engineer, Americas Global Black Belt at Microsoft"
date: 2026-04-29
version: "1.0.0"
---

<!-- markdownlint-disable MD013 MD032 MD060 -->

# Exemplo: agente Architect em ação

![EXEMPLO Sessão real](https://img.shields.io/badge/EXEMPLO-Sessão%20real-7FBA00?style=for-the-badge) ![TIPO Transcript](https://img.shields.io/badge/TIPO-Transcript-1A1A1A?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../../../README.md) → [Agentes](../../README.md) → **Exemplo**

> **Para quem é isto?** Exemplo de sessão real com o agente de estágio. Use como referência de como conduzir a conversa.
>
> **Como ler:** veja o prompt do usuário → veja a resposta do agente → use o mesmo padrão na sua sessão.


## Cenário

**Participantes:** Software Architect (SA), Enterprise Architect (EA)
**Comando do agente:** `/carve-bounded-contexts`
**Entrada:** regras de negócio e mapas de entidades extraídos no estágio Archaeologist a partir de `XYZPROG1.nat`, `XYZRATES.nat` e programas relacionados.
**Objetivo:** identificar bounded contexts limpos para o sistema modernizado, evitando tanto granularidade excessiva quanto contextos grandes demais.

---

## Preparação

1. Abra o GitHub Copilot Chat no VS Code.
2. Confirme que o estágio Archaeologist foi concluído e que o time tem um catálogo com 18 regras de negócio, 6 entidades de domínio e 4 perguntas abertas.
3. Ative a persona do agente Architect.
4. Verifique se os artefatos extraídos estão em `specs/001-rate-processing/RESEARCH.md`.

---

## Prompt de abertura

**SA:** `@architect /carve-bounded-contexts based on our extracted rules and entities`

---

## Transcrição

**Agente:**
> Vou iniciar a análise de bounded contexts. Vou partir das entidades e regras extraídas, propor contextos candidatos e avaliar cada um contra três critérios:
>
> - **Coesão:** as entidades e regras dentro do contexto pertencem semanticamente ao mesmo assunto?
> - **Acoplamento:** quantas dependências entre contextos essa fronteira criaria?
> - **Frequência de mudança:** mudanças em um contexto exigiriam mudanças frequentes em outro?
>
> A partir da saída do Archaeologist, vejo estas entidades de domínio:
>
> - `EMPL-MASTER`: registros de funcionário, como status, grade, departamento e data de contratação.
> - `RATE-TABLE`: definições de taxa, como taxa base, data efetiva e código de motivo.
> - `PERIOD-CALENDAR`: períodos de cálculo, como data inicial, data final e tipo de período.
> - `ACCUM-TOTAL`: resultados acumulados de cálculo por funcionário.
> - `AUDIT-LOG`: entradas de auditoria, com quem mudou, quando mudou e o que mudou.
> - `ERROR-LOG`: erros de processamento e rejeições.
>
> Vou propor **5 bounded contexts candidatos** e avaliar cada um.

**SA:**
> Pode seguir.

**Agente:**
> **Hipótese 1: contexto "Personnel Management"**
>
> Conteria: `EMPL-MASTER`, regras de status de funcionário (BR-001) e lógica baseada em grade (limiar de grade da BR-003).
>
> | Critério | Avaliação |
> | --- | --- |
> | Coesão | **ALTA** — Dados de funcionário e transições de status estão fortemente relacionados. |
> | Acoplamento | **BAIXO** — Outros contextos precisam de ID e status de funcionário, mas não do registro completo. |
> | Frequência de mudança | **INDEPENDENTE** — Regras de status mudam por política de RH, não por mudanças de taxa. |
>
> **Veredito: CANDIDATO FORTE** ✅
>
> Este é um contexto natural. O ciclo de vida do funcionário, como contratação, suspensão e desligamento, é um subdomínio distinto, com seus próprios motivos de mudança.
>
> ---
>
> **Hipótese 2: contexto "Rate Calculation"**
>
> Conteria: `RATE-TABLE`, `ACCUM-TOTAL`, regras de aplicação de taxa, ajuste de data da BR-002, multiplicador de override da BR-003 e proteção de acumulação da BR-004.
>
> | Critério | Avaliação |
> | --- | --- |
> | Coesão | **ALTA** — Toda a lógica de cálculo de taxa se agrupa aqui. |
> | Acoplamento | **MODERADO** — Precisa da grade do funcionário e das datas do período. |
> | Frequência de mudança | **INDEPENDENTE** — Regras de taxa mudam quando a política de remuneração muda. |
>
> **Veredito: CANDIDATO FORTE** ✅
>
> O acoplamento é aceitável porque é somente leitura: Rate Calculation lê grade do funcionário e datas do período, mas não grava nessas entidades.
>
> ---
>
> **Hipótese 3: contexto "Batch Processing"**
>
> Conteria: estrutura do loop `READ`, lógica `ESCAPE TOP/BOTTOM`, acumulação de erros e agendamento de jobs batch.
>
> | Critério | Avaliação |
> | --- | --- |
> | Coesão | **BAIXA** — Isso agrupa coisas por *como* executam, não por *o que* fazem. |
> | Acoplamento | **MUITO ALTO** — Toda regra de negócio roda dentro do loop batch. |
> | Frequência de mudança | **DEPENDENTE** — Mudanças em qualquer regra de negócio tocariam esse contexto. |
>
> **Veredito: REJEITADO** ❌
>
> **"Batch Processing" é um modo técnico de execução, não um conceito de domínio.** Batch descreve *como* o trabalho é agendado e executado, não *qual* problema de negócio está sendo resolvido. No sistema modernizado, as mesmas regras podem rodar em um job batch, uma chamada REST ou um handler de evento. A lógica de domínio não deve estar acoplada ao modo de execução.
>
> Recomendo extrair a orquestração batch como uma **preocupação de infraestrutura**, fora dos bounded contexts, talvez como um job Spring Batch que chama o contexto Rate Calculation.

**EA:**
> Espera: concordo em parte. Você está certo que batch processing não é conceito de domínio, mas há lógica específica de batch no legado. O padrão de acumulação de erros (`#ERROR-COUNT`), continuar após erro (`ESCAPE TOP` em vez de `ESCAPE BOTTOM`) e relatório de resumo no final não são infraestrutura pura.

**Agente:**
> Ponto válido. Vou reconsiderar.
>
> Você está separando duas coisas:
>
> 1. **Agendamento e orquestração batch:** infraestrutura, como horário de execução, particionamento e políticas de retry.
> 2. **Semântica de processamento batch:** algo próximo ao domínio, como acumular erros, continuar após falha e produzir resumo.
>
> Vou ajustar a análise. O *agendamento* batch fica na infraestrutura, mas a *semântica* de processamento batch deve ser modelada como uma **preocupação transversal** dentro de Rate Calculation, não como bounded context separado.
>
> Especificamente:
>
> - Acumulação de erros → parte da estratégia de tratamento de erros de Rate Calculation.
> - Continuar após erro → política de processamento exposta por Rate Calculation, configurável por invocação.
> - Relatório de resumo → read model/projeção que qualquer consumidor pode solicitar.
>
> **Essa separação funciona para vocês dois?**

**SA:**
> Sim, ficou mais limpo. O job batch é só um cliente de Rate Calculation que chama o contexto em loop.

**EA:**
> Concordo. Próximo.

**Agente:**
> Ótimo. Vou continuar com as hipóteses restantes.
>
> ---
>
> **Hipótese 4: contexto "Period Management"**
>
> Conteria: `PERIOD-CALENDAR`, regras de fronteira de período e definições de calendário fiscal.
>
> | Critério | Avaliação |
> | --- | --- |
> | Coesão | **MODERADA** — Definições de período são autocontidas, mas temos apenas uma entidade. |
> | Acoplamento | **BAIXO** — Rate Calculation lê datas de período, mas Period Management é independente. |
> | Frequência de mudança | **INDEPENDENTE** — Calendários mudam anualmente, não junto com taxas ou pessoas. |
>
> **Veredito: LIMÍTROFE** ⚠️
>
> Um contexto com uma única entidade costuma ser um cheiro de granularidade excessiva. Ainda assim, gestão de período/calendário é um subdomínio clássico que tende a crescer: ano fiscal, calendários de feriado, períodos de pagamento e períodos de relatório.
>
> **Quero a opinião de vocês: Period Management tende a crescer em escopo, ou deve permanecer apenas como consulta de intervalo de datas?**

**SA:**
> No legado é só uma tabela de consulta. Mas o time de negócio mencionou que quer suportar vários tipos de calendário no futuro: fiscal, período de pagamento e relatórios.

**EA:**
> Se vai crescer, merece contexto próprio. Melhor ter um contexto fino agora e deixá-lo crescer naturalmente do que extraí-lo depois de Rate Calculation.

**Agente:**
> Concordo. Vou promover Period Management a **bounded context completo** com base no crescimento esperado. Boa decisão.
>
> **Veredito atualizado: ACEITO** ✅ com base no conhecimento de domínio futuro do time.
>
> ---
>
> **Hipótese 5: contexto "Reporting"**
>
> Conteria: `ERROR-LOG`, geração de resumo, relatórios de erro e consultas de auditoria.
>
> | Critério | Avaliação |
> | --- | --- |
> | Coesão | **BAIXA** — "Reporting" é uma preocupação transversal, não um domínio. |
> | Acoplamento | **MUITO ALTO** — Todos os contextos produzem dados que alimentam relatórios. |
> | Frequência de mudança | **DEPENDENTE** — Formatos de relatório mudam sempre que os domínios de origem mudam. |
>
> **Veredito: REJEITADO** ❌
>
> **"Reporting" tem o mesmo problema de "Batch Processing": é uma capacidade técnica, não um domínio de negócio.** Relatórios são *visões* sobre dados de domínio, não um domínio em si. Em um sistema bem estruturado, cada bounded context expõe seus próprios read models, e uma infraestrutura de relatórios os agrega.
>
> Porém, noto que `AUDIT-LOG` é diferente de relatórios gerais. Trilhas de auditoria têm requisitos regulatórios, políticas de retenção e restrições de imutabilidade que fazem delas um subdomínio real.
>
> Quero propor a substituição de "Reporting" por...
>
> ---
>
> **Hipótese 5 revisada: contexto "Audit Trail"**
>
> Conteria: `AUDIT-LOG`, registros imutáveis de eventos, regras de retenção regulatória e API de consulta de auditoria.
>
> | Critério | Avaliação |
> | --- | --- |
> | Coesão | **ALTA** — Eventos de auditoria, retenção e compliance estão fortemente relacionados. |
> | Acoplamento | **BAIXO** — Outros contextos publicam eventos para Audit Trail, mas não dependem dele. |
> | Frequência de mudança | **INDEPENDENTE** — Requisitos de auditoria mudam em ciclos regulatórios, não em ciclos de negócio. |
>
> **Veredito: CANDIDATO FORTE** ✅
>
> O contexto Audit Trail recebe eventos de outros contextos, via eventos de domínio ou barramento de eventos, mas não cria acoplamento bidirecional. Ele é um consumidor autônomo.

**SA:**
> Isso é bem melhor do que misturar com relatórios gerais. Auditores têm requisitos específicos de imutabilidade e retenção.

**EA:**
> Concordo. E mapeia bem para um armazenamento separado: logs de auditoria podem ir para um store append-only, enquanto dados de negócio ficam no PostgreSQL.

**Agente:**
> Perfeito. Vou apresentar o mapa final de bounded contexts.
>
> ---
>
> ## Mapa final de bounded contexts
>
> Com base na avaliação, estes são os **4 bounded contexts aceitos**:
>
> ```
> ┌─────────────────────┐     ┌─────────────────────┐
> │ Personnel Management│     │  Rate Calculation    │
> │                     │────▶│                      │
> │ • EMPL-MASTER       │ ID  │ • RATE-TABLE         │
> │ • Status lifecycle  │ +   │ • ACCUM-TOTAL        │
> │ • Grade management  │grade│ • Date clamping      │
> │ • BR-001            │     │ • Override multiplier │
> │                     │     │ • Accumulation guard  │
> │                     │     │ • BR-002,003,004      │
> └─────────────────────┘     └──────────┬────────────┘
>                                        │ events
> ┌─────────────────────┐     ┌──────────▼────────────┐
> │ Period Management   │     │    Audit Trail        │
> │                     │────▶│                        │
> │ • PERIOD-CALENDAR   │dates│ • AUDIT-LOG           │
> │ • Calendar types    │     │ • Immutable records   │
> │ • Fiscal periods    │     │ • Retention policies  │
> │ • Pay periods       │     │ • Regulatory queries  │
> └─────────────────────┘     └────────────────────────┘
> ```
>
> **Comunicação entre contextos:**
>
> - Personnel → Rate Calculation: ID do funcionário + grade, por consulta síncrona ou cache.
> - Period → Rate Calculation: datas do período, por consulta síncrona ou cache.
> - Rate Calculation → Audit Trail: eventos de domínio assíncronos, fire-and-forget.
> - Todos os contextos → Audit Trail: eventos de auditoria assíncronos.
>
> **Candidatos rejeitados:**
>
> - "Batch Processing": modo técnico de execução, não domínio; preocupação de infraestrutura.
> - "Reporting": camada transversal de visão, não domínio; read models por contexto.
>
> **Importante: estou apresentando isso como recomendação, não como decisão.** O mapa final de contextos deve ser revisado pelo time todo, especialmente pelo Product Owner, que entende melhor as fronteiras de negócio do que o código.
>
> **Vocês querem que eu gere agora o artefato de mapa de contexto para o `DESIGN.md`, ou preferem ajustar as fronteiras primeiro?**

**SA:**
> Vamos revisar com o time todo primeiro. Você consegue exportar isso como um resumo para apresentarmos?

**Agente:**
> Vou gerar um resumo conciso com o diagrama de contexto, os critérios de avaliação de cada hipótese e os itens abertos. Vocês podem usar isso no próximo sync do time.
>
> Uma última observação: eu intencionalmente não decidi automaticamente nenhuma fronteira. **Fronteiras de bounded context são decisões estratégicas que afetam estrutura de time, desenho de API e topologia de deploy.** Elas devem ser sempre uma escolha deliberada do time, não um padrão escolhido pela IA.

---

## O que o time obteve

| Saída | Detalhe |
| --- | --- |
| **5 hipóteses avaliadas** | Cada uma analisada por coesão, acoplamento e frequência de mudança. |
| **4 bounded contexts aceitos** | Personnel Management, Rate Calculation, Period Management e Audit Trail. |
| **2 hipóteses rejeitadas** | "Batch Processing" como modo técnico e "Reporting" como visão transversal. |
| **1 hipótese ajustada** | "Reporting" virou "Audit Trail", um subdomínio real com motivadores regulatórios. |
| **Mapa de comunicação entre contextos** | Consultas síncronas para dados de referência e eventos assíncronos para auditoria. |
| **Negociação agente-time** | Semântica de batch separada da infraestrutura batch. |
| **Period Management promovido** | Decisão baseada no conhecimento do time sobre futuros requisitos de calendário. |

---

## Anti-padrões demonstrados

| Anti-padrão | Onde quase aconteceu | Como o agente evitou |
| --- | --- | --- |
| **Contextos técnicos** | "Batch Processing" foi proposto como bounded context. | O agente rejeitou: modo de execução não é conceito de domínio. |
| **Transversal tratado como contexto** | "Reporting" foi proposto como bounded context. | O agente rejeitou: visões sobre dados não são domínio. |
| **Decidir fronteiras automaticamente** | O agente poderia escolher 4 contextos e seguir adiante. | O agente declarou que eram recomendações, não decisões. |
| **Ignorar conhecimento do time** | Period Management era limítrofe no papel. | O agente perguntou ao time e promoveu com base em insight de domínio. |
| **Pensamento binário** | O questionamento do EA sobre batch poderia ter sido descartado. | O agente ajustou: separou agendamento batch, infraestrutura, de semântica batch, próxima ao domínio. |

---

## O que o time deve aprender

1. **Bounded contexts são fronteiras de domínio, não fronteiras técnicas.** Se o nome do contexto descreve *como* algo funciona, e não *qual problema de negócio* resolve, provavelmente não é um contexto real.

2. **Avalie toda hipótese com os mesmos critérios.** Coesão, acoplamento e frequência de mudança criam uma estrutura consistente de decisão.

3. **Conhecimento do time supera análise de código.** O agente classificou Period Management como limítrofe, mas o conhecimento de requisitos futuros mudou a decisão.

4. **O agente deve recusar decisões estratégicas automáticas.** Fronteiras de contexto afetam times, deploy e contratos de API; exigem julgamento humano.

5. **Discordância é produtiva.** Quando o EA questionou Batch Processing, o modelo ficou melhor. Não trate a primeira resposta do agente como final.

6. **Hipóteses rejeitadas são valiosas.** Saber por que não criamos "Reporting" é tão importante quanto saber por que criamos "Audit Trail".

7. **Padrões de comunicação entre contextos importam cedo.** Decisões síncronas vs. assíncronas moldam toda a arquitetura de integração.


---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="../README.md"><strong>@architect</strong></a><br/>
<sub>Mundo 2.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../../03-builder/"><strong>@builder</strong></a><br/>
<sub>Próximo mundo.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../../../README.md">Voltar ao Kit PT-BR</a></sub>

