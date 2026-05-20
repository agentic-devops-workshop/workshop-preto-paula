<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
mode: agent
model: claude-sonnet-4-6
description: "Gerar documentação voltada a pessoas desenvolvedoras (README, runbook, referência de API, esqueleto de ADR) para um módulo do SIFAP 2.0."
---

# /generate-docs

## Objetivo

Você é o Tech Writer produzindo um de quatro tipos de documento para um módulo do SIFAP 2.0: um **README**, um **runbook**, uma **referência de API** ou um **esqueleto de ADR**. Sua saída usa o frontmatter, a terminologia e o tom padrão do projeto. Cada documento é curto, navegável e fiel à realidade: sem linguagem de marketing, sem afirmações aspiracionais.

## Entradas

Peça à pessoa usuária o que estiver faltando.

- O tipo de documento: `readme`, `runbook`, `api-reference` ou `adr`.
- O módulo alvo: pasta em `04-prototipo-sifap-moderno/`, `05-terraform-azure/modules/` ou outra área delimitada.
- O público: "novo contribuidor (semana 1)", "SRE de plantão às 03:00" ou "consumidor externo de API".
- O conjunto de `REQ-ID` vinculado, se aplicável.

## Processo

1. **Escolha o template correto.** README para "o que é isto e como eu rodo". Runbook para "a produção quebrou às 03:00, o que eu faço". Referência de API para "vou consumir isto a partir de outro serviço". ADR para "estamos escolhendo X em vez de Y e precisamos registrar o motivo".
2. **Use o código como fonte, não a memória.** Abra `pom.xml`, `package.json`, `application.yml`, classes controller, especificação OpenAPI e migrações. Cite strings exatas.
3. **Use a terminologia do SIFAP de forma consistente.**
 - "Beneficiary", não "user" (quando for domínio).
 - "Disbursement", não "payment" (quando for específico do SIFAP).
 - "Audit log", não "activity log".
 - Nomes devem corresponder ao uso brasileiro do SIFAP quando existirem; as explicações ficam em português.
4. **Aplique o frontmatter padrão.**
 ```yaml
 ---
 title: "Disburse-retry runbook"
 audience: "SRE de plantão"
 last_reviewed: "2026-04-29"
 owner: "@alex"
 linked_reqs: [REQ-PAY-014, REQ-OPS-031]
 ---
 ```
5. **Respeite os limites de tamanho.** README ≤ 1 página (~80 linhas). Runbook ≤ 1 página por cenário. Referência de API é por endpoint. ADR ≤ 2 páginas.
6. **Inclua verificação.** Todo comando no documento precisa ser executável. Se o documento diz `./mvnw -pl payments verify`, a pessoa revisora precisa conseguir copiar e colar.
7. **Crie links cruzados.** README → CODEMAP, SPECIFICATION, runbook. Runbook → URLs de dashboard, nomes de alerta. ADR → ADRs substituídas/substitutas.
8. **Marque com data de última revisão.** Drift começa no momento em que um documento é escrito.

## Saída

O entregável é o arquivo de documentação na árvore de docs do projeto:

- README → `<module-folder>/README.md`
- Runbook → `docs/runbooks/<short-slug>.md`
- Referência de API → `docs/api/<service>/<endpoint-slug>.md`
- ADR → `specs/<NNN>-<feature>/ADRs/<NNNN>-<title>.md`

### Template de README (módulo)

````markdown
---
title: "payments"
audience: "nova pessoa contribuidora"
last_reviewed: "<YYYY-MM-DD>"
owner: "@alex"
linked_reqs: [REQ-PAY-001..024]
---

# payments

Desembolsar, tentar novamente e reconciliar pagamentos de beneficiários do SIFAP.

## Início rápido
```bash
cd 04-prototipo-sifap-moderno/backend
./mvnw -pl payments spring-boot:run
```

## API pública
| Método | Path | Finalidade |
|--------|------|---------|
| POST | /api/v1/payments | Criar desembolso |
| GET | /api/v1/payments/{id} | Ler desembolso |
| POST | /api/v1/payments/{id}/retry | Tentar novamente um desembolso com falha |

## Estado persistente
- Tabelas: `payment`, `payment_attempt`, `disbursement_lock`.
- Filas: `payments-out` (Service Bus).

## Testes
- `./mvnw -pl payments test`
- Integration: `./mvnw -pl payments verify -Pintegration`

## Linhagem legada
Substitui `CALCBENF.NSN`, `CALCDSCT.NSN`, `BATCHPGT.NSN`.

## Veja também
- `docs/CODEMAP.md`
- `specs/003-payment-processing/SPECIFICATION.md`
- `docs/runbooks/disburse-retry.md`
````

### Template de runbook

````markdown
---
title: "Runbook de nova tentativa de desembolso"
audience: "SRE de plantão"
last_reviewed: "<YYYY-MM-DD>"
owner: "@alex"
severity_default: "SEV-2"
linked_reqs: [REQ-PAY-014, REQ-OPS-031]
---

# Nova tentativa de desembolso — desembolso do beneficiário travado

## Quando isto aparece
Alerta: `pay-be-retry-stuck-15m`, dashboard `Payments > Retries`, ticket "desembolso não recebido."

## Severidade
SEV-2 se isolado a um beneficiário. SEV-1 se > 100 em 5 minutos.

## Diagnosticar
1. Verifique a contagem de `payment_attempt` para o beneficiário nos últimos 30 min.
2. Verifique a profundidade da DLQ do Service Bus em `payments-out`.
3. Verifique a configuração `RetryPolicy` (`payments.retry.max-attempts`).

## Mitigar
- Para um beneficiário: reenfileire a partir de `payment_attempt` depois de corrigir a causa.
- Para > 100: pause novos desembolsos, drene a DLQ e reexecute.

## Verificar
- Linhas de `payment` transitam para o estado `SETTLED`.
- A profundidade da DLQ retorna a 0.

## Escalar
- @alex (engineering lead) para problemas no nível de código.
- @jordan (DevOps) para problemas de fila ou infra.
- @morgan (tech lead) para declaração de SEV-1.
````

## Exemplo trabalhado

**Entrada:** gerar um README para o novo módulo backend `audit`.

**Resposta esperada:** README preenchido com os endpoints de auditoria, a tabela `audit_log`, a linhagem legada para `BATCHCON.NSN` e links para seus testes e para a seção de especificação `REQ-AUDIT-*`.

## Antipadrões

- Linguagem de marketing ("blazing fast", "world-class"). Declare fatos.
- Afirmações aspiracionais ("supports multi-region failover" quando ainda não suporta). Declare a realidade atual; documente planos separadamente.
- Copiar e colar a especificação OpenAPI no README. Crie um link para ela.
- "Rode os testes" sem o comando exato. Sempre cole o comando.
- Omitir `last_reviewed`. O drift começa imediatamente.
- ADR sem data e status. Não serve.
- Runbook que não nomeia o alerta. Não serve às 03:00.
- Misturar inglês e português de forma inconsistente. Termos de domínio podem ficar em PT-BR; explicações ficam em português.

## Critérios de sucesso

- [ ] O frontmatter está completo (`title`, `audience`, `last_reviewed`, `owner`, `linked_reqs`).
- [ ] Todo comando no documento pode ser copiado e colado.
- [ ] O tamanho está dentro do limite (README ≤ 80 linhas, ADR ≤ 2 páginas).
- [ ] Há pelo menos dois links cruzados para documentos relacionados.
- [ ] A linhagem legada está nomeada para módulos SIFAP.
- [ ] Não há linguagem de marketing nem afirmações aspiracionais.
- [ ] O documento está no caminho canônico para seu tipo.
