---
title: "SIFAP Terminal — Roteiro de Demo Interativa"
description: "Script cena-a-cena para conduzir a demo interativa do simulador SIFAP no Estágio 1 (Arqueologia Digital). Cobre todos os pontos do sistema com comandos exatos, o que aparece na tela e falas-chave de narração. Pensado para uso ao vivo lado a lado com o simulador projetado. Versão alinhada ao terminal v1.1 com fidelidade SIAFI/SIAPE."
author: "Paula Silva — Americas Software GBB, Microsoft"
date: 2026-04-25
version: 1.1.0
status: ready
artifact: sifap-terminal.html (v1.1)
audience: Paula (facilitadora principal)
duration: 18-22 min (versão completa) | 8-10 min (versão reduzida)
event: Hackathon SERPRO 2026 — Estágio 1 Arqueologia Digital
tags: [hackathon, serpro, sifap, demo-script, facilitacao, estagio-1]
---

# SIFAP Terminal — Roteiro de Demo Interativa

> Script para você conduzir ao vivo, projetando o simulador. Cada cena tem comando exato, descrição da tela e falas-chave. Não é discurso para ler — são pontos para você costurar do seu jeito.
>
> **Atualizado para o terminal v1.1** — header padrão SIAFI/SIAPE, prompt `===>`, mensagens canônicas de status, "EXECUTADO" em vez de "OK".

---

## Sumário

1. [O que mudou da v1.0 para v1.1](#o-que-mudou-da-v10-para-v11)
2. [Como usar este roteiro](#como-usar-este-roteiro)
3. [Setup antes de começar](#setup-antes-de-começar)
4. [Visão geral das 20 cenas](#visão-geral-das-20-cenas)
5. [Roteiro completo cena a cena](#roteiro-completo-cena-a-cena)
6. [Plano B — versão reduzida (8 min)](#plano-b--versão-reduzida-8-min)
7. [Cheat sheet — comandos em ordem](#cheat-sheet--comandos-em-ordem)
8. [Change Log](#change-log)

---

## O que mudou da v1.0 para v1.1

| Aspecto | v1.0 | v1.1 (fidelidade SIAFI/SIAPE) |
|---|---|---|
| Prompt de comando | `==>` | `===>` (3 iguais — convenção Natural) |
| Header da tela | Linha única | 4 linhas: PGM, USUARIO, NIVEL, LIBR, TID, DATA, HORA |
| Separadores | `+----+----+` | Linhas `===` horizontais (estilo SIAFI) |
| "BIBLIOT:" | OK | "BIBLIOTECA:" |
| Boot success | "[OK]" | "[EXECUTADO]" |
| PF Keys no rodapé | `PF3=SAIR PF12=LOGOFF` | `PF1=AJUDA PF2=GUIA PF3=SAIR PF12=RETORNA` |
| Mensagens canônicas | Não tinha | Adicionadas: "REGISTRO LOCALIZADO COM SUCESSO", "*** FIM DE LISTAGEM ***", "INFORME O NIS E TECLE ENTER" |
| Erros | `*** ERRO NAT3145 ***` em janela | `NAT3145 - REGISTRO NAO LOCALIZADO` direto na linha de status |
| Boot dramático | Mantido (decisão pedagógica — ambientação) | Mantido |

**Fala extra que vale incluir nas primeiras cenas:** mencionar que em SIAFI/SIAPE/Dataprev reais o prompt era exatamente `===>`, e que o header de 4 linhas era idêntico em todos os sistemas Natural daquela geração. Isso ancora reconhecimento na plateia experiente.

---

## Como usar este roteiro

**Convenções:**

- `[CMD]` — comando exato a digitar no terminal
- `[TELA]` — o que vai aparecer (em uma linha)
- `[FALAR]` — pontos-chave de narração (não decore, costure)
- `[PERGUNTA]` — opcional, para engajar a plateia
- `[GANCHO]` — onde costurar com seu framework
- `[TEMPO]` — estimativa de duração da cena

**Princípio narrativo:** cada cena tem um propósito. As cenas marcadas com `[CRÍTICO]` são as que carregam a tese pedagógica — não corte essas mesmo se o tempo apertar.

---

## Setup antes de começar

| Passo | Ação |
|---|---|
| 1 | Abra `sifap-terminal.html` no browser |
| 2 | `F11` ou clique **Fullscreen** |
| 3 | Clique **Reset** para garantir que está no boot |
| 4 | Aumente zoom do browser `Ctrl/Cmd +` 1-2 vezes (sala grande) |
| 5 | Abra este roteiro num segundo monitor |
| 6 | Cheque microfone, respire fundo |

---

## Visão geral das 20 cenas

| # | Cena | Tempo | Crítico? |
|---|---|---|---|
| 1 | Boot sequence | 30s | Sim |
| 2 | Login | 30s | — |
| 3 | Tour pelo menu | 1m | — |
| 4 | Caso feliz: Maria (BPC ATIVO) | 2m | **Sim** |
| 5 | Histórico de Maria | 1m | — |
| 6 | Caso AGENDADO: José | 1m | — |
| 7 | Caso problemático: Francisco SUSPENSO+BLOQ | 2m | **Sim** |
| 8 | Caso CESSADO: Joana | 30s | — |
| 9 | Beneficiário sem histórico: Antonio | 30s | — |
| 10 | Listagem BPC | 1m | — |
| 11 | Listagem BOLSA FAM | 30s | — |
| 12 | Listagem AUX BRASIL | 30s | — |
| 13 | Listagem TODOS | 30s | — |
| 14 | **Relatório Gerencial (CLÍMAX)** | 2m | **Sim** |
| 15 | Funções bloqueadas | 1m | — |
| 16 | Tela de HELP | 1m | — |
| 17 | Provocando erro NAT3145 | 30s | — |
| 18 | Atalho power user | 30s | — |
| 19 | Logoff | 15s | — |
| 20 | Encerramento e transição | 1m | **Sim** |

**Total versão completa:** 18-22 min
**Total versão reduzida (5 críticas):** ~8 min

---

## Roteiro completo cena a cena

### Cena 1 — Boot sequence

**[TEMPO]** 30 segundos
**[CMD]** Nenhum — clique **Reset**, sequência roda sozinha
**[TELA]** Conexão Adabas, library, Natural 6.3.13, COM-PLETE 7.4 — cada linha terminando em `[EXECUTADO]`

**[FALAR]**
- "Isso é um terminal Natural/Adabas. Tecnologia da Software AG, anos 90."
- "ADABAS é o banco — não é relacional, é nested-file. File 047."
- "COM-PLETE é o monitor de teleprocessamento — pensem nele como o 'web server' do mainframe."
- "Library SIFAPPRD é onde mora o código de produção. Como um 'pacote' Java, mas sem versionamento."
- *(Apontar pro `[EXECUTADO]`)* "Reparem: não é 'OK'. É EXECUTADO. Convenção dos sistemas brasileiros — SIAFI, SIAPE, todos usam essa palavra."

**[GANCHO]** Doc 13 §4.2 — Scenario B Natural+Adabas. Esses são os ingredientes reais que o time vai encontrar no repo.

---

### Cena 2 — Login

**[TEMPO]** 30 segundos
**[CMD]** Digite um nome (sugestão: `OPERADOR1` ou nome de alguém da plateia)
**[TELA]** Tela de logon com header de 3 linhas (DATA, HORA, TID) → menu principal aparece

**[FALAR]**
- "Não tem validação de senha — é demo. No SIFAP real, autenticação era no próprio COM-PLETE com perfis."
- *(Apontar pro prompt)* "Reparem o prompt: `===>` com **três iguais**. Não é dois, não é um. É padrão Natural — quem viu sistema da década de 90 reconhece na hora."
- "BIBLIOTECA: SIFAPPRD. Nome de até 8 caracteres, tudo maiúsculo. Convenção mainframe."

---

### Cena 3 — Tour pelo menu

**[TEMPO]** 1 minuto
**[CMD]** Nenhum — só fala apontando para a tela
**[TELA]** Menu principal com header SIAFI-like de 4 linhas

**[FALAR]**
- *(Apontar pro header)* "Esse cabeçalho de 4 linhas era padrão da Dataprev/SERPRO. Olhem: `PGM: MENU0001`, `USUARIO`, `NIVEL: ADM`, `LIBR: SIFAPPRD`, `TID: T0001`. Cada tela mostra exatamente em qual programa Natural você está."
- "Sete opções funcionais mais o logoff."
- "Numeração — não tem mouse, não tem hover. Operadores experientes digitavam `1` antes do menu terminar de renderizar."
- "Olhem as opções 6 e 7 — BLOQUEADAS. Vamos voltar nelas no final."
- *(Apontar pro rodapé)* "Linha de status no final: 'SELECIONE UMA OPCAO E TECLE ENTER'. Era convenção — Natural sempre tinha uma linha de feedback no final da tela. Operador sabia o que fazer só olhando aquela linha."

**[PERGUNTA]** *(opcional)* "Quem aqui já trabalhou com tela assim de verdade? Levantem a mão."

---

### Cena 4 — Caso feliz: Maria (BPC ATIVO) [CRÍTICO]

**[TEMPO]** 2 minutos
**[CMD]** `1` → Enter → `12345678901` → Enter
**[TELA]** Detalhe da Maria das Graças Silva, BPC ATIVO, R$ 1.412,00. Status no rodapé: "REGISTRO LOCALIZADO COM SUCESSO"

**[FALAR]**
- "Maria, 64 anos, Salvador. BPC — Benefício de Prestação Continuada. R$ 1.412 — salário mínimo de 2024."
- *(Apontar pro header)* "PGM: `CBENF020`. Esse é o nome real do .NSP que renderiza essa tela."
- "Antes desse, o `CBENF010` mostrou o formulário. Cada tela é um programa Natural separado."
- *(Apontar pra linha de status)* "REGISTRO LOCALIZADO COM SUCESSO — mensagem canônica. Era padrão em todo sistema brasileiro daquela geração. Quem operou SIAPE da SERPRO nos anos 2000 leu essa mensagem milhares de vezes."
- "**Vocês vão encontrar arquivos com esses nomes exatos no repo de Arqueologia.** CBENF010.NSP, CBENF020.NSP."

**[GANCHO]** Discovery Agent vai catalogar TODOS os programas — `CBENF*`, `CPAGT*`, `LBENF*`, `RGER*`, `CRPGM*`. Primeiro passo do Estágio 1.

---

### Cena 5 — Histórico de pagamentos da Maria

**[TEMPO]** 1 minuto
**[CMD]** `PF5` (ou `F5`)
**[TELA]** Tabela de 3 pagamentos PAGO, todos mesmo banco/agência/conta. Rodapé: "*** FIM DE LISTAGEM *** 3 REGISTRO(S) LOCALIZADO(S)"

**[FALAR]**
- "3 pagamentos consecutivos, todos PAGO. Caixa Econômica (104), poupança social — padrão histórico de BPC e Bolsa Família."
- "Mesmo banco, agência e conta nos 3 meses. Estabilidade."
- *(Apontar pro `*** FIM DE LISTAGEM ***`)* "Outra mensagem canônica: 'fim de listagem, X registros localizados'. Era assim que Natural avisava que não tinha mais nada pra mostrar. Ninguém precisava scrollar."

---

### Cena 6 — Caso AGENDADO: José

**[TEMPO]** 1 minuto
**[CMD]** `MENU` → Enter → `2` → Enter → `23456789012` → Enter
**[TELA]** Pagamentos do José: 1 AGENDADO + 1 PAGO

**[FALAR]**
- "Status AGENDADO — pagamento de abril ainda não rodou no batch. Está esperando a janela noturna."
- "Conta corrente Banco do Brasil (001). Minoria, mas existe."
- "Tela mostrando estado em momentos diferentes do ciclo: PAGO (efetivado), AGENDADO (programado), e logo veremos BLOQ (bloqueado)."

**[GANCHO]** Modelagem de máquina de estados — tópico do Estágio 2. Time vai precisar definir esses estados explicitamente em código moderno.

---

### Cena 7 — Caso problemático: Francisco (SUSPENSO + BLOQ) [CRÍTICO]

**[TEMPO]** 2 minutos
**[CMD]** `MENU` → Enter → `1` → Enter → `45678901234` → Enter
**[TELA]** Detalhe Francisco, **STATUS SUSPENSO em vermelho**

**[FALAR]**
- "STATUS SUSPENSO. Vermelho. No terminal real era inverso de vídeo — fundo verde, letra preta. A intenção é a mesma."
- "Auxílio Brasil. Início em 11/2022. Algo aconteceu no caminho."

**[CMD]** `PF5`
**[TELA]** Pagamentos: BLOQ em abril (R$ 0,00), PAGO em março

**[FALAR]**
- "Vejam: pagou normalmente em março, BLOQ em abril. R$ 0,00 transferidos."
- "Pergunta cara: **o que torna um benefício SUSPENSO?** E qual o efeito no batch de pagamento?"
- "A resposta está escondida em algum programa Natural lá no repo. Pode ser uma rotina de validação no batch noturno. Pode ser um trigger no Adabas. Pode ser uma flag manual de operador."
- "**Essa pergunta — 'qual a regra?' — é exatamente o trabalho do Analysis Agent.**"

**[PERGUNTA]** *(opcional)* "Onde vocês procurariam essa regra primeiro? No código? Numa documentação que ninguém leu? Falando com um operador prestes a se aposentar?"

---

### Cena 8 — Caso CESSADO: Joana

**[TEMPO]** 30 segundos
**[CMD]** `MENU` → Enter → `1` → Enter → `78901234567` → Enter
**[TELA]** Joana, status CESSADO

**[FALAR]**
- "CESSADO é diferente de SUSPENSO. Suspenso é temporário, reversível. Cessado é definitivo."
- "Esse tipo de distinção é o que o Analysis Agent identifica como **enum candidato** ao gerar tipos em Java."

---

### Cena 9 — Beneficiário sem histórico: Antonio

**[TEMPO]** 30 segundos
**[CMD]** `MENU` → Enter → `2` → Enter → `67890123456` → Enter
**[TELA]** "NENHUM PAGAMENTO REGISTRADO"

**[FALAR]**
- "Antonio está ATIVO mas não tem nenhum pagamento na base. Como assim?"
- "Em produção real isso seria red flag — integridade referencial frouxa, cadastro recém-aprovado, falta de conta bancária."
- "Casos de borda assim são exatamente o que QA e DBA precisam levantar."

---

### Cena 10 — Listagem BPC

**[TEMPO]** 1 minuto
**[CMD]** `MENU` → Enter → `3` → Enter → `1` → Enter
**[TELA]** 4 BPCs (3 ATIVOS + 1 sem histórico de pagamento), todos R$ 1.412

**[FALAR]**
- "Filtro BPC. Quatro beneficiários. **Todos com o mesmo valor**, R$ 1.412."
- "BPC tem valor estruturado — sempre um salário mínimo. Regra fixa."
- "Em modular monolith, **BPC é claramente um bounded context candidato**. Regras próprias, vocabulário próprio, ciclo de vida próprio."

**[GANCHO]** Doc 13 §5 — bounded contexts dentro do monolito modular. Não vamos quebrar em microserviços, mas vamos modular **dentro** do monolito.

---

### Cena 11 — Listagem BOLSA FAM

**[TEMPO]** 30 segundos
**[CMD]** `MENU` → Enter → `3` → Enter → `2` → Enter
**[TELA]** 2 Bolsa Família com R$ 600 e R$ 750

**[FALAR]**
- "Bolsa Família — valor variável. Depende de filhos, condicionalidades de saúde e educação."
- "Outra regra escondida: como esse valor é calculado? Está no `CRPGM042` que veremos daqui a pouco."
- "Outro bounded context, com regras próprias."

---

### Cena 12 — Listagem AUX BRASIL

**[TEMPO]** 30 segundos
**[CMD]** `PF7` → `3` → Enter
**[TELA]** 2 Aux Brasil — 1 SUSPENSO + 1 CESSADO, nenhum ativo

**[FALAR]**
- "Curioso: na nossa base não tem nenhum Aux Brasil ATIVO. Faz sentido historicamente — programa foi substituído."
- "Em modernização real, vocês decidem: mantém? deprecia? migra dados pra outro contexto?"
- "Decisão de Enterprise Architect. Não é técnica pura — tem implicação política."

---

### Cena 13 — Listagem TODOS

**[TEMPO]** 30 segundos
**[CMD]** `PF7` → Enter → `9` → Enter (ou direto `LIST`)
**[TELA]** 8 beneficiários, panorama completo

**[FALAR]**
- "Vista panorâmica. 8 beneficiários, 3 programas, 3 status."
- "Em produção real isso retornaria milhões. A paginação aqui não é simulada — no Natural era controlada por `READ LOGICAL` com `STARTING FROM`."

---

### Cena 14 — Relatório Gerencial [CRÍTICO — CLÍMAX]

**[TEMPO]** 2 minutos — **mais importante de toda a demo**
**[CMD]** `MENU` → Enter → `5` → Enter
**[TELA]** Totais por programa, total geral em amarelo, e duas linhas em cinza no rodapé

**[FALAR]**
- "Esse é o relatório que a diretoria da DATACORP usaria pra prestar contas ao governo federal."
- "Totais por programa, valor total mensal, agregado geral em destaque."
- *(Apontar pra linha em cinza no rodapé)*
- "**Olhem essa linha: `REGRA DE NEGOCIO: PGM CALC-VALOR-MENSAL (NATURAL CRPGM042)`**."
- "Esse programa Natural — `CRPGM042` — calcula quanto cada beneficiário recebe."
- "Tem como saber, só olhando essa tela, o que esse programa faz exatamente?"
- "Não. Mas se vocês escreverem essa lógica em Java errada, **paga errado pra Maria**."
- "Pode pagar a menos. Pode pagar a mais. Pode bloquear quem não devia ser bloqueado."
- "**Por isso Arqueologia Digital existe.** Esse é o trabalho do Estágio 1."

**[PERGUNTA]** *(opcional)* "Quantos casos especiais vocês acham que esse `CALC-VALOR-MENSAL` tem? Chutem alto."

**[GANCHO FORTE]** Logo depois dessa cena, abra o repo de Arqueologia e mostre o `CRPGM042.NSP` real (se já fabricado).

---

### Cena 15 — Funções bloqueadas

**[TEMPO]** 1 minuto
**[CMD]** `MENU` → Enter → `6` → Enter
**[TELA]** Mensagem em vermelho na linha de status do menu: `NAT0850 - FUNCAO BLOQUEADA - PERFIL INSUFICIENTE`

**[FALAR]**
- "Manutenção e Batch Noturno — bloqueados pra esse perfil."
- *(Apontar pra linha em vermelho)* "Reparem como o erro aparece: vermelho, na linha de status, código `NAT0850`. Não é popup, não é alerta. É só uma linha que muda. Quem opera no automático nem percebe."
- "No SIFAP real, essas operações só rodam de madrugada, com perfil OPERACAO, e são auditadas."
- "**Decisão de escopo: NÃO vamos modernizar essas duas funções no hackathon.** Está fora."
- "Modernização real tem 'scope freeze' — você define o que está dentro e respeita."

**[GANCHO]** Doc 12 — gate criteria e escopo finito. Princípio anti-over-engineering.

---

### Cena 16 — Tela de HELP

**[TEMPO]** 1 minuto
**[CMD]** `HELP` → Enter
**[TELA]** Tela de ajuda com versões da stack

**[FALAR]**
- "Tela de ajuda: stack completa. Natural 6.3, Adabas File 047, COM-PLETE 7.4."
- "Library: SIFAPPRD. Implantação: 1997."
- "**Quase 30 anos de produção.** Não é descartável."
- "Está rodando há mais tempo do que muitos de vocês têm de carreira."
- "Modernização não é sobre 'jogar fora o velho'. É sobre extrair conhecimento, preservar regras, e construir novo com fundação sólida."

**[GANCHO]** Doc 11 — business case da modernização. ROI vem de continuidade + agilidade futura.

---

### Cena 17 — Provocando erro NAT3145

**[TEMPO]** 30 segundos
**[CMD]** `PF3` → `1` → Enter → `99999999999` → Enter
**[TELA]** Formulário de consulta volta a aparecer, mas agora com mensagem em vermelho na linha de status: `NAT3145 - REGISTRO NAO LOCALIZADO PARA NIS 99999999999`

**[FALAR]**
- "Mensagens de erro do Natural são códigos. `NAT3145`."
- *(Apontar)* "Reparem: o formulário não sai, não pisca alerta. Só aparece a mensagem em vermelho na linha de status. **Era exatamente assim no SIAFI, no SIAPE, em todo Natural brasileiro.** Você precisa olhar pra linha 24 pra saber o que aconteceu."
- "Sem stack trace. Sem hint. Sem 'did you mean'. Você precisa do manual da Software AG, em PDF, traduzido nos anos 90."
- "DX brutal — outro motivo pra modernizar. Não é só tech debt: é **friction debt**."

---

### Cena 18 — Atalho power user

**[TEMPO]** 30 segundos
**[CMD]** `BENEF NIS=12345678901` → Enter
**[TELA]** Detalhe da Maria direto, sem passar pelo menu

**[FALAR]**
- "Power users do Natural usavam comandos diretos. Pulavam o menu inteiro."
- "Equivalente ao keyboard shortcut moderno — quem dominava era 10x mais rápido que quem clicava."
- "UX 'invisível pra novato, poderosa pra expert' é interessante de preservar quando se moderniza."

---

### Cena 19 — Logoff

**[TEMPO]** 15 segundos
**[CMD]** `PF12` → Enter
**[TELA]** Sessão encerrada, mensagem centralizada

**[FALAR]**
- "Encerra sessão limpa."
- "Em mainframe real, cada sessão custa CPU e licença. Logoff é hábito, não opcional."

---

### Cena 20 — Encerramento e transição [CRÍTICO]

**[TEMPO]** 1 minuto
**[CMD]** Nenhum — você fala olhando pra plateia

**[FALAR]**
- "Vocês acabaram de ver — em 20 minutos — o que vão precisar entender de verdade nas próximas 3 horas."
- "Não está tão difícil quanto parece. **Porque vocês não vão estar sozinhos.**"
- "Discovery Agent vai catalogar todos esses programas Natural pra vocês."
- "Analysis Agent vai extrair as regras escondidas — incluindo o tal `CALC-VALOR-MENSAL`."
- "Vocês — humanos — vão **revisar, validar, corrigir, decidir**. Não escrever do zero."
- "Esse é o trabalho do AI-Native Engineer: **pilotar agentes**, não substituir agentes."
- "Próximo: abrir o repositório do legado e olhar o código Natural de verdade."

*(Saia do simulador, abra o repo de Arqueologia.)*

---

## Plano B — versão reduzida (8 min)

| # | Cena | Tempo |
|---|---|---|
| 1 | Boot sequence | 30s |
| 2 | Login → Menu rápido | 1m |
| 4-5 | Maria + histórico | 2m |
| 7 | Francisco SUSPENSO+BLOQ | 2m |
| 14 | Relatório Gerencial (CLÍMAX) | 2m |
| 20 | Encerramento | 1m |

**Total:** 8 min e meio. Mantém os 3 momentos críticos (caso feliz, caso problemático, regra escondida) e o gancho final.

---

## Cheat sheet — comandos em ordem

Para imprimir e deixar lado a lado com o laptop.

```
[Reset]
[aguardar boot — ~10s]

OPERADOR1                    [Enter]   → Login → Menu
1                            [Enter]   → Cons Beneficiário (CBENF010)
12345678901                  [Enter]   → Maria detalhe (CBENF020)
PF5                                    → Maria pagamentos (CPAGT010)
MENU                         [Enter]
2                            [Enter]   → Cons Pagamentos (CPAGT005)
23456789012                  [Enter]   → José pagamentos (AGENDADO)
MENU                         [Enter]
1                            [Enter]
45678901234                  [Enter]   → Francisco SUSPENSO
PF5                                    → Francisco pagamento BLOQ
MENU                         [Enter]
1                            [Enter]
78901234567                  [Enter]   → Joana CESSADO
MENU                         [Enter]
2                            [Enter]
67890123456                  [Enter]   → Antonio sem histórico
MENU                         [Enter]
3                            [Enter]   → Listagem por programa (LBENF030)
1                            [Enter]   → Listagem BPC (LBENF031)
PF7                                    → Volta filtro
2                            [Enter]   → Listagem BOLSA FAM
PF7                                    → Volta filtro
3                            [Enter]   → Listagem AUX BRASIL
MENU                         [Enter]
5                            [Enter]   → RELATÓRIO GERENCIAL (CLÍMAX) (RGER010)
MENU                         [Enter]
6                            [Enter]   → Erro NAT0850 (bloqueado)
HELP                         [Enter]   → Tela técnica (HELP0001)
PF3                                    → volta
1                            [Enter]
99999999999                  [Enter]   → Erro NAT3145
BENEF NIS=12345678901        [Enter]   → atalho power user
PF12                                   → Logoff
```

---

## Change Log

| Versão | Data | Mudanças |
|---|---|---|
| 1.0.0 | 2026-04-25 | Versão inicial — 20 cenas, plano B reduzido, cheat sheet operacional |
| 1.1.0 | 2026-04-25 | Atualizado para terminal v1.1 com fidelidade SIAFI/SIAPE — header de 4 linhas, prompt `===>`, mensagens canônicas, "EXECUTADO". Falas adicionais nas cenas 1, 2, 3, 4, 5, 14, 15, 17 destacando elementos de fidelidade reconhecíveis pela plateia experiente |

---

*Documento de facilitação ao vivo. Use junto com `sifap-terminal.html` v1.1. Parte do material do Hackathon SERPRO 2026, Estágio 1: Arqueologia Digital.*
