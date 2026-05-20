<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Software Architect — Kit Copilot

![PERSONA Software Architect](https://img.shields.io/badge/PERSONA-Software%20Architect-00A4EF?style=for-the-badge) ![MARIO 🔷 Daisy](https://img.shields.io/badge/MARIO-🔷%20Daisy-1A1A1A?style=for-the-badge) ![PAR Par 2 · Arquitetura](https://img.shields.io/badge/PAR-Par%202%20·%20Arquitetura-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../../README.md) → [Personas](../OVERVIEW.md) → **Software Architect**

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

> `CODEMAP.md`, desenho de módulos, contratos de API e planejamento de implementação.

Leia primeiro: [PERSONA.md](PERSONA.md).

## Fase do SDLC
Desenho, Supervisão da Implementação

## Conteúdo do kit

| Arquivo | Tipo | Propósito |
|------|------|---------|
| `PERSONA.md` | Persona | Responsabilidades, passagens, prompts e rubrica do Software Architect |
| `.github/agents/software-architect.agent.md` | Agent | Arquitetura |
| `.github/prompts/codemap.prompt.md` | Prompt | `/codemap` |
| `.github/prompts/impl-plan.prompt.md` | Prompt | `/impl-plan` |
| `.github/prompts/api-validate.prompt.md` | Prompt | `/api-validate` |
| `.github/instructions/backend.instructions.md` | Instructions | Convenções backend |
| `.github/instructions/frontend.instructions.md` | Instructions | Convenções frontend |

## Instalação
```bash
cp -r .github/* /path/to/your-repo/.github/
[ -f hooks.json ] && cp hooks.json /path/to/your-repo/
[ -f mcp.json ] && cp mcp.json /path/to/your-repo/.vscode/
```

## Boas práticas
- Prefira composição a herança, fronteiras claras a abstrações genéricas e dados claros a código esperto.
- Contratos de API são compromisso público; quebre apenas com versão e guia de migração.
- Mantenha regra de negócio fora do banco e do framework.
- Pasta `util` crescendo costuma indicar bounded context ausente.

## Referências
- [Clean Arquitetura - Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Domain-Driven Design - Eric Evans](https://www.domainlanguage.com/ddd/)
- [Hexagonal Arquitetura - Alistair Cockburn](https://alistair.cockburn.us/hexagonal-architecture/)
- [Microsoft .NET Arquitetura Guides](https://learn.microsoft.com/dotnet/architecture/)

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

