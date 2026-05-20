<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ❓ FAQ — Perguntas Frequentes

![DOC FAQ](https://img.shields.io/badge/DOC-FAQ-00A4EF?style=for-the-badge) ![USE Dúvida específica](https://img.shields.io/badge/USE-Dúvida%20específica-1A1A1A?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Docs](README.md) → **FAQ**

> **Para quem é isto?** Para você que tem uma dúvida específica e quer resposta em uma frase.
>
> **Como usar:** `Ctrl+F` na pergunta. Se não achar, abra [troubleshooting.md](troubleshooting.md).

---

## 🎯 Sobre o workshop

**Q: Não programo. Posso participar?**
R: Sim. PO, Tech Writer e parte do QA não precisam codar. Veja [`07-conceitos/`](../07-conceitos/) primeiro para se familiarizar com os conceitos. Cada `PERSONA.md` tem "defaults de emergência".

**Q: Quanto tempo dura?**
R: 8 horas (10:00–18:00 num dia). Cronograma exato em [`00-TEAM-FLOW.md`](../00-TEAM-FLOW.md) §2.

**Q: Quantas pessoas por time?**
R: 5 pessoas. Cada uma veste 2 personas (1 par). Total: 10 personas cobertas.

**Q: Posso escolher minhas 2 personas?**
R: Sim, mas combine com o time. Pares 1, 4 e 5 acomodam não-devs. Pares 2 e 3 pedem background técnico.

**Q: O que é o "SIFAP"?**
R: Sistema fictício de pagamentos do governo, com 29 anos em Natural/Adabas. O workshop simula modernizar para Java + Next.js. Veja [`01-arqueologia/legado-sifap/README.md`](../01-arqueologia/legado-sifap/README.md).

---

## 🤖 Sobre o Copilot

**Q: Qual modelo do Copilot devo usar?**
R: Sonnet 4.6 para a maioria das tarefas. Haiku para coisas mecânicas. Opus para decisões arquiteturais. Veja [`09-cheat-sheets/model-routing.md`](../09-cheat-sheets/model-routing.md).

**Q: Quando usar Ask, Plan ou Agent?**
R: Ask = conversar/entender. Plan = planejar mudança multi-arquivo. Agent = delegar Issue completa. [`07-conceitos/04-3-modos-do-copilot.md`](../07-conceitos/04-3-modos-do-copilot.md).

**Q: O Agent pode mergear sozinho?**
R: NÃO. Ele abre PR. **Você revisa** com o mesmo cuidado que revisaria PR humano.

**Q: Posso usar Cursor / Codeium / outro assistente?**
R: NÃO. Stack fixa: somente GitHub Copilot. Veja [.github/copilot-instructions.md](../.github/copilot-instructions.md).

---

## 📐 Sobre Spec-Kit e EARS

**Q: Por que toda EARS precisa de `source_legacy:`?**
R: Para garantir que vocês modernizaram o **sistema real** e não o brief. CI rejeita PRs sem isso. [`01-arqueologia/LEGACY-EXPLORATION-CHECKLIST.md`](../01-arqueologia/LEGACY-EXPLORATION-CHECKLIST.md).

**Q: E se a feature é nova, não tem paralelo no legado?**
R: Use `source_legacy: "[GREENFIELD] <justificativa de 1 linha>"`. Exemplo: `"[GREENFIELD] OAuth2 não existia em terminal 3270."`.

**Q: Posso pular `/speckit.clarify`?**
R: Não. Pular = bugs descobertos no Estágio 3 (tarde demais).

**Q: O `/speckit.analyze` está reclamando. E agora?**
R: Bom. Resolva antes de implementar. Cada apontamento = uma hora a menos de retrabalho.

---

## 🌿 Sobre Git e branches

**Q: Posso mexer direto na `main`?**
R: NÃO. Sempre via PR. Veja [`00-GIT-WORKFLOW.md`](../00-GIT-WORKFLOW.md) regra #1.

**Q: Qual prefixo de branch usar?**
R: `spec/<NNN>-...` no Estágio 2, `impl/...` no Estágio 3, `infra/...` no Estágio 4. Tabela completa em [`00-GIT-WORKFLOW.md`](../00-GIT-WORKFLOW.md).

**Q: Como meu PR é aprovado?**
R: CI verde + 1 review do par downstream. Par 1 → Par 2 → Par 3 → Par 4 → Par 5 → Par 1.

**Q: Posso fazer `git push --force`?**
R: Só em branch sua e somente com `--force-with-lease`. Nunca em `develop` ou `main`.

---

## ☁️ Sobre Terraform e Azure

**Q: Posso rodar `terraform apply`?**
R: ❌ NÃO. Só `plan`. Criar recursos Azure custa dinheiro real, não está autorizado no workshop.

**Q: Onde guardo secrets?**
R: Azure Key Vault. Nunca em `variables.tf` ou `.env` commitado. Veja módulo `12-scripts/` e o referencial `infra/`.

---

## 🍄 Sobre estágios e passagens

**Q: O que é "passagem H1, H2, H3"?**
R: Momentos de transferência entre pares ao final de cada estágio. Conversa síncrona de 5 minutos, não slide. Detalhes em [`00-TEAM-FLOW.md`](../00-TEAM-FLOW.md) §3.

**Q: Posso adiantar o Estágio 2 enquanto o 1 ainda está rolando?**
R: NÃO. Sem o Estágio 1 completo, suas EARS não terão `source_legacy:` e o CI rejeita.

**Q: Quem lidera cada estágio?**
R: Tabela em [`05-personas/OVERVIEW.md`](../05-personas/OVERVIEW.md). Resumo: S1 = todos paralelos, S2 = Par 2, S3 = Pares 3+4, S4 = Par 5.

---

## 🆘 Sobre travas

**Q: Travei. O que faço?**
R: Regra dos 20 min ([`00-TEAM-FLOW.md`](../00-TEAM-FLOW.md) §6): tente sozinho 5 min, par 10 min, time 20 min, facilitador 30 min.

**Q: Não sei se minha persona é técnica ou não-técnica.**
R: Olhe o `PERSONA.md` da sua persona em [`05-personas/`](../05-personas/). Lê 5 min, descobre.

**Q: Como peço ajuda direito?**
R: 3 linhas: (1) Objetivo, (2) O que tentei, (3) Bloqueio. Exemplo em [`00-TEAM-FLOW.md`](../00-TEAM-FLOW.md) §6.

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="troubleshooting.md"><strong>Troubleshooting</strong></a><br/>
<sub>Erros comuns, soluções.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../README.md"><strong>Kit PT-BR</strong></a><br/>
<sub>Hub principal.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>
