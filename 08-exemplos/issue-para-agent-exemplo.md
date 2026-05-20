<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Issue para Copilot Agent — Exemplo Preenchido

![EXEMPLO Pronto](https://img.shields.io/badge/EXEMPLO-Pronto-7FBA00?style=for-the-badge) ![USE Como referência](https://img.shields.io/badge/USE-Como%20referência-1A1A1A?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Exemplos](README.md) → **issue-para-agent-exemplo**


> **Para quem é isto?** Para o Par 3 e Par 5 no Estágio 4 — modelo de Issue que produz PR bom de primeira.
>
> **O que você terá ao final desta leitura:**
>
> 1. Estrutura: objetivo, contexto, requisitos funcionais/técnicos, aceite, escopo
> 2. Verá frase fraca vs versão forte (diff syntax)
> 3. Reproduzirá o nível de detalhe para suas Issues
> 4. Saberá os critérios de revisão do PR do Agent

![EXEMPLO](https://img.shields.io/badge/EXEMPLO-Issue%20para%20Agent-FFB900?style=for-the-badge) ![ESTÁGIO](https://img.shields.io/badge/ESTÁGIO-04%20·%20Evolu%C3%A7%C3%A3o-1A1A1A?style=for-the-badge) ![REFERÊNCIA](https://img.shields.io/badge/REFERÊNCIA-Issue%20completa%20pronta%20para%20o%20Agent-737373?style=for-the-badge)

> [!NOTE]
> Este é um exemplo de Issue completa e bem estruturada — o tipo que faz o Copilot Agent abrir um PR bom de primeira. Copie a estrutura, troque o conteúdo pela funcionalidade do **seu** time.

> [!TIP]
> **Regra de ouro.** Issue ruim → PR ruim. Gaste mais tempo na Issue, menos consertando o PR.

---

## Como usar este exemplo

1. Crie uma Issue nova no GitHub do seu time.
2. Copie o conteúdo abaixo (entre os marcadores `BEGIN ISSUE` / `END ISSUE`).
3. Troque:
   - Título e descrição
   - Requisitos funcionais/técnicos
   - Critérios de aceite
   - REQ-ID referenciada
4. Dispare o Agent (Copilot Workspace ou painel do VS Code).
5. Aguarde o PR. Revise com o checklist em [`../04-evolucao/GUIDE.md`§"Como revisar um PR do Agent"](../04-evolucao/GUIDE.md).

---

## BEGIN ISSUE ────────────────────────────────────────────────────────────────

# [REQ-PAY-NOTIF-01] Notificar beneficiário por e-mail ao aprovar pagamento

## 🎯 Objetivo (uma frase)

Quando um pagamento é aprovado (transição `PENDING → APPROVED`), o sistema deve enviar e-mail ao beneficiário informando valor, data e número do pagamento, sem bloquear a aprovação se o envio falhar.

## 📚 Contexto

- **Origem da feature:** complementa REQ-PAY-005 (aprovação por operador autorizado) com camada de comunicação ao beneficiário. Não vem do legado — é **greenfield** justificado por LGPD Art. 9º (transparência).
- **Por que agora:** beneficiários hoje só descobrem a aprovação consultando a tela. Em prod isso gera ~12.000 ligações/mês ao 0800 perguntando "meu pagamento foi liberado?".
- **Quem usa:** todo beneficiário com e-mail cadastrado. Quem não tem e-mail continua consultando como antes (sem regressão).

## 📝 Requisitos funcionais

- [ ] Quando o status de um Payment for alterado de `PENDING` para `APPROVED`, disparar envio de e-mail ao beneficiário cujo `Beneficiary.email` esteja preenchido.
- [ ] O e-mail deve conter, no mínimo:
  - Nome do beneficiário
  - Número do pagamento (`payment.id`)
  - Valor líquido aprovado (`payment.net_amount`)
  - Data de aprovação (timestamp da transição)
  - Texto: *"Em caso de divergência, contate sua agência."*
- [ ] Se o envio do e-mail falhar (timeout SMTP, endereço inválido, etc.), **NÃO bloquear** a aprovação do pagamento. A falha deve ser registrada em log estruturado (nível WARN) e no `payment_audit.reason`.
- [ ] Beneficiários sem e-mail cadastrado: **não tentar enviar**. Apenas logar em DEBUG.
- [ ] Template do e-mail deve ser configurável via arquivo `email-templates/payment-approved.html` em `src/main/resources`.

## 🔧 Requisitos técnicos

### Arquitetura

- [ ] Criar `EmailNotificationService` em `payment/application/` (não em `infrastructure/` — interface fica em `payment/domain/NotificationPort`, implementação em `payment/infrastructure/`).
- [ ] Usar **eventos do Spring (`ApplicationEvent`)** para desacoplar `PaymentService.approve()` do envio: `PaymentService` publica `PaymentApprovedEvent`, listener consome e dispara o e-mail.
- [ ] Implementação SMTP via **Spring Mail Starter** (`spring-boot-starter-mail`).

### Configuração

- [ ] Adicionar configuração ao `application.yml`:

  ```yaml
  sifap:
    notification:
      email:
        enabled: true
        from: nao-responda@sifap.gov.br
        retry:
          attempts: 3
          backoff-ms: 500
  ```

- [ ] Adicionar variáveis ao `docker-compose.yml`: `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`, `SMTP_PASSWORD`.
- [ ] Subir um **MailHog** (Docker) na porta 1025 (SMTP) e 8025 (UI) para testes locais.

### Migração de banco

- [ ] Adicionar `V5__add_beneficiary_email.sql`:

  ```sql
  ALTER TABLE beneficiary.beneficiary
    ADD COLUMN email VARCHAR(120),
    ADD CONSTRAINT beneficiary_email_format_ck
      CHECK (email IS NULL OR email ~ '^[^@\s]+@[^@\s]+\.[^@\s]+$');
  ```

> [!CAUTION]
> **Não editar** migrações anteriores. Sempre criar nova (regra do Estágio 3).

### Testes

- [ ] **Unitário** com Mockito: `PaymentServiceTest.shouldPublishEvent_whenApprove()`.
- [ ] **Integração** com Testcontainers: `EmailNotificationIntegrationTest` que sobe MailHog em container e valida que o e-mail chegou.
- [ ] **Negativo**: simular falha do SMTP → confirmar que a aprovação completa mesmo assim.

## ✅ Critérios de aceite

- [ ] `./mvnw test` passa, incluindo os 3 testes novos.
- [ ] `docker compose up` sobe MailHog e o backend conecta sem erros.
- [ ] Aprovar um pagamento via Swagger UI envia e-mail visível em `http://localhost:8025`.
- [ ] Aprovar pagamento de beneficiário sem e-mail completa normalmente (sem log de erro).
- [ ] Forçar SMTP fora do ar (`docker stop mailhog`) e tentar aprovar → aprovação completa, log WARN aparece, `payment_audit.reason` contém *"email delivery failed: ..."*.
- [ ] Endpoint Swagger documenta a nova feature (sem mudança de assinatura — é interno).
- [ ] Cobertura de testes do módulo `payment` não cai (gate ≥ 70%).

## 📎 Contexto técnico do projeto

- **Stack:** Java 21, Spring Boot 3.3, PostgreSQL 16, Maven, Flyway.
- **Módulo afetado:** `payment` (toca `Beneficiary` indiretamente via FK).
- **Arquivos relevantes para o Agent ler:**
  - `src/main/java/br/gov/client/sifap/payment/application/PaymentService.java`
  - `src/main/java/br/gov/client/sifap/payment/domain/Payment.java`
  - `src/main/java/br/gov/client/sifap/beneficiary/domain/Beneficiary.java`
  - `src/main/resources/db/migration/` (referência de versionamento Flyway)
- **REQ-IDs relacionadas:** REQ-PAY-005 (aprovação), REQ-PAY-006 (auditoria), REQ-PAY-008 (mascaramento de CPF — log de e-mail também deve mascarar).
- **ADRs relevantes:**
  - [ADR-001](../02-spec-moderna/ADR-TEMPLATE.md) — Monolito modular: respeitar fronteiras domain/application/infrastructure.

## 🚫 Fora de escopo (não fazer)

- ❌ Envio de SMS (próxima Issue, REQ-PAY-NOTIF-02).
- ❌ Personalização do template por programa social (REQ-PAY-NOTIF-03).
- ❌ Painel administrativo para reenvio manual.
- ❌ Alteração na assinatura pública de `POST /api/v1/payments/{id}/approve`.

## END ISSUE ──────────────────────────────────────────────────────────────────

---

## Por que esta Issue funciona com o Agent

| Elemento | Por que importa |
|---|---|
| **Título com REQ-ID** | Linka diretamente para a rastreabilidade do projeto |
| **Objetivo em uma frase** | Agent não chuta; lê o objetivo antes de gerar plano |
| **Requisitos funcionais como checkboxes** | Agent gera código que cobre cada item; PR vem com os mesmos checkboxes marcados |
| **Decisão arquitetural explícita** | "EmailNotificationService em application/, não infrastructure/" — bloqueia improvisação |
| **Configuração já desenhada** | YAML pronto. Agent não precisa inventar nome de propriedade |
| **Migração de banco escrita** | Reduz risco de Agent criar SQL ruim |
| **Critérios de aceite são testes reais** | Agent escreve teste que valida o critério |
| **Lista de arquivos relevantes** | Agent foca leitura nesses; não vai vasculhar o repo todo |
| **"Fora de escopo"** | Impede expansão de escopo (agent eager). PR pequeno = revisão rápida |

## O que NÃO escrever na Issue

Compare lado a lado — frase fraca (que o Agent interpreta livremente) versus versão forte (que produz PR determinístico):

```diff
- "Faça um sistema de notificação"
+ "Adicione e-mail quando status muda de PENDING para APPROVED"

- "Use boas práticas"
+ "Use ApplicationEvent; interface em domain, impl em infrastructure"

- "Escreva testes"
+ "Teste unitário com Mockito + integração com Testcontainers + MailHog"

- "Trate erros"
+ "Falha de SMTP NÃO bloqueia aprovação; registra WARN e em payment_audit.reason"
```

> [!TIP]
> Linhas `-` (vermelho) viram chute do Agent. Linhas `+` (verde) viram PR previsível.

---

## Após criar a Issue

1. **Confirme** que o repositório está com Copilot Agent habilitado (ou use Copilot Workspace).
2. **Atribua** a Issue ao bot do Copilot (`@github-copilot`) ou inicie via Workspace.
3. **Aguarde** ~5-15 minutos. O Agent vai analisar o codebase, escrever o plano e abrir um PR.
4. **Revise** o PR usando o checklist em `04-evolucao/GUIDE.md`.
5. **Comente correções** no PR — o Agent re-itera com os comentários.
---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="PaymentService-exemplo.java"><strong>PaymentService.java (exemplo)</strong></a><br/>
<sub>Service Java implementando REQ-PAY-001/002.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../04-evolucao/GUIDE.md"><strong>Estágio 4 — Evolução</strong></a><br/>
<sub>16:10–16:50 · Copilot Agent + Terraform + CI/CD.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>
