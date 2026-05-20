<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Relatório de Coerência do Kit de Agentes — v1.0.0

![DOC Relatório interno](https://img.shields.io/badge/DOC-Relatório%20interno-737373?style=for-the-badge) ![TIPO Auditoria](https://img.shields.io/badge/TIPO-Auditoria-1A1A1A?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Docs](README.md) → **Coherence Report**

> **Para quem é isto?** Documentação transversal usada durante o workshop.
>
> **O que você terá ao final desta leitura:** contexto adicional sobre o tópico do título.


**Data:** 2026-04-29
**Escopo:** Todos os arquivos criados ou modificados nas ondas 1, 2 e 3.
**Veredito:** PASS — todas as 6 verificações passam. O kit está pronto para uso no hackathon.

## Resumo Executivo

O conjunto de ferramentas SDLC de 4 agentes é coerente em todas as camadas: agentes apontam para prompts, prompts apontam para templates, cartões de persona apontam para agent-kits, e a matriz persona-agente é consistente nos 4 locais em que aparece. Nenhum token proibido foi encontrado. Todos os arquivos têm YAML frontmatter completo com a string de author correta.

---

## 1. Verificação de Referências Cruzadas

| De                                              | Para                                        | Status                        |
| ----------------------------------------------- | ------------------------------------------- | ----------------------------- |
| `06-agentes-de-estagio/README.md` → agents                 | `.github/agents/*.agent.md`                 | ✅ Todos os 4 resolvem        |
| `06-agentes-de-estagio/*/README.md` → agents               | `.github/agents/*.agent.md`                 | ✅ Todos os 4 resolvem        |
| Arquivos de agent → 18 prompts                  | `.github/prompts/*/*.prompt.md`             | ✅ Todos os 18 resolvem       |
| `generate-adr.prompt.md` → template de ADR      | `02-spec-moderna/templates/ADR.template.md` | ✅ Resolve                    |
| 10 cartoes de persona → agent-kits              | `06-agentes-de-estagio/0N-*/README.md`                 | ✅ Todos os 40 links resolvem |
| `docs/persona-agent-matrix.md` → agent-kits     | `06-agentes-de-estagio/README.md`                      | ✅ Resolve                    |
| `docs/4-agents-explained.md` → todas as camadas | Agentes, instruções, matriz                 | ✅ Todas resolvem             |

**Resultado:** PASS — zero links quebrados.

## 2. Scan No-Silver-Platter

Tokens testados: `BENEFICIARIO`, `PROGRAMA-SOCIAL`, `PAGAMENTO`, `AUDITORIA`, `BATCHPGT`, `BATCHCON`, `BATCHREL`, `CADBENEF`, `CALCBENF`, `CALCCORR`, `CALCDSCT`, `CGPB`, `DEFIS`, `Roberto Carlos`, `Cláudia Regina`, FNR 150-153, `SF01`, `SF05`, `SF10`.

**Resultado:** PASS — zero ocorrências em todos os arquivos das Waves 1-3.

Exemplos de sessão usam apenas nomes fictícios: `XYZPROG1`, `XYZRATES`, `EMPL-MASTER`, `RATE-TABLE`, `PERIOD-CALENDAR`.

## 3. Consistência da Matriz Persona × Agent

Verificada em 4 locais:

| Local                                                     | Status                      |
| --------------------------------------------------------- | --------------------------- |
| `docs/persona-agent-matrix.md` (canonica)                 | ✅                          |
| `06-agentes-de-estagio/README.md` (mesma tabela)                     | ✅ Corresponde              |
| `06-agentes-de-estagio/*/README.md` "Who Uses" (recortes por agente) | ✅ Todos os 4 correspondem  |
| `05-personas/*/PERSONA.md` "Agents I Use" (recortes por persona) | ✅ Todos os 10 correspondem |

Atribuições de protagonista: Requirements Engineer → @archaeologist, Software Architect → @architect, Developer → @builder, Technical Lead → @evolution.

**Resultado:** PASS — todos os 4 locais concordam.

## 4. Consistência do Roteamento de Modelo

| Agent          | Agent               | Prompts                                       | Consistente?   |
| -------------- | ------------------- | --------------------------------------------- | -------------- |
| @archaeologist | `claude-opus-4-7`   | Todos os 5: `claude-opus-4-7`                 | ✅             |
| @architect     | `claude-opus-4-7`   | Todos os 4: `claude-opus-4-7`                 | ✅             |
| @builder       | `claude-sonnet-4-6` | Todos os 5: `claude-sonnet-4-6`               | ✅             |
| @evolution     | `claude-sonnet-4-6` | 2× `claude-sonnet-4-6`, 2× `claude-haiku-4-5` | ✅ Documentado |

Exceções Haiku: `write-github-issue` (rascunho estruturado) e `final-experience-report` (formatação das respostas da equipe) — ambas são tarefas compactas de preenchimento de template.

**Resultado:** PASS.

## 5. Cobertura de DoD (Prompt → Modelo)

| Prompt                                         | Modelo                                                        | Status |
| ---------------------------------------------- | ------------------------------------------------------------- | ------ |
| `/archaeology-kickoff`                         | `01-arqueologia/templates/inventory.template.md`              | ✅     |
| `/extract-business-rules`                      | `01-arqueologia/templates/business-rules-catalog.template.md` | ✅     |
| `/map-dependencies`                            | `01-arqueologia/templates/dependency-map.template.mmd`        | ✅     |
| `/catalog-mysteries`                           | `01-arqueologia/templates/mysteries-found.template.md`        | ✅     |
| `/discovery-report`                            | `01-arqueologia/templates/discovery-report.template.md`       | ✅     |
| `/carve-bounded-contexts`                      | `02-spec-moderna/templates/bounded-contexts.template.md`      | ✅     |
| `/generate-adr`                                | `02-spec-moderna/templates/ADR.template.md`                   | ✅     |
| `/final-experience-report`                     | `04-evolucao/templates/agent-experience-report.template.md`   | ✅     |
| Builder prompts (5)                            | N/A — output is code                                          | ⚪     |
| Remaining evolution prompts (3)                | N/A — format defined inline                                   | ⚪     |
| `/write-ears-spec`, `/design-modular-monolith` | N/A — format defined inline                                   | ⚪     |

**Resultado:** PASS — 8 templates cobrem todas as saídas de artefatos estruturados.

## 6. Consistência de Estilo

- **YAML frontmatter:** Completo em todos os novos arquivos (title, description, author, date, version, status, tags).
- **String de author:** `Paula Silva, AI-Native Software Engineer, Americas Global Black Belt at Microsoft` — consistente em todos os lugares.
- **Data:** `2026-04-29` — consistente em todos os arquivos da Wave 3.
- **Linhas de navegação:** Presentes nos READMEs dos agent-kits e nos docs.
- **Contagens de linhas:** Exemplos 200-300 linhas, templates 30-95 linhas, adições de persona ~10 linhas cada — tudo dentro dos limites.

**Resultado:** PASS.

---

## Resumo

| #   | Verificação                 | Resultado                                      |
| --- | --------------------------- | ---------------------------------------------- |
| 1   | Links de referência cruzada | ✅ PASS                                        |
| 2   | Scan no-silver-platter      | ✅ PASS — zero tokens proibidos                |
| 3   | Matriz Persona × Agent      | ✅ PASS — consistente em 4 locais              |
| 4   | Roteamento de modelo        | ✅ PASS — 2 exceções Haiku documentadas        |
| 5   | DoD → cobertura de modelo | ✅ PASS — 8 templates para saídas estruturadas |
| 6   | Consistência de estilo      | ✅ PASS                                        |

**Veredito geral: PASS.** O kit está na v1.0.0 e pronto para congelamento de funcionalidades.

## Itens Adiados

Nenhum. O kit está completo para o hackathon.

---

| Anterior                                      | Início                             | Próximo                                          |
| --------------------------------------------- | ---------------------------------- | ------------------------------------------------ |
| [4 Agentes Explicados](4-agents-explained.md) | [Início do Team Kit](../README.md) | [Matriz Persona-Agente](persona-agent-matrix.md) |


---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="FAQ.md"><strong>FAQ</strong></a><br/>
<sub>Perguntas frequentes.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="troubleshooting.md"><strong>Troubleshooting</strong></a><br/>
<sub>Erros comuns.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>

