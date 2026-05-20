<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Documentação

![PASTA Docs transversais](https://img.shields.io/badge/PASTA-Docs%20transversais-00A4EF?style=for-the-badge) ![USE Suporte](https://img.shields.io/badge/USE-Suporte-1A1A1A?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → **Docs**


> **Para quem é isto?** Quem busca documentação transversal — independente de qual estágio está rodando.
>
> **O que você terá ao final desta leitura:**
>
> 1. Índice da documentação não vinculada a um estágio específico
> 2. Glossário visual, runbook, persona-agent-matrix, sdlc-flow-guide, ADRs
> 3. Links rápidos para os recursos consultados a qualquer hora do dia

Esta pasta pertence à persona **Tech Writer**, mas recebe contribuições de todas as pessoas. Ela guarda a documentação curada pela equipe, que se acumula ao longo do workshop.

## Como usar esta pasta

1. **Antes de começar o dia**, leia [sdlc-flow-guide.md](sdlc-flow-guide.md) para entender o mapa completo.
2. **Quando escolher suas personas**, leia [persona-agent-matrix.md](persona-agent-matrix.md) para saber quando você protagoniza, apoia ou observa.
3. **Durante o Estágio 1**, atualize [glossary.md](glossary.md) e registre termos com fonte no legado.
4. **Durante decisões técnicas**, crie ADRs em [adr/](adr/).
5. **No final**, revise [runbook.md](runbook.md) para que outra pessoa consiga rodar e operar o sistema.

## Estrutura

| Caminho                      | Finalidade                                                                             |
| ---------------------------- | -------------------------------------------------------------------------------------- |
| [`adr/`](adr/)               | Registros de decisão de arquitetura (um arquivo por decisão)                           |
| [`glossary.md`](glossary.md) | Glossário de domínio — preenchido durante o Estágio 1 (Arqueologia)                    |
| [`4-agents-explained.md`](4-agents-explained.md) | Explicação didática dos 4 agentes de etapa e sua relação com persona-kits |
| [`persona-agent-matrix.md`](persona-agent-matrix.md) | Matriz de quem protagoniza, apoia ou observa em cada etapa |
| [`sdlc-flow-guide.md`](sdlc-flow-guide.md) | Fluxo completo do dia, passagens, prompts e entregáveis |
| `api.md` _(criado pelo time)_ | Visão geral da OpenAPI e resumo dos endpoints (gerado automaticamente quando possível) |
| [`runbook.md`](runbook.md)   | Como executar o sistema localmente, em CI e no Azure                                   |

## Convenções

- Um ADR por decisão. Numere-os em sequência: `0001-title.md`, `0002-title.md`.
- Termos do glossário em ordem alfabética, com citações para o programa legado onde o termo se originou.
- Todo README em subpastas tem YAML frontmatter e segue [`.github/copilot-instructions.md`](../.github/copilot-instructions.md).
- Toda decisão importante vira ADR. Conversa em chat não é registro suficiente.
- Todo termo do glossário que nasceu do legado precisa de fonte (`.NSN`, `.ddm` ou documento histórico).

## Definição de Pronto da documentação

- [ ] Glossário com fontes do legado.
- [ ] ADRs com contexto, opções, decisão e consequências.
- [ ] Runbook com comandos de execução, validação e troubleshooting.
- [ ] Links internos apontam para arquivos dentro de `pt-br/`.
- [ ] Documentos explicam o porquê antes do passo a passo.

## Links rápidos

- [Fluxo da equipe](../00-TEAM-FLOW.md)
- [Persona kits consolidados](../05-personas/) — leia `PERSONA.md` dentro do kit do seu papel
- [Guias de estágio](../01-arqueologia/GUIDE.md)
---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="../09-cheat-sheets/README.md"><strong>Cartões de Referência</strong></a><br/>
<sub>3 cartões de 1 página: Copilot, Spec-Kit, modelos.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="glossario-visual.md"><strong>Glossário Visual</strong></a><br/>
<sub>30+ termos técnicos com analogia em 3 linhas.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>
