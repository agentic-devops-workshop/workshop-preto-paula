<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Developer — Kit Copilot

![PERSONA Developer](https://img.shields.io/badge/PERSONA-Developer-7FBA00?style=for-the-badge) ![MARIO 🟩 Luigi](https://img.shields.io/badge/MARIO-🟩%20Luigi-1A1A1A?style=for-the-badge) ![PAR Par 3 · Implementação](https://img.shields.io/badge/PAR-Par%203%20·%20Implementação-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../../README.md) → [Personas](../OVERVIEW.md) → **Developer**

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

> Implementação, TDD, correção de bugs e ciclo entender → reproduzir → corrigir → verificar.

Leia primeiro: [PERSONA.md](PERSONA.md).

## Fase do SDLC
Implementação, Evolução

## Conteúdo do kit

| Arquivo | Tipo | Propósito |
|------|------|---------|
| `PERSONA.md` | Persona | Responsabilidades, passagens, prompts e rubrica do Developer |
| `.github/agents/implementer.agent.md` | Agent | Implementação, TDD e correção de bugs |
| `.github/prompts/implement.prompt.md` | Prompt | `/implement` |
| `.github/prompts/fix-bug.prompt.md` | Prompt | `/fix-bug` |
| `.github/prompts/tdd.prompt.md` | Prompt | `/tdd` |
| `.github/prompts/refactor.prompt.md` | Prompt | `/refactor` |

## Instalação
```bash
cp -r .github/* /path/to/your-repo/.github/
[ -f hooks.json ] && cp hooks.json /path/to/your-repo/
[ -f mcp.json ] && cp mcp.json /path/to/your-repo/.vscode/
```

## Boas práticas
- Escreva teste antes quando o design estiver claro; escreva junto quando estiver explorando. Sempre commit com testes.
- Um PR deve contar uma história: um assunto, pequeno o bastante para revisar em cerca de 20 minutos.
- Refatore em commit separado da mudança de comportamento.
- Comentários explicam o porquê; código explica o quê.

## Referências
- [Clean Code - Robert C. Martin](https://www.oreilly.com/library/view/clean-code-a/9780136083238/)
- [Refactoring - Martin Fowler](https://refactoring.com/)
- [Test-Driven Development - Kent Beck](https://www.oreilly.com/library/view/test-driven-development/0321146530/)
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

