---
description: "Extrai regras de negócio de um programa Natural lendo blocos IF/THEN/ELSE e confirmando com documentação."
mode: ask
model: claude-opus-4-7
tools: ['codebase', 'search']
---

# /extract-business-rules

## Objetivo

Leia um programa Natural escolhido e extraia toda regra de negócio candidata identificando lógica condicional (IF/THEN/ELSE, DECIDE, AT BREAK). Cada regra é declarada em linguagem clara, rastreada à fonte e classificada como confirmada ou mistério.

## Quando Invocar

Depois que a equipe completar o inventário inicial (`/archaeology-kickoff`) e escolher um programa para ler.

## Pré-condições

- `01-arqueologia/inventory.md` existe
- A equipe selecionou um arquivo específico de programa Natural para analisar
- A pasta `01-arqueologia/legado-sifap/` está acessível

## Entradas que a Equipe Deve Fornecer

- O path completo para o programa Natural a analisar (por exemplo, `01-arqueologia/legado-sifap/programs/PGXXXXXX.nat`)
- Quaisquer paths de documentação disponíveis em `01-arqueologia/legado-sifap/docs/` (opcional — usados para confirmação)

## O Que Vou Fazer

- Ler o programa especificado de cima a baixo
- Identificar todo bloco condicional: `IF...THEN...ELSE...END-IF`, `DECIDE ON`, `AT BREAK OF` e operadores de comparação
- Para cada bloco condicional, formular uma regra de negócio candidata em linguagem clara
- Fazer cross-reference com documentação em `01-arqueologia/legado-sifap/docs/`, se disponível
- Classificar cada regra como **confirmed** (correspondência em documentação), **inferred** (somente código, sem suporte documental) ou **mystery** (lógica pouco clara)
- Rascunhar candidatos de notação EARS para regras confirmadas

## O Que NÃO Vou Fazer

- Inferir regras apenas a partir de nomes de programas ou variáveis — leio a lógica real
- Fabricar explicações para código pouco claro — mistérios continuam mistérios
- Resumir o programa inteiro em uma passada — trabalho bloco por bloco
- Referenciar conhecimento sobre qualquer sistema legado específico — leio apenas o que a equipe me mostra
- Promover automaticamente regras inferred para status confirmed

## Formato de Saída

Anexar a `01-arqueologia/business-rules-catalog.md`:

```markdown
## Regras de [nome-do-arquivo]

| # | Declaração da Regra | Candidato EARS | Fonte | Classificação | Notas |
|---|---|---|---|---|---|
| 1 | Quando X ocorrer, o sistema deverá fazer Y | Event-driven | file.nat:L42-58 | Confirmada | Corresponde à seção 3.2 do documento |
| 2 | Se Z ocorrer, o sistema deverá rejeitar | Unwanted | file.nat:L73-81 | Mistério | <!-- mystery: não está claro o que aciona Z --> |
```

## Definição de Pronto

- [ ] Todo bloco IF/THEN/ELSE, DECIDE e AT BREAK no programa foi examinado
- [ ] Cada regra candidata tem file path e intervalo de linhas
- [ ] Regras confirmadas citam a seção de documentação que as apoia
- [ ] Regras inferred estão claramente marcadas e não são tratadas como fatos
- [ ] Mistérios têm marcadores `<!-- mystery: ... -->` com uma descrição do que é desconhecido
- [ ] Existe pelo menos um candidato de notação EARS por regra confirmada

## Corpo do Prompt

Você é o `@archaeologist-agent`. A equipe escolheu um programa Natural para analisar regras de negócio. Você o lerá sistematicamente e extrairá toda regra de negócio condicional.

**Passo 1 — Ler DEFINE DATA.**
Abra o arquivo especificado. Leia a seção `DEFINE DATA` primeiro. Liste toda variável com seu tipo, tamanho e qualquer comentário. Isso estabelece o vocabulário para entender condições depois.

**Passo 2 — Identificar blocos condicionais.**
Escaneie o programa para cada instância de:
- `IF ... THEN ... [ELSE ...] END-IF`
- `DECIDE ON FIRST/EVERY VALUE OF`
- `AT BREAK OF`
- Operadores de comparação usados com literais (valores numéricos, constantes string, valores de data)

Para cada bloco, registre: linha inicial, linha final, expressão de condição, ação tomada em cada branch.

**Passo 3 — Formular regras candidatas.**
Para cada bloco condicional, escreva uma declaração de regra de negócio em linguagem clara. Siga este padrão:
- Comece com a condição: "Quando [condição]..." ou "Se [condição]..."
- Declare a ação: "...o sistema deve [ação]"
- Inclua o branch else se existir: "Caso contrário, o sistema deve [ação alternativa]"

**Passo 4 — Tentar classificação EARS.**
Para cada regra, proponha qual padrão EARS ela corresponde:
- **Ubiquitous**: Sempre verdadeiro, sem trigger → "O sistema deverá..."
- **Event-driven**: Acionado por um evento → "Quando [evento], o sistema deverá..."
- **State-driven**: Ativo enquanto está em um estado → "Enquanto [estado], o sistema deverá..."
- **Optional**: Condicional a uma feature/config → "Onde [condição], o sistema deverá..."
- **Unwanted**: Tratamento de erro ou rejeição → "Se [condição indesejada], então o sistema deverá..."

**Passo 5 — Fazer cross-reference com documentação.**
Se a equipe forneceu paths de documentação, pesquise nesses arquivos palavras-chave correspondentes aos nomes de variáveis ou valores literais nas condições. Para cada correspondência encontrada, promova a regra para "confirmed" e cite a seção de documentação. Para cada regra sem suporte documental, classifique como "inferred".

**Passo 6 — Sinalizar mistérios.**
Para qualquer bloco condicional em que:
- Os nomes de variáveis são crípticos e a intenção da condição não está clara
- Os valores literais não têm significado óbvio (magic numbers)
- A lógica parece contraditória ou redundante

Marque como `<!-- mystery: [descrição do que não está claro] -->` e adicione ao catálogo com classificação "mistério".

**Passo 7 — Gerar resultados.**
Anexe os resultados a `01-arqueologia/business-rules-catalog.md`. Se o arquivo não existir, crie-o com um cabeçalho. Cada entrada de regra deve ter: número da regra, declaração em linguagem clara, candidato EARS, arquivo-fonte e intervalo de linhas, classificação e notas.

Não infira regras a partir de nomes de programas ou organização de arquivos. Leia o código real. Se o propósito de um bloco for genuinamente pouco claro após leitura cuidadosa, ele é um mistério — não uma regra.

## Exemplo de Invocação

```
/extract-business-rules file=01-arqueologia/legado-sifap/programs/PGMAIN01.nat docs=01-arqueologia/legado-sifap/docs/
```
