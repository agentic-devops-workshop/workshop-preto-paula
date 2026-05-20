<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
name: Triagem de testes flaky
description: "Use quando um teste for intermitente, quando pedirem para investigar instabilidade no CI, 'quarantine a flaky test', ou para criar um dashboard de flaky tests."
---

# Triagem de testes flaky

## Quando invocar
- O CI falha e o re-run passa.
- "This test is flaky - help me fix it."
- "Build a flaky-test quarantine process."

## Workflow de diagnóstico
1. **Reproduza**: rode o teste isoladamente 50× com `--repeat-each 50` (Playwright) ou `pytest --count=50`. Se ele falhar <1×, provavelmente depende da ordem.
2. **Categorize** a causa raiz do flake:
 - **Async/timing** - await ausente, race condition, sleep hard-coded
 - **Order dependency** - estado compartilhado, DB não limpo, singleton global
 - **External dependency** - rede, relógio, filesystem
 - **Non-determinism** - iteração sobre mapa não ordenado, random seed
 - **Resource contention** - porta, file lock, colisão de worker paralelo
3. **Corrija na raiz**: substitua sleeps por esperas explícitas, isole estado, fixe random seeds, use portas com escopo de teste.
4. **Coloque em quarentena se não der para corrigir em <1 dia**: mova para uma tag `flaky/`, abra uma issue de rastreamento, defina um SLA de 30 dias para corrigir ou excluir.

## Política de quarentena
- Testes em quarentena rodam, mas não quebram o build.
- Qualquer coisa em quarentena por >30 dias é excluída; um teste que não pode ser corrigido é pior que nenhum teste.
- Dashboard: acompanhe a taxa de flake por teste ao longo de 100 execuções. Qualquer coisa >5% entra em quarentena automaticamente.

## Anti-padrões
- `sleep(1000)` - sempre errado.
- Repetir a asserção com um loop - esconde bugs de timing.
- `@Retry(3)` - mascara flakes e recompensa testes ruins.

## Referências
- [Google - Flaky Tests at Google](https://testing.googleblog.com/2016/05/flaky-tests-at-google-and-how-we.html)
- [Microsoft Research - Empirical Study of Flaky Tests](https://www.microsoft.com/en-us/research/publication/an-empirical-analysis-of-flaky-tests/)
