<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ⚔️ Os 3 modos do Copilot (resumo)


> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Conceitos](00-README.md) → **3 modos do Copilot**

> **Para quem é isto?** Para quem precisa lembrar **rapidamente** quando usar Ask, Plan ou Agent.
>
> **Em uma frase:** **Ask** = conversar. **Plan** = abrir o mapa. **Agent** = delegar pro Yoshi.

![CONCEITO 04](https://img.shields.io/badge/CONCEITO-04-00A4EF?style=for-the-badge) ![T\u00d3PICO 3 Modos](https://img.shields.io/badge/T%C3%93PICO-3%20Modos-1A1A1A?style=for-the-badge) ![Todo o dia](https://img.shields.io/badge/USE-Todo%20o%20dia-737373?style=for-the-badge)

---

## 🎯 A decisão em 5 segundos

```
A pergunta que importa:
"Quanto contexto eu já tenho?"

        ╔════════════════════════════════════════╗
        ║                                        ║
        ║  POUCO       ─────►  💬 Ask           ║
        ║  contexto             (conversar)      ║
        ║                                        ║
        ║  TENHO       ─────►  🗺 Plan          ║
        ║  contexto             (planejar)       ║
        ║  e quero                               ║
        ║  mudar                                 ║
        ║  vários                                ║
        ║  arquivos                              ║
        ║                                        ║
        ║  TENHO       ─────►  🦖 Agent         ║
        ║  contexto             (delegar)        ║
        ║  e a tarefa                            ║
        ║  é grande                              ║
        ║  e bem                                 ║
        ║  definida                              ║
        ║                                        ║
        ╚════════════════════════════════════════╝
```

---

## 🎮 Mapa Mario × Copilot

| Modo | Mario | Custo | Quando |
|---|---|---|---|
| 💬 **Ask** | Falar com Toad no início da fase | 💎 Baixíssimo | Tirar dúvida, explorar, entender |
| 🗺 **Plan** | Abrir o mapa antes de pular | 💎💎 Médio | Mudar vários arquivos com cuidado |
| 🦖 **Agent** | Subir no Yoshi (corre por você) | 💎💎💎 Alto | Issue inteira → PR sozinho |

---

## 📜 Versão completa

Este é o resumo de 1 página. A explicação detalhada com exemplos por persona vive no cheat-sheet:

👉 [`../09-cheat-sheets/copilot-3-modes.md`](../09-cheat-sheets/copilot-3-modes.md)

---

## 💡 3 prompts prontos (um por modo)

```text
# 💬 Ask — explorar
"Explique linha por linha o que CALCDSCT.NSN faz. Foque em decisões de
negócio, ignore IO."

# 🗺 Plan — mudar com cuidado
"Plan: adicionar validação de CPF (módulo 11) ao BeneficiaryService.
Liste arquivos a tocar, ordem das mudanças, e os testes a criar.
NÃO implemente ainda."

# 🦖 Agent — delegar
[Crie GitHub Issue completa primeiro — formato em
08-exemplos/issue-para-agent-exemplo.md — depois dispare o Agent]
```

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="03-glossario-visual.md"><strong>Glossário Visual</strong></a><br/>
<sub>30+ termos em 3 linhas.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="05-ears-receita-de-cogumelo.md"><strong>EARS como receita</strong></a><br/>
<sub>Requisitos sem ambiguidade.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>
