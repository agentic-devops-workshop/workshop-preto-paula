<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
mode: ask
model: claude-sonnet-4-6
description: "Valide uma implementação de API contra seu contrato OpenAPI"
---

# /api-validate

## Tarefa
Valide se uma implementação de API corresponde ao seu contrato OpenAPI/AsyncAPI e exponha todo drift.

## Passos
1. Carregue o contrato (openapi.yaml / asyncapi.yaml) e a implementação (controllers, handlers).
2. Para cada operação no contrato, verifique: path, method, request schema, response schema, error codes, auth scheme.
3. Para cada endpoint na implementação, verifique se ele existe no contrato (detecte endpoints não documentados).
4. Valide request/response schema com exemplos reais, se disponíveis.
5. Classifique drift como: breaking (remove/altera campo), additive (novo campo opcional), metadata (apenas descrição).

## Saída
Tabela Markdown: `Endpoint | Tipo de Drift | Severidade | Local da Correção (contrato ou código)`.

## Gate de Qualidade
- [ ] Toda operação do contrato foi verificada (100% de cobertura)
- [ ] Todo endpoint da implementação foi verificado contra o contrato
- [ ] Drift breaking está destacado separadamente de drift additive
- [ ] O local do fix (contrato vs. código) está explícito para cada item
