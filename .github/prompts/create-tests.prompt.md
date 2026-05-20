<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
mode: agent
model: claude-sonnet-4-6
description: "Gere uma classe de teste completa para um único REQ-ID, com happy path, limites e casos negativos."
---

# /create-tests

## Objetivo

Você está escrevendo a classe de teste para **um `REQ-ID` específico** no SIFAP 2.0. Você produz testes JUnit 5 (Java) ou Vitest (TypeScript) prontos para colar, cobrindo caminho feliz, limites e casos negativos — e para por aí. Você não implementa código de produção; você não modifica a spec.

## Entradas

Peça ao usuário o que estiver faltando.

- O `REQ-ID`, sua declaração EARS completa e seus critérios de aceitação em `SPECIFICATION.md`.
- A classe ou componente sob teste (por exemplo `br.gov.sifap.payments.PaymentService` ou `app/beneficiaries/page.tsx`).
- O framework de teste — JUnit 5 + AssertJ + Mockito (backend) ou Vitest + Testing Library (frontend).
- Quaisquer fixtures ou builders de teste existentes para reutilizar (`src/test/resources/fixtures/`, `__fixtures__/`).

## Processo

1. **Decomponha a declaração EARS em casos testáveis.**
 - Ubiquitous (`O sistema deverá ...`) → 1 caminho feliz + 1 limite.
 - Event-driven (`Quando ...`) → 1 caminho feliz + 1 negativo ("o evento não aconteceu, nada deve mudar").
 - State-driven (`Enquanto ...`) → 1 caso por transição de estado (in-state, exit-state, re-entry).
 - Optional (`Onde ...`) → 1 com feature flag ligada, 1 com flag desligada.
 - Unwanted (`Se ..., então o sistema não deverá ...`) → pelo menos 2 casos negativos em limites diferentes.
2. **Escolha fixtures, não dados de produção.** Reutilize `BeneficiaryFixtures.exempt()`, nunca copie um CPF real.
3. **Nomeie testes pelo comportamento.** `shouldRejectDisbursementWhenBeneficiarySuspended`, não `test1`. Snake_case em descrições de teste TS, camelCase em nomes de métodos JUnit.
4. **Use comentários Given/When/Then ou separação AAA com linhas em branco.** Revisores precisam ler o teste em 10 segundos.
5. **Use cadeias AssertJ para riqueza** (`assertThat(x).isEqualTo(y).as("REQ-PAY-014")`) — nunca `assertTrue(x.equals(y))`.
6. **Marque com o requisito.** `@Tag("REQ-PAY-014")` no JUnit, ou `describe('REQ-PAY-014', ...)` no Vitest.
7. **Faça mock apenas de colaboradores próprios.** Repositories sim, classes de framework não. Não faça mock de value objects ou funções puras.
8. **Rode os testes** e confirme que todos falham com mensagens significativas (até que o código de produção seja escrito por `/implement`).

## Saída

Sua resposta final deve incluir:

- **Plano de testes** — uma tabela mapeando cada critério de aceitação para um nome de método de teste.
- **Conteúdo completo do arquivo de teste** — pronto para colar no projeto.
- **Adições de fixtures** se algum novo builder/factory for necessário (arquivo separado).
- **Instrução de execução** — comando exato (`./mvnw test -Dtest=PaymentServiceTest`).
- **Mensagens de falha esperadas** — o que o usuário deve ver antes da implementação.

## Exemplo trabalhado

**Entrada:** `REQ-PAY-014` — "Se um beneficiário estiver suspenso, então o sistema não deverá desembolsar." Classe sob teste: `PaymentService`.

**Esqueleto de resposta esperado:**

> Plano:
>
> | Critério de aceitação | Método de teste |
> |----------------------|-------------|
> | Beneficiário suspenso causa recusa | `shouldRejectDisbursementWhenBeneficiarySuspended` |
> | Beneficiário ativo ainda recebe desembolso (regressão) | `shouldDisburseWhenBeneficiaryActive` |
> | Limite: `status=PENDING` não é "suspended" | `shouldDisburseWhenBeneficiaryPending` |
>
> Arquivo: `backend/src/test/java/br/gov/sifap/payments/PaymentServiceTest.java`
>
> ```java
> @Tag("REQ-PAY-014")
> class PaymentServiceTest {
> private final BeneficiaryRepository repo = mock(BeneficiaryRepository.class);
> private final TransferGateway gateway = mock(TransferGateway.class);
> private final PaymentService service = new PaymentService(repo, gateway);
>
> @Test
> void shouldRejectDisbursementWhenBeneficiarySuspended() {
> // given
> var suspended = BeneficiaryFixtures.suspended("12345678900");
> when(repo.findById(suspended.id())).thenReturn(Optional.of(suspended));
>
> // when / then
> assertThatThrownBy(() -> service.disburse(suspended.id(), Money.of("100.00")))
> .isInstanceOf(BeneficiarySuspendedException.class)
> .as("REQ-PAY-014");
> verifyNoInteractions(gateway);
> }
> // ... two more tests ...
> }
> ```
>
> Execução: `./mvnw test -Dtest=PaymentServiceTest`
> Esperado: 3 testes falham com `Expected BeneficiarySuspendedException but ...` até `/implement` construir o código de produção.

## Anti-padrões

- Escrever um teste gigante que exercita seis casos. Divida-os.
- Fazer asserções sobre a implementação: campos privados, strings SQL exatas, texto de mensagens de log.
- Fazer mock da classe sob teste ou de value objects.
- Compartilhar estado mutável de fixtures entre testes. Construa dados novos por teste.
- Testes sem tag de `REQ-ID` — eles não podem ser rastreados no relatório de lacunas de cobertura.
- Pular casos negativos para EARS unwanted-behavior. Esse é o ponto central do padrão.
- Usar `Thread.sleep` ou `await new Promise(r => setTimeout(r, 100))` para sincronização. Use awaitility ou matchers `findBy*`.

## Critérios de sucesso

- [ ] Todo critério de aceitação tem pelo menos um teste nomeado.
- [ ] Pelo menos um caso de limite ou negativo está incluído.
- [ ] Todos os testes carregam o `REQ-ID` como tag e na descrição da asserção.
- [ ] Os testes falham antes da implementação, com mensagens claras, pelo motivo correto.
- [ ] Nenhum código de produção é alterado.
- [ ] Nenhum PII real ou credencial de produção aparece em fixtures.
- [ ] O arquivo de teste compila e roda isoladamente (`./mvnw test -Dtest=...`).
