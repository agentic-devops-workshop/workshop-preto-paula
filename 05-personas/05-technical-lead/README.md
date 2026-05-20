<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Technical Lead — Kit Copilot

![PERSONA Technical Lead](https://img.shields.io/badge/PERSONA-Technical%20Lead-7FBA00?style=for-the-badge) ![MARIO 🟥 Mario](https://img.shields.io/badge/MARIO-🟥%20Mario-1A1A1A?style=for-the-badge) ![PAR Par 3 · Implementação](https://img.shields.io/badge/PAR-Par%203%20·%20Implementação-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../../README.md) → [Personas](../OVERVIEW.md) → **Technical Lead**

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

> Context engineering, roteamento de modelos, quality gates e orquestração técnica.

Leia primeiro: [PERSONA.md](PERSONA.md).

## Fase do SDLC
Todas as fases (coordenação técnica)

## Conteúdo do kit

| Arquivo | Tipo | Propósito |
|------|------|---------|
| `PERSONA.md` | Persona | Responsabilidades, passagens, prompts e rubrica do Technical Lead |
| `.github/agents/tech-lead.agent.md` | Agent | Governança técnica |
| `.github/prompts/setup-project.prompt.md` | Prompt | `/setup-project` |
| `.github/prompts/routing-table.prompt.md` | Prompt | `/routing-table` |
| `.github/prompts/audit-context.prompt.md` | Prompt | `/audit-context` |
| `hooks.json` | Hooks | Escopo, lint e testes |

## Instalação
```bash
cp -r .github/* /path/to/your-repo/.github/
[ -f hooks.json ] && cp hooks.json /path/to/your-repo/
[ -f mcp.json ] && cp mcp.json /path/to/your-repo/.vscode/
```

## Boas práticas
- Bloqueie mudanças ruins, não pessoas; revise o PR e proteja o tempo de quem revisa.
- `CODEMAP.md` é memória de trabalho do time; se está desatualizado, o time voa às cegas.
- Roteamento de modelo importa: Opus para descoberta, Sonnet para implementação, Haiku para transformações mecânicas.
- Custo por feature é métrica de engenharia; acompanhe junto com cobertura.

## Referências
- [Staff Engineer - Will Larson](https://staffeng.com/)
- [The Manager's Path - Camille Fournier](https://www.oreilly.com/library/view/the-managers-path/9781491973882/)
- [Accelerate - Forsgren, Humble, Kim](https://itrevolution.com/product/accelerate/)
- [GitHub Copilot Best Practices](https://docs.github.com/en/copilot)

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

