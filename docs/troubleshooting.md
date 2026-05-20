<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# 🆘 Troubleshooting Consolidado

![DOC Troubleshooting](https://img.shields.io/badge/DOC-Troubleshooting-00A4EF?style=for-the-badge) ![USE Algo deu errado](https://img.shields.io/badge/USE-Algo%20deu%20errado-1A1A1A?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Docs](README.md) → **Troubleshooting**

> **Para quem é isto?** Para quando algo deu errado e você quer ver se o erro já foi resolvido por alguém. Pesquise com `Ctrl+F`.
>
> **Como funciona:** problemas agrupados por **categoria**. Cada item tem **sintoma**, **causa provável**, **solução**.

---

## 📚 Índice

- [💻 Setup e ambiente](#-setup-e-ambiente)
- [🤖 Copilot / agente / persona](#-copilot--agente--persona)
- [📐 Spec-Kit e EARS](#-spec-kit-e-ears)
- [☕ Backend (Java / Spring Boot)](#-backend-java--spring-boot)
- [⚛️ Frontend (Next.js / Node)](#%EF%B8%8F-frontend-nextjs--node)
- [🐳 Docker e docker-compose](#-docker-e-docker-compose)
- [🌿 Git e GitHub](#-git-e-github)
- [☁️ Terraform e Azure](#%EF%B8%8F-terraform-e-azure)

---

## 💻 Setup e ambiente

### "VS Code não abre o devcontainer"

- **Causa:** Docker Desktop não está rodando.
- **Solução:** abra o app Docker Desktop, espere a baleia ficar verde, depois `Cmd+Shift+P` → *Dev Containers: Reopen in Container*.

### "Faltando Java/Node/Maven"

- **Causa:** você está rodando fora do devcontainer.
- **Solução:** ou usa o devcontainer (recomendado), ou instala as versões da tabela em [`00-SETUP.md` Passo 1](../00-SETUP.md#-passo-1).

### "git: command not found" no terminal do VS Code (Mac)

- **Causa:** xcode-tools sem CLI tools.
- **Solução:** `xcode-select --install`.

### Erro `EACCES` ao rodar scripts

- **Causa:** falta permissão.
- **Solução:** `chmod +x scripts/*.sh`.

---

## 🤖 Copilot / agente / persona

### "Slash command (`/ears-convert`, `/tdd`, etc.) não aparece"

- **Causa:** você não copiou seu persona-kit para `.github/`.
- **Solução:**
  ```bash
  cp -r 05-personas/XX-sua-persona/.github/* .github/
  cp -r 05-personas/YY-sua-outra-persona/.github/* .github/
  ```
  Recarregue a janela: `Cmd+Shift+P` → *Developer: Reload Window*.

### "Não consigo selecionar `@archaeologist` no Chat"

- **Causa 1:** a pasta `06-agentes-de-estagio/` não está no workspace.
- **Causa 2:** Copilot extensão desatualizada.
- **Solução:** confirme `ls 06-agentes-de-estagio/`. Atualize a extensão GitHub Copilot Chat na aba de extensões.

### "O Copilot está respondendo fora de contexto"

- **Causa:** nenhum agente de estágio selecionado, ou agente errado.
- **Solução:** confirme com o time qual estágio vocês estão, selecione o agente correspondente no dropdown do Chat.

### "Quero usar Plan mode mas só aparece Ask"

- **Causa:** versão antiga do Copilot.
- **Solução:** atualize a extensão. Plan mode é GA desde 2025.

---

## 📐 Spec-Kit e EARS

### "`specify version` dá comando não encontrado"

- **Causa:** Spec-Kit não instalado.
- **Solução:**
  ```bash
  uv tool install specify-cli --from git+https://github.com/github/spec-kit.git
  specify version
  ```

### "CI rejeitou meu PR: `missing source_legacy`"

- **Causa:** uma das EARS não tem a linha `source_legacy:`.
- **Solução:** abra o `SPECIFICATION.md`, procure REQ-IDs sem `source_legacy:`, e ou (a) aponte para `01-arqueologia/legado-sifap/...#L<linha>` ou (b) marque `[GREENFIELD] <motivo>`.
- 📘 Veja [`07-conceitos/05-ears-receita-de-cogumelo.md`](../07-conceitos/05-ears-receita-de-cogumelo.md).

### "`/speckit.clarify` está fazendo 12 perguntas. É muito?"

- **Causa:** não é muito. É bom.
- **Solução:** responda todas. Cada pergunta evita um bug futuro.

---

## ☕ Backend (Java / Spring Boot)

### "Backend não sobe — erro de conexão com Postgres"

- **Causa:** Postgres não subiu antes do backend.
- **Solução:** `docker compose down && docker compose up -d postgres && sleep 10 && docker compose up -d backend`.

### "Flyway: `Migration checksum mismatch`"

- **Causa:** alguém editou uma migration antiga (proibido).
- **Solução:** **nunca edite V1, V2, V3 depois de mergeados.** Crie V<N+1>. Se já editou, restaure a versão original do `git log` e crie nova migration.

### "Testcontainers: `Could not find a valid Docker environment`"

- **Causa:** Docker não está rodando ou o socket está em outro caminho.
- **Solução (macOS):** `export DOCKER_HOST=unix:///var/run/docker.sock` ou `export TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock`.

---

## ⚛️ Frontend (Next.js / Node)

### "Frontend mostra `ECONNREFUSED localhost:8080`"

- **Causa:** backend não está no ar ou está em outra porta.
- **Solução:** verifique `docker compose ps`. Backend deve estar em 8080. Se subiu pelo docker-compose do root, frontend está em 3001, não 3000.

### "`Module not found: shadcn/ui`"

- **Causa:** dependências não instaladas.
- **Solução:** `cd 04-prototipo-sifap-moderno/frontend && npm install`.

---

## 🐳 Docker e docker-compose

### "`Cannot connect to the Docker daemon`"

- **Causa:** Docker Desktop parado.
- **Solução:** abra o app, espere a baleia ficar verde, tente de novo.

### "`port is already allocated`"

- **Causa:** outra coisa usando 5432 / 8080 / 3000.
- **Solução:** `lsof -i :8080` para descobrir o que está usando, mate o processo, ou mude a porta no `docker-compose.yml`.

### "`Out of memory` no Docker Desktop"

- **Causa:** RAM alocada baixa.
- **Solução:** Docker Desktop → Settings → Resources → Memory → 8GB+.

---

## 🌿 Git e GitHub

### "Push rejeitado: `protected branch`"

- **Causa:** você tentou push direto em `main` ou `develop`.
- **Solução:** crie uma branch e abra PR. 📘 Veja [`00-GIT-WORKFLOW.md`](../00-GIT-WORKFLOW.md).

### "Conflito de merge"

- **Causa:** alguém mudou o mesmo arquivo na develop antes de você.
- **Solução:**
  ```bash
  git fetch origin
  git rebase origin/develop
  # Resolva conflitos nos arquivos com <<<<<<<
  git add <arquivo>
  git rebase --continue
  ```

### "Esqueci de criar branch e commitei na develop"

- **Solução:**
  ```bash
  git reset --soft HEAD~1
  git stash
  git checkout -b nova-branch
  git stash pop
  git commit -m "..."
  ```

### "`gh: command not found`"

- **Causa:** GitHub CLI não instalado.
- **Solução:** `brew install gh && gh auth login`.

---

## ☁️ Terraform e Azure

### "`Error: building AzureRM Client`"

- **Causa:** não logado no Azure CLI.
- **Solução:** `az login` (workshop usa Azure CLI auth, não service principal).

### "`terraform apply` — POSSO RODAR?"

- **Resposta:** ❌ **NÃO.** Workshop só permite `terraform plan`. `apply` cria recursos reais que custam dinheiro.

### "`terraform plan` mostra mil recursos novos"

- **Causa:** state file vazio.
- **Solução:** isso é esperado. Plan mostra o que seria criado se rodasse apply. Não rode apply.

---

## 🛟 Plano B — Copilot fora do ar

Se o Copilot Chat parar de responder por mais de 5 minutos:

1. ⚠️ **NÃO espere.** O dia tem 8h, você não pode pagar 30 min ocioso.
2. 🔄 Tente recarregar o VS Code (`Cmd+Shift+P` → *Reload Window*). Se voltar, continue.
3. 📋 Se continuar offline, **vá para [`../08-exemplos/`](../08-exemplos/)** e copie a estrutura do artefato similar ao seu.
4. ✍️ Adapte com sua cabeça (você tem o suficiente — os exemplos são gabarito).
5. 📝 No PR, escreva: *"Feito manualmente em X min (Copilot offline)"* — isso ajuda no relatório de evolução do Estágio 4.
6. 🤝 Combine com o par receptor que o PR pode ter menos refinamento que o normal.

**O CI continua validando.** Você não está perdido — só sem assistente.

| Artefato sem Copilot | Use como base |
|---|---|
| EARS no Estágio 2 | [`../08-exemplos/SPECIFICATION-exemplo.md`](../08-exemplos/SPECIFICATION-exemplo.md) |
| ADR no Estágio 2 | [`../08-exemplos/ADR-001-monolito-modular-exemplo.md`](../08-exemplos/ADR-001-monolito-modular-exemplo.md) |
| Service Java no Estágio 3 | [`../08-exemplos/PaymentService-exemplo.java`](../08-exemplos/PaymentService-exemplo.java) |
| Migration no Estágio 3 | [`../08-exemplos/V1__init_payment_module-exemplo.sql`](../08-exemplos/V1__init_payment_module-exemplo.sql) |
| Issue para Agent no Estágio 4 | [`../08-exemplos/issue-para-agent-exemplo.md`](../08-exemplos/issue-para-agent-exemplo.md) |

---

## 🆘 Quando nada acima funciona

| Travado há | Faça isto |
|---|---|
| 5 min | Releia o erro com calma. Use Copilot Ask: *"O que esse erro significa: <cole o erro>"* |
| 10 min | Pergunte ao seu par. Duas cabeças. |
| 20 min | Levante a mão para o facilitador (regra do TEAM-FLOW §6) |
| 30 min | Pare. Faça outra tarefa enquanto alguém ajuda. Não fure as 8 horas batendo cabeça num bug. |

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="../README.md"><strong>Kit PT-BR</strong></a><br/>
<sub>Hub principal.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="FAQ.md"><strong>FAQ</strong></a><br/>
<sub>Perguntas frequentes.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>
