<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# QA Engineer — Kit Copilot

![PERSONA Qa Engineer](https://img.shields.io/badge/PERSONA-Qa%20Engineer-FFB900?style=for-the-badge) ![MARIO 🐢 Koopa](https://img.shields.io/badge/MARIO-🐢%20Koopa-1A1A1A?style=for-the-badge) ![PAR Par 4 · Qualidade](https://img.shields.io/badge/PAR-Par%204%20·%20Qualidade-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../../README.md) → [Personas](../OVERVIEW.md) → **Qa Engineer**

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

> Geração de testes a partir de specs, análise de cobertura e gates de qualidade.

Leia primeiro: [PERSONA.md](PERSONA.md).

## Fase do SDLC
Implementação, Qualidade, Evolução

## Conteúdo do kit

| Arquivo | Tipo | Propósito |
|------|------|---------|
| `PERSONA.md` | Persona | Responsabilidades, passagens, prompts e rubrica do QA Engineer |
| `.github/agents/qa-engineer.agent.md` | Agent | Geração de testes, cobertura e qualidade |
| `.github/prompts/create-tests.prompt.md` | Prompt | `/create-tests` |
| `.github/prompts/coverage-gaps.prompt.md` | Prompt | `/coverage-gaps` |
| `.github/prompts/test-strategy.prompt.md` | Prompt | `/test-strategy` |
| `.github/instructions/tests.instructions.md` | Instructions | Convenções de teste |

## Instalação
```bash
cp -r .github/* /path/to/your-repo/.github/
[ -f hooks.json ] && cp hooks.json /path/to/your-repo/
[ -f mcp.json ] && cp mcp.json /path/to/your-repo/.vscode/
```

## Boas práticas
- Use pirâmide de testes: maioria unitária, parte integração, poucos end-to-end.
- Teste instável é teste quebrado: isole, corrija ou remova.
- Cobertura é necessária, mas não suficiente; teste sem asserção não prova comportamento.
- Todo requisito deve ter ao menos um teste, e todo teste deve apontar para um requisito.

## Referências
- [Google Testing Blog](https://testing.googleblog.com/)
- [xUnit Test Patterns - Gerard Meszaros](http://xunitpatterns.com/)
- [Software Testing ISTQB](https://www.istqb.org/)
- [Property-Based Testing - jqwik/fast-check](https://jqwik.net/)

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

