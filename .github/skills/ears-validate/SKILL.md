<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
name: Validação EARS
description: 'Use ao validar requisitos contra padrões da notação EARS. Aciona com "EARS", "requirement review", "requirement quality", "shall statement", "REQ-ID".'
---

# Validação EARS

## Quando invocar
- "Revise estes requisitos quanto à conformidade EARS."
- "Este requisito é testável?"
- "Classifique este requisito por padrão EARS."

## Padrões EARS

| Padrão | Modelo |
|---|---|
| Ubiquitous | `O <sistema> deverá <resposta>.` |
| Event-driven | `Quando <gatilho>, o <sistema> deverá <resposta>.` |
| State-driven | `Enquanto <estado>, o <sistema> deverá <resposta>.` |
| Optional | `Onde <feature estiver incluída>, o <sistema> deverá <resposta>.` |
| Unwanted | `Se <condição indesejada>, então o <sistema> deverá <mitigação>.` |
| Complex | `Enquanto <estado>, quando <gatilho>, o <sistema> deverá <resposta>.` |

## Checklist de validação
- [ ] Exatamente um padrão por requisito.
- [ ] Sujeito não ambíguo ("the system", não "it").
- [ ] Resposta observável e testável.
- [ ] Nenhum "and" escondido que mascara dois requisitos em um.
- [ ] Sem detalhes de implementação ("use Redis") - apenas comportamento.
- [ ] Tem um REQ-ID no formato `REQ-NNN`.
- [ ] Tem pelo menos um critério de aceitação.
- [ ] **Tem um `source_legacy:` não vazio apontando para `01-arqueologia/legado-sifap/natural-programs/*.NSN`, `01-arqueologia/legado-sifap/adabas-ddms/*.ddm` ou `[GREENFIELD] + justification`.**

## Defeitos comuns
| Defeito | Exemplo | Correção |
|---|---|---|
| Ambíguo | "O sistema deve ser rápido." | "Quando uma pessoa usuária envia um formulário, o sistema deverá responder em até 500ms." |
| Composto | "Fazer login e enviar e-mail." | Dividir em dois requisitos. |
| Não testável | "O sistema deverá ser fácil de usar." | Substituir por métrica de UX mensurável. |
| Passivo | "Login deverá ser suportado." | "O sistema deverá aceitar autenticação por usuário/senha." |

## Template de saída
```markdown
### REQ-NNN (<pattern>)
<EARS statement>

**source_legacy**: 01-arqueologia/legado-sifap/natural-programs/<FILE>.NSN#L<start>-L<end>
_(ou `[GREENFIELD] <justificativa>` quando não houver paralelo legado)_

**Critérios de aceite**
- <criterion 1>
- <criterion 2>

**Rastreado a partir de**: US-NNN, ADR-NNN
**Prioridade**: P0 / P1 / P2
**Status**: proposto / aprovado / implementado / verificado
```

## Gate de qualidade
Rejeite qualquer requisito sem **REQ-ID**, **classificação de padrão**, **critérios de aceitação** ou **`source_legacy`**. O job de CI `legacy-traceability` em `.github/workflows/spec-quality.yml` aplica isso em todo PR.
