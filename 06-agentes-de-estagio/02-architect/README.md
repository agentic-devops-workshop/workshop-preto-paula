<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# @architect — Estágio 2: Spec Moderna

![MUNDO 2 de 4](https://img.shields.io/badge/MUNDO-2%20de%204-00A4EF?style=for-the-badge) ![AGENTE @architect](https://img.shields.io/badge/AGENTE-@architect-1A1A1A?style=for-the-badge) ![ESTÁGIO 2](https://img.shields.io/badge/ESTÁGIO-2-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../../README.md) → [Agentes](../README.md) → **@architect**


> **Para quem é isto?** Para o Par 2 (EA+SA) durante o Estágio 2.
>
> **O que você terá ao final desta leitura:**
>
> 1. Como ativar o agente `@architect` no Copilot Chat
> 2. Prompts para EARS, ADR, C4, bounded contexts
> 3. Regra: não aceita requisito sem evidência no legado

> Use este agente quando o time já tem descobertas do legado e precisa transformá-las em especificação moderna. Ele ajuda a decidir limites, escrever EARS, registrar ADRs e preparar o terreno para implementação.

## Objetivo da etapa

Transformar o relatório de descoberta em uma spec implementável: requisitos rastreáveis, bounded contexts, diagramas C4, ADRs e plano técnico alinhado ao GitHub Spec-Kit.

## Quando usar

- **Horário:** 14:00–15:00.
- **Protagonista:** Software Architect.
- **Suporte forte:** Requirements Engineer, Enterprise Architect, Product Owner e Technical Lead.
- **Pré-requisito:** Passagem #1 com evidências do legado e `source_legacy:` disponíveis.

## Passo a passo com o agente

1. Selecione o agente `@architect` no Copilot Chat.
2. Cole o prompt de abertura abaixo.
3. Liste as regras e mistérios vindos do Estágio 1.
4. Use `/speckit.specify` e `/speckit.clarify` para formalizar requisitos.
5. Use `/speckit.plan` para gerar plano técnico, dados e contratos.
6. Registre decisões importantes em ADRs.
7. Faça o Passagem #2 para os Pares 3 e 4.

```text
Estou iniciando o Estágio 2 — Spec Moderna.
Temos relatório de descoberta, catálogo de regras, glossário, DDMs e mapa de dependências.
Ajude a transformar isso em requisitos EARS com source_legacy, bounded contexts,
C4, ADRs e plano técnico para Java 21 + Spring Boot + PostgreSQL + Next.js.
```

## O que perguntar

| Situação | Prompt útil |
| --- | --- |
| Regra de negócio bruta | "Converta esta regra em EARS com REQ-ID, acceptance e source_legacy." |
| Limite de módulo incerto | "Compare 2 ou 3 bounded contexts possíveis e mostre prós/contras." |
| Decisão arquitetural | "Gere um ADR com contexto, opções, decisão, consequências e riscos." |
| Plano técnico | "Prepare `/speckit.plan` considerando modular monolith, JPA e PostgreSQL." |

## Definição de Pronto

- [ ] Pelo menos 12 requisitos EARS com REQ-IDs.
- [ ] 100% dos requisitos com `source_legacy:` ou `[GREENFIELD]` justificado.
- [ ] C4 L1/L2/L3 ou equivalente Mermaid para orientar implementação.
- [ ] ADRs principais: modular monolith, persistência e autenticação.
- [ ] `spec.md`, `plan.md` e `tasks.md` preparados ou em progresso no fluxo Spec-Kit.
- [ ] Escopo assinado pelo Product Owner no Passagem #2 (~16:00).

## Anti-padrões

| Não faça | Faça |
| --- | --- |
| Escrever requisito sem evidência | Exija `source_legacy:` ou justificativa greenfield |
| Criar arquitetura bonita demais para o tempo | Prefira decisões simples, testáveis e implementáveis |
| Misturar ADR com opinião solta | Registre opções, decisão e consequências |
| Entregar spec sem acceptance | Todo requisito precisa ser testável |

## Navegação

| Anterior | Início | Próximo |
| --- | --- | --- |
| [@archaeologist](../01-archaeologist/README.md) | [Kit PT-BR](../../README.md) | [@builder](../03-builder/README.md) |

— Paula


---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="../01-archaeologist/"><strong>@archaeologist</strong></a><br/>
<sub>Mundo anterior.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../03-builder/"><strong>@builder</strong></a><br/>
<sub>Próximo mundo: implementação.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../../README.md">Voltar ao Kit PT-BR</a></sub>

