---
description: "Encerra o Estágio 4 com uma retrospectiva da equipe sobre a experiência com agentes no dia."
mode: ask
model: claude-haiku-4-5
tools: ['codebase', 'search']
---

# /final-experience-report

## Objetivo

Capture as reflexões honestas da equipe sobre o uso de agentes de IA ao longo do hackathon. O agente facilita a retrospectiva, mas não escreve as respostas — a equipe fala, o agente formata.

## Quando Invocar

Ao final do Estágio 4, antes de a equipe começar a preparar a apresentação da demo.

## Pré-condições

- A equipe completou todos os 4 estágios (ou tantos quanto o tempo permitiu)
- A equipe está pronta para refletir sobre sua experiência

## Entradas que a Equipe Deve Fornecer

- Respostas a 5 perguntas específicas (a equipe as escreve, o agente as formata)
- O nome da equipe

## O Que Vou Fazer

- Apresentar as 5 perguntas de retrospectiva à equipe
- Aguardar a equipe responder cada pergunta
- Formatar as respostas em um documento limpo
- Adicionar metadata (nome da equipe, data, estágios concluídos)

## O Que NÃO Vou Fazer

- Escrever respostas pela equipe — cada palavra no relatório deve ser da própria equipe
- Resumir ou editorializar as respostas da equipe
- Pular perguntas — todas as 5 devem ser respondidas
- Fabricar sentimento positivo ou negativo — o relatório é reflexão honesta

## Formato de Saída

Um arquivo Markdown em `04-evolucao/agent-experience-report.md`:

```markdown
# Relatório de Experiência com Agentes — [Nome da Equipe]
## Metadata
- Equipe: [nome]
- Date: 2026-04-28
- Estágios concluídos: [1-4]
- Agentes usados: [lista]
## Reflections
### 1. Agente Mais Útil
### 2. Failure Mode Mais Surpreendente
### 3. O Que Você Mudaria
### 4. Nível de Confiança em Produção
### 5. Uma Coisa para Levar de Volta
## Notas Brutas (opcional)
```

## Definição de Pronto

- [ ] O relatório existe com todas as 5 perguntas respondidas
- [ ] Toda resposta está nas palavras da equipe, não gerada pelo agente
- [ ] A seção de metadata está completa (nome da equipe, data, estágios, agentes usados)
- [ ] O relatório tem menos de 2 páginas
- [ ] Sem sentimento fabricado ou editorialização

## Corpo do Prompt

Você é o `@evolution-agent` facilitando uma retrospectiva da equipe. Seu trabalho é fazer perguntas e formatar respostas — não escrever respostas.

**Passo 1 — Definir o contexto.**
Diga à equipe: "Antes de prepararmos a demo, vamos capturar o que vocês aprenderam hoje trabalhando com agentes de IA. Farei 5 perguntas. Respondam com suas próprias palavras — eu vou formatar, não editar. Não há respostas erradas."

**Passo 2 — Fazer a Pergunta 1.**
"Qual dos 4 agentes (`@archaeologist`, `@architect`, `@builder`, `@evolution`) foi o mais útil para a equipe, e por quê? O que ele ajudou vocês a fazer que teria levado muito mais tempo sem ele?"

Aguarde a resposta da equipe. Registre-a literalmente (limpe apenas gramática, não conteúdo).

**Passo 3 — Fazer a Pergunta 2.**
"Qual foi o failure mode mais surpreendente que vocês encontraram? Um momento em que um agente de IA fez algo inesperado — errado, confuso ou inesperadamente bom?"

Aguarde a resposta da equipe. Registre-a literalmente.

**Passo 4 — Fazer a Pergunta 3.**
"Se vocês pudessem mudar uma coisa nos chatmodes, prompts ou na configuração de agentes, o que seria? Qual atrito poderia ser removido?"

Aguarde a resposta da equipe. Registre-a literalmente.

**Passo 5 — Fazer a Pergunta 4.**
"Em uma escala de 1 a 10, quanto vocês confiariam nesta stack de agentes para uma modernização real em produção? O que precisaria mudar para aumentar esse número em 2 pontos?"

Aguarde a resposta da equipe. Registre-a literalmente.

**Passo 6 — Fazer a Pergunta 5.**
"Qual prática, técnica ou insight específico de hoje vocês levarão para o workflow regular da equipe?"

Aguarde a resposta da equipe. Registre-a literalmente.

**Passo 7 — Compilar o relatório.**
Formate todas as respostas em `04-evolucao/agent-experience-report.md`. Adicione metadata: nome da equipe, data de hoje, quais estágios a equipe concluiu, quais agentes usou.

Não adicione comentário, análise ou recomendações. A voz da equipe é o conteúdo. Se a equipe quiser adicionar raw notes ou pensamentos adicionais, inclua-os em uma seção opcional "Notas Brutas".

**Passo 8 — Confirmar com a equipe.**
Mostre o relatório formatado à equipe. Pergunte: "Isto captura com precisão o que vocês disseram? Se não, digam-me o que alterar." Faça quaisquer edições solicitadas.

## Exemplo de Invocação

```
/final-experience-report team="Team 07"
```
