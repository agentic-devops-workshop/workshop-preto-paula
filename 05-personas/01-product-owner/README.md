<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Product Owner — Kit Copilot

![PERSONA Product Owner](https://img.shields.io/badge/PERSONA-Product%20Owner-F25022?style=for-the-badge) ![MARIO 👸 Peach](https://img.shields.io/badge/MARIO-👸%20Peach-1A1A1A?style=for-the-badge) ![PAR Par 1 · Visão](https://img.shields.io/badge/PAR-Par%201%20·%20Visão-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../../README.md) → [Personas](../OVERVIEW.md) → **Product Owner**

> **Para quem é isto?** Para a pessoa que vai vestir esta persona durante o workshop.
>
> **O que você terá ao final desta leitura:**
>
> 1. Lista de arquivos do kit (agente, prompts, skills, MCP)
> 2. Como instalar no `.github/` do repo do time
> 3. Boas práticas específicas desta persona
>
> 📘 **Antes de mais nada:** abra o `PERSONA.md` desta pasta para ver missão, atributos e prompts prontos.


<!-- markdownlint-disable MD013 MD022 MD031 MD032 MD060 -->

> Escrita de especificação, refinamento de backlog e validação de aceite usando EARS e o fluxo SDD.

Leia primeiro: [PERSONA.md](PERSONA.md).

## Fase do SDLC
Descoberta → Especificação → Aceite

## Conteúdo do kit

| Arquivo | Tipo | Propósito |
|------|------|---------|
| `PERSONA.md` | Persona | Responsabilidades, passagens, prompts e rubrica do Product Owner |
| `.github/agents/product-owner.agent.md` | Agent | Assistente de Product Owner para spec, backlog e aceite |
| `.github/prompts/spec.prompt.md` | Prompt | `/spec` — escreve uma seção de `SPECIFICATION.md` a partir de user stories em EARS |
| `.github/prompts/update-spec.prompt.md` | Prompt | `/update-spec` — atualiza a spec quando uma feature muda |
| `.github/prompts/acceptance-check.prompt.md` | Prompt | `/acceptance-check` — verifica se o código atende aos critérios de aceite |
| `mcp.json` | MCP | Manifesto de servidores GitHub + Azure DevOps work items |

## Instalação
```bash
cp -r .github/* /path/to/your-repo/.github/
[ -f hooks.json ] && cp hooks.json /path/to/your-repo/
[ -f mcp.json ] && cp mcp.json /path/to/your-repo/.vscode/
```

## Boas práticas
- Escreva requisitos em EARS para que cada frase seja testável.
- Mantenha cada user story ligada a um resultado mensurável.
- Marque suposições explicitamente; suposição escondida vira bug de produção.
- Trate `CONSTITUTION.md` como fonte de verdade para itens inegociáveis.

## Referências
- [EARS Notation - Alistair Mavin](https://alistairmavin.com/ears/)
- [Spec-Driven Development (Spec-Kit)](https://github.com/github/spec-kit)
- [User Story Mapping - Jeff Patton](https://www.jpattonassociates.com/user-story-mapping/)
- [GitHub Copilot for PMs](https://docs.github.com/en/copilot)

---

## Navegação

| Anterior | Início |
|--------|------|
| [Persona Kits](../README.md) | [Kit PT-BR](../../README.md) |

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="../OVERVIEW.md"><strong>OVERVIEW</strong></a><br/>
<sub>Tabela das 10 personas.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="PERSONA.md"><strong>PERSONA.md</strong></a><br/>
<sub>Ficha desta persona.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>

