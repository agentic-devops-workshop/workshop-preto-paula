<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
mode: ask
model: claude-opus-4-6
description: "Detecte contradições entre requisitos em SPECIFICATION.md — mesma feature, regras diferentes — antes que virem bugs em produção."
---

# /contradiction-check

## Objetivo

Você é o requirements engineer auditando `SPECIFICATION.md` em busca de **contradições**: pares de requisitos que não podem ser satisfeitos ao mesmo tempo. Contradições descobertas agora são correções de spec; contradições descobertas em produção são incidentes. O entregável é uma lista de candidatos a conflito com evidência, severidade e resolução proposta.

## Entradas

Peça ao usuário o que estiver faltando.

- O arquivo de spec (`specs/<NNN>-<feature>/SPECIFICATION.md`).
- Quaisquer specs pai relacionadas cujos REQ-IDs sejam referenciados por esta.
- A CONSTITUTION (`specs/<NNN>-<feature>/CONSTITUTION.md`) — contradições também devem ser verificadas contra regras constitucionais.
- Qualquer log de esclarecimento já produzido por `/clarify`.

## Processo

1. **Indexe todos os requisitos.** Para cada `REQ-ID`, capture: padrão EARS, gatilho (evento/estado/condição), ação, ator, resultado e limites quantitativos.
2. **Faça varredura em pares dentro de cada domínio.** Agrupe REQ-IDs por domínio (`PAY-*`, `BEN-*` etc.). Compare cada par. Pares entre domínios vêm depois.
3. **Procure as quatro contradições clássicas.**
 - **Contradição direta** — REQ-A diz "the system shall X under condition C"; REQ-B diz "the system shall not X under condition C."
 - **Conflito de limite** — REQ-A diz "respond within 200 ms"; REQ-B diz "perform 5 sequential checks each up to 80 ms" — os orçamentos não podem ser cumpridos juntos.
 - **Conflito de estado** — REQ-A permite uma ação enquanto está no estado S1; REQ-B a proíbe durante o estado sobreposto S2 ⊆ S1.
 - **Conflito de ator** — REQ-A concede permissão ao papel R1; REQ-B proíbe a mesma operação ao papel R2 onde R2 ⊇ R1.
4. **Verifique contra a CONSTITUTION.** Qualquer requisito que viole uma regra constitucional é uma contradição com a própria constituição (normalmente regras C de segurança, dados ou compliance).
5. **Verifique contra invariantes do legado.** Se um REQ contradiz comportamento imposto pelo SIFAP legado (documentado em `02-cenario-sifap-legado/legacy-docs/REGRAS-NEGOCIO-2012.md`), sinalize como risco de regressão.
6. **Pontue a severidade.**
 - **Critical** — contradição direta, sem implementação possível que satisfaça ambos.
 - **Major** — conflito de limite ou estado resolvível apenas alterando um REQ.
 - **Minor** — divergência terminológica escondendo um acordo real.
7. **Proponha resoluções.** Para cada achado, sugira uma opção: (a) mesclar REQs, (b) dividir REQs por subcondição, (c) restringir o escopo de um REQ, (d) escalar para o product owner.

## Saída

Um relatório Markdown:

```markdown
## Relatório de Contradições — <feature>

### Resumo
- Requisitos analisados: 47
- Achados: 6 (Critical 1, Major 3, Minor 2)
- Maior severidade: REQ-PAY-014 vs REQ-PAY-019

### Critical
| # | REQ-A | REQ-B | Tipo | Evidência | Resolução proposta |
|---|-------|-------|------|----------|---------------------|
| 1 | REQ-PAY-014 ("Se o beneficiário estiver suspenso, então o sistema não deverá desembolsar") | REQ-PAY-019 ("Quando uma programação recorrente disparar, o sistema deverá desembolsar para todos os beneficiários inscritos") | Contradição direta (sobreposição quando beneficiário suspenso está inscrito em programação recorrente) | EARS unwanted vs event-driven; estado `SUSPENDED` não excluído em REQ-PAY-019 | Ajustar REQ-PAY-019: "...para todos os beneficiários inscritos, exceto os que estão no estado `SUSPENDED`. (Veja REQ-PAY-014.)" |

### Major
| # | REQ-A | REQ-B | Tipo | Evidência | Resolução proposta |
|---|-------|-------|------|----------|---------------------|
| 2 | REQ-PAY-006 ("responder a /payments em até 250 ms p95") | REQ-PAY-008 + REQ-PAY-009 (duas chamadas externas sequenciais de 200 ms) | Conflito de limite | 200 + 200 > 250 | Paralelizar (novo ADR) ou relaxar REQ-PAY-006 para 500 ms; encaminhar ao produto. |

### Minor
| # | REQ-A | REQ-B | Tipo | Evidência | Resolução proposta |
|---|-------|-------|------|----------|---------------------|
| 3 | REQ-BEN-007 usa "user" | REQ-BEN-008 usa "beneficiary" | Desvio terminológico | Mesmo ator em ambos | Substituir "user" por "beneficiary" globalmente. |

### Conflitos constitucionais
| # | REQ | Regra | Conflito |
|---|-----|------|---------|
| — | nenhum encontrado | | |

### Riscos de regressão legada
| # | REQ | Invariante legado | Conflito |
|---|-----|------------------|---------|
| 4 | REQ-PAY-021 ("arredondar para 2 casas decimais, half-up") | `CALCBENF.NSN` arredonda half-down para saldos negativos | Risco de mudança de comportamento; sinalizar para produto. |

### Próximo passo recomendado
Resolver Critical e Major antes da aprovação da spec. Levar (1) e (2) ao product owner.
```

## Exemplo trabalhado

**Entrada:** Auditar `specs/003-payment-processing/SPECIFICATION.md` (47 REQs).

**Resposta esperada:** a estrutura acima, com uma contradição crítica entre REQs de unwanted-behavior e event-driven, dois conflitos de limite e um alerta de regressão legada para comportamento de arredondamento.

## Antipadrões

- Relatar "the spec is contradictory" sem nomear os pares. Revisores não conseguem agir.
- Verificar apenas dentro de um domínio. Muitas contradições cruzam domínios.
- Ignorar a constituição. Conflitos constitucionais têm severidade maior que conflitos entre REQs pares.
- Confundir ambiguidade com contradição. Ambiguidade é para `/clarify`; contradição é incompatibilidade.
- Resolver silenciosamente na própria cabeça. Sempre exponha e encaminhe — mesmo quando parecer "obvious".
- Pular verificações de regressão legada. A modernização do SIFAP vive ou morre pela fidelidade ao legado.
- Tratar conflitos de limite como "can fix in design". Se a matemática não fecha, o REQ está errado.

## Critérios de sucesso

- [ ] Todo achado cita dois REQ-IDs (ou um REQ-ID e uma regra constitucional, ou um REQ-ID e um invariante legado).
- [ ] Achados classificados por tipo (Direct / Threshold / State / Actor) e severidade (Critical / Major / Minor).
- [ ] Cada achado tem uma resolução proposta em uma linha.
- [ ] Conflitos constitucionais verificados.
- [ ] Riscos de regressão legada verificados contra `02-cenario-sifap-legado/legacy-docs/`.
- [ ] Achados Critical e Major sinalizados para resolução antes do sign-off da fase.
- [ ] Saída pronta para colar no PR da spec ou em um ticket de esclarecimento.
