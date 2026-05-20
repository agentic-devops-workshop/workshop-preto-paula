<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
name: Designer de estratégia de testes
description: "Use quando pedirem para desenhar uma estratégia de testes, escolher o formato da pirâmide de testes, definir metas de cobertura ou avaliar investimentos em testes nas camadas unit / integration / E2E."
---

# Designer de estratégia de testes

## Quando invocar
- "Design a test strategy for…"
- "How much unit vs integration vs E2E testing?"
- "What coverage target is right?"
- "Audit our test pyramid."

## Workflow
1. **Inventarie** o código sob teste: módulos, APIs públicas, integrações externas, caminhos críticos.
2. **Classifique o risco** por módulo (P0 / P1 / P2) com base no blast radius se ele quebrar.
3. **Aloque a pirâmide**: mire em 70% unit, 20% integration, 10% E2E como ponto de partida; justifique desvios.
4. **Defina metas de cobertura**: baseline de 80% de cobertura de linhas, 90% para módulos P0, cobertura de branches acompanhada separadamente.
5. **Defina o orçamento de flaky tests**: taxa flaky máxima de 1%; qualquer coisa acima dispara quarentena.
6. **Escolha ferramentas por camada**: unit (Vitest/JUnit/pytest), integration (Testcontainers), E2E (Playwright).
7. **Saída**: um doc de estratégia de uma página com metas por camada, ferramentas, thresholds de cobertura e regras de quarentena.

## Heurísticas
- Se um teste E2E puder ser reescrito como integration + contract test, faça isso - E2E é caro e flaky.
- Contract tests vencem mocks para qualquer coisa que cruza uma fronteira de serviço.
- Mutation testing (Stryker, PIT) é a única forma honesta de detectar testes fantasmas.

## Referências
- [Google Testing Blog - Test Sizes](https://testing.googleblog.com/2010/12/test-sizes.html)
- [ISTQB Foundation Syllabus](https://www.istqb.org/certifications/certified-tester-foundation-level)
- [Martin Fowler - Practical Test Pyramid](https://martinfowler.com/articles/practical-test-pyramid.html)
