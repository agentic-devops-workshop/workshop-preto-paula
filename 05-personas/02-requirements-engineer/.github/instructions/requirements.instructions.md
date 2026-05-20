<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
applyTo: "docs/**/*.md"
---

# Convenções de Documentação de Requisitos

## Formato
- Notação EARS para requisitos formais
- Given/When/Then para critérios de aceitação
- Numeração sequencial dentro de features
- MUST/SHALL para obrigatório, SHOULD para recomendado
- **Todo requisito carrega uma linha `source_legacy:`** apontando para `01-arqueologia/legado-sifap/natural-programs/*.NSN`, `01-arqueologia/legado-sifap/adabas-ddms/*.ddm` ou `[GREENFIELD] + justification`. O CI rejeita requisitos sem essa linha.
