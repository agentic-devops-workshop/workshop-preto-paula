<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Tech Writer — Kit do Copilot

![PERSONA Tech Writer](https://img.shields.io/badge/PERSONA-Tech%20Writer-1A1A1A?style=for-the-badge) ![MARIO 🎺 Bardo](https://img.shields.io/badge/MARIO-🎺%20Bardo-1A1A1A?style=for-the-badge) ![PAR Par 5 · Operações](https://img.shields.io/badge/PAR-Par%205%20·%20Operações-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../../README.md) → [Personas](../OVERVIEW.md) → **Tech Writer**

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

> Documentação de API, README, `CODEMAP.md`, changelog e detecção de drift.

Leia primeiro: [PERSONA.md](PERSONA.md).

## Fase do SDLC
Transversal, documentação, evolução

## Conteúdo do kit

| Arquivo | Tipo | Propósito |
|------|------|---------|
| `PERSONA.md` | Persona | Responsabilidades, passagens, prompts e rubrica do Tech Writer |
| `.github/agents/tech-writer.agent.md` | Agent | Documentação de API, README, `CODEMAP.md`, changelog e drift |
| `.github/prompts/generate-docs.prompt.md` | Prompt | `/generate-docs` |
| `.github/prompts/update-codemap.prompt.md` | Prompt | `/update-codemap` |
| `.github/prompts/doc-drift.prompt.md` | Prompt | `/doc-drift` |

## Instalação
```bash
cp -r .github/* /path/to/your-repo/.github/
[ -f hooks.json ] && cp hooks.json /path/to/your-repo/
[ -f mcp.json ] && cp mcp.json /path/to/your-repo/.vscode/
```

## Boas práticas
- Documentação é funcionalidade: entregue, versione e revise junto com o código.
- Escreva para quem tem 30 segundos; comece pela resposta e depois dê contexto.
- Diagramas comprimem explicações longas; use Mermaid e mantenha atualizado.
- Documentação obsoleta é pior que ausência; inclua verificação de drift no CI.

## Referências
- [Diátaxis Framework](https://diataxis.fr/)
- [Google Developer Documentation Style Guide](https://developers.google.com/style)
- [Write the Docs](https://www.writethedocs.org/)
- [Mermaid - Diagramming as Code](https://mermaid.js.org/)

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

