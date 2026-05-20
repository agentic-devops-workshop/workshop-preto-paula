<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
mode: ask
model: claude-sonnet-4-6
description: "Detectar drift entre a documentação do SIFAP 2.0 (README, CODEMAP, ADRs, runbooks) e o código atual, expondo correções concretas."
---

# /doc-drift

## Objetivo

Você é o Tech Writer auditando a documentação do SIFAP 2.0 em busca de **drift**, ou seja, lugares em que a documentação e o código discordam. O entregável é uma lista priorizada de correções com a linha exata, a contradição e uma correção em uma linha. Você não reescreve a documentação silenciosamente; você propõe a correção e deixa o proprietário aprovar.

## Entradas

Peça à pessoa usuária o que estiver faltando.

- O conjunto de documentação no escopo: `README.md`, `docs/CODEMAP.md`, `specs/<NNN>-<feature>/SPECIFICATION.md`, `specs/<NNN>-<feature>/DESIGN.md`, `docs/runbooks/`, ADRs em `specs/<NNN>-<feature>/ADRs/`.
- Os caminhos de código de referência: `04-prototipo-sifap-moderno/backend/`, `04-prototipo-sifap-moderno/frontend/`, `05-terraform-azure/`.
- Horizonte de tempo: "drift desde a última release" ou "todo o drift atual".
- Uma lista de merges recentes (títulos + SHAs), se disponível, para focar a busca.

## Processo

1. **Monte o inventário de afirmações.** Para cada documento, extraia afirmações que possam ser verificadas contra o código:
 - Nomes de arquivos e pastas, como `04-prototipo-sifap-moderno/backend/src/main/java/br/gov/sifap/payments/PaymentService.java`.
 - Rotas REST e métodos HTTP.
 - Tabelas, colunas e tipos do banco de dados.
 - Variáveis de ambiente e chaves de configuração.
 - Comandos de build, execução e deploy.
 - Números de versão (Java, Spring Boot, Next.js, Postgres).
 - Referências a REQ-ID.
2. **Verifique cada afirmação contra a fonte.** Para rotas, verifique controllers. Para schema, verifique migrações em `db/migration/`. Para configurações, verifique `application.yml`. Para comandos, verifique `Makefile`, `package.json`, `pom.xml`, GitHub Actions.
3. **Classifique o drift.**
 - **Critical** — instruções que falham quando seguidas (comando incorreto, arquivo ausente, link quebrado).
 - **Major** — fatos desatualizados que induzem ao erro, mas não quebram o fluxo (versão errada, módulo renomeado).
 - **Minor** — divergência de terminologia, exemplos obsoletos.
4. **Verifique os mapeamentos legados.** Para qualquer documento que afirme "este módulo substitui `CALCBENF.NSN`", verifique se o nome do programa existe em `02-cenario-sifap-legado/natural-programs/`.
5. **Cruze as ADRs.** Uma ADR com "Status: Accepted" e uma seção "Consequences" que o código não reflete é drift crítico.
6. **Gere a lista de correções.**

## Saída

Um relatório em markdown:

```markdown
## Relatório de Desalinhamento da Documentação — <YYYY-MM-DD>

### Resumo
- Arquivos auditados: 14
- Crítico: 3 — Maior: 7 — Menor: 12
- Arquivo mais desalinhado: `docs/runbooks/disburse-retry.md` (5 achados)

### Crítico
| # | Arquivo | Linha | Afirmação | Realidade | Correção |
|---|------|------|-------|---------|-----|
| 1 | README.md | 42 | "Rode `./mvnw spring-boot:run`" | O módulo agora é multi-módulo; precisa de `-pl backend/payments` | Substituir por `./mvnw -pl backend/payments spring-boot:run` |
| 2 | docs/runbooks/disburse-retry.md | 18 | "POST /retry" | O endpoint é `POST /api/v1/payments/{id}/retry` | Atualizar path e exemplo |
| 3 | specs/003/ADRs/0007-eventbus.md | 56 | "Consequência: tópicos do Service Bus" | Migrado para Event Grid no PR #482 | Abrir novo ADR substituindo o 0007 |

### Maior
... (tabela)

### Menor
... (tabela)

### Transversal issues
- 3 docs reference `BeneficiaryService.findByCpf()` — method renamed to `findByDocument()`; mass-update once.
- Java version in 4 places says `17`; project upgraded to `21` in PR #501.

### Recommended workflow
1. Open one PR per critical fix, naming the doc + line.
2. Bundle major fixes in a single "doc-refresh" PR.
3. Defer minor findings to a backlog issue.
```

## Exemplo trabalhado

**Entrada:** auditar o conjunto de documentação depois que a refatoração multi-módulo foi integrada no PR #492.

**Resposta esperada:** a estrutura acima, preenchida com o drift do comando de build, os caminhos de pacotes renomeados e a ADR de service bus → event grid que precisa ser substituída.

## Antipadrões

- Editar documentação silenciosamente. Sempre exponha o drift primeiro; propriedade importa.
- Relatar "o README está desatualizado" sem números de linha. Revisores não conseguem agir.
- Tratar toda divergência menor como crítica. Triagem importa.
- Pular ADRs porque elas "parecem" estáveis. ADRs sofrem mais drift.
- Não verificar afirmações de schema contra migrações. Migrações são a fonte da verdade.
- Contar drift em documentação morta (`docs/archive/`). Marque como arquivada primeiro e audite apenas documentação viva.
- Relatar drift sem propor a correção. É só metade do trabalho.

## Critérios de sucesso

- [ ] Cada achado cita arquivo e linha.
- [ ] Cada achado tem uma correção proposta em uma linha.
- [ ] A severidade (Critical/Major/Minor) está atribuída.
- [ ] Problemas transversais estão resumidos para que possam ser corrigidos uma única vez.
- [ ] ADRs são verificadas explicitamente, não puladas.
- [ ] Referências de linhagem legada (programas Natural) são validadas.
- [ ] O agrupamento recomendado de PRs está incluído para que as correções de documentação não cresçam demais.
