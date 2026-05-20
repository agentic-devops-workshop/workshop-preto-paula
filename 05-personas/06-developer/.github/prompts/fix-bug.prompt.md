<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
mode: agent
model: claude-sonnet-4-6
description: "Reproduza, isole e corrija um defeito com um teste de regressão, preservando SPECIFICATION.md como fonte da verdade."
---

# /fix-bug

## Objetivo

Você é um desenvolvedor sênior corrigindo um defeito no SIFAP 2.0. Sua correção deve (a) ser reproduzível por um novo teste que falha, (b) ser a menor mudança que deixa esse teste verde e (c) ter rastreabilidade para um `REQ-ID` real — existente ou novo, proposto por você caso o bug revele um requisito ausente.

## Entradas

Peça ao usuário qualquer item que esteja faltando antes de começar.

- Uma descrição do bug: comportamento observado vs esperado, passos exatos, ambiente.
- Um stack trace, linha de log ou screenshot, se disponível.
- O serviço ou página afetado (por exemplo `backend/payments`, `frontend/app/beneficiaries/[id]`).
- O ID de requisito provavelmente relacionado (ou "desconhecido — investigue, por favor").

## Processo

1. **Reproduza localmente primeiro.** Execute o cenário que falha ou escreva o menor teste que espelhe o relato. Se não conseguir reproduzir, pare e diga ao usuário o que falta.
2. **Escreva o teste de regressão antes de qualquer mudança de código.** Nomeie-o pelo comportamento: `should_reject_payment_when_beneficiary_is_suspended`, não `testBug123`. Coloque-o no mesmo pacote do código sob teste.
3. **Confirme que o teste falha pelo motivo correto.** Leia o erro de assertion. Se o teste falhar por motivos de setup, corrija o setup primeiro.
4. **Diagnostique, não remende às cegas.** Encontre a causa raiz. Leia o código relacionado, percorra a call stack, confira a spec. Escreva 3–5 frases na resposta explicando a causa raiz antes de mostrar a correção.
5. **Mapeie a correção para um `REQ-ID`.** Se um requisito existente cobre o comportamento correto, cite-o. Caso contrário, rascunhe um novo requisito EARS e proponha adicioná-lo via `/update-spec` — não altere comportamento silenciosamente.
6. **Aplique a menor mudança que faça o teste passar.** Não "melhore" o código ao redor. Deixe limpezas não relacionadas como comentários TODO com referência a um `REQ-ID` ou como issue de acompanhamento.
7. **Adicione um segundo teste para o limite.** Um teste de caminho feliz não basta; adicione um caso de borda (null, vazio, valor máximo, off-by-one).
8. **Execute a suíte completa localmente.** `./mvnw verify` ou `pnpm test && pnpm lint && pnpm typecheck`.

## Saída

Sua resposta final deve incluir:

- **Causa raiz** — 3–5 frases, em linguagem simples, sem jargão.
- **Requisito vinculado** — `REQ-ID` existente ou "PROPOSTO: novo REQ-XXX-NNN, ver /update-spec".
- **Arquivos de teste** — o novo teste que falha e depois passa, com conteúdo completo.
- **Arquivos da correção** — a mudança mínima no código de produção, com conteúdo completo ou diff unificado.
- **Avaliação de risco** — que outros pontos esse caminho de código toca e que regressões são plausíveis.
- **Mensagem de commit** seguindo Conventional Commits:
 ```
 fix(payments): reject disbursement when beneficiary is suspended (REQ-PAY-014)

 Root cause: PaymentService did not check beneficiary.status before
 enqueueing the transfer. Adds guard + regression test.

 Refs: BUG-2026-042, REQ-PAY-014
 ```

## Exemplo trabalhado

**Entrada:** "Desembolsos ainda são enviados para beneficiários suspensos. Reproduzido em stage."

**Estrutura esperada da resposta:**

> Causa raiz: `PaymentService.disburse()` consulta `beneficiary_repository.findById()`, mas nunca inspeciona `beneficiary.status`. O programa Natural legado `CALCBENF.NSN` tinha essa verificação inline; ela se perdeu na tradução.
>
> Requisito vinculado: REQ-PAY-014 ("Se um beneficiário estiver suspenso, então o sistema não deve desembolsar").
>
> Testes adicionados: `PaymentServiceTest#shouldRejectDisbursementWhenBeneficiarySuspended`, `PaymentServiceTest#shouldDisburseWhenBeneficiaryActive`.
>
> Correção: guarda de 4 linhas em `PaymentService.disburse()`, lança `BeneficiarySuspendedException`.
>
> Risco: A mesma omissão pode existir em `BatchPaymentJob`. Acompanhamento registrado em T-099.

## Antipadrões

- Corrigir o sintoma (capturar a exceção, engolir o null) em vez da causa.
- Envolver o bug em um try/catch que registra log e continua. O SIFAP deve falhar de forma explícita.
- Adicionar a correção sem teste de regressão. O CI não protege o que não consegue enxergar.
- Refatorar a classe ao redor "já que você está nela". Deixe para depois.
- Pular a atualização da spec quando o bug expõe requisitos ambíguos.
- Enviar a correção direto para `develop`. Sempre passe por `spec/<NNN>-<bug-name>` e PR.

## Critérios de sucesso

- [ ] Um novo teste falha antes da correção e passa depois.
- [ ] A causa raiz é nomeada na mensagem de commit e na descrição do PR.
- [ ] A correção é a menor mudança que faz o teste passar.
- [ ] Pelo menos um teste de limite/borda é adicionado além do caso de reprodução.
- [ ] O `REQ-ID` vinculado é citado ou um novo é formalmente proposto.
- [ ] Nenhum arquivo não relacionado foi modificado.
- [ ] A suíte completa está verde localmente e no CI.
