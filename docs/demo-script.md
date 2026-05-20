<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# 🎤 Script da Demo — 3 minutos finais

![DEMO Script](https://img.shields.io/badge/DEMO-Script-FFB900?style=for-the-badge) ![DURAÇÃO 3 min](https://img.shields.io/badge/DURA%C3%87%C3%83O-3%20min-1A1A1A?style=for-the-badge) ![FORMATO 30s por par](https://img.shields.io/badge/FORMATO-30s%20por%20par-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Docs](README.md) → **Script da Demo**

> **Para quem é isto?** Para o time inteiro, especialmente o PO (conduz) e o TL (cronometra).
>
> **O que você terá ao final desta leitura:**
>
> 1. Roteiro literal de fala — 30 segundos por par
> 2. Browser/abas que precisam estar prontas
> 3. Plano B se algo falhar ao vivo

---

## ⏱ O roteiro (3 minutos · 5 pares × 30s + 30s buffer)

### `[00:00–00:30]` 👸 Par 1 — Visão (PO conduz)

> "Modernizamos o SIFAP, sistema de pagamentos com 29 anos. **Escopo do dia:** ciclo mensal de pagamento + cadastro de beneficiários. Não-escopo: integrações SISBB e relatórios TCU — backlog. Critério de sucesso: API rodando + 1 fluxo end-to-end."

🖥 **Abrir:** [README.md](../README.md) da branch `main`

---

### `[00:30–01:00]` 🌟 Par 2 — Arquitetura (EA ou SA fala)

> "Bounded contexts: **beneficiary, payment, audit, admin**. Monolito modular em Spring Boot 3.3 (ADR-001), Postgres 16 (ADR-002), Next.js 15 no frontend (ADR-003). Cada REQ-ID rastreia ao legado — mostro um exemplo."

🖥 **Abrir:** [`02-spec-moderna/SPECIFICATION.md`](../02-spec-moderna/) e mostrar `source_legacy:` em uma EARS

---

### `[01:00–01:30]` 🟥 Par 3 — Implementação (Dev fala)

> "Implementamos o endpoint `POST /api/v1/payments` que aplica BR-013 (teto de 30% em descontos não judiciais). Vou criar um pagamento agora pelo Swagger."

🖥 **Demo ao vivo:**
1. Abrir <http://localhost:8080/swagger-ui.html>
2. Auth com `operator1` / `client2026`
3. POST `/payments` → mostra resposta 201

> ⚠️ **Plano B se a demo ao vivo falhar:** abrir screenshot/print do PR mergeado e mostrar testes verdes.

---

### `[01:30–02:00]` 🦖 Par 4 — Qualidade (DBA ou QA fala)

> "Migração Flyway V2 criou a tabela `payment` com chave estrangeira para `beneficiary` e regra de auditoria via trigger. **18 testes**, cobertura **74%**. BR-013 tem 3 testes de aceitação."

🖥 **Abrir:** página de Actions com checks verdes no PR

---

### `[02:00–02:30]` 🍄 Par 5 — Operações (DevOps fala)

> "Terraform pronto para Container Apps + Postgres Flexible + Key Vault. Rodei `terraform plan` — sem erro. CI roda lint, testes, Bicep validate. Documentação do agente em `agent-experience-report.md`."

🖥 **Abrir:** saída do `terraform plan` (texto)

---

### `[02:30–03:00]` 🎺 PO encerra

> "Em 8 horas, 5 pessoas modernizaram um sistema de 29 anos preservando regras de negócio críticas. **Princesa salva.** 👸"

🖥 **Abrir:** demo do SIFAP 2.0 rodando no frontend

---

## 📋 Checklist pré-demo (16:50–17:00)

- [ ] Browser aberto com 5 abas pré-carregadas:
  - [ ] `localhost:8080/swagger-ui.html`
  - [ ] `localhost:3001` (frontend)
  - [ ] PR mergeado no GitHub (Actions verde visível)
  - [ ] `SPECIFICATION.md` no GitHub
  - [ ] Terminal com `terraform plan` rodado e visível
- [ ] Cada par ensaiou sua fala de 30s (sem ler)
- [ ] Quem é o **timekeeper** está definido (TL recomendado)
- [ ] Resolução de tela: **1920×1080** ou maior
- [ ] Não-uso: alertas/notificações desligadas (Do Not Disturb)

---

## 🚨 Plano B (se algo falhar ao vivo)

| Falhou | O que fazer |
|---|---|
| Backend caiu | Screenshots prontos da resposta do Swagger |
| Internet caiu | Screenshots do PR + Actions + Swagger local |
| Demo ao vivo travou | "Esperado em demos — vamos para o screenshot" — confiança |
| Tempo estourou | PO corta — *"Obrigado, equipe XX, fica para depois"* |

---

## 🎯 Regra de ouro da demo

> **Mostre o que funciona. Não invente narrativa.**
> Se a feature não compilou, não fale dela. Foque no que está verde.

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="lessons-learned.md"><strong>Lições aprendidas</strong></a><br/>
<sub>Erros comuns.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="STATUS.md"><strong>Dashboard do dia</strong></a><br/>
<sub>Acompanhar progresso visual.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>
