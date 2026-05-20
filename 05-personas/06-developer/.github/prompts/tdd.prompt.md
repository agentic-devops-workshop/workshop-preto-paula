<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
mode: agent
model: claude-sonnet-4-6
description: "Conduza uma feature por um ciclo TDD red-green-refactor rigoroso. Um teste falhando, o menor código que passa e então refatoração."
---

# /tdd

## Objetivo

Você produzirá um ciclo TDD completo para um único comportamento no SIFAP 2.0. O entregável são três commits — `red`, `green`, `refactor` — cada um separado. Nenhum código de produção é escrito sem um teste falhando, e nenhum teste é escrito para passar imediatamente.

## Entradas

Peça ao usuário o que estiver faltando.

- O comportamento a descobrir, em linguagem simples (por exemplo "calcular ICMS para beneficiários isentos de imposto").
- O `REQ-ID` vinculado em `SPECIFICATION.md`.
- O arquivo ou classe alvo. Se não existir, diga isso — TDD também guia o design, então criar é aceitável.
- O framework de teste — JUnit 5 + AssertJ para Java, Vitest + Testing Library para TypeScript.

## Processo

Você executa exatamente três fases. Não as compacte.

### Fase 1 — RED (escreva o teste que falha)

1. Escolha o **caso não trivial mais simples** para o comportamento. Não o caso vazio, nem o catastrófico — o menor caso que exercita lógica real.
2. Nomeie o teste pelo comportamento, não pelo método: `calculatesIcmsAsZeroForTaxExemptBeneficiary`, não `test1`.
3. Use estrutura Given/When/Then ou Arrange/Act/Assert, com linhas em branco entre as seções.
4. Execute o teste. Confirme que falha. Leia a mensagem de falha e confirme que falha pelo motivo esperado (erro de compilação, incompatibilidade de assertion — não erro de setup).
5. Commit: `test(taxes): red — icms is zero for tax-exempt beneficiary`.

### Fase 2 — GREEN (menor código para passar)

6. Escreva a **menor quantidade de código de produção** que faça o teste passar. "Fake it till you make it" é permitido: retornar um valor fixo é aceitável no primeiro ciclo.
7. Execute o teste único. Confirme verde. Execute a suíte completa. Confirme que continua verde.
8. Commit: `feat(taxes): green — implement REQ-TAX-009 (minimal)`.

### Fase 3 — REFACTOR (melhore com tudo verde)

9. Procure duplicação, nomes que mentem, primitive obsession. Aplique uma pequena manobra de Fowler (Extract Method, Inline Variable, Rename).
10. Execute todos os testes após cada micro-passo. Eles devem permanecer verdes.
11. Pare quando o design estiver bom o suficiente para o próximo ciclo, não perfeito.
12. Commit: `refactor(taxes): extract ExemptionPolicy from TaxCalculator`.

## Saída

Sua resposta final deve incluir:

- **O comportamento descoberto** — uma frase.
- **Os três commits** — mensagem, arquivos tocados e resultado de teste de cada um.
- **O arquivo de teste** — conteúdo completo.
- **O código de produção** — conteúdo completo após a fase de refatoração.
- **Dica do próximo ciclo** — que teste você escreveria em seguida (limite, erro, segunda variação). Não o implemente.

## Exemplo trabalhado

**Entrada:** Comportamento — "ICMS é zero quando o beneficiário está marcado como isento de imposto." REQ-TAX-009.

**Estrutura esperada da resposta:**

> Comportamento: `TaxCalculator.icms(beneficiary, amount)` retorna `BigDecimal.ZERO` quando `beneficiary.isTaxExempt()` é true.
>
> Commit RED: adicionou `TaxCalculatorTest#calculatesIcmsAsZeroForTaxExemptBeneficiary`. Falhou com `NullPointerException` (a classe não existia).
>
> Commit GREEN: criou `TaxCalculator` com um método `icms` de uma linha retornando `ZERO`. O teste passa. Suíte completa verde (47 testes).
>
> Commit REFACTOR: extraiu a interface `ExemptionPolicy` — antecipa variantes de isenção estadual e federal sem implementá-las ainda.
>
> Dica do próximo ciclo: escreva um teste para um beneficiário não isento com uma alíquota de ICMS diferente de zero. Isso forçará `icms` a realmente olhar para `amount`.

## Antipadrões

- Escrever o teste e o código juntos. Isso é verificação, não TDD.
- Pular a fase de refatoração. A maior parte do valor de design mora ali.
- Dois testes falhando ao mesmo tempo. Um red por vez.
- Testar métodos privados. Teste pela interface pública.
- Escrever um primeiro teste gigante que cobre seis casos. Dê passos menores.
- Mockar tudo. Um teste que mocka todo colaborador não testa nada.
- "Refatorar" mudando comportamento. Se o teste muda, você trapaceou.

## Critérios de sucesso

- [ ] Existem três commits separados: `test:` (red), `feat:` (green), `refactor:`.
- [ ] O commit red é reproduzivelmente vermelho — você consegue fazer checkout desse commit e o build falha.
- [ ] O commit green é o mínimo para passar — um método, com valor fixo se necessário.
- [ ] O commit refactor altera estrutura, não comportamento. Nomes de teste e assertions permanecem inalterados.
- [ ] A suíte completa está verde no final.
- [ ] O comportamento descoberto mapeia exatamente para um critério de aceitação de um `REQ-ID`.
