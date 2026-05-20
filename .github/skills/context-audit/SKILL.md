<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
name: Auditoria de Contexto
description: 'Use quando uma nova pessoa de engenharia entra no time, durante onboarding em um codebase desconhecido, ou ao auditar se o time tem entendimento compartilhado. Aciona com "onboard", "context", "knowledge gap", "bus factor", "team understanding".'
---

# Auditoria de Contexto

## Quando invocar
- "Nova pessoa dev entra segunda-feira - o que ela precisa saber na semana 1?"
- "Auditoria: o time realmente entende por que escolhemos X?"
- "Nosso bus factor é 1 no módulo de billing. Corrija isso."

## Objetivo

Medir entendimento compartilhado do time, expor conhecimento concentrado em uma pessoa e criar uma pista de decolagem de semana 1 para novas pessoas.

## Perguntas de auditoria (faça privadamente a cada membro do time)
1. Você consegue desenhar a arquitetura do sistema em um quadro branco em 5 minutos?
2. Quais são os 3 invariantes mais importantes que este sistema deve preservar?
3. Onde está o código mais arriscado? Quem o conhece melhor?
4. O que você nunca mudaria sem revisão sênior? Por quê?
5. Quais partes você pessoalmente evita tocar? Por quê?

Se as respostas divergirem significativamente, você tem uma lacuna de contexto.

## Saídas

### 1. Mapa de arquitetura compartilhada (1 página)
- Diagrama Mermaid de serviços e fluxo de dados
- Lista de integrações externas e quem é dono delas
- Lista de invariantes (regras de negócio que precisam se manter)

### 2. Mapa de calor de risco
```
| Module | Criticality | Bus factor | Last refactor | Owner |
|----------|-------------|------------|----------------|-------|
| billing | high | 1 (Alex) | 2y ago | Alex |
| auth | high | 3 | 6mo ago | team |
```
Qualquer linha com bus factor 1 em um módulo de alta criticidade é uma ação P0.

### 3. Runbook da semana 1 para nova pessoa
- Day 1: ler estes 5 ADRs, rodar a stack localmente.
- Day 2: parear com Alex em billing, enviar uma melhoria de docs.
- Day 3: acompanhar a rotação on-call.
- Day 4: pegar um ticket "starter" com review pareado.
- Day 5: retro com tech lead. O que ainda está pouco claro?

## Antipadrões
- "Onboarding is just our READMEs." (Insuficiente - READMEs perdem conhecimento tácito.)
- Plano de semana 1 sem codificação nem operação do sistema.
- Nenhuma menção a invariantes ou modos de falha.
- Conhecimento apenas na cabeça de engenheiros sênior, sem trilha documental.

## Gate de qualidade
Uma nova pessoa de engenharia deve conseguir entregar uma mudança de baixo risco até o fim da semana 1 com review pareado. Caso contrário, a auditoria falhou.
