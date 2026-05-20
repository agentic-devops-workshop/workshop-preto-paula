---
description: "Sintetiza as saídas do Estágio 1 em um único relatório de descoberta pronto para passagem ao Estágio 2."
mode: ask
model: claude-opus-4-7
tools: ['codebase', 'search']
---

# /discovery-report

## Objetivo

Agregue todos os artefatos do Estágio 1 em um único relatório de descoberta que serve como documento de passagem para o Estágio 2. O relatório deve ser autocontido: qualquer pessoa que o leia deve entender o que a equipe encontrou sem precisar abrir artefatos individuais.

## Quando Invocar

Ao final do Estágio 1, depois que a equipe completar o inventário, extração de regras de negócio, mapeamento de dependências e catálogo de mistérios.

## Pré-condições

Todos os quatro artefatos do Estágio 1 devem existir:
- `01-arqueologia/inventory.md` (de `/archaeology-kickoff`)
- `01-arqueologia/business-rules-catalog.md` (de `/extract-business-rules`)
- `01-arqueologia/dependency-map.md` (de `/map-dependencies`)
- `01-arqueologia/mysteries-found.md` (de `/catalog-mysteries`)

Se qualquer artefato estiver ausente ou vazio, o agente recusará gerar o relatório e listará o que está faltando.

## Entradas que a Equipe Deve Fornecer

- Confirmação de que todos os quatro artefatos estão completos (ou reconhecimento das lacunas)
- O nome da equipe para o cabeçalho do relatório

## O Que Vou Fazer

- Verificar que todos os quatro artefatos de entrada existem e não estão vazios
- Escrever um resumo executivo (máximo de 5 frases) cobrindo o que foi encontrado
- Organizar findings em categorias "confirmado" e "arriscado"
- Propor 3-5 hipóteses de recorte de bounded context com base em clusters de dependências
- Cruzar mistérios com regras de negócio para identificar lacunas de maior risco

## O Que NÃO Vou Fazer

- Gerar o relatório se algum artefato de entrada estiver ausente — listarei o que é necessário
- Decidir bounded contexts — proponho hipóteses para o architect avaliar
- Preencher lacunas por adivinhação — se a equipe não encontrou algo, permanece não encontrado
- Adicionar nova análise além do que os artefatos contêm — sintetizo, não descubro

## Formato de Saída

Um arquivo Markdown em `01-arqueologia/discovery-report.md`:

```markdown
# Relatório de Descoberta — Estágio 1
## Resumo Executivo (máximo de 5 frases)
## O Que Sabemos (Confirmado)
### Regras de Negócio (somente confirmadas)
### Dependências (arestas verificadas)
### Estruturas de Dados (DDMs documentados)
## O Que Traz Risco
### Mistérios que Bloqueiam o Estágio 2
### Regras com Evidência Fraca
## Hipóteses de Recorte Recomendadas
### Hipótese 1: [Nome] — [racional de 1 linha]
...
## Artefatos-Fonte
## Aprovação da Equipe
```

## Definição de Pronto

- [ ] O relatório existe e tem menos de 3 páginas quando impresso
- [ ] O resumo executivo tem exatamente 5 frases ou menos
- [ ] Toda afirmação na seção "O Que Sabemos" referencia um artefato-fonte por path relativo
- [ ] Mistérios que bloqueiam o Estágio 2 estão listados com destaque com seus MYS-IDs
- [ ] 3-5 hipóteses de recorte são propostas, cada uma com nome e racional de 1 linha
- [ ] Hipóteses são explicitamente rotuladas como hipóteses, não decisões

## Corpo do Prompt

Você é o `@archaeologist-agent`. O Estágio 1 está terminando. A equipe precisa de um único documento que capture tudo que descobriu, pronto para o `@architect-agent` usar no Estágio 2.

**Passo 1 — Verificar entradas.**
Verifique se todos os quatro artefatos obrigatórios existem sob `01-arqueologia/`:
1. `inventory.md`
2. `business-rules-catalog.md`
3. `dependency-map.md`
4. `mysteries-found.md`

Se algum arquivo estiver ausente ou vazio, pare imediatamente. Liste os artefatos ausentes e diga à equipe qual prompt rodar para criá-los. Não prossiga com um relatório parcial.

**Passo 2 — Escrever o resumo executivo.**
Leia todos os quatro artefatos. Escreva exatamente 5 frases ou menos que respondam:
1. Qual é o tamanho da codebase legada? (programas, DDMs, linhas de código se contadas)
2. Quantas regras de negócio confirmadas foram encontradas?
3. Quão conectado é o sistema? (call graph denso vs. programas isolados)
4. Qual é o maior risco ao entrar no Estágio 2? (o mistério mais crítico)
5. Qual é o nível de confiança da equipe para a modernização? (alto/médio/baixo, com base em evidências)

**Passo 3 — Construir a seção "O Que Sabemos".**
Do catálogo de regras de negócio, extraia somente regras classificadas como "confirmed". Liste-as com seus candidatos de notação EARS e referências de fonte.

Do dependency map, liste arestas program-to-program e program-to-data verificadas. Inclua as contagens totais.

Do inventário, resuma as estruturas DDM documentadas.

Toda afirmação deve citar seu artefato-fonte: `[Veja business-rules-catalog.md, Regra #3](business-rules-catalog.md)`.

**Passo 4 — Construir a seção "O Que Traz Risco".**
Do catálogo de mistérios, extraia todos os mistérios classificados como "blocks-stage-2". Liste-os com seus MYS-IDs, descrições e caminhos de resolução sugeridos.

Do catálogo de regras de negócio, extraia regras classificadas como "inferred" (somente código, sem suporte documental). Elas não estão confirmadas — carregam risco se usadas como base para requisitos.

**Passo 5 — Propor hipóteses de recorte.**
Analise o dependency map em busca de clusters — grupos de programas fortemente conectados entre si e fracamente conectados a outros grupos. Cada cluster é um bounded context candidato.

Para cada hipótese, forneça:
- Um nome em linguagem de negócio (não jargão técnico)
- Quais programas pertencem a ela
- Quais DDMs ela possui
- Um racional de 1 linha para explicar por que esta é uma fronteira natural

Proponha 3-5 hipóteses. Rotule-as explicitamente como hipóteses, não decisões. O `@architect-agent` no Estágio 2 avaliará e decidirá.

**Passo 6 — Listar artefatos-fonte.**
No fim do relatório, liste todos os quatro artefatos-fonte com paths relativos para que qualquer pessoa possa navegar até os detalhes.

**Passo 7 — Adicionar sign-off da equipe.**
Adicione uma seção para sign-off da equipe: "Reviewed by: [names], Date: [date], Confidence: [high/medium/low]". Deixe em branco para a equipe preencher.

Escreva o relatório completo em `01-arqueologia/discovery-report.md`. O relatório deve ser autocontido e ter menos de 3 páginas quando impresso.

## Exemplo de Invocação

```
/discovery-report team="Team 07"
```
