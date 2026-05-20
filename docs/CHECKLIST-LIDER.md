<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# 🏆 Checklist do Líder do Time

![CHECKLIST Líder](https://img.shields.io/badge/CHECKLIST-L%C3%ADder-F25022?style=for-the-badge) ![QUANDO O dia inteiro](https://img.shields.io/badge/QUANDO-O%20dia%20inteiro-1A1A1A?style=for-the-badge) ![PERSONA Technical Lead](https://img.shields.io/badge/PERSONA-Technical%20Lead-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Docs](README.md) → **Checklist do Líder**

> **Para quem é isto?** Para a pessoa que cobre a persona **Technical Lead** (Par 3) e está coordenando o time durante o dia.
>
> **O que você terá ao final desta leitura:**
>
> 1. Lista cronológica do que verificar a cada bloco do dia
> 2. Sinais de alerta antes de cada passagem (H1, H2, H3)
> 3. Defaults de emergência (o que fazer se algo travar)

---

## 🌅 Antes do workshop começar (D-1, noite anterior)

- [ ] 5 laptops com VS Code Insiders instalado
- [ ] 5 contas GitHub com Copilot ativo (verificar em <https://github.com/settings/copilot>)
- [ ] Repositório `hackathon-team-XX` criado e clonado por todos
- [ ] `./11-scripts/setup.sh` rodado e verde em pelo menos 1 laptop
- [ ] Branch `develop` criada e protegida
- [ ] Todos os 5 membros confirmados (par + 2 personas cada)

---

## ⏰ Hora a hora — pontos de checagem

### 🕙 10:00–11:00 · Setup + persona-kits

- [ ] **10:15** — Todos os laptops abriram o devcontainer
- [ ] **10:30** — `docker compose up` verde em todos
- [ ] **10:45** — Cada pessoa copiou seus 2 persona-kits para `.github/`
- [ ] **10:55** — Cada pessoa testou um slash command do seu kit

### 🕚 11:00–12:00 · Estágio 1 — Arqueologia (parte 1)

- [ ] **11:00** — Time inteiro selecionou `@archaeologist` no Chat
- [ ] **11:10** — Cada par sabe quais 3 programas Natural vai ler
- [ ] **11:30** — Stand-up de 2 min: cada par diz 1 frase do que descobriu
- [ ] **11:45** — Glossário em `glossary.md` tem ≥15 termos

### 🕐 13:30–14:00 · Estágio 1 — Síntese + Passagem H1

- [ ] **13:35** — Time consolidou catálogo BR (≥15 regras com `Programa Fonte`)
- [ ] **13:45** — Mistérios documentados (≥5)
- [ ] **13:50** — Facilitador validou `LEGACY-EXPLORATION-CHECKLIST.md`
- [ ] **14:00** — **Passagem H1**: Par 1 entrega `discovery-report.md` ao Par 2

> 🚨 **Sinal vermelho:** se às 13:50 falta `Programa Fonte` em qualquer regra, **pause tudo** e foque em preencher. CI rejeita PRs sem isso.

### 🕑 14:00–15:00 · Estágio 2 — Spec Moderna

- [ ] **14:05** — Time selecionou `@architect`
- [ ] **14:30** — PO aprovou escopo (3–8 features na v1)
- [ ] **14:45** — Pelo menos 1 ADR rascunhada
- [ ] **15:00** — **Passagem H2**: spec EARS + ADRs entregues aos Pares 3+4

> 🚨 **Sinal vermelho:** REQ-ID sem `source_legacy:` → bloqueia o PR. Cheque cada um.

### 🕒 15:00–16:10 · Estágio 3 — Implementação

- [ ] **15:05** — Time selecionou `@builder`
- [ ] **15:30** — Migration Flyway V2 criada e roda local
- [ ] **15:50** — 1+ endpoint REST funciona via Swagger
- [ ] **16:00** — Pelo menos 1 teste passa
- [ ] **16:10** — **Passagem H3**: código mergeado em `develop`, CI verde

> 🚨 **Sinal vermelho:** CI vermelho ou cobertura <70% → priorize correção, não novas features.

### 🕓 16:10–16:50 · Estágio 4 — Evolução com Agent

- [ ] **16:15** — Time selecionou `@evolution`
- [ ] **16:20** — Pelo menos 1 Issue bem escrita para o Copilot Agent
- [ ] **16:35** — PR do Agent revisado e mergeado (ou ajustes solicitados)
- [ ] **16:45** — `terraform plan` rodando sem erro
- [ ] **16:50** — `agent-experience-report.md` preenchido

### 🕔 16:50–17:00 · Preparação da demo

- [ ] Cada par tem 30s de fala combinada
- [ ] Demo testada uma vez (sem ao vivo do `docker compose`)
- [ ] Browser pronto: Swagger + frontend + PR mergeado abertos

### 🎤 17:00–17:30 · Demos

- [ ] PO conduz, marca tempo
- [ ] Time inteiro fica visível na câmera
- [ ] Princesa salva! 👸 (SIFAP 2.0 rodando)

---

## 🚦 As 3 perguntas que você faz a cada 30 minutos

```
1. Alguém está travado há mais de 20 minutos?
2. CI está verde?
3. A próxima passagem (H1/H2/H3) está no horário?
```

Se algum "não" → intervenha.

---

## 🆘 Defaults de emergência (TL salva o time)

| Situação | O que você faz |
|---|---|
| Par perdido há 15 min | Senta com eles, pergunta "qual o objetivo agora?" |
| CI vermelho há 30 min | Para outras frentes, time inteiro foca |
| PO mudou escopo no meio | Você diz NÃO. Escopo trava no H2. |
| Dev quer refatorar sem teste | Você diz NÃO. Aborta. |
| Agent gerou PR ruim | NÃO merga. Solicita ajustes ou faz manual. |
| Falta meia hora e demo não roda | Reduz escopo da demo, não tenta consertar |
| Copilot caiu | Plano B em [troubleshooting.md](troubleshooting.md#-plano-b--copilot-fora-do-ar) |

---

## 🎯 Sua meta como líder

> **Não é fazer o trabalho de todos. É garantir que ninguém esteja parado.**

Você não codifica mais nem menos que os outros. Você mantém o **ritmo** e o **escopo**.

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="../00-TEAM-FLOW.md"><strong>TEAM-FLOW</strong></a><br/>
<sub>Cronograma do dia completo.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="lessons-learned.md"><strong>Lições aprendidas</strong></a><br/>
<sub>Erros comuns dos times.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>
