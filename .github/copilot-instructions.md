# Instruções do GitHub Copilot — Workshop de Modernização de Legado

> Estas instruções dizem ao Copilot o que sua equipe está construindo, qual stack usar,
> quais convenções seguir e o que NÃO fazer. Elas se aplicam a todo o repositório
> da equipe.

## Ferramentas Aprovadas — Somente Estas

Este workshop roda com uma **toolchain fixa**. Usar qualquer outra coisa fragmenta a equipe e quebra as demos.

| Use estas | Por quê |
|-----------|-----|
| **VS Code** (ou VS Code Insiders) | Editor único para toda a equipe. O devcontainer + extensões assumem isso. |
| **GitHub Copilot** (modos Ask + Plan + Agent) | Assistente de IA principal. Copilot Workspace também é permitido para delegação Issue → PR. |
| **GitHub Copilot CLI** *(opcional)* | Para tarefas em fluxo de terminal. As mesmas regras de roteamento de modelo se aplicam. |
| **GitHub Spec-Kit** (`Specify CLI` + `/speckit.*`) | Toolkit oficial de Spec-Driven Development para especificação, planejamento, tarefas e implementação. |
| **GitHub** (Issues, PRs, Actions, Projects) | Fonte da verdade para trabalho, código e CI. |
| **Docker / Docker Compose** | Paridade do ambiente local. |
| **Terraform** | IaC (Azure provider). |

**Não use** outros assistentes de IA, IDEs ou frameworks de SDD durante o workshop. Especificamente:

- ❌ Sem Cursor, Windsurf, Antigravity, Codex, Cline, Continue, Aider, Codeium, Tabnine ou outros concorrentes do Copilot
- ❌ Sem UIs web de chat para geração de código (seus prompts e saídas devem permanecer rastreáveis pelo histórico do Copilot Chat)
- ❌ Sem IntelliJ, Eclipse, Sublime ou plugins do Neovim como editor principal
- ❌ Sem frameworks alternativos de SDD (Kiro, pipelines YAML customizados etc.). O GitHub Spec-Kit oficial é permitido e obrigatório.

Por que isso é rígido: o workshop mede **engenharia agentic com a stack oficial Microsoft + GitHub**. Misturar ferramentas quebra os passagens da equipe, fragmenta a rubrica e torna impossível validar o rastreamento de spec → code → test.

## Contexto do Projeto

Este repositório pertence a uma equipe do workshop que está modernizando o legado **SIFAP**
(Sistema de Fiscalização e Administração de Pagamentos) de Natural/Adabas para
uma stack moderna. O workspace de referência fica em
[`workshop-legacy-modernization-datacorp`](https://github.com/paulasilvatech/workshop-legacy-modernization-datacorp).

## Duas Camadas de Agentes — Ambas Obrigatórias

Este kit inclui **duas camadas complementares de agentes**. Elas não são duplicadas; cobrem eixos ortogonais (role × stage). Use ambas.

```mermaid
flowchart TB
    classDef person fill:#E5F6FD,stroke:#00A4EF,color:#0A0A0A
    classDef stage fill:#FFF7E0,stroke:#FFB900,color:#0A0A0A
    classDef tool fill:#F1F8E3,stroke:#7FBA00,color:#0A0A0A

    P1[Pessoa · Par 1<br/>2 personas]:::person
    P2[Pessoa · Par 2<br/>2 personas]:::person
    P3[Pessoa · Par 3<br/>2 personas]:::person
    P4[Pessoa · Par 4<br/>2 personas]:::person
    P5[Pessoa · Par 5<br/>2 personas]:::person

    PK[05-personas/NN-*/<br/>agente Copilot + prompts + skills + MCP<br/>por persona, o dia todo]:::tool
    AK[06-agentes-de-estagio/<br/>@archaeologist · @architect ·<br/>@builder · @evolution<br/>por estágio, com tempo definido]:::tool

    P1 & P2 & P3 & P4 & P5 -- "copiar os 2 kits próprios para .github/" --> PK
    P1 & P2 & P3 & P4 & P5 -- "selecionar no Copilot chat para o estágio atual" --> AK
```

### `05-personas/NN-*/` — instalado uma vez, roda o dia todo

- Um kit por persona (Product Owner, Requirements Engineer, …, Tech Writer).
- Cada kit contém: 1 `agent.md` + 2–4 `prompts/*.prompt.md` + 1–2 `skills/*/SKILL.md` + `instructions/*.instructions.md` opcional + `mcp.json`.
- Cada pessoa da equipe copia **ambos** os seus kits de persona para a pasta `.github/` do repositório da equipe via `cp -r 05-personas/XX-*/.github/* .github/`.
- Depois disso, o Copilot fica configurado para o papel dessa pessoa durante todo o dia. Slash commands (`/spec`, `/ears-convert`, …) ficam disponíveis.

### `06-agentes-de-estagio/` — selecionado novamente a cada estágio

- Um agente por estágio: `@archaeologist` (Estágio 1) · `@architect` (Estágio 2) · `@builder` (Estágio 3) · `@evolution` (Estágio 4).
- Compartilhado por toda a equipe. Define uma "persona protagonista" + "personas secundárias" + "personas observadoras" para aquele estágio.
- Ativado no Copilot Chat: abra o painel de chat, clique no seletor de agente, escolha o agente do estágio. Cole o prompt de abertura do README daquele kit.
- O agente orienta a **coordenação da equipe** durante o estágio: quais artefatos produzir, como percorrer o código legado, quando escalar etc.

### Como elas se combinam em uma janela típica de 30 minutos

1. **Você** abriu o Copilot Chat com seu persona-kit carregado → slash commands como `/ears-convert` funcionam porque sua pasta `.github/prompts/` está populada.
2. **Sua equipe** selecionou o agente de estágio `@archaeologist` → o chat entra em modo de arqueologia, guiando a leitura do legado.
3. Você pede ao `@archaeologist` para resumir o que `BATCHPGT.NSN` faz → ele responde no enquadramento de arqueologia.
4. Em seguida, você roda `/ears-convert` sobre suas descobertas → seu persona-kit (RE) assume e gera YAML com linhas `source_legacy:`.
5. As duas camadas trabalharam juntas. Nenhuma é opcional.

## Stack-Alvo

- **Backend:** Java 21 + Spring Boot 3.3 + JPA/Hibernate + PostgreSQL 16
- **Frontend:** Next.js 15 (App Router) + TypeScript 5 (strict) + Tailwind CSS + shadcn/ui
- **Containers:** Docker + Docker Compose
- **IaC:** Terraform (Azure provider ~> 3.x)
- **CI/CD:** GitHub Actions
- **Testing:** JUnit 5 + Testcontainers (backend); Vitest + Testing Library (frontend)

## Regras de Geração de Código

### Java
- Use recursos do Java 21: records para DTOs, sealed interfaces para uniões discriminadas, pattern matching, virtual threads
- Use `Optional` corretamente — nunca retorne `null` de métodos públicos
- `@Transactional` somente na camada de service, nunca em repositories
- Valide entradas na camada de controller com `@Valid` + Bean Validation
- Nomes de classes em inglês; comentários em inglês
- Testes unitários são obrigatórios para lógica de negócio
- Nunca exponha dados sensíveis (CPF, valores de benefício) em logs — mascare-os

### TypeScript / Next.js
- `strict: true` em `tsconfig.json` — sem exceções
- Use server actions para mutations; nunca exponha secrets em client components
- Prefira `async/await` a cadeias `.then()`
- Somente named exports — sem default exports em arquivos de componentes

### REST APIs
- Convenção de path: `/api/v1/{resource}`
- Use verbos HTTP corretamente (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`)
- Retorne status codes apropriados (`201` para criação, `204` para sem conteúdo, `409` para conflito)
- Todos os endpoints devem ter annotations OpenAPI/Swagger

### Terraform
- Todo recurso deve ter `tags` incluindo `project`, `environment`, `owner`
- Secrets somente via `azurerm_key_vault_secret` — nunca em `locals` ou `variables`
- Um módulo por área de serviço Azure (networking, compute, database, monitoring)
- `terraform fmt` e `terraform validate` devem passar antes do commit

## Regras de Segurança (OWASP Top 10)

- Valide entradas em toda fronteira do sistema
- Nunca faça hardcode de secrets, API keys ou credenciais
- Consultas SQL somente via JPA/JPQL — sem concatenação de strings
- CORS configurado explicitamente — sem wildcard `*` em produção
- Autenticação via OAuth2/JWT (Spring Security no backend)
- Todos os recursos Azure usam Managed Identity para autenticação serviço-a-serviço

## Spec-Driven Development (Spec-Kit)

- Todo requisito usa **notação EARS** (Easy Approach to Requirements Syntax)
- Todo requisito tem um **REQ-ID** único no formato `REQ-NNN`
- **Todo requisito carrega uma linha `source_legacy:`** apontando para `01-arqueologia/legado-sifap/natural-programs/*.NSN`, `01-arqueologia/legado-sifap/adabas-ddms/*.ddm` ou `[GREENFIELD] + justificativa`. O job de CI `legacy-traceability` rejeita PRs que violam isso. Consulte [`01-arqueologia/LEGACY-EXPLORATION-CHECKLIST.md`](../01-arqueologia/LEGACY-EXPLORATION-CHECKLIST.md).
- Testes rastreiam para REQ-IDs por comentários inline
- Estratégia de branch: uma branch por spec, nomeada `spec/<NNN>-<feature>`
- Ordem de merge: `spec/*` → `develop` → `stage` → `main`

## Exploração do Legado — OBRIGATÓRIA

Antes de escrever qualquer EARS no Estágio 2, seu par DEVE ter lido os programas Natural atribuídos em [`01-arqueologia/legado-sifap/natural-programs/`](../01-arqueologia/legado-sifap/natural-programs/) e os DDMs em [`01-arqueologia/legado-sifap/adabas-ddms/`](../01-arqueologia/legado-sifap/adabas-ddms/). Specs escritas sem leitura do legado perdem 29 anos de regras de negócio. A matriz de HARD GATE fica em [`01-arqueologia/LEGACY-EXPLORATION-CHECKLIST.md`](../01-arqueologia/LEGACY-EXPLORATION-CHECKLIST.md).

## Os 5 Pares e 10 Personas

> **Cada equipe tem 5 pessoas. Cada pessoa veste 2 personas (1 par).** Não há passagem interna entre personas de um par — colaboração contínua. Atualize os nomes abaixo com as atribuições da sua equipe.

| Par | Persona A | Persona B | Fase do SDLC |
|------|-----------|-----------|------------|
| 1 · Visão | [ ] 01 Product Owner — nome? | [ ] 02 Requirements Engineer — nome? | Descoberta + Especificação |
| 2 · Arquitetura | [ ] 03 Enterprise Architect — nome? | [ ] 04 Software Architect — nome? | Especificação + Design |
| 3 · Implementação | [ ] 05 Technical Lead — nome? | [ ] 06 Developer — nome? | Implementação + Evolução |
| 4 · Qualidade | [ ] 07 DBA — nome? | [ ] 08 QA Engineer — nome? | Implementação (dados + testes) |
| 5 · Operações | [ ] 09 DevOps Engineer — nome? | [ ] 10 Tech Writer — nome? | Transversal + Evolução |

Veja [`00-TEAM-FLOW.md`](../00-TEAM-FLOW.md) para diagramas de passagem e a linha do tempo do dia.

## Como Usar o Copilot (3 modos)

| Modo | Quando usar | Exemplo |
|------|-------------|---------|
| **Chat** | Explorar, planejar, debater trade-offs | "Explique este programa Natural linha por linha" |
| **Plan** | Planejamento de mudanças multi-arquivo antes da execução | "Planeje o bounded context `notification` com domain/application/infrastructure" |
| **Agent** | Delegar features completas via Issue | "Implemente REQ-PAY-03: geração de ciclo com audit log" |

> Veja [`09-cheat-sheets/copilot-3-modes.md`](../09-cheat-sheets/copilot-3-modes.md) para
> orientações detalhadas e exemplos de prompts.

## Regras Rígidas — Não Faça Isto

- ❌ Não gere código sem antes verificar o protótipo existente em `prototype/` (symlink criado por `11-scripts/setup.sh`)
- ❌ Não escreva um EARS sem `source_legacy:` — o CI rejeitará o PR
- ❌ Não adicione dependências sem justificativa em um ADR
- ❌ Não escreva testes depois do fato — escreva-os enquanto implementa
- ❌ Não exponha secrets em mensagens de commit, logs ou descrições de PR
- ❌ Não faça merge em `main` sem pelo menos uma revisão entre pares
- ❌ Não pule as conversas guiadas de passagem nas transições de estágio (veja [`00-TEAM-FLOW.md`](../00-TEAM-FLOW.md))

## Referências

- [Team Flow](../00-TEAM-FLOW.md) — linha do tempo diária, passagens, escalonamento
- [Persona Kits](../05-personas/) — seus 2 cartões de papel em `PERSONA.md`, além de agentes, prompts e skills do Copilot (copie seus 2 kits para esta `.github/`)
- [Cheat Sheets](../09-cheat-sheets/) — Copilot, Spec-Kit, roteamento de modelo
- [Legado Incluído](../01-arqueologia/legado-sifap/) — o que você está modernizando (15 programas .NSN + 4 DDMs)
- [`01-arqueologia/LEGACY-EXPLORATION-CHECKLIST.md`](../01-arqueologia/LEGACY-EXPLORATION-CHECKLIST.md) — HARD GATE antes do Estágio 2
- Protótipo de Referência (`prototype/`) — starter code em execução (symlink, criado por setup.sh)
- Módulos de infraestrutura (`infra/`) — módulos Terraform Azure (symlink, criado por setup.sh)
- [Spec-Kit SDD Plugin](https://github.com/github/spec-kit) — engine de Spec-Driven Development
- Docs didáticos trilíngues: [`pt-br/`](../) · [`es/`](../../es/) · [`en/`](../../en/)
