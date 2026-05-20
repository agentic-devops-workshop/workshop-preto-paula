---
description: "Avalia as hipóteses de recorte do Estágio 1 e decide bounded contexts para o Modular Monolith."
mode: ask
model: claude-opus-4-7
tools: ['codebase', 'search']
---

# /carve-bounded-contexts

## Objetivo

Transforme as hipóteses de recorte do relatório de descoberta do Estágio 1 em bounded contexts avaliados e decididos. Cada contexto recebe nome, responsabilidades, dados próprios e regras de comunicação inter-context.

## Quando Invocar

No início do Estágio 2, imediatamente após revisar o relatório de descoberta do Estágio 1.

## Pré-condições

- `01-arqueologia/discovery-report.md` existe com pelo menos 3 hipóteses de recorte
- A equipe revisou o relatório de descoberta e está pronta para tomar decisões arquiteturais

## Entradas que a Equipe Deve Fornecer

- Path para o relatório de descoberta
- Quaisquer restrições ou preferências adicionais (por exemplo, "queremos manter pagamentos e inscrição separados")

## O Que Vou Fazer

- Ler as hipóteses de recorte do relatório de descoberta
- Avaliar cada hipótese contra três critérios: coesão, acoplamento e frequência de mudança
- Apresentar a análise à equipe para cada hipótese
- Documentar rejeições com raciocínio
- Formalizar contextos aceitos com nomes, responsabilidades e ownership de dados

## O Que NÃO Vou Fazer

- Decidir automaticamente quais hipóteses aceitar — a equipe toma a decisão final
- Propor microservices — isto é um Modular Monolith
- Fabricar contexto de negócio para as hipóteses — trabalho apenas com o que o Estágio 1 descobriu
- Pular os critérios de avaliação — toda hipótese recebe a análise completa

## Formato de Saída

Um arquivo Markdown em `02-spec-moderna/bounded-contexts.md`:

```markdown
# Mapa de Bounded Contexts
## Critérios de Avaliação
## Avaliação de Hipóteses
### [Nome da Hipótese] — ACEITA / REJEITADA
## Bounded Contexts Finais
### [Nome do Contexto]
- Responsabilidade:
- Dados próprios (DDMs/tabelas):
- Interface pública:
- Por que é um contexto próprio:
## Comunicação Inter-Context
## Diagrama Mermaid do Mapa de Contexto
```

## Definição de Pronto

- [ ] Toda hipótese do relatório de descoberta é avaliada contra os três critérios
- [ ] Hipóteses rejeitadas têm raciocínio documentado
- [ ] 2-5 bounded contexts são finalizados com nomes em linguagem de negócio
- [ ] Cada contexto tem: parágrafo de responsabilidade, lista de dados próprios, esboço de interface pública
- [ ] Um diagrama Mermaid de context map mostra relacionamentos entre contextos
- [ ] Nenhum contexto é uma ilha isolada — caminhos de comunicação são definidos

## Corpo do Prompt

Você é o `@architect-agent`. A equipe está começando o Estágio 2 e precisa decidir bounded contexts para o Modular Monolith.

**Passo 1 — Ler o relatório de descoberta.**
Abra `01-arqueologia/discovery-report.md`. Extraia a seção de hipóteses de recorte. Liste cada hipótese com nome, programas incluídos, DDMs próprios e racional.

**Passo 2 — Avaliar contra três critérios.**
Para cada hipótese, analise:

**Coesão** — As regras de negócio neste grupo se relacionam à mesma capacidade de negócio? Verifique revisando as regras confirmadas de `01-arqueologia/business-rules-catalog.md` que pertencem a esse grupo. Alta coesão = candidato forte.

**Acoplamento** — Quantas dependências cruzam esta fronteira? Verifique o dependency map em `01-arqueologia/dependency-map.md`. Conte arestas que cruzariam entre este contexto e outros. Baixo acoplamento = candidato forte. Alto acoplamento sugere que a fronteira pode estar no lugar errado.

**Frequência de mudança** — No sistema legado, quais programas deste grupo provavelmente eram modificados juntos? Use padrões de nomes de arquivos e relacionamentos de chamada como proxies. Programas que se chamam intensamente provavelmente mudam juntos e pertencem ao mesmo contexto.

Apresente cada avaliação como scorecard: High/Medium/Low para cada critério.

**Passo 3 — Apresentar à equipe para decisão.**
Para cada hipótese, apresente:
- O scorecard
- Uma recomendação (aceitar, rejeitar ou mesclar com outra hipótese)
- O raciocínio

Então pergunte à equipe: "Vocês aceitam esta recomendação? Se não, o que mudariam?"

A equipe toma a decisão final. Se a equipe sobrescrever sua recomendação, documente o raciocínio dela.

**Passo 4 — Formalizar contextos aceitos.**
Para cada bounded context aceito, escreva:
- **Name**: Um nome em linguagem de negócio (por exemplo, "Payment Processing", não "payment-svc")
- **Responsibility**: 1 parágrafo descrevendo o que este contexto possui
- **Owned data**: Quais DDMs/tabelas pertencem exclusivamente a este contexto
- **Public interface**: Quais operações este contexto expõe a outros contextos (assinaturas de métodos ou nomes de eventos — não implementação)
- **Why it's its own context**: 1 frase conectando aos critérios de avaliação

**Passo 5 — Definir comunicação inter-context.**
Para cada par de contextos que precisa se comunicar, especifique:
- A direção (A chama B, ou bidirecional)
- O mecanismo: chamada de método in-process via interface, domain event ou tipo de shared kernel
- Os dados trocados (somente IDs? DTOs completos? Events?)

Reforce: isto é um Modular Monolith. A comunicação é in-process, não via HTTP entre serviços.

**Passo 6 — Desenhar o context map.**
Crie um diagrama Mermaid mostrando todos os contextos como caixas, com setas rotuladas para relacionamentos de comunicação. Use a paleta de cores do kit: fill `#0f172a`, stroke `#334155`, text `#e2e8f0`.

**Passo 7 — Escrever a saída.**
Escreva em `02-spec-moderna/bounded-contexts.md`.

## Exemplo de Invocação

```
/carve-bounded-contexts report=01-arqueologia/discovery-report.md
```
