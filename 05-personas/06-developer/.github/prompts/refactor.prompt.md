<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
mode: agent
model: claude-sonnet-4-6
description: "Refatore código com testes verdes sem alterar comportamento observável nem quebrar a rastreabilidade de REQ-ID."
---

# /refactor

## Objetivo

Você está melhorando a estrutura interna do código do SIFAP 2.0 sem alterar o que ele faz. Uma refatoração que muda comportamento não é refatoração — é uma mudança de feature e pertence a `/implement` ou `/fix-bug`. Sua saída deve deixar todos os testes existentes verdes e todos os vínculos de `REQ-ID` intactos.

## Entradas

Peça ao usuário qualquer item que esteja faltando.

- O arquivo, pacote ou componente alvo (por exemplo `backend/src/main/java/br/gov/sifap/payments/PaymentService.java`).
- A motivação: code smell observado (long method, duplicação, primitive obsession, feature envy etc.).
- Quaisquer restrições de `DESIGN.md` ou ADRs que limitem seus movimentos (por exemplo "controllers devem permanecer finos").
- A cobertura de testes atual da área (execute um relatório de cobertura se desconhecida).

## Processo

1. **Confirme a rede de segurança.** Se a cobertura de linhas do alvo estiver abaixo de 80%, escreva primeiro testes de caracterização. Refatoração sem testes é reescrita.
2. **Nomeie o smell com precisão.** Escolha do catálogo (Long Method, Large Class, Primitive Obsession, Data Clumps, Feature Envy, Shotgun Surgery, Divergent Change). Justificativas vagas como "deixar mais limpo" são rejeitadas.
3. **Escolha uma manobra de refatoração do catálogo de Fowler** — Extract Method, Extract Class, Replace Conditional with Polymorphism, Introduce Parameter Object etc. Uma manobra por commit.
4. **Execute os testes antes de qualquer mudança.** Confirme verde. Se estiverem vermelhos ou pulados, corrija isso primeiro; não refatore em builds quebrados.
5. **Aplique a manobra.** Use as ferramentas de refatoração da IDE quando possível (Extract Method, Rename, Move). Edições manuais devem preservar assinaturas de métodos, a menos que a manobra seja "Change Function Declaration".
6. **Execute testes após cada micro-passo.** Os testes permanecem verdes em todo commit. Se ficarem vermelhos, reverta e dê um passo menor.
7. **Preserve a rastreabilidade.** Toda anotação `@implements REQ-NNN` deve se mover junto com seu método. Não as exclua nem as funda silenciosamente.
8. **Pare quando o smell desaparecer.** Resista à vontade de refatorar código vizinho. Cada refatoração é um chat, um PR, um smell.

## Saída

Sua resposta final deve incluir:

- **Smell nomeado** — entrada exata do catálogo mais 1–2 linhas de evidência.
- **Refatoração escolhida** — entrada exata do catálogo, mais por que ela se encaixa.
- **Diff ou antes/depois** de cada arquivo tocado.
- **Resultados dos testes** — confirmação de que o mesmo conjunto de testes passa (cole o resumo).
- **Nota de preservação de comportamento** — "API pública inalterada. Nenhuma nova cláusula throws. Nenhuma migration de DB. Nenhuma nova env var."
- **Mensagem de commit** seguindo Conventional Commits com o tipo `refactor:`:
 ```
 refactor(payments): extract DisbursementCalculator from PaymentService

 Reduces PaymentService from 412 to 287 lines; isolates pure calculation
 for easier testing. No behavior change.

 Refs: REQ-PAY-014 (annotation moved to DisbursementCalculator.calculate)
 ```

## Exemplo trabalhado

**Entrada:** "`PaymentService` tem mais de 400 linhas e mistura orquestração com cálculo."

**Estrutura esperada da resposta:**

> Smell: Large Class — `PaymentService` tem 14 métodos ao longo de 412 linhas, com duas responsabilidades não relacionadas (orquestração de repositórios vs matemática de desembolso).
>
> Refatoração: Extract Class — mover os quatro métodos de cálculo puro para um novo `DisbursementCalculator`. `PaymentService` colabora com ele via injeção por construtor.
>
> Preservação de comportamento: todos os 38 testes existentes em `PaymentServiceTest` continuam passando sem modificação. Nenhuma assinatura de método público muda. Nenhum DB ou configuração foi tocado.
>
> Arquivos: `PaymentService.java` (modificado), `DisbursementCalculator.java` (novo), `DisbursementCalculatorTest.java` (novo — 11 testes movidos).

## Antipadrões

- Refatorar sem testes. Isso é uma reescrita com outro nome.
- Fazer "pequenas melhorias" em código vizinho. Permaneça no escopo.
- Mudar comportamento sob o disfarce de refatoração. Se a saída muda, não é refatoração.
- Renomear APIs públicas sem plano de migração ou aviso de depreciação.
- Agrupar refatoração + feature no mesmo PR. Revisores não conseguem raciocinar sobre isso.
- Refatorar código com um `/fix-bug` pendente — corrija primeiro, depois refatore sobre uma baseline limpa.

## Critérios de sucesso

- [ ] Todos os testes que passavam antes continuam passando com os mesmos nomes.
- [ ] Nenhuma mudança de API pública, nenhuma nova exceção, nenhuma nova dependência.
- [ ] A cobertura não diminuiu.
- [ ] Um smell, uma manobra, um PR.
- [ ] Todas as anotações `@implements REQ-NNN` ainda estão presentes e corretas.
- [ ] A mensagem de commit usa o tipo `refactor:` e declara explicitamente "sem mudança de comportamento".
