<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# 🎨 Spec-Kit como Super Mario Maker


> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Conceitos](00-README.md) → **Spec-Kit como Mario Maker**

> **Para quem é isto?** Para quem ouviu falar de "Spec-Driven Development" ou "Spec-Kit" e ficou com aquela cara de quem foi pego de surpresa.
>
> **Em uma frase:** Spec-Kit é o **Super Mario Maker** do nosso workshop — você **desenha a fase** antes de jogar. Sem ele, cinco pessoas pulando juntas viram cinco fases diferentes que não encaixam.

![CONCEITO 01](https://img.shields.io/badge/CONCEITO-01-F25022?style=for-the-badge) ![T\u00d3PICO Spec-Kit](https://img.shields.io/badge/T%C3%93PICO-Spec--Kit-1A1A1A?style=for-the-badge) ![ESTÁGIO 2](https://img.shields.io/badge/USE%20NO-Est%C3%A1gio%202-737373?style=for-the-badge)

---

## 🎮 A analogia em 30 segundos

Imagina que você abriu o **Super Mario Maker 2** com 4 colegas, e o desafio é:

> "Em 1 hora, construam **uma fase só**, juntos, e depois joguem essa fase para resgatar a Princesa."

Sem combinar nada, todo mundo começa a colocar peças. Resultado: o Toad colocou 30 Goombas, a Peach colocou um lago de lava, o Luigi esqueceu de pôr a bandeira final. **A fase fica injogável.**

**Spec-Kit é o conjunto de ferramentas que força o time a fazer 5 perguntas antes de colocar a primeira peça.** Cada pergunta é um comando:

```
/speckit.constitution   ← qual o estilo da fase?
/speckit.specify        ← lista de peças que vai ter
/speckit.clarify        ← dúvidas antes de começar
/speckit.plan           ← onde cada peça vai
/speckit.tasks          ← quem coloca o quê
/speckit.analyze        ← playtest antes de publicar
/speckit.implement      ← agora sim, monta de verdade
```

Quando você termina, **a fase é montável por 5 pessoas e jogável até o fim**.

---

## 🍄 Mapeamento completo: Mario Maker × Spec-Kit

| Comando Spec-Kit | Etapa em Mario Maker | O que produz |
|---|---|---|
| `/speckit.constitution` | 📦 **Settings do Course Maker** | "Estilo SMB3, mundo gelo, tempo 300, dificuldade Normal" |
| `/speckit.specify` | 📋 **Sketch no papel** antes do editor | "Vai ter: 4 Goombas, 1 Koopa, 2 plataformas, 1 power-up" |
| `/speckit.clarify` | ⚠️ **Beta-tester pergunta** | "Mario começa onde mesmo? E se ele cair no rio, volta pra onde?" |
| `/speckit.plan` | 📐 **Layout no editor** | Posição de cada plataforma na grade |
| `/speckit.tasks` | 🔢 **Lista de cada peça a colocar** | "Coloco o Goomba 1 na coord (3, 5), o cano em (8, 2)..." |
| `/speckit.analyze` | 👁 **Playtest interno** | Roda a fase 1x antes de publicar |
| `/speckit.implement` | 🚀 **Publicar e jogar** | Mario corre pela fase de verdade |

---

## 🏚 O que acontece se você pula um comando

Quando o jogo vai mal, é quase sempre porque alguém pulou uma etapa.

| Você pulou… | O que acontece no jogo |
|---|---|
| `constitution` | Cada um monta numa engine diferente. Não rodam juntos. |
| `specify` | Você descobre no meio da fase que faltou uma peça crítica. |
| `clarify` | O playtest acha 3 bugs que vocês podiam ter previsto. |
| `plan` | Vocês colocam a bandeira antes do primeiro inimigo. Quebra a história. |
| `tasks` | Duas pessoas colocam a mesma peça no mesmo lugar. Conflito de merge. |
| `analyze` | A Princesa fica num lugar inalcançável. Descoberto só na hora da demo. 💀 |

---

## 📜 Como isso fica no nosso workshop SIFAP

Quando o Estágio 2 começa, sua dupla vai fazer **exatamente** isso, mas para a feature "Geração de Ciclo de Pagamento":

```bash
# 1. Princípios já existem (no .specify/memory/constitution.md)
cat .specify/memory/constitution.md

# 2. Cria a spec
/speckit.specify Geração de Ciclo de Pagamento mensal para beneficiários ACTIVE

# 3. Resolve dúvidas
/speckit.clarify
# → "Beneficiários SUSPENDED devem entrar? E recém-cadastrados (<30 dias)?"

# 4. Planeja
/speckit.plan
# → Bicep de módulo payment + DTO + service + Flyway V2

# 5. Quebra em tarefas
/speckit.tasks
# → 7 tarefas com dono, estimativa e dependência

# 6. Verifica consistência
/speckit.analyze
# → "Tarefa 4 não cobre o cenário 'mês com 28 dias'"

# 7. Implementa
/speckit.implement
# → Código sai com os REQ-IDs já amarrados
```

E cada **REQ-ID** que sair daqui precisa ter uma linha `source_legacy:` apontando para um `.NSN`. Como uma **moeda numerada** em Mario — sem o número, não vale ponto.

---

## 🎯 Por que o Spec-Kit existe (em uma frase)

> **Spec-Kit existe para que cinco pessoas que nunca se viram saiam de "ideia vaga" para "código que compila e teste que passa" — sem reuniões, em algumas horas.**

Sem Spec-Kit:

```
ideia → "ah, deixa eu mandar código" → 5 versões diferentes → caos → game over
```

Com Spec-Kit:

```
ideia → constitution → specify → clarify → plan → tasks → analyze → implement → 🏰
```

---

## 💡 Dicas práticas de prompts

Cole estes prompts direto no Copilot Chat (depois de selecionar `@architect` no Estágio 2):

```text
# Para começar uma feature
/speckit.specify Quero criar a funcionalidade de notificação por
e-mail quando um pagamento é aprovado. Beneficiários ACTIVE recebem.
Anexar fonte legada se houver paralelo.

# Para forçar clarificação
/speckit.clarify
# Depois leia as perguntas que o Spec-Kit fizer — responda TODAS.
# Cada pergunta não respondida = bug futuro.

# Para gerar plano
/speckit.plan A spec acima usando Spring Boot 3, Spring Mail,
Postgres 16 e Testcontainers para teste de integração.

# Para revisar antes de codar
/speckit.analyze
# Se aparecer "inconsistência X", PARE e resolva antes de implement.
```

---

## 🆘 Se travar

| Sintoma | O que fazer |
|---|---|
| "Não sei o que escrever em `/speckit.specify`" | Olhe [`08-exemplos/SPECIFICATION-exemplo.md`](../08-exemplos/SPECIFICATION-exemplo.md). Copie o estilo. |
| "O `/speckit.clarify` está fazendo 12 perguntas. É muito?" | Não. É bom. Cada pergunta evita um bug. Responda. |
| "`/speckit.analyze` está reclamando" | Bom. **Não pule.** Resolva os apontamentos antes de implementar. |
| "Onde fica o Spec-Kit instalado?" | `.specify/` no raiz do repo. Comandos via Copilot Chat. Veja [`09-cheat-sheets/spec-kit-workflow.md`](../09-cheat-sheets/spec-kit-workflow.md). |

---

## 🔗 Para se aprofundar

- 📜 [Repo oficial do Spec-Kit](https://github.com/github/spec-kit)
- 🎴 [Cheat-sheet de 1 página](../09-cheat-sheets/spec-kit-workflow.md)
- 📘 [Exemplo de SPECIFICATION pronta](../08-exemplos/SPECIFICATION-exemplo.md)
- 🎯 [Estágio 2 — onde isso é usado](../02-spec-moderna/GUIDE.md)

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="00-README.md"><strong>Índice de Conceitos</strong></a><br/>
<sub>Mapa das analogias.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="02-agentes-como-super-mario.md"><strong>Agentes como Super Mario</strong></a><br/>
<sub>persona-kit × agent-kit × 3 modos do Copilot.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>
