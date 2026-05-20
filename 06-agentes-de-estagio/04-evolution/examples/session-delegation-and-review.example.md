<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
title: "Exemplo: agente de Evolução em ação"
description: "Transcrição didática de sessão mostrando como o agente de Evolução escreve issues, delega ao Copilot Agent e revisa PR"
tags: [exemplo, transcricao, evolution, hackathon]
author: "Paula Silva, AI-Native Software Engineer, Americas Global Black Belt at Microsoft"
date: 2026-04-29
version: "1.0.0"
---

<!-- markdownlint-disable MD013 MD032 MD060 -->

# Exemplo: agente de Evolução em ação

![EXEMPLO Sessão real](https://img.shields.io/badge/EXEMPLO-Sessão%20real-7FBA00?style=for-the-badge) ![TIPO Transcript](https://img.shields.io/badge/TIPO-Transcript-1A1A1A?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../../../README.md) → [Agentes](../../README.md) → **Exemplo**

> **Para quem é isto?** Exemplo de sessão real com o agente de estágio. Use como referência de como conduzir a conversa.
>
> **Como ler:** veja o prompt do usuário → veja a resposta do agente → use o mesmo padrão na sua sessão.


## Cenário

**Participantes:** Tech Lead (TL), Product Owner (PO)
**Comandos do agente:** `/write-github-issue`, `/delegate-to-copilot-agent`, `/review-agent-pr`
**Objetivo:** criar uma issue bem estruturada no GitHub para adicionar validação de entrada ao endpoint de cálculo de taxa, delegar a implementação ao Copilot Agent e revisar o PR resultante com olhar crítico.

---

## Preparação

1. Abra o GitHub Copilot Chat no VS Code.
2. Confirme que o estágio Builder foi concluído: o endpoint de cálculo de taxa existe, mas ainda não valida a entrada.
3. Ative a persona do agente de Evolução.
4. Confirme que o repositório do time está conectado ao GitHub e com Copilot Agent habilitado.
5. Verifique se o mapa de bounded contexts e a especificação estão em `specs/001-rate-processing/`.

---

## Prompt de abertura

**TL:** `@evolution /write-github-issue — precisamos adicionar validação de entrada ao endpoint de cálculo de taxa. Hoje ele aceita qualquer payload sem verificar campos obrigatórios, faixas de valor ou formatos de data.`

---

## Transcrição

### Parte 1: escrever a issue no GitHub

**Agente:**
> Vou rascunhar uma issue estruturada. Primeiro vou buscar contexto na especificação e nos artefatos de design.
>
> Em `SPECIFICATION.md`, encontrei estes requisitos relevantes:
>
> - **REQ-012:** validação de entrada na camada de controller com `@Valid` + Bean Validation.
> - **REQ-018:** valores de taxa devem ser não negativos e menores ou iguais a 9999.99, com origem na BR-004.
> - **REQ-023:** datas efetivas não podem estar no futuro.
>
> **Rascunho da issue:**
>
> **Título:** Adicionar validação de entrada ao endpoint de cálculo de taxa
> **Rótulos:** `enhancement`, `backend`, `rate-calculation`
>
> **Contexto:** O endpoint `POST /api/v1/rates/calculate` aceita qualquer payload JSON sem validação, violando a REQ-012.
>
> **Critérios de aceite:**
>
> - [ ] `employeeId` obrigatório, não vazio, compatível com `^[A-Z]{2}\d{6}$`.
> - [ ] `effectiveDate` obrigatório, ISO-8601 válido e não no futuro.
> - [ ] `rateValue` obrigatório, >= 0 e <= 9999.99 (REQ-018 / BR-004).
> - [ ] `reasonCode` obrigatório, um de: `ADJ`, `PRO`, `NEW`, `COR`.
> - [ ] HTTP 400 com resposta estruturada listando TODAS as falhas de validação.
> - [ ] Formato: `{ "errors": [{ "field": "...", "message": "..." }] }`.
> - [ ] Testes existentes continuam passando; novos testes cobrem cada regra isolada e combinada.
>
> **Rastreabilidade:** REQ-012, REQ-018, REQ-023.
>
> **Antes de publicar, quero confirmar alguns pontos:**
>
> 1. O padrão de ID de funcionário `^[A-Z]{2}\d{6}$` está correto? Inferi isso a partir dos dados de teste em `RESEARCH.md`, mas não vi especificação explícita.
> 2. `ADJ`, `PRO`, `NEW`, `COR` são a lista completa de códigos de motivo? O legado tinha esses quatro, mas pode haver outros.
> 3. O limite 9999.99 deve valer para o valor de taxa de **entrada**, ou apenas para o **resultado calculado**? A REQ-018 fala em "proteção de acumulação", sugerindo saída, mas validar entrada também faz sentido.

**PO:**
> Boas perguntas.
> 1. O padrão do ID está correto: código de departamento com duas letras mais sequência de seis dígitos.
> 2. Existe um quinto código de motivo: `TRN`, para transferências. Adicione.
> 3. Aplique o limite tanto na entrada quanto na saída. Ninguém deve conseguir enviar uma taxa acima de 9999.99.

**TL:**
> Concordo com os três pontos. Atualize a issue e publique.

**Agente:**
> Atualizei com `TRN` e limite duplo. A issue está pronta para publicação.
>
> **Issue criada: #42 — "Adicionar validação de entrada ao endpoint de cálculo de taxa"**

---

### Parte 2: delegar ao Copilot Agent

**TL:** `@evolution /delegate-to-copilot-agent issue #42`

**Agente:**
> Payload de delegação preparado.
>
> **Payload de delegação:**
>
> **Destino:** GitHub Copilot Agent · **Issue:** #42
>
> **Instruções:**
>
> 1. **Criar** `RateCalculationRequest.java`: `record` com anotações `@NotBlank`, `@Pattern`, `@PastOrPresent`, `@DecimalMin` e `@DecimalMax`.
> 2. **Modificar** `RateCalculationController.java`: adicionar `@Valid` ao parâmetro do request body.
> 3. **Criar** `ValidationErrorHandler.java`: `@RestControllerAdvice`, coletar TODOS os erros de campo, não retornar apenas no primeiro erro, e devolver HTTP 400 com lista.
> 4. **Criar** `RateCalculationRequestValidationTest.java`: testar cada regra, múltiplas violações, valores de fronteira e os cinco códigos de motivo.
>
> **Restrições:** sem novas dependências, sem mudanças na camada de serviço e todos os testes existentes devem continuar passando.
>
> **Pronto para delegar. Devo enviar isso ao Copilot Agent agora?**

**TL:**
> Sim, delegue.

**Agente:**
> Delegação enviada. O Copilot Agent trabalhará em uma branch `copilot/issue-42` e abrirá um PR quando terminar.
>
> Recomendo que o time revise o PR em até 30 minutos. PRs gerados por agente se beneficiam de revisão rápida, enquanto o contexto ainda está fresco.
>
> *(O tempo passa: aproximadamente 15 minutos.)*

---

### Parte 3: revisar o PR do agente

**TL:** `@evolution /review-agent-pr #43 — Copilot Agent opened PR #43 for issue #42`

**Agente:**
> Vou iniciar uma revisão estruturada do PR #43. Vou comparar com os critérios de aceite da issue #42 e procurar problemas comuns em código gerado por agente.
>
> **Estrutura da revisão:**
>
> 1. O PR atende todos os critérios de aceite?
> 2. Há problemas de corretude?
> 3. Há problemas de qualidade?
> 4. Os testes são suficientes?
>
> ---
>
> **Verificação dos critérios de aceite:**
>
> | Critério | Status |
> | --- | --- |
> | Validação de `employeeId` | ✅ `@NotBlank` + `@Pattern("^[A-Z]{2}\\d{6}$")` |
> | Validação de `effectiveDate` | ✅ `@NotNull` + `@PastOrPresent` |
> | Faixa de `rateValue` | ✅ `@DecimalMin("0")` + `@DecimalMax("9999.99")` |
> | Validação de `reasonCode` | ✅ Anotação customizada `@ValidReasonCode` com `ADJ`, `PRO`, `NEW`, `COR`, `TRN` |
> | HTTP 400 com todos os erros | ⚠️ Veja o achado abaixo |
> | Resposta de erro estruturada | ✅ Formato correto |
> | Testes existentes passam | ✅ CI verde |
> | Novos testes presentes | ⚠️ Veja o achado abaixo |
>
> ---
>
> **Achado 1 — PRECISA CORRIGIR 🔴**
>
> **Arquivo:** `ValidationErrorHandler.java`, linha 24
>
> ```java
> @ExceptionHandler(MethodArgumentNotValidException.class)
> public ResponseEntity<Map<String, Object>> handleValidationErrors(
>         MethodArgumentNotValidException ex) {
>     FieldError fieldError = ex.getBindingResult().getFieldErrors().get(0);
>     Map<String, Object> error = Map.of(
>         "field", fieldError.getField(),
>         "message", fieldError.getDefaultMessage()
>     );
>     return ResponseEntity.badRequest().body(Map.of("errors", List.of(error)));
> }
> ```
>
> **Problema:** o handler retorna apenas o **primeiro** erro de campo (`.get(0)`), não todos. O critério de aceite exige listar TODAS as falhas de validação.
>
> **Correção:** transformar todos os erros de campo em uma lista:
>
> ```java
> @ExceptionHandler(MethodArgumentNotValidException.class)
> public ResponseEntity<Map<String, Object>> handleValidationErrors(
>         MethodArgumentNotValidException ex) {
>     List<Map<String, String>> errors = ex.getBindingResult()
>         .getFieldErrors()
>         .stream()
>         .map(fieldError -> Map.of(
>             "field", fieldError.getField(),
>             "message", fieldError.getDefaultMessage()
>         ))
>         .toList();
>     return ResponseEntity.badRequest().body(Map.of("errors", errors));
> }
> ```
>
> ---
>
> **Achado 2 — DEVE CORRIGIR 🟡**
>
> **Arquivo:** `RateCalculationRequestValidationTest.java`, linha 78
>
> ```java
> @Test
> void shouldRejectMultipleInvalidFields() {
>     RateCalculationRequest request = new RateCalculationRequest(
>         "",           // invalid employeeId
>         LocalDate.now().plusDays(1),  // future date
>         new BigDecimal("10000.00"),   // exceeds max
>         "XXX"         // invalid reason code
>     );
>     Set<ConstraintViolation<RateCalculationRequest>> violations =
>         validator.validate(request);
>     assertThat(violations).isNotEmpty();
> }
> ```
>
> **Problema:** o teste usa `.isNotEmpty()` em vez de `.hasSize(4)`. Com 4 campos inválidos, devemos afirmar exatamente 4 violações para detectar short-circuit.
>
> **Correção:** `assertThat(violations).hasSize(4);`
>
> ---
>
> **Achado 3 — DEVE CORRIGIR 🟡**
>
> **Arquivo:** `RateCalculationRequestValidationTest.java`
>
> **Problema:** o teste de código de motivo inválido verifica apenas que existe uma violação, mas não valida o **conteúdo da mensagem de erro**. A mensagem deve listar os códigos válidos, por exemplo: "deve ser um de: ADJ, PRO, NEW, COR, TRN", em vez de um genérico "valor inválido".
>
> **Asserção ausente:** adicione `.contains("ADJ", "PRO", "NEW", "COR", "TRN")` ao teste de código de motivo.
>
> ---
>
> **Resumo da revisão:**
>
> | Categoria | Quantidade | Detalhes |
> | --- | --- | --- |
> | 🔴 Precisa corrigir | 1 | Handler de erro retorna um único erro em vez de coletar todos. |
> | 🟡 Deve corrigir | 2 | Asserção de contagem incompleta; falta validar a mensagem de erro. |
> | 🟢 Parece bom | 5 | Request record, anotações, mudança no controller, validator de motivo e testes básicos. |
>
> **Recomendação:** solicitar mudanças. O achado obrigatório é um problema funcional de corretude. Os demais melhoram a confiabilidade dos testes.
>
> **É por isso que revisão humana no loop importa.** O agente implementou corretamente 90% dos requisitos, mas o handler de erro tinha um bug sutil que só aparece com múltiplos campos inválidos.

**TL:**
> Boa captura no handler de erro. Esse é exatamente o tipo de bug que passaria se a gente fizesse merge sem revisar.

**PO:**
> A asserção da mensagem de erro também é importante. O time de frontend precisa de mensagens previsíveis para mostrar às pessoas usuárias. "Invalid value" não ajuda.

**TL:**
> Vamos solicitar mudanças. Eu corrijo o bloqueador obrigatório e peço para o agente melhorar os testes.

**Agente:**
> Perfeito. Vou preparar o comentário de revisão com os três achados vinculados às linhas específicas de código.
>
> Uma observação para o time: **esse padrão de revisão — delegar e depois revisar criticamente — é o fluxo recomendado para código gerado por agente.** O agente cuida do boilerplate e da estrutura; a pessoa revisora captura nuances. Nenhum dos dois é suficiente sozinho.
>
> | Fase | Ator | Força |
> | --- | --- | --- |
> | Escrita da issue | Humano + agente | Humano fornece intenção; agente estrutura e conecta à spec. |
> | Implementação | Agente | Geração rápida de boilerplate e padrões consistentes. |
> | Revisão | Humano + agente | Agente detecta problemas estruturais; humano detecta problemas semânticos. |
> | Correção | Humano | Ajustes finos que exigem entender o porquê. |

---

## O que o time obteve

| Saída | Detalhe |
| --- | --- |
| **Issue estruturada no GitHub** | 8 critérios de aceite, 3 rastros para REQ-ID e notas técnicas de implementação. |
| **Payload de delegação para agente** | Instruções detalhadas com orientação por arquivo e restrições. |
| **Revisão crítica de PR** | 1 correção obrigatória e 2 correções recomendadas identificadas em código gerado por agente. |
| **Human-in-the-loop demonstrado** | O agente gerou o código; pessoas capturaram bugs semânticos. |
| **Loop de confirmação** | O agente fez 3 perguntas de clarificação antes de publicar a issue. |
| **Alinhamento do time** | PO confirmou regras de negócio; TL confirmou abordagem técnica. |

---

## Anti-padrões demonstrados

| Anti-padrão | Onde quase aconteceu | Como o agente/time evitou |
| --- | --- | --- |
| **Merge cego de PR de agente** | O código gerado parecia correto à primeira vista. | A revisão estruturada do agente de Evolução capturou o bug do handler de erro único. |
| **Critérios de aceite vagos** | A issue poderia dizer apenas "adicionar validação". | O agente buscou REQ-IDs na spec e escreveu critérios testáveis. |
| **Códigos de motivo incompletos** | O agente tinha apenas 4 códigos vindos da análise do legado. | O agente pediu confirmação; o PO adicionou o quinto código, `TRN`. |
| **Padrões de entrada assumidos** | O formato do ID de funcionário foi inferido, não confirmado. | O agente sinalizou a inferência e pediu validação ao time. |
| **Testes que passam sem verificar o bastante** | O teste de múltiplos campos usava `.isNotEmpty()` em vez de `.hasSize(4)`. | A revisão capturou a asserção fraca. |
| **Mensagens de erro genéricas** | O agente usou mensagens padrão do Bean Validation. | A revisão sinalizou a necessidade de mensagens acionáveis e específicas. |

---

## O que o time deve aprender

1. **Escreva a issue antes de delegar.** Critérios de aceite claros são o fator mais importante para qualidade de código gerado por agente.

2. **Sempre confirme regras de negócio antes de publicar.** As 3 perguntas do agente revelaram um código de motivo ausente, esclareceram um limite ambíguo e confirmaram o formato do ID.

3. **Código gerado por agente tem falhas típicas.** Agentes costumam tratar o primeiro erro e retornar, o que passa em testes de campo único, mas falha em cenários com múltiplos campos inválidos.

4. **Revise testes com o mesmo rigor do código de produção.** `.isNotEmpty()` quase nunca é suficiente em testes de validação.

5. **O payload de delegação é um contrato.** Quanto mais específicas forem as instruções, melhor tende a ser a saída.

6. **Humano no loop não é opcional.** O bug obrigatório teria chegado à produção sem revisão.

7. **A cadeia de três passos é o padrão de Evolução.** `/write-github-issue` → `/delegate-to-copilot-agent` → `/review-agent-pr`. Pular qualquer etapa reduz a qualidade.


---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="../README.md"><strong>@evolution</strong></a><br/>
<sub>Mundo 4.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../../README.md"><strong>Agentes</strong></a><br/>
<sub>Voltar à visão.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../../../README.md">Voltar ao Kit PT-BR</a></sub>

