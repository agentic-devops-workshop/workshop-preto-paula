<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
name: Refatorar com segurança
description: "Use ao refatorar código legado, extrair um serviço ou fazer mudanças que preservam comportamento. Aciona com 'refactor', 'legacy code', 'strangler fig', 'characterization test', 'mikado method'."
---

# Refatorar com segurança

## Quando invocar
- Ao trabalhar em código sem testes suficientes.
- Ao dividir um monólito / extrair um serviço.
- Quando uma mudança é "uma linha", mas toca um caminho assustador.

## Primeira regra
**Refatoração preserva comportamento.** Se você não consegue provar que o comportamento foi preservado, não é refatoração - é reescrita. Coloque testes de caracterização em prática primeiro.

## O workflow
1. **Caracterize** - escreva testes que fixem o comportamento atual, incluindo peculiaridades. Não corrija bugs ainda; o objetivo é uma rede de segurança, não correção.
2. **Passos pequenos e reversíveis** - uma transformação que preserva comportamento por vez. Faça commit entre cada uma.
3. **Mantenha verde** - execute testes após cada passo. Reverta imediatamente se ficar vermelho e você não souber por quê.
4. **Separe commits de refatoração de commits de mudança de comportamento** - revisores conseguem focar, o bisect continua útil.
5. **Integre com frequência** - branches de refatoração de vida longa apodrecem.

## Padrões
### Strangler Fig (para sistemas)
1. Coloque uma façade (proxy, router, feature flag) na frente do sistema antigo.
2. Direcione uma fatia fina de tráfego para a nova implementação.
3. Faça a nova implementação crescer fatia por fatia; reduza a antiga.
4. Apague a antiga quando seu tráfego chegar a zero.

### Mikado Method (para código)
1. Escreva o objetivo.
2. Tente de forma ingênua; anote o que quebra como **pré-requisitos**.
3. Reverta. Ataque primeiro um pré-requisito. Faça recursão.
4. As folhas são concluídas primeiro; o objetivo original cai por último.

### Branch by Abstraction
Introduza uma interface, migre chamadores para ela, troque implementações e aposente a antiga - tudo sem uma branch de vida longa.

## Testes de caracterização - como
- Execute o código com entradas representativas, registre a saída (golden files / snapshot tests).
- Prefira observar de fora (HTTP, CLI, estado do DB) - resiliente a refatoração interna.
- Cubra também os casos estranhos; são eles que quebram.
- Aceite que alguns comportamentos são *bugs que você agora está fixando* - marque-os, corrija depois que a rede de segurança existir.

## Antipadrões
- PRs de "refactor" que também corrigem bugs, mudam APIs e renomeiam arquivos - impossíveis de revisar, impossíveis de reverter.
- Reescrita big-bang sem entrega por meses.
- Deletar código antigo antes que o novo código atenda 100% do tráfego.
- Refatorar sem testes, confiando em verificação manual de caminho feliz.

## Referências
- [Martin Fowler - Refactoring (2nd ed.)](https://martinfowler.com/books/refactoring.html)
- [Michael Feathers - Working Effectively with Legacy Code](https://www.oreilly.com/library/view/working-effectively-with/0131177052/)
- [Mikado Method](https://mikadomethod.info/)
- [Fowler - Strangler Fig Application](https://martinfowler.com/bliki/StranglerFigApplication.html)
