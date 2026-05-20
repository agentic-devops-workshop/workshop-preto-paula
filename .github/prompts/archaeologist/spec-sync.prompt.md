<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
mode: ask
model: claude-sonnet-4-6
description: "Sincronize SPECIFICATION.md com o codebase atual"
---

# /spec-sync

## Tarefa
Detecte drift entre SPECIFICATION.md e a implementação. Produza um relatório de sincronização e uma proposta de atualização da spec.

## Passos
1. Faça parse dos REQ-IDs de SPECIFICATION.md.
2. Use grep no codebase para referências a REQ-ID (em comentários, nomes de testes, mensagens de commit).
3. Para cada REQ-ID: classifique como Implemented (tem código + teste), Partial (apenas código), Orphaned (sem código), Undocumented (o código referencia um REQ-ID desconhecido).
4. Para drift comportamental: escolha 3 fluxos representativos, compare a spec com o caminho real do código.
5. Proponha adições/atualizações em SPECIFICATION.md para quaisquer itens Undocumented encontrados.

## Saída
- Tabela de drift: `REQ-ID | Status | Evidência (arquivo:linha) | Ação`
- Patch proposto da spec em bloco diff cercado por crases
- Resumo "Top 3 drifts by risk"

## Gate de Qualidade
- [ ] Todo REQ-ID na spec está classificado
- [ ] Todo achado Undocumented tem um REQ-ID proposto e uma declaração EARS
- [ ] Evidência cita arquivo:linha exato
- [ ] O patch proposto compila contra a estrutura atual de SPECIFICATION.md
