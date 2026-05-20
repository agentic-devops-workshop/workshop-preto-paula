<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Enterprise Architect — Kit Copilot

![PERSONA Enterprise Architect](https://img.shields.io/badge/PERSONA-Enterprise%20Architect-00A4EF?style=for-the-badge) ![MARIO 🌟 Rosalina](https://img.shields.io/badge/MARIO-🌟%20Rosalina-1A1A1A?style=for-the-badge) ![PAR Par 2 · Arquitetura](https://img.shields.io/badge/PAR-Par%202%20·%20Arquitetura-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../../README.md) → [Personas](../OVERVIEW.md) → **Enterprise Architect**

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

> `CONSTITUTION.md`, ADRs, revisões arquiteturais e governança de segurança.

Leia primeiro: [PERSONA.md](PERSONA.md).

## Fase do SDLC
Arquitetura, Desenho, Segurança

## Conteúdo do kit

| Arquivo | Tipo | Propósito |
|------|------|---------|
| `PERSONA.md` | Persona | Responsabilidades, passagens, prompts e rubrica do Enterprise Architect |
| `.github/agents/enterprise-architect.agent.md` | Agent | Arquitetura e segurança |
| `.github/prompts/create-constitution.prompt.md` | Prompt | `/create-constitution` |
| `.github/prompts/create-adr.prompt.md` | Prompt | `/create-adr` |
| `.github/prompts/architecture-review.prompt.md` | Prompt | `/architecture-review` |
| `.github/instructions/security.instructions.md` | Instructions | Convenções de segurança |
| `.github/instructions/infrastructure.instructions.md` | Instructions | Convenções de IaC |
| `hooks.json` | Hooks | Bloqueios de edição para `CONSTITUTION.md` |

## Instalação
```bash
cp -r .github/* /path/to/your-repo/.github/
[ -f hooks.json ] && cp hooks.json /path/to/your-repo/
[ -f mcp.json ] && cp mcp.json /path/to/your-repo/.vscode/
```

## Boas práticas
- Use C4 L1/L2 para visão executiva e L3/L4 para implementação.
- Toda decisão arquitetural precisa de ADR com contexto, decisão e consequências.
- Prefira arquitetura previsível e operável em produção.
- Use os pilares do Azure Well-Architected como gates de revisão, não como checklist tardio.

## Referências
- [C4 Model - Simon Brown](https://c4model.com/)
- [Microsoft Azure Well-Architected Framework](https://learn.microsoft.com/azure/well-architected/)
- [ADR - Arquitetura Decision Records](https://adr.github.io/)
- [Azure Arquitetura Center](https://learn.microsoft.com/azure/architecture/)

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

