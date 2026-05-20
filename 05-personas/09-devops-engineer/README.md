<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# DevOps Engineer — Kit Copilot

![PERSONA Devops Engineer](https://img.shields.io/badge/PERSONA-Devops%20Engineer-1A1A1A?style=for-the-badge) ![MARIO 🍄 Captain Toad](https://img.shields.io/badge/MARIO-🍄%20Captain%20Toad-1A1A1A?style=for-the-badge) ![PAR Par 5 · Operações](https://img.shields.io/badge/PAR-Par%205%20·%20Operações-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../../README.md) → [Personas](../OVERVIEW.md) → **Devops Engineer**

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

> Pipelines CI/CD, IaC, observabilidade e resposta a incidentes.

Leia primeiro: [PERSONA.md](PERSONA.md).

## Fase do SDLC
Transversal, Evolução, Operações

## Conteúdo do kit

| Arquivo | Tipo | Propósito |
|------|------|---------|
| `PERSONA.md` | Persona | Responsabilidades, passagens, prompts e rubrica do DevOps Engineer |
| `.github/agents/devops-engineer.agent.md` | Agente | CI/CD, IaC, monitoramento e incidentes |
| `.github/prompts/pipeline.prompt.md` | Prompt | `/pipeline` |
| `.github/prompts/iac-module.prompt.md` | Prompt | `/iac-module` |
| `.github/prompts/incident-rca.prompt.md` | Prompt | `/incident-rca` |
| `.github/instructions/cicd.instructions.md` | Instruções | Convenções de CI/CD |
| `.github/instructions/infrastructure.instructions.md` | Instruções | Convenções de infraestrutura |

## Instalação
```bash
cp -r .github/* /path/to/your-repo/.github/
[ -f hooks.json ] && cp hooks.json /path/to/your-repo/
[ -f mcp.json ] && cp mcp.json /path/to/your-repo/.vscode/
```

## Boas práticas
- Tudo como código: infraestrutura, configuração, políticas e runbooks.
- Pipeline com mais de 10 minutos vira gargalo; paralelize ou reduza etapas.
- Secrets vivem em vault, não em `.env`, variáveis soltas de CI ou código-fonte.
- Blue/green e canary resolvem problemas diferentes; escolha pelo custo de rollback.

## Referências
- [Terraform Best Practices](https://developer.hashicorp.com/terraform/language/style)
- [GitHub Actions Hardening](https://docs.github.com/en/actions/security-for-github-actions/security-guides/security-hardening-for-github-actions)
- [Azure Verified Modules](https://azure.github.io/Azure-Verified-Modules/)
- [The DevOps Handbook - Gene Kim et al.](https://itrevolution.com/product/the-devops-handbook-second-edition/)

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

