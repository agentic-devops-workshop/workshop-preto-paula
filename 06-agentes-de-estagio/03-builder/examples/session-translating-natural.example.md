<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
title: "Exemplo: agente Builder em ação"
description: "Transcrição didática de sessão mostrando como o agente Builder traduz padrões Natural/Adabas para Java 21 + Spring Boot"
tags: [exemplo, transcricao, builder, hackathon]
author: "Paula Silva, AI-Native Software Engineer, Americas Global Black Belt at Microsoft"
date: 2026-04-29
version: "1.0.0"
---

<!-- markdownlint-disable MD013 MD032 MD060 -->

# Exemplo: agente Builder em ação

![EXEMPLO Sessão real](https://img.shields.io/badge/EXEMPLO-Sessão%20real-7FBA00?style=for-the-badge) ![TIPO Transcript](https://img.shields.io/badge/TIPO-Transcript-1A1A1A?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../../../README.md) → [Agentes](../../README.md) → **Exemplo**

> **Para quem é isto?** Exemplo de sessão real com o agente de estágio. Use como referência de como conduzir a conversa.
>
> **Como ler:** veja o prompt do usuário → veja a resposta do agente → use o mesmo padrão na sua sessão.


## Cenário

**Participantes:** Developer (DEV), Tech Lead (TL)
**Comando do agente:** `/translate-natural-to-java`
**Programa-alvo:** `XYZRATES.nat`
**Objetivo:** traduzir um programa Natural que gerencia histórico de taxas, com um grupo PE de 12 ocorrências mensais, para código idiomático em Java 21 + Spring Boot.

---

## Preparação

1. Abra o GitHub Copilot Chat no VS Code.
2. Confirme que os estágios Archaeologist e Architect foram concluídos.
3. Verifique se o mapa de bounded contexts classifica este programa no contexto "Rate Calculation".
4. Ative a persona do agente Builder.
5. Abra `XYZRATES.nat`; ele contém um grupo PE com 12 ocorrências, cada uma com data, valor de taxa e código de motivo.

---

## Prompt de abertura

**DEV:** `@builder /translate-natural-to-java XYZRATES.nat — focus on the PE group and the FOR loop that processes it`

---

## Transcrição

**Agente:**
> Vou iniciar a análise de tradução de `XYZRATES.nat`. Antes de escrever qualquer código Java, preciso entender a estrutura de dados.
>
> **Passo 1 — Analisar o grupo PE**
>
> Encontrei esta definição de grupo periódico no DDM Adabas:
>
> ```
> 01 RATE-HISTORY (PE/12)
>   02 RATE-DATE      (D)      /* Effective date     */
>   02 RATE-VALUE     (P7.2)   /* Rate amount        */
>   02 RATE-REASON    (A3)     /* Reason code        */
> ```
>
> Este é um grupo PE clássico do Adabas: um array de tamanho fixo com 12 ocorrências, uma por mês. Nem todos os slots precisam estar preenchidos; ocorrências vazias costumam ter datas zeradas.
>
> Para Java, vejo **duas abordagens viáveis**:
>
> ---
>
> **Opção A: coluna JSONB no PostgreSQL**
>
> Armazenar o histórico de taxas como um array JSON em uma única coluna:
>
> ```java
> @Entity
> @Table(name = "rate_table")
> public class RateTable {
>     @Id
>     private Long id;
>
>     @JdbcTypeCode(SqlTypes.JSON)
>     @Column(name = "rate_history", columnDefinition = "jsonb")
>     private List<RateHistoryEntry> rateHistory;
> }
>
> public record RateHistoryEntry(
>     LocalDate rateDate,
>     BigDecimal rateValue,
>     String rateReason
> ) {}
> ```
>
> **Prós:** simples, preserva a semântica de "coleção embutida" e evita overhead de join.
> **Contras:** não há integridade referencial em cada item individual e indexar entradas específicas é mais trabalhoso.
>
> ---
>
> **Opção B: `@OneToMany` com tabela separada**
>
> Usar um `@OneToMany` JPA padrão com `@OrderColumn` para preservar o índice da ocorrência. A tabela separada `rate_history_entry` teria sua própria chave primária.
>
> **Prós:** integridade relacional completa, JPA padrão e entradas consultáveis individualmente.
> **Contras:** overhead de join, risco de N+1 e mapeamento mais complexo.
>
> **Qual abordagem o time prefere?**

**DEV:**
> Estou inclinado a JSONB. A gente sempre lê e grava o histórico de taxas como um todo.

**TL:**
> Concordo. JSONB mapeia melhor a semântica de grupo PE.

**Agente:**
> Então seguimos com JSONB. Vou traduzir o loop `FOR`.
>
> **Passo 2 — Traduzir o loop `FOR`**
>
> Este é o loop Natural que processa o grupo PE:
>
> ```natural
> FOR #I = 1 TO 12
>   IF RATE-HISTORY.RATE-DATE(#I) NE ' '
>     IF RATE-HISTORY.RATE-DATE(#I) <= #EFF-DATE
>       COMPUTE #APPLICABLE-RATE = RATE-HISTORY.RATE-VALUE(#I)
>     END-IF
>   END-IF
> END-FOR
> ```
>
> O loop encontra a taxa mais recente em ou antes da data efetiva. Ele percorre os 12 slots, ignora os vazios e sobrescreve `#APPLICABLE-RATE` a cada ocorrência válida. Como grupos PE costumam estar em ordem cronológica, a última ocorrência que qualifica vence.
>
> Tradução idiomática em Java 21:
>
> ```java
> public BigDecimal findApplicableRate(List<RateHistoryEntry> rateHistory,
>                                      LocalDate effectiveDate) {
>     return rateHistory.stream()
>         .filter(entry -> entry.rateDate() != null)
>         .filter(entry -> !entry.rateDate().isAfter(effectiveDate))
>         .reduce((first, second) -> second)  // take the last qualifying entry
>         .map(RateHistoryEntry::rateValue)
>         .orElseThrow(() -> new NoApplicableRateException(
>             "No rate found on or before " + effectiveDate));
> }
> ```
>
> Decisões importantes de tradução:
>
> - `RATE-DATE NE ' '` vira `entry.rateDate() != null` (data em branco no Natural vira `null` em Java).
> - `RATE-DATE <= #EFF-DATE` vira `!entry.rateDate().isAfter(effectiveDate)` (comparação inclusiva).
> - A semântica "a última vence" vira `reduce((first, second) -> second)`.
> - Ausência de taxa aplicável vira exceção explícita, em vez do zero implícito do Natural.

**TL:**
> Espera: vejo um caso de borda. O que acontece se duas entradas tiverem a **mesma** data? No Natural, vence a de índice maior, porque o loop `FOR` sobrescreve o valor. No seu stream, `.reduce((first, second) -> second)` também pega a última, então o comportamento bate. Mas e se a lista JSONB não tiver ordem garantida?

**Agente:**
> Excelente ponto. Você está certo: **a abordagem com stream depende de a lista estar ordenada cronologicamente**, algo que o grupo PE garante por índice de ocorrência, mas o JSONB não garante sozinho.
>
> Preciso adicionar ordenação explícita:
>
> ```java
> public BigDecimal findApplicableRate(List<RateHistoryEntry> rateHistory,
>                                      LocalDate effectiveDate) {
>     return rateHistory.stream()
>         .filter(entry -> entry.rateDate() != null)
>         .sorted(Comparator.comparing(RateHistoryEntry::rateDate))
>         .filter(entry -> !entry.rateDate().isAfter(effectiveDate))
>         .reduce((first, second) -> second)
>         .map(RateHistoryEntry::rateValue)
>         .orElseThrow(() -> new NoApplicableRateException(
>             "No rate found on or before " + effectiveDate));
> }
> ```
>
> O `.sorted(Comparator.comparing(RateHistoryEntry::rateDate))` garante ordem cronológica independentemente da ordem física no JSONB. Boa captura; é exatamente esse tipo de suposição implícita que gera bugs sutis em modernização.

**DEV:**
> Legal. Mas e o caso de mesma data? A gente deveria ordenar por data e depois por algum critério de desempate?

**Agente:**
> No legado, o critério de desempate é o índice da ocorrência, ou seja, a posição no grupo PE. Como usamos uma `List`, que preserva a ordem de inserção, podemos usar uma **ordenação estável**. Tanto `List.sort()` quanto `Stream.sorted()` em Java são estáveis, então elementos com chaves iguais mantêm sua ordem original.
>
> Portanto, se duas entradas tiverem a mesma data, a que aparece depois na lista, equivalente ao índice legado mais alto, continuará sendo a última ocorrência válida após a ordenação. Isso preserva o comportamento legado.
>
> Vou documentar essa premissa com um comentário:
>
> ```java
> // Stable sort preserves insertion order for same-date entries,
> // matching the legacy PE group occurrence-index tiebreaker behavior.
> .sorted(Comparator.comparing(RateHistoryEntry::rateDate))
> ```

**TL:**
> Bom. Continue.

**Agente:**
> **Passo 3 — Identificar dependências órfãs**
>
> Ao varrer `XYZRATES.nat`, encontrei um `CALLNAT` que não pertence ao bounded context de Rate Calculation:
>
> ```natural
> CALLNAT 'XYZAUDIT' #EMPL-ID #ACTION-CODE #OLD-RATE #NEW-RATE #TIMESTAMP
> ```
>
> Essa é uma chamada para um programa de **registro de auditoria** (`XYZAUDIT`). No legado, é uma chamada síncrona direta: o programa de processamento de taxa pausa, grava um registro de auditoria e continua.
>
> No nosso mapa de bounded contexts, auditoria pertence ao contexto **Audit Trail**, não ao Rate Calculation. Precisamos decidir como tratar essa comunicação entre contextos.
>
> Vejo três opções:
>
> 1. **Chamada direta de serviço:** `rateService.update()` chama `auditService.log()` de forma síncrona. É simples, mas cria acoplamento forte.
> 2. **Eventos de domínio:** `rateService.update()` publica um `RateChangedEvent`, e o contexto Audit Trail assina o evento. É menos acoplado e respeita a fronteira do bounded context.
> 3. **`ApplicationEvent` do Spring:** eventos leves em processo com `ApplicationEventPublisher`. Têm acoplamento moderado e não exigem infraestrutura externa de mensageria.
>
> **Não vou juntar esses conceitos silenciosamente.** O legado trata auditoria como uma chamada inline, mas a nossa arquitetura diz que ela é um contexto separado. A travessia de fronteira precisa ser explícita.
>
> **Qual abordagem vocês querem adotar?**

**DEV:**
> Gosto de eventos de domínio. Mantém os contextos limpos.

**TL:**
> Concordo, mas vamos usar `ApplicationEventPublisher` do Spring por enquanto. Não precisamos de Kafka ou RabbitMQ neste estágio; podemos evoluir depois se necessário.

**Agente:**
> Boa escolha pragmática. A tradução fica assim:
>
> ```java
> // Domain event record (shared kernel package)
> public record RateChangedEvent(
>     String employeeId, String actionCode,
>     BigDecimal oldRate, BigDecimal newRate, Instant timestamp) {}
>
> // In Rate Calculation service — publishes event
> @Service
> public class RateCalculationService {
>     private final ApplicationEventPublisher eventPublisher;
>     // ... constructor injection ...
>     public void updateRate(String employeeId, BigDecimal newRate) {
>         BigDecimal oldRate = findCurrentRate(employeeId);
>         // ... perform update ...
>         eventPublisher.publishEvent(new RateChangedEvent(
>             employeeId, "UPDATE", oldRate, newRate, Instant.now()));
>     }
> }
>
> // In Audit Trail context — listens AFTER_COMMIT only
> @Component
> public class AuditTrailListener {
>     @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
>     public void onRateChanged(RateChangedEvent event) { /* write audit log */ }
> }
> ```
>
> Ponto-chave: `@TransactionalEventListener(AFTER_COMMIT)` evita registros de auditoria fantasmas em transações que sofrerem rollback.
>
> **Devo continuar com o restante de `XYZRATES`, ou vocês querem revisar e ajustar primeiro?**

**DEV:**
> Vamos revisar com o time todo primeiro. A abordagem de eventos mexe na nossa estrutura de pacotes.

**TL:**
> Concordo. O mapeamento de `CALLNAT` para evento é exatamente o padrão que devemos documentar para os outros times.

---

## O que o time obteve

| Saída | Detalhe |
| --- | --- |
| **Mapeamento PE → JSONB** | O time escolheu JSONB em vez de `@OneToMany` com base no padrão de acesso. |
| **Loop `FOR` → stream** | Tradução idiomática em Java 21 com ordenação explícita e desempate por ordenação estável. |
| **Caso de borda capturado** | O Tech Lead identificou a premissa de ordenação; o agente adicionou ordenação explícita. |
| **`CALLNAT` → evento de domínio** | Dependência órfã de auditoria foi identificada e traduzida como padrão orientado a eventos. |
| **4 decisões de tradução documentadas** | Cada uma com padrão legado, padrão moderno e justificativa. |
| **Shared kernel identificado** | `RateChangedEvent` foi colocado em pacote compartilhado entre contextos. |

---

## Anti-padrões demonstrados

| Anti-padrão | Onde quase aconteceu | Como o agente/time evitou |
| --- | --- | --- |
| **Juntar conceitos silenciosamente** | O `CALLNAT` para `XYZAUDIT` poderia virar uma chamada direta de serviço. | O agente sinalizou violação de bounded context e apresentou opções. |
| **Premissa implícita de ordenação** | O stream assumia ordem cronológica sem `sort`. | O Tech Lead percebeu; o agente adicionou ordenação explícita e documentação. |
| **Over-engineering** | Eventos de domínio poderiam levar a uma discussão prematura sobre Kafka. | O TL escolheu `ApplicationEventPublisher`, uma opção pragmática. |
| **Tradução literal** | O loop `FOR` poderia virar um `for` indexado em Java. | O agente usou streams idiomáticos, preservando semântica, não sintaxe. |

---

## O que o time deve aprender

1. **Apresente opções, não uma sentença final.** O agente ofereceu JSONB e `@OneToMany` com trade-offs claros; o conhecimento do time sobre acesso aos dados decidiu a melhor opção.

2. **Casos de borda vivem no espaço entre legado e moderno.** O loop `FOR` tinha uma garantia implícita de ordenação pelo grupo PE. O stream Java não tinha. A observação do Tech Lead evitou um bug sutil.

3. **Dependências órfãs revelam arquitetura.** Ao encontrar um `CALLNAT` para auditoria, o agente não o colocou inline sem pensar; reconheceu a fronteira de bounded context e pediu uma decisão.

4. **Pragmatismo vence pureza.** `ApplicationEventPublisher` é mais simples que um event bus completo e ainda respeita a fronteira entre contextos. O time pode migrar para Kafka depois sem mudar o domínio.

5. **Ordenação estável é sua amiga.** A ordenação estável do Java preserva a ordem de inserção para chaves iguais, reproduzindo o desempate por índice de ocorrência do grupo PE legado.

6. **`@TransactionalEventListener(AFTER_COMMIT)` evita registros fantasmas.** Use `AFTER_COMMIT` para efeitos colaterais que só devem acontecer se a transação for concluída com sucesso.


---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="../README.md"><strong>@builder</strong></a><br/>
<sub>Mundo 3.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../../04-evolution/"><strong>@evolution</strong></a><br/>
<sub>Próximo mundo.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../../../README.md">Voltar ao Kit PT-BR</a></sub>

