<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# @archaeologist — Estágio 1: Arqueologia

![MUNDO 1 de 4](https://img.shields.io/badge/MUNDO-1%20de%204-F25022?style=for-the-badge) ![AGENTE @archaeologist](https://img.shields.io/badge/AGENTE-@archaeologist-1A1A1A?style=for-the-badge) ![ESTÁGIO 1](https://img.shields.io/badge/ESTÁGIO-1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../../README.md) → [Agentes](../README.md) → **@archaeologist**


> **Para quem é isto?** Para o time durante o Estágio 1 — todos os 5 pares em paralelo.
>
> **O que você terá ao final desta leitura:**
>
> 1. Como ativar o agente `@archaeologist` no Copilot Chat
> 2. Prompts típicos para extração de regras
> 3. O que o agente NÃO faz (não escreve código moderno)

> Use este agente quando a equipe estiver lendo o legado. Ele ajuda a observar, catalogar e perguntar melhor. Ele não reescreve código e não inventa regra de negócio.

## Objetivo da etapa

Transformar arquivos Natural/Adabas em evidências úteis para o Estágio 2: glossário, regras de negócio, mapa de dependências, DDMs compreendidos e mistérios registrados.

## Quando usar

- **Horário:** 11:00–12:00 + 13:30–14:00.
- **Protagonista:** Requirements Engineer.
- **Suporte forte:** Tech Writer, Enterprise Architect e DBA.
- **Pré-requisito:** pasta `01-arqueologia/legado-sifap/` disponível no workspace.

## Passo a passo com o agente

1. Abra o Copilot Chat no VS Code.
2. Selecione o agente `@archaeologist`.
3. Cole o prompt de abertura abaixo.
4. Leia um programa por vez. Não pule arquivos porque parecem simples.
5. Para cada achado, registre evidência com caminho e, quando possível, linha.
6. Ao final, confira a Definição de Pronto antes do Passagem #1.

```text
Estou iniciando o Estágio 1 — Arqueologia.
Temos código Natural/Adabas em 01-arqueologia/legado-sifap/.
Ajude o time a explorar sistematicamente: programas, DDMs, CALLNATs,
regras de negócio, mistérios e riscos de migração.
Comece me dizendo qual arquivo abrir primeiro e que perguntas fazer.
```

## O que perguntar

| Situação | Prompt útil |
| --- | --- |
| Programa Natural desconhecido | "Leia este programa comigo e separe entrada, processamento, saída e regra de negócio." |
| DDM Adabas | "Explique estes campos, marque MU/PE/DE e sugira mapeamento PostgreSQL." |
| Regra ambígua | "Não invente. Registre como mistério, com hipótese, evidência e impacto." |
| CALLNAT | "Mapeie quem chama quem e gere um Mermaid simples." |

## Definição de Pronto

- [ ] Glossário com pelo menos 30 termos relevantes.
- [ ] Catálogo de regras com programa-fonte preenchido.
- [ ] Mapa de dependências cobrindo os programas lidos.
- [ ] DDMs principais com campos, tipos e observações de modelagem.
- [ ] Pelo menos 5 mistérios documentados com evidência.
- [ ] Relatório de descoberta pronto para o Passagem #1 (~14:30).

## Anti-padrões

| Não faça | Faça |
| --- | --- |
| Pedir "resuma tudo" sem abrir arquivo | Abra um arquivo e leia com o agente |
| Transformar hipótese em requisito | Marque como mistério até haver evidência |
| Editar o legado | Trate `01-arqueologia/legado-sifap/` como somente leitura |
| Começar arquitetura no Estágio 1 | Guarde ideias para o `@architect` |

## Navegação

| Anterior | Início | Próximo |
| --- | --- | --- |
| [Visão dos Agent Kits](../README.md) | [Kit PT-BR](../../README.md) | [@architect](../02-architect/README.md) |

— Paula


---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="../README.md"><strong>Visão dos agentes</strong></a><br/>
<sub>Os 4 mundos.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../02-architect/"><strong>@architect</strong></a><br/>
<sub>Próximo mundo: spec moderna.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../../README.md">Voltar ao Kit PT-BR</a></sub>

