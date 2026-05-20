<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# 🍄 EARS como receita do Cogumelo Vermelho


> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Conceitos](00-README.md) → **EARS como receita**

> **Para quem é isto?** Para quem ouviu "vamos escrever EARS" no Estágio 2 e ficou sem saber por onde começar.
>
> **Em uma frase:** EARS é a **receita exata** do cogumelo que vai virar SIFAP 2.0 — sem ambiguidade, sem "a gosto", sem "uma pitada de…". Cada requisito vira uma frase com formato fixo, **testável**.

![CONCEITO 05](https://img.shields.io/badge/CONCEITO-05-7FBA00?style=for-the-badge) ![T\u00d3PICO EARS](https://img.shields.io/badge/T%C3%93PICO-EARS-1A1A1A?style=for-the-badge) ![ESTÁGIO 2](https://img.shields.io/badge/USE%20NO-Est%C3%A1gio%202-737373?style=for-the-badge)

---

## 🧁 A analogia: receita de bolo (ou de power-up do Mario)

Imagina duas versões da receita do **Cogumelo Vermelho** (o que faz Mario crescer):

### ❌ Receita ruim (vaga)

> "Pegue um cogumelo, deixe-o vermelhinho e bonzinho. Se quiser, coloque pintinhas. Aí o Mario fica grande."

Resultado: cada Toad-cozinheiro faz um cogumelo diferente. Um sai laranja, outro sai sem pintinha. O Mario não cresce.

### ✅ Receita boa (EARS)

> **QUANDO** o Toad encontrar uma muda de cogumelo, **o SISTEMA deve** plantá-la em terra úmida a 22°C, regar 50ml por 3 dias, e pintar com tinta vermelha código #E60012 e 5 pintos brancos #FFFFFF de 8mm.

Agora **qualquer Toad** reproduz o mesmo cogumelo. E você consegue **testar**: o cogumelo tem 5 pintos? Sim → ✅. Cor #E60012? Sim → ✅.

**EARS é isso para software**: substitui "o sistema deve ser bom" por uma frase que tem teste objetivo.

---

## 📜 Os 6 padrões EARS (com SIFAP + Mario)

EARS tem **6 formatos prontos**. Você escolhe o que combina com sua regra.

### 1️⃣ **Ubiquitous** — sempre vale

> **O [sistema] deve [ação].**

| 🍄 Mario | 🏛 SIFAP |
|---|---|
| O Mario deve usar a paleta de cores oficial Nintendo. | O SIFAP deve armazenar todos os registros em UTC. |

> ✅ Use quando a regra vale **sempre, sem condição**.

---

### 2️⃣ **Event-driven** — quando algo acontece

> **Quando [evento], o [sistema] deve [ação].**

| 🍄 Mario | 🏛 SIFAP |
|---|---|
| Quando Mario tocar num cogumelo, ele deve crescer. | Quando um beneficiário é cadastrado, o SIFAP deve validar o CPF (módulo 11). |

> ✅ Use quando a regra **dispara por um evento**.

---

### 3️⃣ **State-driven** — enquanto algo é verdade

> **Enquanto [condição], o [sistema] deve [ação].**

| 🍄 Mario | 🏛 SIFAP |
|---|---|
| Enquanto Mario estiver com estrela, ele deve ser invencível. | Enquanto um pagamento estiver PENDING, o SIFAP deve permitir cancelamento pelo OPERATOR. |

> ✅ Use quando a regra vale **durante um estado** que muda.

---

### 4️⃣ **Optional** — se o jogador escolher

> **Onde [condição opcional], o [sistema] deve [ação].**

| 🍄 Mario | 🏛 SIFAP |
|---|---|
| Onde o jogador apertar B durante o pulo, Mario deve dar um pulo mais alto. | Onde o operador escolher exportar, o SIFAP deve gerar CSV em UTF-8. |

> ✅ Use quando é **escolha do usuário**.

---

### 5️⃣ **Unwanted Behavior** — o que NÃO pode acontecer

> **O [sistema] não deve [ação proibida].**

| 🍄 Mario | 🏛 SIFAP |
|---|---|
| Mario não deve atravessar paredes. | O SIFAP não deve permitir DELETE em registros de auditoria. |

> ✅ Use para **proibições explícitas** (segurança, compliance).

---

### 6️⃣ **Complex** — combinação

> **Enquanto [condição], quando [evento], onde [opcional], o [sistema] deve [ação].**

| 🍄 Mario | 🏛 SIFAP |
|---|---|
| Enquanto Mario for grande, quando ele agachar num cano verde com seta para baixo, onde o jogador apertar baixo por 1 segundo, ele deve entrar no cano. | Enquanto o beneficiário for ACTIVE, quando o ciclo for gerado em dezembro, onde o programa for "Bolsa Família", o SIFAP deve aplicar bônus de 13º. |

> ✅ Use para regras com **várias condições combinadas** (raro, mas existe).

---

## ⭐ A regra de ouro: `source_legacy:` é obrigatório

Toda EARS no nosso workshop carrega **uma moeda numerada** — o `source_legacy:`. Sem ela, a moeda não vale ponto (o CI rejeita).

```yaml
REQ-PAY-DSCT-01:
  pattern: unwanted
  text: "O SIFAP não deve permitir que o total de descontos não-judiciais
         exceda 30% do valor bruto."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L142-L148  # 🪙 moeda numerada
  acceptance:
    - "Desconto não judicial de 35% é truncado para 30%"
    - "Desconto judicial de 50% é aceito integralmente"
    - "Mistura de jud (20%) + não-jud (25%) = 45% aceito"
```

### Casos especiais

| Situação | `source_legacy:` |
|---|---|
| Regra extraída do legado | `01-arqueologia/legado-sifap/natural-programs/X.NSN#L<inicio>-L<fim>` |
| Campo de DDM | `01-arqueologia/legado-sifap/adabas-ddms/X.ddm` |
| Funcionalidade nova (não existe no legado) | `"[GREENFIELD] OAuth2 não existia em terminal 3270, mas a API moderna precisa"` |

---

## 🧪 O teste do espelho: "consegue virar teste?"

Ao escrever uma EARS, pergunte:

> *"Como eu testaria isso automaticamente?"*

Se não souber responder, **a EARS está vaga**.

| ❌ EARS vaga | ✅ EARS testável |
|---|---|
| O sistema deve ser seguro | O SIFAP deve mascarar CPF em logs no formato `XXX.XXX.NNN-NN` |
| Pagamentos devem ser processados | Quando um ciclo for gerado, o SIFAP deve criar registros de pagamento para todos os beneficiários ACTIVE |
| Auditoria completa | Quando qualquer entidade for alterada, o SIFAP deve gravar evento de auditoria com `before_json` e `after_json` |

---

## 💡 Dicas práticas de prompts (Estágio 2)

```text
# Converter regra do catálogo BR em EARS
/ears-convert BR-013: descontos têm teto de 30% exceto judiciais.
Use legado-sifap/natural-programs/CALCDSCT.NSN#L142-L148 como source_legacy.

# Validar uma EARS já escrita
"@architect, esta EARS é testável? Como você testaria?

REQ-PAY-001: Quando um beneficiário é cadastrado, o SIFAP deve validar
o CPF usando módulo 11."

# Buscar lacunas no SPECIFICATION
"/speckit.analyze
Estamos com 12 REQ-IDs. Quais cenários do CALCBENF.NSN não estão cobertos?"
```

---

## 🆘 Se travar

| Sintoma | O que fazer |
|---|---|
| "Não sei qual padrão usar" | Comece pelo **event-driven** (`Quando…`). Cobre 60% dos casos. |
| "Minha regra parece grande demais para uma EARS" | Quebra em duas. EARS pequena e testável > EARS gigante. |
| "Não acho `source_legacy:`" | Pare. Volte ao `01-arqueologia/` e leia o `.NSN` correspondente. Sem isso o CI rejeita. |
| "É **greenfield** mesmo?" | Documente o motivo: *"[GREENFIELD] X não existia no legado porque…"* |

---

## 🔗 Para se aprofundar

- 📘 [Exemplo de SPECIFICATION pronta](../08-exemplos/SPECIFICATION-exemplo.md) — 8 REQ-IDs reais com source_legacy
- 🎯 [Estágio 2 GUIDE](../02-spec-moderna/GUIDE.md)
- 🎴 [Spec-Kit cheat-sheet](../09-cheat-sheets/spec-kit-workflow.md)
- 🔬 [LEGACY-EXPLORATION-CHECKLIST](../01-arqueologia/LEGACY-EXPLORATION-CHECKLIST.md) — onde a regra do `source_legacy:` é validada

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="04-3-modos-do-copilot.md"><strong>3 modos do Copilot</strong></a><br/>
<sub>Ask · Plan · Agent.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="06-adr-carta-da-princesa.md"><strong>ADR como carta da Princesa</strong></a><br/>
<sub>Decisão técnica registrada por escrito.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>
