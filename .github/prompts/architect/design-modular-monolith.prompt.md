---
description: "Produz um design de alto nível para o Modular Monolith com base nos bounded contexts e na spec EARS."
mode: ask
model: claude-opus-4-7
tools: ['codebase', 'search']
---

# /design-modular-monolith

## Objetivo

Produza o design de alto nível do Modular Monolith: estrutura de packages Java, interfaces de módulos, comunicação cross-context, um diagrama C4 component e um esqueleto OpenAPI listando endpoints por bounded context.

## Quando Invocar

Depois que os bounded contexts forem decididos e a spec EARS estiver escrita. Normalmente este é o último prompt do Estágio 2.

## Pré-condições

- `02-spec-moderna/bounded-contexts.md` existe com contextos finalizados
- `02-spec-moderna/SPECIFICATION.md` existe com requisitos EARS
- Pelo menos 1 ADR existe documentando escolhas-chave de design

## Entradas que a Equipe Deve Fornecer

- O nome base do package Java (por exemplo, `com.datacorp.sifap`)
- Preferência de estilo de comunicação inter-context: somente interfaces, domain events ou misto
- Quaisquer restrições não funcionais (por exemplo, "deve suportar 1000 usuários concorrentes" — se declaradas na spec)

## O Que Vou Fazer

- Definir a estrutura de packages Java (um package top-level por contexto)
- Especificar a interface pública de cada módulo de contexto
- Definir mecanismos de comunicação cross-context
- Gerar um diagrama Mermaid C4 component
- Gerar um esqueleto OpenAPI com pelo menos um endpoint por contexto

## O Que NÃO Vou Fazer

- Sugerir microservices — isto é um único Modular Monolith implantável
- Escrever código de implementação — isso pertence ao Estágio 3
- Fabricar requisitos não funcionais ausentes na spec
- Pular a fronteira inter-context — toda chamada cross-context deve passar por interfaces definidas

## Formato de Saída

Dois arquivos:
1. `02-spec-moderna/modular-monolith-design.md` — documento de design com diagrama Mermaid
2. `02-spec-moderna/openapi.yaml` — OpenAPI 3.0 skeleton

```markdown
# Design do Modular Monolith
## Estrutura de Packages
## Interfaces de Módulo
## Comunicação Cross-Context
## Diagrama C4 Component (Mermaid)
## Resumo de Endpoints
## ADRs Relacionados
```

## Definição de Pronto

- [ ] A estrutura de packages mapeia 1:1 para bounded contexts
- [ ] Cada contexto tem uma interface pública definida (assinaturas de interface Java)
- [ ] Comunicação cross-context é especificada (mecanismo + dados trocados)
- [ ] O diagrama Mermaid C4 component renderiza corretamente
- [ ] O esqueleto OpenAPI tem pelo menos 1 endpoint por contexto com método, path e resumo
- [ ] O design referencia ADRs e requisitos EARS relacionados

## Corpo do Prompt

Você é o `@architect-agent`. A equipe tem bounded contexts e uma especificação EARS. Agora você desenhará a estrutura do Modular Monolith.

**Passo 1 — Definir estrutura de packages.**
Leia `02-spec-moderna/bounded-contexts.md`. Para cada contexto, crie um package Java:

```
[base-package]/
├── [context-1]/         # Bounded context 1
│   ├── api/             # REST controllers (public)
│   ├── domain/          # Entities, value objects (internal)
│   ├── service/         # Business logic (internal)
│   └── repository/      # Data access (internal)
├── [context-2]/         # Bounded context 2
│   └── ...
└── shared/              # Transversal: audit, exceptions, base types
```

Somente `api/` e interfaces explicitamente exportadas são públicas. Todo o resto é interno ao módulo.

**Passo 2 — Definir interfaces de módulo.**
Para cada bounded context, defina a interface pública — o que outros contextos podem chamar:
- Liste cada assinatura de método com tipos de parâmetros e tipos de retorno
- Use Java records para DTOs que cruzam fronteiras de contexto
- Use `Optional` para retornos anuláveis

Apresente dois padrões e deixe a equipe escolher:
1. **Direct interface**: Context A chama diretamente a interface de service do Context B (mais simples, acoplamento mais forte)
2. **Domain events**: Context A publica um evento, Context B assina (acoplamento mais fraco, mais complexidade)

Documente a escolha da equipe.

**Passo 3 — Mapear requisitos para endpoints.**
Leia `02-spec-moderna/SPECIFICATION.md`. Para cada requisito que implique uma operação user-facing ou API-facing, defina um endpoint REST:
- HTTP method (GET, POST, PUT, DELETE)
- Path (seguindo convenções RESTful)
- Resumo (1 linha)
- Tipo de request body (se aplicável)
- Tipo de response body
- REQ-ID relacionado

Agrupe endpoints por bounded context.

**Passo 4 — Desenhar o diagrama C4 component.**
Crie um diagrama Mermaid no nível C4 Component (Level 3) mostrando:
- Cada bounded context como um container
- Componentes-chave dentro de cada container (controllers, services, repositories)
- Relacionamentos entre containers (com setas rotuladas)
- Sistemas externos (database, frontend, external APIs)

Use a paleta de cores do kit: fill `#0f172a`, stroke `#334155`, text `#e2e8f0`.

**Passo 5 — Gerar esqueleto OpenAPI.**
Escreva um arquivo YAML OpenAPI 3.0 com:
- Seção Info (title, version, description)
- Paths para cada endpoint identificado no Passo 3
- Schemas request/response como referências (detalhes de schema podem ser preenchidos no Estágio 3)
- Tags organizadas por bounded context

Isto é um esqueleto — define assinaturas, não implementações. O `@builder-agent` completará os detalhes.

**Passo 6 — Fazer cross-reference com ADRs.**
Liste todos os ADRs de `02-spec-moderna/ADRs/` relacionados ao design. Para cada ADR, anote qual parte do design ele afeta.

**Passo 7 — Escrever arquivos de saída.**
Escreva o documento de design em `02-spec-moderna/modular-monolith-design.md` e o esqueleto OpenAPI em `02-spec-moderna/openapi.yaml`.

Este design é o blueprint para o Estágio 3. Ele deve ser específico o suficiente para o `@builder-agent` gerar código, mas abstrato o suficiente para permitir flexibilidade de implementação.

## Exemplo de Invocação

```
/design-modular-monolith package=com.datacorp.app communication=mixed
```
