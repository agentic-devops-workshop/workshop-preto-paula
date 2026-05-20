<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
mode: ask
model: claude-sonnet-4-6
description: "Converta requisitos informais para notação EARS com rastreabilidade obrigatória ao legado"
---

# /ears-convert

## Pré-checagem dura (workshop SIFAP)
Antes de escrever qualquer EARS, **exija uma fonte legada** para cada declaração de entrada. Fontes aceitáveis:
- um arquivo em `01-arqueologia/legado-sifap/natural-programs/*.NSN` (preferido, com intervalo de linhas)
- um arquivo em `01-arqueologia/legado-sifap/adabas-ddms/*.ddm`
- o marcador literal `[GREENFIELD]` com uma justificativa em uma linha

Se o usuário fornecer uma declaração **sem** identificar uma fonte legada, NÃO produza uma EARS. Responda:

> "Ainda não posso emitir esta EARS. Informe qual arquivo em `01-arqueologia/legado-sifap/` é a fonte (por exemplo, `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN`) ou marque como `[GREENFIELD]` com uma justificativa em uma linha. O CI rejeita EARS sem `source_legacy`."

Somente depois que toda declaração tiver uma fonte aceitável, prossiga para os passos abaixo.

## Tarefa
Converter uma lista de requisitos informais para notação EARS, classificar cada um por padrão, anexar a fonte legada e sinalizar qualquer coisa que não possa ser expressa em EARS.

## Passos
1. Para cada declaração de entrada, identifique o padrão: Ubiquitous, Event-driven, State-driven, Optional, Unwanted ou Complex.
2. Reescreva a declaração usando o template EARS correto:
 - Ubiquitous: `O sistema deverá ...`
 - Event-driven: `QUANDO <gatilho> o sistema deverá ...`
 - State-driven: `ENQUANTO <estado> o sistema deverá ...`
 - Optional: `ONDE <feature> estiver incluída o sistema deverá ...`
 - Unwanted: `SE <indesejado> ENTÃO o sistema deverá ...`
 - Complex: combine os padrões acima com `AND / OR` dentro da cláusula de gatilho.
3. Atribua um REQ-ID no formato `REQ-<DOMAIN>-NNN`.
4. Anexe a linha `source_legacy:` fornecida pelo usuário (não invente).
5. Se um requisito não puder se tornar testável (vago, contraditório ou sem métrica), sinalize como `NEEDS-CLARIFICATION` com a ambiguidade específica.

## Saída
Para cada requisito, emita o bloco YAML abaixo (não uma tabela plana) para que o job de CI `legacy-traceability` possa processá-lo:

```yaml
REQ-<DOMAIN>-NNN:
 pattern: <ubiquitous|event-driven|state-driven|optional|unwanted|complex>
 text: "<declaração EARS>"
 source_legacy: <path legado com intervalo de linhas, ou [GREENFIELD] + justificativa>
 original: "<entrada literal>"
 notes: "<opcional, por exemplo, motivo de NEEDS-CLARIFICATION>"
```

## Gate de Qualidade
- [ ] 100% das declarações de entrada processadas
- [ ] **Todo REQ-ID emitido tem uma linha `source_legacy:` não vazia**
- [ ] Nenhuma declaração EARS contém palavras como "appropriate", "reasonable", "fast" sem uma métrica
- [ ] Todo REQ-ID é único
- [ ] Itens `NEEDS-CLARIFICATION` têm uma pergunta específica anexada
