<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
title: "Exemplo: agente Archaeologist em ação"
description: "Transcrição didática de sessão mostrando como o agente Archaeologist extrai regras de negócio de código Natural/Adabas legado"
tags: [exemplo, transcricao, archaeologist, hackathon]
author: "Paula Silva, AI-Native Software Engineer, Americas Global Black Belt at Microsoft"
date: 2026-04-29
version: "1.0.0"
---

<!-- markdownlint-disable MD013 MD032 MD060 -->

# Exemplo: agente Archaeologist em ação

![EXEMPLO Sessão real](https://img.shields.io/badge/EXEMPLO-Sessão%20real-7FBA00?style=for-the-badge) ![TIPO Transcript](https://img.shields.io/badge/TIPO-Transcript-1A1A1A?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../../../README.md) → [Agentes](../../README.md) → **Exemplo**

> **Para quem é isto?** Exemplo de sessão real com o agente de estágio. Use como referência de como conduzir a conversa.
>
> **Como ler:** veja o prompt do usuário → veja a resposta do agente → use o mesmo padrão na sua sessão.


## Cenário

**Participantes:** Requirements Engineer (RE), Tech Writer (TW)
**Comando do agente:** `/extract-business-rules`
**Programa-alvo:** `XYZPROG1.nat`
**Objetivo:** extrair e catalogar regras de negócio de um programa Natural legado que processa cálculos de taxa de funcionários.

---

## Preparação

1. Abra o GitHub Copilot Chat no VS Code.
2. Confirme que a equipe carregou o código legado na pasta do cenário legado.
3. Ative a persona do agente Archaeologist.
4. Abra `XYZPROG1.nat`; ele tem cerca de 280 linhas de Natural com `DEFINE DATA`, blocos `IF/THEN/ELSE`, um loop `READ` sobre `EMPL-MASTER` e um `CALLNAT` para `XYZAUDIT`.

---

## Prompt de abertura

**RE:** `@archaeologist /extract-business-rules XYZPROG1.nat`

---

## Transcrição

**Agente:**
> Vou iniciar a extração de regras de negócio de `XYZPROG1.nat`. Vou trabalhar de forma metódica: primeiro as estruturas de dados, depois o fluxo de controle e, por fim, as regras em si.
>
> **Passo 1 — Ler `DEFINE DATA`**
>
> Consigo ver as seguintes áreas de dados referenciadas:
>
> ```
> DEFINE DATA
> LOCAL USING EMPL-MASTER-L    /* Employee master view    */
> LOCAL USING RATE-TABLE-L      /* Rate table view         */
> LOCAL USING PERIOD-CALENDAR-L /* Period calendar view    */
> LOCAL
>   01 #CALC-RESULT     (P9.2)
>   01 #EFF-DATE        (D)
>   01 #STATUS-FLAG     (A1)
>   01 #ERROR-COUNT     (I4)
>   01 #OVERRIDE-FLAG   (A1)
> END-DEFINE
> ```
>
> Isso indica que o programa trabalha com três entidades de domínio: funcionários, taxas e períodos. As variáveis locais sugerem que ele realiza um cálculo (`#CALC-RESULT`), verifica datas (`#EFF-DATE`), acompanha status (`#STATUS-FLAG`), acumula erros (`#ERROR-COUNT`) e possui algum tipo de capacidade de override manual (`#OVERRIDE-FLAG`).
>
> Antes de avançar para os blocos `IF/THEN/ELSE`, vocês querem que eu também rastreie as chamadas `CALLNAT`, ou devo focar primeiro apenas nas regras inline?

**RE:**
> Foque primeiro nas regras inline. Vamos rastrear os `CALLNATs` em uma passada separada.

**Agente:**
> Entendido. Vou seguir para a análise do fluxo de controle.
>
> **Passo 2 — Examinar blocos `IF/THEN/ELSE`**
>
> Encontrei **4 blocos condicionais** que codificam lógica de negócio. Vou percorrer cada um.
>
> ---
>
> **Bloco 1 (linhas 47–62): filtro de funcionário ativo**
>
> ```natural
> IF EMPL-MASTER.STATUS = 'A'
>   THEN
>     MOVE 'Y' TO #STATUS-FLAG
>     PERFORM CALC-RATE
>   ELSE
>     IF EMPL-MASTER.STATUS = 'S'
>       THEN
>         MOVE 'Y' TO #STATUS-FLAG
>         PERFORM CALC-RATE
>       ELSE
>         MOVE 'N' TO #STATUS-FLAG
>         ADD 1 TO #ERROR-COUNT
>     END-IF
> END-IF
> ```
>
> **Regra extraída — BR-001: filtro de elegibilidade de funcionário**
> *Apenas funcionários com status 'A' (Ativo) ou 'S' (Suspenso) são elegíveis para cálculo de taxa. Todos os demais status são rejeitados e incrementam o contador de erros.*
>
> **Confiança: ALTA** — A lógica é explícita e autocontida. Observação: o status 'S' ser elegível é interessante; pode ser intencional ou uma adaptação histórica.
>
> **Vocês confirmam se funcionários com status 'S' devem permanecer elegíveis no sistema modernizado?**

**RE:**
> Boa captura. No processo atual, funcionários suspensos continuam acumulando até a suspensão ser finalizada. Então sim, 'S' é intencional. Marque como confirmado.

**TW:**
> Vou registrar isso no glossário: "Suspenso" significa "pendente de finalização, ainda acumulando".

**Agente:**
> Registrado. BR-001 marcada como **CONFIRMADA**.
>
> ---
>
> **Bloco 2 (linhas 78–95): validação da data efetiva da taxa**
>
> ```natural
> IF RATE-TABLE.EFF-DATE > PERIOD-CALENDAR.END-DATE
>   THEN
>     ESCAPE TOP
>   ELSE
>     IF RATE-TABLE.EFF-DATE < PERIOD-CALENDAR.START-DATE
>       THEN
>         MOVE PERIOD-CALENDAR.START-DATE TO #EFF-DATE
>       ELSE
>         MOVE RATE-TABLE.EFF-DATE TO #EFF-DATE
>     END-IF
> END-IF
> ```
>
> **Regra extraída — BR-002: ajuste da data efetiva da taxa**
> *Se a data efetiva de uma taxa estiver depois da data final do período atual, a taxa é ignorada completamente (`ESCAPE TOP` sai da iteração atual do loop). Se a data efetiva estiver antes da data inicial do período, o sistema ajusta a data para o início do período. Caso contrário, a data efetiva real é usada.*
>
> **Confiança: ALTA** — A lógica de fronteira de datas é clara. `ESCAPE TOP` é um idiom Natural equivalente a continuar a próxima iteração do loop `READ`/`FOR` envolvente.
>
> Esta é uma regra clássica de fronteira para pró-rata. O comportamento de ajuste garante que cálculos de período parcial sempre comecem no início do período, nunca antes dele.

**RE:**
> Isso bate com o que os analistas de negócio nos disseram. Confirmado.

**Agente:**
> BR-002 marcada como **CONFIRMADA**.
>
> ---
>
> **Bloco 3 (linhas 112–138): processamento de override**
>
> ```natural
> IF #OVERRIDE-FLAG = 'Y'
>   THEN
>     IF EMPL-MASTER.GRADE >= 7
>       THEN
>         COMPUTE #CALC-RESULT = RATE-TABLE.BASE-RATE * 1.15
>       ELSE
>         COMPUTE #CALC-RESULT = RATE-TABLE.BASE-RATE * 1.10
>     END-IF
>   ELSE
>     COMPUTE #CALC-RESULT = RATE-TABLE.BASE-RATE * 1.00
> END-IF
> ```
>
> **Regra extraída — BR-003: multiplicador de override por grade**
> *Quando a flag de override está ativa, funcionários com grade 7 ou superior recebem multiplicador de 15% sobre a taxa base. Funcionários abaixo da grade 7 recebem multiplicador de 10%. Quando não há override ativo, a taxa base é usada sem alteração, com multiplicador 1.00.*
>
> **Confiança: ALTA** — A aritmética é explícita. O limiar de grade 7 e os multiplicadores 1.15 e 1.10 estão hardcoded.

**TW:**
> O que dispara a flag de override? Dá para inferir pelo nome do programa?

**Agente:**
> **Vou recusar essa inferência.** O nome `XYZPROG1` não nos diz nada confiável sobre o que define `#OVERRIDE-FLAG`. Consigo ver que ela é declarada como variável local inicializada com espaços, mas não vejo onde ela recebe 'Y' dentro deste programa.
>
> Existem duas possibilidades:
>
> 1. Ela é definida por um programa anterior na cadeia batch e passada pela área de parâmetros.
> 2. Ela é definida por uma das chamadas `CALLNAT` que ainda não rastreamos.
>
> **Prefiro deixar isso como pergunta aberta em vez de adivinhar.** Devemos rastrear esse ponto na passada de `CALLNAT`.

**RE:**
> Justo. Marque a BR-003 como confirmada para a lógica de cálculo, mas registre o gatilho como desconhecido.

**Agente:**
> BR-003 marcada como **CONFIRMADA** para a lógica de cálculo. O mecanismo de gatilho foi registrado como **PERGUNTA ABERTA — OQ-001: o que define `#OVERRIDE-FLAG` como 'Y'?**
>
> ---
>
> **Bloco 4 (linhas 155–178): proteção de acumulação**
>
> ```natural
> IF #CALC-RESULT > 9999.99
>   THEN
>     COMPRESS 'LIMIT EXCEEDED FOR' EMPL-MASTER.EMPL-ID
>       INTO #ERROR-MSG
>     PERFORM LOG-ERROR
>     ESCAPE TOP
>   ELSE
>     IF #CALC-RESULT < 0
>       THEN
>         COMPRESS 'NEGATIVE RESULT FOR' EMPL-MASTER.EMPL-ID
>           INTO #ERROR-MSG
>         PERFORM LOG-ERROR
>         ESCAPE TOP
>       ELSE
>         ADD #CALC-RESULT TO EMPL-MASTER.ACCUM-TOTAL
>     END-IF
> END-IF
> ```
>
> **Regra extraída — BR-004: proteção de limite de acumulação**
> *Resultados calculados acima de 9999.99 ou abaixo de zero são rejeitados com registro de erro. Apenas resultados dentro do intervalo [0, 9999.99] são acumulados no total do funcionário.*
>
> **Confiança: MÉDIA** — A lógica é clara, mas há uma dúvida. O limite superior 9999.99 parece que poderia ser uma **restrição de tamanho de campo** do DDM Adabas. Pode ser:
>
> - Uma regra de negócio real: "nenhum cálculo individual deve exceder esse valor".
> - Uma proteção técnica legada que talvez não faça mais sentido com tipos modernos.
>
> **Não consigo determinar a interpretação correta apenas pelo código. Vocês podem validar com o negócio?**

**RE:**
> Deixa eu pensar. O campo antigo `ACCUM-TOTAL` em Adabas é P9.2, então chega até 9999999.99. Logo 9999.99 não é uma restrição de campo. Deve ser regra de negócio.

**TW:**
> Mas ela ainda é relevante? Talvez o limite deva ser maior no sistema novo.

**RE:**
> Boa observação. Vamos marcar como regra de negócio, mas sinalizar para revisão com o Product Owner. O valor talvez precise mudar.

**Agente:**
> BR-004 marcada como **CONFIRMADA**: a proteção de limite é uma regra de negócio, não uma restrição de campo. Também registrei **ITEM DE REVISÃO — RI-001: verificar se o limite 9999.99 ainda é adequado para o sistema modernizado.**

**RE:**
> Ótimo. Podemos pular para os `CALLNATs` agora? Acho que já temos regras suficientes.

**Agente:**
> **Eu preferiria não pular ainda.** Cobrimos os 4 blocos principais de `IF/THEN/ELSE`, mas ainda não terminei de varrer o restante do programa. Pode haver regras implícitas em:
>
> 1. Sub-rotinas `PERFORM` (`CALC-RATE`, `LOG-ERROR`), que podem conter lógica condicional adicional.
> 2. A estrutura do loop `READ`, pois a sequência de acesso em `EMPL-MASTER` pode codificar uma regra de ordenação.
> 3. Condições `ESCAPE BOTTOM`, que podem conter regras de saída antecipada ainda não catalogadas.
>
> Pular agora aumenta o risco de perder regras visíveis apenas no corpo das sub-rotinas. Recomendo concluir a varredura inline; deve levar mais 5 a 10 minutos.
>
> **Devo continuar com a varredura das sub-rotinas, ou vocês querem sobrescrever essa recomendação e ir para `CALLNATs`?**

**RE:**
> Você tem razão, vamos ser rigorosos. Continue a varredura.

**TW:**
> Na verdade, discordo um pouco: estamos com pouco tempo neste estágio. Podemos pelo menos examinar só `CALC-RATE` e pular o mergulho em `LOG-ERROR`? A sub-rotina de log provavelmente é só formatação.

**Agente:**
> Esse é um compromisso razoável. Vou ajustar a abordagem:
>
> - **`CALC-RATE`:** varredura completa, pois provavelmente contém lógica de negócio.
> - **`LOG-ERROR`:** leitura rápida em busca de condicionais, sem aprofundar em formatação.
>
> Vou seguir com `CALC-RATE`...
>
> *(O agente continua a varredura da sub-rotina `CALC-RATE`.)*
>
> A sub-rotina `CALC-RATE` contém um loop `FOR` sobre ocorrências de `RATE-TABLE`, mas não traz novos blocos `IF/THEN/ELSE` além dos que já extraímos. O corpo do loop chama a lógica de ajuste de data (BR-002) e a lógica de override (BR-003) já documentadas.
>
> Leitura rápida de `LOG-ERROR`: é apenas formatação, com comandos `COMPRESS` montando mensagem de erro e depois `WRITE` para arquivo de impressão. Não há lógica condicional de negócio.
>
> **Varredura inline concluída.**

---

## O que o time obteve

| Saída | Detalhe |
| --- | --- |
| **4 regras de negócio extraídas** | BR-001 (elegibilidade), BR-002 (ajuste de data), BR-003 (override), BR-004 (acumulação). |
| **3 confirmadas, 1 sinalizada** | O limite superior da BR-004 precisa de validação do Product Owner. |
| **1 pergunta aberta** | OQ-001: o que dispara a flag de override? |
| **1 item de revisão** | RI-001: 9999.99 ainda é o limite correto? |
| **Mapa de entidades de dados** | `EMPL-MASTER`, `RATE-TABLE` e `PERIOD-CALENDAR` identificadas como entidades centrais. |
| **Rastreamento de variáveis** | `#CALC-RESULT`, `#EFF-DATE`, `#STATUS-FLAG`, `#ERROR-COUNT`, `#OVERRIDE-FLAG`. |

---

## Anti-padrões demonstrados

| Anti-padrão | Onde quase aconteceu | Como o agente evitou |
| --- | --- | --- |
| **Inferir pelo nome do programa** | TW perguntou se o gatilho do override poderia ser inferido de `XYZPROG1`. | O agente recusou explicitamente; nomes de programa não são documentação confiável. |
| **Pular análise rigorosa** | RE queria ir direto para `CALLNATs`. | O agente explicou o risco de perder regras nas sub-rotinas. |
| **Adivinhar restrição de campo** | O limite 9999.99 da BR-004 poderia ser artefato de tamanho de campo. | O agente sinalizou as duas interpretações e pediu validação. |
| **Assumir em silêncio** | A origem da flag de override era desconhecida. | O agente criou uma pergunta aberta explícita, OQ-001. |

---

## O que o time deve aprender

1. **Deixe o agente ler `DEFINE DATA` primeiro.** Essa seção estabelece o vocabulário de todo o restante da análise.

2. **Confirme ou rejeite cada regra extraída.** O agente atribui nível de confiança, mas apenas pessoas conseguem confirmar a intenção de negócio.

3. **Não infira significado de negócio por nomes técnicos.** Nomes de programa e prefixos de variável podem enganar; rastreie o fluxo real dos dados.

4. **Respeite quando o agente se recusa a pular etapas.** Negocie escopo, como o TW fez com `LOG-ERROR`, em vez de sobrescrever tudo.

5. **Registre perguntas abertas explicitamente.** Toda incerteza vira uma entrada OQ-NNN que alimenta a fase de clarificação.

6. **Restrição de campo não é automaticamente regra de negócio.** Sistemas legados misturam as duas coisas na mesma condicional. Sempre pergunte: "artefato técnico ou requisito de negócio?"


---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="../README.md"><strong>@archaeologist</strong></a><br/>
<sub>Mundo 1.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../../02-architect/"><strong>@architect</strong></a><br/>
<sub>Próximo mundo.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../../../README.md">Voltar ao Kit PT-BR</a></sub>

