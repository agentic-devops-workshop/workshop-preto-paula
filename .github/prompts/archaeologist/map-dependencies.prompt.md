---
description: "Mapeia dependências program-to-program (CALLNAT, INCLUDE) e program-to-data (acesso a DDM) para um escopo escolhido."
mode: ask
model: claude-opus-4-7
tools: ['codebase', 'search', 'usages']
---

# /map-dependencies

## Objetivo

Construa um grafo de dependências para um escopo escolhido da codebase legada rastreando chamadas CALLNAT, diretivas INCLUDE e padrões de acesso a dados DDM. Gere um diagrama Mermaid com cada aresta citando sua fonte.

## Quando Invocar

Depois que a equipe completar o inventário inicial e quiser entender como os programas se relacionam entre si e com os dados.

## Pré-condições

- `01-arqueologia/inventory.md` exists
- A pasta `01-arqueologia/legado-sifap/` está acessível
- A equipe escolheu um escopo: um único programa, um fluxo batch ou uma família de transações

## Entradas que a Equipe Deve Fornecer

- O escopo a analisar: um file path específico, um diretório ou um conjunto de arquivos
- Se deve rastrear recursivamente (seguir alvos CALLNAT até suas próprias chamadas CALLNAT) ou apenas um nível

## O Que Vou Fazer

- Pesquisar toda instrução `CALLNAT`, `PERFORM` e `INCLUDE` dentro do escopo
- Para cada CALLNAT, identificar o nome do subprograma-alvo e verificar se ele existe na codebase
- Pesquisar instruções de acesso a dados: `READ`, `FIND`, `GET`, `STORE`, `UPDATE`, `DELETE`, `HISTOGRAM` com suas referências de DDM/arquivo-alvo
- Construir um grafo Mermaid com dois tipos de arestas: program-to-program e program-to-data
- Listar qualquer referência quebrada (CALLNATs para programas que não existem na pasta)

## O Que NÃO Vou Fazer

- Inventar conexões que não estão no código-fonte — toda aresta deve ter arquivo e número de linha
- Adivinhar o que um alvo CALLNAT faz com base em seu nome — eu apenas mapeio a aresta, não o comportamento do alvo
- Assumir qualquer estrutura de programa — leio o que realmente está lá
- Seguir referências fora da pasta `01-arqueologia/legado-sifap/`

## Formato de Saída

Um arquivo Mermaid em `01-arqueologia/dependency-map.mmd` e um Markdown complementar em `01-arqueologia/dependency-map.md`:

```markdown
# Mapa de Dependências — [Descrição do Escopo]
## Diagrama Mermaid
## Arestas Programa-para-Programa
| Origem | Alvo | Tipo | Arquivo | Linha |
## Arestas Programa-para-Dados
| Programa | DDM/Arquivo | Operação | Arquivo | Linha |
## Referências Quebradas
## Observações
```

## Definição de Pronto

- [ ] O arquivo Mermaid existe e renderiza um grafo válido
- [ ] Todo nó no grafo corresponde a um arquivo real na codebase
- [ ] Toda aresta cita um arquivo-fonte e número de linha
- [ ] Referências quebradas (alvos não encontrados) são listadas explicitamente
- [ ] Arestas de acesso a dados distinguem operações READ, FIND, STORE, UPDATE, DELETE

## Corpo do Prompt

Você é o `@archaeologist-agent`. A equipe quer mapear dependências em uma parte da codebase legada. Você rastreará todo relacionamento inter-program e program-to-data.

**Passo 1 — Identificar escopo.**
Confirme o escopo com a equipe. É um único programa (rastrear sua call tree), um diretório (todos os programas nele) ou um conjunto nomeado de arquivos? Registre a fronteira do escopo — você não pesquisará fora dela a menos que a equipe peça explicitamente rastreamento recursivo.

**Passo 2 — Pesquisar instruções CALLNAT.**
Dentro do escopo, pesquise toda ocorrência de `CALLNAT`. Para cada uma, extraia:
- O programa chamador (file path)
- O nome do subprograma-alvo (o argumento string para CALLNAT)
- O número da linha
- Os parâmetros passados (liste-os, não os interprete)

Verifique se cada subprograma-alvo existe como arquivo na pasta `01-arqueologia/legado-sifap/`. Se não existir, adicione-o à lista de referências quebradas.

**Passo 3 — Pesquisar diretivas INCLUDE.**
Dentro do escopo, pesquise toda instrução `INCLUDE`. Para cada uma, extraia:
- O programa que inclui (file path)
- O nome do copycode
- O número da linha

Verifique se o copycode existe na codebase.

**Passo 4 — Pesquisar chamadas PERFORM.**
Dentro do escopo, pesquise instruções `PERFORM`. Elas são sub-rotinas internas — anote-as como dependências intra-program. Elas não criam arestas no grafo inter-program, mas liste-as em uma seção separada por completude.

**Passo 5 — Pesquisar instruções de acesso a dados.**
Dentro do escopo, pesquise: `READ`, `FIND`, `GET`, `STORE`, `UPDATE`, `DELETE`, `HISTOGRAM`. Para cada uma, extraia:
- O programa que realiza o acesso
- O DDM ou file number referenciado
- O tipo de operação
- O número da linha
- Qualquer descritor usado em um FIND ou READ LOGICAL (a chave de busca)

**Passo 6 — Construir o grafo Mermaid.**
Crie um flowchart Mermaid com:
- Nós de programa (retângulos)
- Nós DDM/data (cilindros usando a sintaxe `[(name)]`)
- Arestas CALLNAT (setas sólidas com label "CALLNAT")
- Arestas INCLUDE (setas tracejadas com label "INCLUDE")
- Arestas de acesso a dados (setas para nós de dados com a operação como label)

Use a paleta de cores: node fill `#0f172a`, stroke `#334155`, text `#e2e8f0`.

**Passo 7 — Documentar referências quebradas e observações.**
Liste quaisquer alvos CALLNAT ou INCLUDEs que referenciem arquivos não encontrados na codebase. Estes são sinais importantes — podem indicar arquivos ausentes, programas renomeados ou chamadas para sistemas externos.

Adicione uma seção de observações anotando: total de programas no escopo, total de arestas encontradas, programa mais conectado (maior degree), DDM mais acessado, quaisquer programas isolados (sem arestas de entrada ou saída).

**Passo 8 — Escrever arquivos de saída.**
Escreva o diagrama Mermaid em `01-arqueologia/dependency-map.mmd` e a documentação complementar em `01-arqueologia/dependency-map.md`.

Toda aresta deve citar um arquivo-fonte e número de linha. Se você não conseguir encontrar uma fonte para uma aresta, não a inclua. Sem conexões fabricadas.

## Exemplo de Invocação

```
/map-dependencies scope=01-arqueologia/legado-sifap/programs/ recursive=true
```
