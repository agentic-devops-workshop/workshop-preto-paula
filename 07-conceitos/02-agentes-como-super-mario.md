<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# 🍄 Agentes Copilot como Super Mario Bros


> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Conceitos](00-README.md) → **Agentes como Super Mario**

> **Para quem é isto?** Para quem nunca selecionou um agente no Copilot Chat e está perdido entre "persona-kit", "agent-kit", Ask, Plan e Agent.
>
> **Em uma frase:** o workshop é um **co-op de Super Mario** com 5 jogadores. Sua **persona** é o seu **personagem**. O **agente do estágio** é o **mundo atual**. Os **3 modos do Copilot** são suas **ações de turno**.

![CONCEITO 02](https://img.shields.io/badge/CONCEITO-02-00A4EF?style=for-the-badge) ![T\u00d3PICO Agentes](https://img.shields.io/badge/T%C3%93PICO-Agentes%20%2B%20Personas-1A1A1A?style=for-the-badge) ![Todo o dia](https://img.shields.io/badge/USE-Todo%20o%20dia-737373?style=for-the-badge)

---

## 🎮 O jogo inteiro em uma imagem

```
                                                                      🏰
🟦 MUNDO 1 ──🟢cano──> 🟫 MUNDO 2 ──🟢cano──> 🟧 MUNDO 3 ──🟢cano──> CASTELO
@archaeologist          @architect              @builder              @evolution
(arqueologia)           (spec moderna)          (implementação)       (evolução)
                                                                          │
                                                                          ▼
                                                                     👸 SIFAP 2.0
                                                                       rodando!
```

Cinco jogadores. Quatro mundos. Uma princesa para salvar (SIFAP 2.0 rodando). Cada mundo tem um **agente diferente** que muda a "música" da conversa com o Copilot. Cada jogador tem um **personagem fixo** (persona) que carrega habilidades especiais o dia inteiro.

---

## 🧑‍🤝‍🧑 Os 5 personagens jogáveis (suas personas)

Você escolhe **um par** (duas personas) e fica com elas o dia inteiro. É como pegar dois personagens em Super Mario 3D World e revezar:

| Persona | Personagem | Habilidade especial |
|---|---|---|
| 👑 **Product Owner** | 👸 **Peach** | Pode "planar" sobre detalhes técnicos para enxergar o todo |
| 📜 **Requirements Engineer** | 📖 **Toad** | Lê pergaminhos antigos (legado) e traduz para EARS |
| 🧙 **Enterprise Architect** | 🌟 **Rosalina** | Visão galáctica do sistema (C4 L1) |
| 🧙‍♂️ **Software Architect** | 🔷 **Daisy** | Conhece cada cano de cada mundo (bounded contexts) |
| ⚔️ **Technical Lead** | 🟥 **Mario** | Líder do time, faz o pulo do "boss" (revisão de PR) |
| 🗡 **Developer** | 🟩 **Luigi** | Salta mais alto (escreve mais código por turno) |
| 🧝 **DBA** | 🦖 **Yoshi** | Engole tabelas inteiras e cospe SQL |
| 🏹 **QA Engineer** | 🐢 **Koopa Troopa amigo** | Casca dura: testes rigorosos |
| 🛠 **DevOps Engineer** | 🍄 **Captain Toad** | Carrega a mochila pesada (Terraform, CI) |
| 📖 **Tech Writer** | 🎺 **Bardo Mushroom** | Conta a história em palavras (docs, ADRs) |

### Sua persona vem com **inventário** (persona-kit)

Cada classe tem em [`05-personas/0X-nome/`](../05-personas/):

- 🎒 **`PERSONA.md`** — sua ficha de personagem
- ⚔️ **`.github/prompts/*.prompt.md`** — seus golpes especiais (slash commands)
- 🛡 **`.github/skills/*/SKILL.md`** — passivas (conhecimento de domínio)
- 🗺 **`.github/instructions/*.instructions.md`** — modo automático em certos arquivos
- 📡 **`mcp.json`** — radar (servidores MCP)

> [!TIP]
> Você carrega **as duas mochilas** dia inteiro (suas 2 personas). Faça `cp -r 05-personas/XX-*/.github/* .github/` para cada uma logo no setup, e os golpes ficam disponíveis automaticamente no Copilot Chat.

---

## 👹 Os 4 mundos (agentes de estágio)

Cada **estágio** do dia tem um agente diferente. O time inteiro **seleciona o mesmo agente** no Copilot Chat no início do estágio (ícone do agente no canto do chat).

| Mundo | Agente | Música/clima | Protagonistas | Objetivo |
|---|---|---|---|---|
| 🟦 **1-1** Overworld | [`@archaeologist`](../06-agentes-de-estagio/01-archaeologist/) | Detetive | RE, Tech Writer | Ler legado, extrair regras, mapear |
| 🟫 **2-1** Underground | [`@architect`](../06-agentes-de-estagio/02-architect/) | Arquiteta misteriosa | EA, SA | Escrever EARS, ADRs, C4 |
| 🟧 **3-1** Athletic | [`@builder`](../06-agentes-de-estagio/03-builder/) | Forja barulhenta | Dev, DBA, QA | Código, testes, migrations |
| 🏰 **4-Castle** | [`@evolution`](../06-agentes-de-estagio/04-evolution/) | Tema épico de Bowser | DevOps, TW + Dev | Delegar ao Agent, IaC, CI/CD |

### A diferença prática

| Sem agente selecionado | Com agente selecionado |
|---|---|
| Copilot responde "no geral", às vezes sugere coisa fora de contexto | Copilot **assume o enquadramento do mundo** (não sugere código no estágio 1) |
| Cada pessoa pergunta diferente, recebe respostas inconsistentes | O time inteiro tem o **mesmo briefing** |

---

## ⚔️ Seus 3 ataques de turno (modos do Copilot)

No Copilot, todo turno você escolhe um dos 3 modos. **Trocar de modo é grátis** — escolher o errado custa tempo.

| Modo | Ataque | Custo de mana 💎 | Quando usar |
|---|---|---|---|
| 💬 **Ask** | *"Conversar com Toad"* | Baixíssimo | Tirar dúvida, explorar, entender |
| 🗺 **Plan** | *"Abrir o mapa antes de andar"* | Médio | Mudar vários arquivos com cuidado |
| 🦖 **Agent** | *"Subir no Yoshi e ele corre por você"* | Alto | Delegar quest inteira (Issue → PR) |

📘 Detalhes em [`04-3-modos-do-copilot.md`](04-3-modos-do-copilot.md) e [`../09-cheat-sheets/copilot-3-modes.md`](../09-cheat-sheets/copilot-3-modes.md).

---

## 🎬 Cena típica de combate (Estágio 2)

> Você é **Requirements Engineer (Toad)**. Estágio 2 acabou de começar.

```
[Time grita] "Seleciona @architect no chat!"
       ↓
[Música muda — entrou o Mundo 2]
       ↓
Você usa Ask 💬:
   "@architect, qual a melhor ordem para escrever EARS
    desse catálogo BR-001 a BR-015?"
       ↓
[@architect responde — enquadramento de arquiteta]
       ↓
Você usa seu golpe de Toad: /ears-convert 📜
       ↓
EARS sai com source_legacy ⭐
       ↓
[Goomba do CI valida] ✅
       ↓
+10 XP, +1 REQ-ID na pochete 🪙
```

---

## 🛑 Mortes comuns no raid

Cada um destes erros é equivalente a "cair no buraco":

| ❌ Erro | 💀 Consequência | ✅ Como evitar |
|---|---|---|
| Usar **Agent** para tarefa de 5 min | Gasta mana demais, perde turno | Use Ask ou Plan |
| Selecionar agente errado para o mundo | Conversa fica fora de contexto | Confirme com o time antes do estágio |
| Ignorar inventário (persona-kit) | Você ataca de mãos vazias | Copie seus 2 kits no setup, faça `/help` no Chat |
| Pular um mundo inteiro | Game over no fim do dia (demo fracassa) | Cumpra a DoD de cada estágio |
| Delegar Issue vaga ao Agent | PR vem ruim, retrabalho dobro | Veja [`08-exemplos/issue-para-agent-exemplo.md`](../08-exemplos/issue-para-agent-exemplo.md) |

---

## 🧩 Por que **duas camadas** de agente?

Algumas pessoas perguntam: *"se eu já tenho persona, por que preciso de @archaeologist também?"*

**Porque você não joga sozinho.** O persona é **seu personagem individual**. O @archaeologist é **o mundo em que o time todo está agora**. Os dois coexistem.

```
              YOUR persona-kit             agent-kit (TIME inteiro)
              (sua mochila pessoal)        (mundo atual selecionado)
                       ╲                          ╱
                        ╲                        ╱
                         ╲                      ╱
                          ▼                    ▼
                ┌─────────────────────────────────────┐
                │      Copilot Chat aberto            │
                │                                     │
                │  Você fala como Toad/Mario/etc.     │
                │  O Chat responde no contexto        │
                │  do mundo (@archaeologist etc.)     │
                │                                     │
                └─────────────────────────────────────┘
```

Exemplo concreto:

```
🍄 Persona: você é Developer (Luigi)
🟧 Agente:  time está no Mundo 3-1 (@builder)

Você pergunta: "Como faço um service de pagamento?"

Resposta: ENQUADRADA no estilo @builder (foca em Java + Testcontainers,
não em arqueologia ou em IaC). E sua skill de Luigi (TDD, JPA) já está
ativa, então /tdd-cycle funciona direto.
```

---

## 🆘 Se travar

| Sintoma | O que fazer |
|---|---|
| "Não sei selecionar agente no Chat" | Painel do Copilot Chat → ícone de seleção no topo do input → escolha do dropdown |
| "Meu slash command não funciona" | Você copiou seu persona-kit para `.github/`? Veja `00-00-SETUP.md` Passo 8 |
| "O Copilot está respondendo fora de contexto" | Provavelmente nenhum agent-kit selecionado. Selecione o do estágio atual |
| "Por que preciso escolher de novo no Estágio 3?" | Mundo mudou. Som da fase mudou. Selecione `@builder`. |

---

## 🔗 Para se aprofundar

- 👥 [Lista completa de personas](../05-personas/OVERVIEW.md)
- 🌍 [Lista dos 4 agentes](../06-agentes-de-estagio/)
- 🎴 [Cheat-sheet de 1 página dos 3 modos](../09-cheat-sheets/copilot-3-modes.md)
- 📊 [Matriz persona × agente](../docs/persona-agent-matrix.md)

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="01-spec-kit-como-mario-maker.md"><strong>Spec-Kit como Mario Maker</strong></a><br/>
<sub>Como você "desenha a fase" antes de jogar.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="03-glossario-visual.md"><strong>Glossário Visual</strong></a><br/>
<sub>30+ termos em 3 linhas cada.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>
