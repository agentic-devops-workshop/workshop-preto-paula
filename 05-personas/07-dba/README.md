<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# DBA — Kit Copilot

![PERSONA Dba](https://img.shields.io/badge/PERSONA-Dba-FFB900?style=for-the-badge) ![MARIO 🦖 Yoshi](https://img.shields.io/badge/MARIO-🦖%20Yoshi-1A1A1A?style=for-the-badge) ![PAR Par 4 · Qualidade](https://img.shields.io/badge/PAR-Par%204%20·%20Qualidade-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../../README.md) → [Personas](../OVERVIEW.md) → **Dba**

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

> Migrações, otimização de consultas e auditoria contra SQL injection.

Leia primeiro: [PERSONA.md](PERSONA.md).

## Fase do SDLC
Implementação, Qualidade

## Conteúdo do kit

| Arquivo | Tipo | Propósito |
|------|------|---------|
| `PERSONA.md` | Persona | Responsabilidades, passagens, prompts e rubrica do DBA |
| `.github/agents/dba.agent.md` | Agent | Migrações, otimização de consultas e auditoria SQL |
| `.github/prompts/migration.prompt.md` | Prompt | `/migration` |
| `.github/prompts/query-audit.prompt.md` | Prompt | `/query-audit` |
| `.github/instructions/database.instructions.md` | Instructions | Convenções de banco de dados |

## Instalação
```bash
cp -r .github/* /path/to/your-repo/.github/
[ -f hooks.json ] && cp hooks.json /path/to/your-repo/
[ -f mcp.json ] && cp mcp.json /path/to/your-repo/.vscode/
```

## Boas práticas
- Índices aceleram leitura e desaceleram escrita; meça os dois lados.
- Migrações precisam ser compatíveis por pelo menos dois deploys: expandir, depois contrair.
- Consultas N+1 são bug de performance; detecte antes de staging.
- Backup que nunca foi restaurado não é backup validado.

## Referências
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Use the Index, Luke - Markus Winand](https://use-the-index-luke.com/)
- [High Performance MySQL / PostgreSQL - Schwartz et al.](https://www.oreilly.com/)
- [Azure Database for PostgreSQL Best Practices](https://learn.microsoft.com/azure/postgresql/)

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

