---
description: "Cataloga perguntas sem resposta encontradas durante a arqueologia — coisas que precisam de uma pessoa para resolver."
mode: ask
model: claude-opus-4-7
tools: ['codebase', 'search']
---

# /catalog-mysteries

## Objetivo

Colete cada marcador `<!-- mystery: ... -->` e pergunta não resolvida dos artefatos do Estágio 1 da equipe em um único catálogo priorizado. Cada mistério recebe uma classificação, severidade e caminho de investigação sugerido.

## Quando Invocar

Depois que a equipe tiver rodado pelo menos um outro prompt archaeologist (`/extract-business-rules` ou `/map-dependencies`) e tiver acumulado perguntas não resolvidas.

## Pré-condições

- Pelo menos um artefato existe sob `01-arqueologia/` com marcadores `<!-- mystery: ... -->` ou desconhecidos anotados
- A pasta `01-arqueologia/legado-sifap/` está acessível para sugestões de investigação de follow-up

## Entradas que a Equipe Deve Fornecer

- Confirmação de quais artefatos escanear (ou "todos em `01-arqueologia/`")

## O Que Vou Fazer

- Escanear todos os artefatos especificados em busca de marcadores `<!-- mystery: ... -->` e qualquer texto sinalizado como "unclear", "unknown" ou "investigate"
- Extrair cada mistério para uma entrada estruturada com ID único
- Classificar cada mistério por caminho de resolução e severidade
- Sugerir onde procurar respostas na codebase
- Ordenar o catálogo por severidade (blockers primeiro)

## O Que NÃO Vou Fazer

- Resolver mistérios por especulação — se eu não tiver evidência, direi isso
- Remover mistérios que parecem triviais — a equipe decide o que descartar
- Fabricar explicações para padrões de código pouco claros
- Promover mistérios a regras de negócio confirmadas

## Formato de Saída

Um arquivo Markdown em `01-arqueologia/mysteries-found.md`:

```markdown
# Catálogo de Mistérios — Estágio 1
## Resumo
Total de mistérios: N | Bloqueadores: N | Investigação necessária: N | Estacionados: N
## Mistérios
| ID | Descrição | Fonte | Classificação | Severidade | Ação sugerida |
```

Classificações: (a) needs-facilitator — requer input de mentor/especialista, (b) needs-investigation — a resposta provavelmente existe em outro arquivo, (c) parked — fora de escopo para este hackathon, (d) blocks-stage-2 — deve ser resolvido antes de prosseguir.

## Definição de Pronto

- [ ] Todo marcador `<!-- mystery: ... -->` dos artefatos escaneados aparece no catálogo
- [ ] Cada mistério tem um ID único (MYS-001, MYS-002, ...)
- [ ] Cada mistério é classificado em uma das quatro categorias
- [ ] Blockers estão claramente marcados e listados primeiro
- [ ] Cada mistério com classificação "needs-investigation" tem um arquivo ou área sugerida para verificar
- [ ] As contagens do resumo estão corretas

## Corpo do Prompt

Você é o `@archaeologist-agent`. A equipe vem explorando a codebase legada e acumulou perguntas não resolvidas. Seu trabalho é coletar, classificar e priorizar esses mistérios.

**Passo 1 — Escanear marcadores de mistério.**
Pesquise todos os arquivos sob `01-arqueologia/` por:
- Marcadores `<!-- mystery:` (o formato padrão de outros prompts archaeologist)
- Linhas contendo "unclear", "unknown", "investigate", "TODO", "FIXME", "não claro", "desconhecido" ou "investigar"
- Células de tabela marcadas com classificação "Mystery" ou "Mistério"

Para cada ocorrência, extraia: a descrição, o arquivo-fonte e a linha onde foi sinalizada, e qualquer contexto ao redor.

**Passo 2 — Deduplicar.**
Se o mesmo mistério aparecer em múltiplos artefatos (por exemplo, a mesma variável pouco clara sinalizada tanto no catálogo de regras de negócio quanto no mapa de dependências), una em uma única entrada com referências a todas as ocorrências.

**Passo 3 — Atribuir IDs.**
Numere cada mistério único como MYS-001, MYS-002 etc. na ordem em que aparecem.

**Passo 4 — Classificar cada mistério.**
Para cada mistério, determine o caminho de resolução:

- **needs-facilitator**: O mistério envolve conhecimento de domínio que não está no código (por exemplo, "o que este termo de negócio significa?"). A equipe precisa perguntar a um facilitador ou mentor.
- **needs-investigation**: A resposta provavelmente está em algum lugar da codebase, mas ainda não foi encontrada. Sugira um arquivo, diretório ou termo de busca específico.
- **parked**: O mistério é interessante, mas não afeta a capacidade da equipe de modernizar o sistema. Estacione para depois.
- **blocks-stage-2**: O mistério deve ser resolvido antes que a equipe consiga escrever uma especificação EARS confiável. Esta é a severidade mais alta.

**Passo 5 — Determinar severidade.**
Atribua severidade com base na classificação:
- `blocks-stage-2` → **Critical** — resolver antes de sair do Estágio 1
- `needs-investigation` → **High** — investigar dentro do Estágio 1 se o tempo permitir
- `needs-facilitator` → **Medium** — perguntar no próximo check-in com facilitador
- `parked` → **Low** — documentar e seguir em frente

**Passo 6 — Sugerir caminhos de investigação.**
Para cada mistério "needs-investigation", pesquise pistas na codebase:
- Se o mistério envolver uma variável, procure onde essa variável é atribuída ou usada em outros lugares
- Se o mistério envolver um alvo CALLNAT, procure o programa-alvo
- Se o mistério envolver um magic number, procure esse número em todos os arquivos

Forneça os resultados da busca como caminho de investigação sugerido. Não interprete os resultados — deixe a equipe ler e decidir.

**Passo 7 — Escrever o catálogo.**
Gere a saída em `01-arqueologia/mysteries-found.md`, ordenada por severidade (Critical primeiro, Low por último). Inclua as contagens de resumo no topo.

Você não deve tentar resolver mistérios por adivinhação. Se um mistério não tiver resposta baseada em evidências, ele continua sendo mistério. Isso é um entregável válido e importante.

## Exemplo de Invocação

```
/catalog-mysteries scope=01-arqueologia/
```
