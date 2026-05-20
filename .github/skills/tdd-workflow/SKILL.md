<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
name: Workflow de TDD
description: "Use ao praticar desenvolvimento orientado por testes, escrever primeiro um teste que falha ou orientar red-green-refactor. Aciona com 'TDD', 'red-green-refactor', 'test first', 'failing test', 'write a test'."
---

# Workflow de TDD

## Quando invocar
- Ao iniciar um novo comportamento ou uma correção de bug.
- Ao parear / fazer mobbing em código desconhecido e querer uma rede de segurança.
- Quando mudanças continuam quebrando coisas que ninguém esperava.

## O ciclo
```
VERMELHO → escreva o menor teste que falha e expressa o próximo comportamento
VERDE → escreva o menor código que faz o teste passar
REFATORAR → melhore o design; os testes continuam verdes
```
Faça commit em cada verde. Um comportamento por ciclo.

## Regras
1. **Nenhum código de produção sem um teste que falha.** Sem teste, sem mudança.
2. **Um teste falhando por vez.** Nunca tenha dois reds.
3. **Menor passo que falha.** Se seu primeiro teste é difícil de escrever, o design está dizendo algo.
4. **Nomes de teste descrevem comportamento**, não implementação: `calculates_tax_for_tax_exempt_customer`, não `test_method1`.
5. Estrutura **Given-When-Then / Arrange-Act-Assert** no corpo do teste.
6. **A fase de refatoração não é opcional** - é onde mora a maior parte do valor.

## Escolhendo o próximo teste
Ordene os testes para guiar o design:
- Comece pelo caso não trivial mais simples (o "0→1" ou caminho feliz com uma entrada).
- Depois adicione uma única variação (um limite, uma ramificação, um erro).
- Resista a escrever um teste gigante que cobre tudo.

## Faking e stubbing
- Use um test double apenas quando o colaborador real for lento, não determinístico ou ainda não estiver escrito.
- Não faça mock de tipos que você não controla - primeiro envolva-os em uma abstração fina.
- Um teste que mocka tudo não testa nada.

## Quando TDD é difícil, geralmente é o design
- Difícil construir o objeto sob teste → colaboradores demais, violação de SRP.
- Não dá para fazer assertion sem ler três outros objetos → Law of Demeter / problema de encapsulamento.
- Precisa mockar o mundo → acoplamento oculto; introduza uma abstração.

## Antipadrões
- Escrever o código e depois o teste (isso é verificação, não TDD).
- Pular a fase de refatoração.
- Testes que duplicam a implementação (detectores de mudança).
- Fixtures de teste gigantes compartilhadas entre arquivos - frágeis.
- Fazer assertion sobre detalhes de implementação (métodos privados, string SQL exata).

## Referências
- [Kent Beck - Test Driven Development: By Example](https://www.oreilly.com/library/view/test-driven-development/0321146530/)
- [GOOS - Growing Object-Oriented Software, Guided by Tests](http://www.growing-object-oriented-software.com/)
- [Martin Fowler - Mocks Aren't Stubs](https://martinfowler.com/articles/mocksArentStubs.html)
