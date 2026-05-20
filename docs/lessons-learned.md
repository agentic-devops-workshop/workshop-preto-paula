<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# 🪦 Lições Aprendidas — Erros Comuns dos Times

![DOC Lessons Learned](https://img.shields.io/badge/DOC-Lessons%20Learned-FFB900?style=for-the-badge) ![USO Antes do dia 2](https://img.shields.io/badge/USO-Antes%20do%20dia%202-1A1A1A?style=for-the-badge) ![LEIA 5 min](https://img.shields.io/badge/LEIA-5%20min-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Docs](README.md) → **Lições aprendidas**

> **Para quem é isto?** Para todo o time, especialmente o líder. Leia **antes** do workshop começar.
>
> **O que você terá ao final desta leitura:**
>
> 1. 10 erros que times anteriores cometeram
> 2. Consequência observada (quanto tempo perderam)
> 3. Como evitar — o antídoto exato

---

## 🩸 Os 10 erros mais comuns

### ❌ 1. "Vamos só ler o brief do cliente, não precisa olhar o legado"

- **Consequência:** time escreve EARS sem `source_legacy:`. CI rejeita PR às 14:30. Time perde 1h refazendo.
- **Antídoto:** Hard gate do Estágio 1 — facilitador valida em 13:50. Veja [`01-arqueologia/LEGACY-EXPLORATION-CHECKLIST.md`](../01-arqueologia/LEGACY-EXPLORATION-CHECKLIST.md).

### ❌ 2. "Vou começar a codar enquanto outro escreve a spec"

- **Consequência:** código não bate com EARS. Refator no fim do dia. Demo capenga.
- **Antídoto:** Estágio 3 **só começa** depois do H2. Sem exceção. TL aborta tentativas de adiantar.

### ❌ 3. PO concorda com tudo e nada vira não-escopo

- **Consequência:** time tenta implementar 12 features em 3h. Termina 0.
- **Antídoto:** PO diz NÃO 3× ao dia. Regra: *"afeta o ciclo mensal? sim → v1. Não? → backlog."*

### ❌ 4. Cada pessoa pergunta ao Copilot de um jeito diferente

- **Consequência:** respostas inconsistentes. Time discute com a IA em vez de fazer.
- **Antídoto:** Time inteiro seleciona **o mesmo agente de estágio** (`@archaeologist` etc.) no Chat.

### ❌ 5. "Vou pular `/speckit.clarify`, demora demais"

- **Consequência:** ambiguidades viram bug no Estágio 3. 30 min de pergunta agora poupam 2h depois.
- **Antídoto:** Cada pergunta do `clarify` = 1 bug evitado. Responda **todas**.

### ❌ 6. Dev faz `git push --force` em `develop`

- **Consequência:** trabalho de 2 pessoas perdido.
- **Antídoto:** Branch protection no `develop` (Passo 4 do `00-SETUP.md`). E nunca `--force` em compartilhada.

### ❌ 7. "Vou editar V1 da migration em vez de criar V2"

- **Consequência:** Flyway detecta checksum mismatch, banco quebra.
- **Antídoto:** **NUNCA edite migration antiga.** Sempre crie V<N+1>. Veja `docs/troubleshooting.md`.

### ❌ 8. Time delega Issue vaga ao Copilot Agent

- **Consequência:** PR vem ruim, ninguém entende, descarta. Trabalho perdido.
- **Antídoto:** Use o template em [`08-exemplos/issue-para-agent-exemplo.md`](../08-exemplos/issue-para-agent-exemplo.md). Issue boa = PR bom.

### ❌ 9. Rodar `terraform apply` em vez de `plan`

- **Consequência:** ⚠️ recursos Azure criados de verdade → conta. Workshop **NÃO autoriza** `apply`.
- **Antídoto:** **Só `terraform plan`.** Veja `04-evolucao/GUIDE.md`.

### ❌ 10. Não ensaiar a demo

- **Consequência:** time gasta os 3 minutos da demo procurando aba certa, comando que falha, PR perdido.
- **Antídoto:** **16:50–17:00** é só ensaio. Script literal em [`demo-script.md`](demo-script.md).

---

## 🎯 Os 5 reflexos que separam time bom de time excelente

1. 🔁 **Stand-up de 2 min** ao fim de cada estágio
2. 💬 **PR com descrição** sempre (template no GitHub)
3. ⚡ **Commits pequenos** com REQ-ID no message
4. 🤝 **20-min rule:** travou? Pede ajuda. Não sofre calado.
5. 📜 **Confiar no processo** — não tente inventar fluxo novo no meio

---

## 🧠 A regra mestre

> **Modernizar é arqueologia digital, não greenfield.**
> Quem trata SIFAP como projeto novo perde 29 anos de regra de negócio.
> Quem trata como arqueologia entrega um SIFAP 2.0 que substitui o 1.0.

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="CHECKLIST-LIDER.md"><strong>Checklist do Líder</strong></a><br/>
<sub>Hora a hora do dia.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="demo-script.md"><strong>Script da Demo</strong></a><br/>
<sub>3 minutos finais ensaiados.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>
