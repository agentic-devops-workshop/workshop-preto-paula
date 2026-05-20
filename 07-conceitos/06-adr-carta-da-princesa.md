<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# 💌 ADR como carta da Princesa


> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Conceitos](00-README.md) → **ADR como carta**

> **Para quem é isto?** Para quem ouviu "vamos escrever um ADR" e ficou pensando que era um exame médico.
>
> **Em uma frase:** ADR é uma **carta curta** que a Princesa Peach deixa em cima da mesa explicando *"escolhemos isto, não aquilo, e por quê"*. Quando o time mudar daqui a 6 meses, alguém abre a carta e entende a decisão sem precisar invocar fantasmas.

![CONCEITO 06](https://img.shields.io/badge/CONCEITO-06-FFB900?style=for-the-badge) ![T\u00d3PICO ADR](https://img.shields.io/badge/T%C3%93PICO-ADR-1A1A1A?style=for-the-badge) ![ESTÁGIO 2](https://img.shields.io/badge/USE%20NO-Est%C3%A1gio%202-737373?style=for-the-badge)

---

## 💌 A analogia: cartas que a Princesa deixa em cada castelo

Imagina que Mario chega num castelo novo. Encima da mesa tem uma carta da Princesa:

> *"Querido Mario,*
>
> *Quando o reino decidiu construir o castelo neste vale (e não na montanha), foi porque:*
> *(a) o vale fica perto do reino vizinho de comércio,*
> *(b) a montanha gelaria demais no inverno.*
>
> *Considerei também construir na praia, mas dragões marinhos atacam.*
>
> *Se você quiser mudar de lugar, leia esta carta primeiro e me diga se as razões ainda valem.*
>
> *— Peach"*

Resultado: Mario, 200 anos depois, **entende a decisão** sem precisar perguntar a um vovô Toad. Se as razões mudaram (acabaram os dragões), ele pode tomar uma nova decisão consciente. Se ainda valem, ele respeita.

**ADR = carta da Princesa para o time futuro.**

---

## 📜 Anatomia de uma ADR (template)

```markdown
# ADR-001: Monólito modular (não microsserviços)

**Status:** Aceita
**Data:** 2026-04-15
**Autores:** Enterprise Architect + Software Architect

## Contexto

Estamos modernizando o SIFAP (29 anos em Natural/Adabas). Temos 8 horas.
4 bounded contexts identificados: beneficiary, payment, audit, admin.
Time não tem experiência operacional com microsserviços.

## Decisão

Vamos construir um **monólito modular** em Spring Boot 3.3, com cada
bounded context num módulo Maven separado e enforcement de fronteira via
ArchUnit.

## Alternativas consideradas

- **Microsserviços**: rejeitado — complexidade operacional alta para o
  prazo. Adiar para v2.
- **Monólito tradicional sem fronteiras**: rejeitado — perdemos a chance
  de extrair microsserviços depois.

## Consequências

- ✅ Deploy unificado (uma imagem Docker)
- ✅ Refactor entre módulos é seguro (compilação detecta)
- ✅ Caminho aberto para microsserviços depois (basta separar módulos)
- ❌ Não dá para escalar payment independente de beneficiary
- ❌ Falha de payment derruba o app todo
```

---

## 🎯 Por que o ADR existe

Sem ADR:

```
[Hoje]   "Vamos com monólito modular!"
         👍

[6 meses depois]
   Dev novo: "Por que não microsserviços?"
   Senior:   "Não lembro..."
   Dev novo: "Vou refatorar"
   ☠️ 3 semanas de retrabalho
```

Com ADR:

```
[Hoje]   ADR-001 escrita

[6 meses depois]
   Dev novo abre ADR-001
   "Ah, tinha 8h de prazo. Faz sentido."
   Continua o trabalho. 0 retrabalho.
```

---

## 🧭 Quando escrever uma ADR

Use o **teste das 3 perguntas**:

1. A decisão **afeta múltiplos arquivos ou pessoas**?
2. **Reverter** a decisão depois custaria mais de 1 dia?
3. Alguém vai **perguntar de novo** em 6 meses?

Se 2 de 3 forem "sim" → escreva ADR.

### Exemplos: ADR ou não?

| Decisão | ADR? | Por quê |
|---|---|---|
| Trocar `final` por `var` em uma variável | ❌ | Local, reversível |
| Escolher Spring Boot 3 vs Quarkus | ✅ | Múltiplos arquivos, irreversível em 1 dia |
| Nomear endpoint `/api/v1/payments` em vez de `/api/payments` | ✅ | Versionamento de API afeta todo o time |
| Adicionar Lombok como dependência | ✅ | Afeta todo o módulo |
| Usar `@Autowired` ou constructor injection | 🟡 | Só ADR se decidir como **padrão do time** |

---

## ✍️ Como escrever uma boa ADR (5 minutos)

A maioria das ADRs ruins é ruim por uma de **5 razões**. Evite todas:

| ❌ Anti-padrão | ✅ Como deve ser |
|---|---|
| Só fala da escolha, não das alternativas | **Liste pelo menos 2 alternativas** rejeitadas, com motivo |
| Lista benefícios mas não custos | **Inclua consequências negativas** (sempre tem) |
| Fala "vamos usar X" sem contexto | **Conta a situação** que forçou a decisão |
| Demora 3 páginas | **1 página** é o ideal. 5 seções: contexto, decisão, alternativas, consequências, status |
| Nunca é atualizada | Quando rejeitar uma ADR antiga, **marque "Superada por ADR-NNN"** — não delete |

---

## 🍄 Exemplo concreto: ADR do nosso SIFAP

Veja [`08-exemplos/ADR-001-monolito-modular-exemplo.md`](../08-exemplos/ADR-001-monolito-modular-exemplo.md) para uma ADR completa pronta. Use como template — copie a estrutura, troque a decisão.

---

## 💡 Dicas práticas de prompts (Estágio 2)

```text
# Pedir ao @architect para sugerir ADR
"@architect, vamos decidir: PostgreSQL ou MongoDB para SIFAP 2.0?
Considere: dados financeiros, auditoria pesada, sem expertise em NoSQL.
Sugira ADR no formato padrão."

# Pedir para desafiar uma decisão (importante!)
"@architect, leia esta ADR-002 e faça o papel de advogado do diabo.
Quais 3 razões para REJEITAR essa decisão?"

# Quando aparecer um disagree no time
"/speckit.clarify
Não temos consenso entre 'monólito modular' e 'microsserviços'.
Liste prós e contras objetivos para cada um."
```

---

## ✅ Checklist da ADR pronta

Antes de mergear sua ADR, confira:

- [ ] Tem **número** (`ADR-NNN`)
- [ ] Tem **status** (Proposta / Aceita / Rejeitada / Superada)
- [ ] Tem **data** e **autores**
- [ ] Seção **Contexto** explica o "por quê desta decisão agora"
- [ ] Seção **Decisão** em **uma frase** ("escolhemos X usando Y")
- [ ] Seção **Alternativas** lista pelo menos 2, com motivo de rejeição
- [ ] Seção **Consequências** tem positivas **e** negativas
- [ ] Cabe em **1 página** (raramente vale a pena ser maior)
- [ ] PO leu e entendeu (mesmo sem ser técnico) — se não, simplifique

---

## 🆘 Se travar

| Sintoma | O que fazer |
|---|---|
| "Não sei se vale ADR" | Aplique o teste das 3 perguntas (acima) |
| "Não sei quais alternativas listar" | Pergunte ao `@architect`: *"Quais 3 alternativas eu deveria considerar para X?"* |
| "Minha ADR ficou de 5 páginas" | Corte. Se não couber em 1 página, são 2 ADRs disfarçadas. |
| "Time não chega a consenso" | Use `/speckit.clarify` com a pergunta. O Spec-Kit força explicitação. |

---

## 🔗 Para se aprofundar

- 📘 [Exemplo completo: ADR-001 Monólito Modular](../08-exemplos/ADR-001-monolito-modular-exemplo.md)
- 📋 [Template em branco](../02-spec-moderna/ADR-TEMPLATE.md)
- 🎯 [Estágio 2 GUIDE](../02-spec-moderna/GUIDE.md) — onde isso é usado
- 🌐 [adr.github.io](https://adr.github.io) — site oficial do padrão ADR

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="05-ears-receita-de-cogumelo.md"><strong>EARS como receita do Cogumelo</strong></a><br/>
<sub>Requisitos sem ambiguidade.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../05-personas/OVERVIEW.md"><strong>Personas (Overview)</strong></a><br/>
<sub>Escolha seu personagem para o dia.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>
