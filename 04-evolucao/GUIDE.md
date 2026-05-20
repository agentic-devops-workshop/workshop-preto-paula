<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Estágio 4 — Evolução com Agentes (40 min)



> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 4](README.md) → **GUIDE**

> **Para quem é isto?** Para o par 5 (DevOps+TW) que lidera + par 3 que co-lidera (escreve Issues e revisa PRs do Agent).
>
> **O que você terá ao final desta leitura:**
>
> 1. 1+ Issue bem escrita para o Copilot Agent
> 2. 1+ PR gerado pelo Agent, revisado e mergeado
> 3. `terraform plan` rodando sem erro (sem `apply` real)
> 4. CI verde em `develop`
> 5. Relatório honesto da experiência com o Agent

![ESTÁGIO 04 de 04](https://img.shields.io/badge/EST%C3%81GIO-04%20de%2004-FFB900?style=for-the-badge) ![Duração 40 min](https://img.shields.io/badge/DURA%C3%87%C3%83O-40%20min-737373?style=for-the-badge) ![Líder Par 5](https://img.shields.io/badge/L%C3%8DDER-Par%205%20(DevOps%2BTW)-1A1A1A?style=for-the-badge)

> ⏰ **Cronograma exato** vive em [`00-TEAM-FLOW.md`](../00-TEAM-FLOW.md) §2. Os badges aqui mostram apenas a **duração** do estágio.

> **Categoria:** Evolução · **Quem trabalha agora:** Par 5 lidera · Par 3 co-lidera

> 🧭 **Antes de entrar neste estágio** (1 minuto de leitura):
>
> - **Agent, Terraform, IaC, ACR, Key Vault** — termos novos? [`../07-conceitos/03-glossario-visual.md`](../07-conceitos/03-glossario-visual.md).
> - **Issue completa para o Agent** (modelo): [`../08-exemplos/issue-para-agent-exemplo.md`](../08-exemplos/issue-para-agent-exemplo.md). **Esta é a peça mais importante deste estágio.**
> - **Diferença entre Ask, Plan e Agent:** [`../09-cheat-sheets/copilot-3-modes.md`](../09-cheat-sheets/copilot-3-modes.md). No Estágio 4 é Agent.
> - **PR do Agent veio ruim?** A causa quase sempre é a Issue. Volte ao exemplo acima antes de tentar de novo.
> - **`terraform apply`?** ❌ NÃO. Só `plan` no workshop — recurso Azure real custa dinheiro.

## ⛳ Definition of Ready — antes de começar

> [!IMPORTANT]
> Não abra este estágio sem antes confirmar:
>
> - [ ] Estágio 3 terminou (Passagem H3 — código + testes verdes)
> - [ ] Você selecionou **`@evolution`** no Copilot Chat
> - [ ] CI verde em `develop`
> - [ ] Cobertura de testes ≥70% (backend) e ≥60% (frontend)
> - [ ] Aba aberta em [`08-exemplos/issue-para-agent-exemplo.md`](../08-exemplos/issue-para-agent-exemplo.md)
> - [ ] `az login` funcionando (somente `terraform plan`, nunca `apply`)

## Quem trabalha aqui

![Linha do tempo do dia: pré-evento, 4 estágios e demo, com as três passagens H1, H2, H3](../assets/timeline-stages.svg)

## Objetivo

Usar o **modo Agent do GitHub Copilot** para implementar funcionalidades completas via Issues e Pull Requests, e explorar infraestrutura como código (Terraform) para deploy no Azure.

## Por que isso importa

Até agora vocês usaram o Copilot para **perguntar e explorar** (Ask) e para **planejar mudanças** (Plan). No Estágio 4 vocês usam como **delegado**: escrevem uma Issue, deixam o Agent trabalhar sozinho, e revisam o PR ao final. É o mais perto que dá de "dirigir o time de IA". A qualidade da sua Issue determina a qualidade do PR — entra lixo, sai lixo.

## Como pensar nisso

Pense no Agent como **um dev júnior muito rápido e literal**: ele faz exatamente o que está escrito. Não infere. Não pergunta. Se você não disse "respeite a arquitetura modular", ele talvez não respeite. Se você não disse "inclua testes", ele talvez não inclua. **A Issue é o seu contrato com ele.**

---

## Parte 1: Modo Agent do GitHub Copilot (2 horas)

### O que é o modo Agent?

**O modo Agent do Copilot** é o terceiro modo do GitHub Copilot (além de Ask e Plan). No modo Agent você:

1. **Escreve uma GitHub Issue** descrevendo a funcionalidade completa
2. **Dispara o Agent** no VS Code (via painel do Copilot → "Start Agent" ou pelo Copilot Workspace no github.com)
3. **O Agent analisa o codebase inteiro**, planeja as mudanças e implementa código + testes + docs
4. **Abre um Pull Request** para você revisar

Pense no Agent como **um dev júnior muito rápido** — ele faz o trabalho pesado, mas VOCÊ precisa revisar tudo antes do merge.

> **Diferença entre os 3 modos:**
>
> - **Ask**: você pergunta, o Copilot responde (exploração, dúvidas)
> - **Plan**: você descreve uma mudança, o Copilot propõe plano e arquivos afetados
> - **Agent**: você descreve a funcionalidade inteira via Issue, o Copilot implementa sozinho (delegação)

### Como escrever uma boa Issue para o Agent

Uma Issue bem escrita é 80% do sucesso. Siga este formato:

---

#### Exemplo real: notificação de pagamento por e-mail

```markdown
## Título

Adicionar notificação por e-mail na confirmação de pagamento

## Descrição

Quando um pagamento é confirmado (status mudando de PENDING para APPROVED),
o sistema deve enviar um e-mail de notificação ao beneficiário informando
o valor e a data de pagamento.

## Requisitos Funcionais

- [ ] Quando o status de um pagamento mudar para APPROVED, enviar um e-mail
- [ ] O e-mail deve conter: nome do beneficiário, valor, data, número do pagamento
- [ ] Se o envio falhar, registrar no log de auditoria (não bloquear o pagamento)
- [ ] O modelo do e-mail deve ser configurável

## Requisitos Técnicos

- [ ] Criar um EmailService no módulo payment/application
- [ ] Usar Spring Mail configurado via application.yml
- [ ] Criar um teste unitário mockando o envio de e-mail
- [ ] Criar um teste de integração com MailHog (container Docker)
- [ ] Adicionar a variável SMTP_HOST ao docker-compose.yml

## Arquitetura

- Seguir a estrutura modular existente (domain/application/infrastructure)
- O EmailService deve ser injetado no PaymentService
- Usar eventos Spring (ApplicationEvent) para desacoplar

## Critérios de Aceite

- [ ] Teste unitário passando
- [ ] Teste de integração passando
- [ ] docker compose up funcionando com MailHog
- [ ] E-mail recebido no MailHog ao aprovar um pagamento via Swagger

## Contexto

- Backend: Java 21 + Spring Boot 3
- Módulo relevante: src/.../payment/
- Referências: PaymentService.java, PaymentController.java
- REQ-ID relacionada: REQ-PAY-NOTIF-01
```

---

### Checklist para escrever Issues

Antes de submeter a Issue para o Agent, verifique:

- [ ] **Título claro** — descreve a funcionalidade em uma frase
- [ ] **Descrição com contexto** — o Agent precisa entender o "por quê"
- [ ] **Requisitos como checklist** — itens verificáveis
- [ ] **Requisitos técnicos** — onde no código, quais padrões seguir
- [ ] **Critérios de aceite** — como saber que está pronto
- [ ] **Referências de arquivo** — ajudam o Agent a achar o código certo
- [ ] **REQ-ID rastreada** — se a feature vem do Estágio 2

### Workflow do Agent

1. **Crie a Issue** no GitHub usando o formato acima
2. **Dispare o Agent** (via Copilot Workspace ou VS Code)
3. **Espere o PR** — o Agent trabalha e abre um PR
4. **Revise o PR** usando o checklist abaixo
5. **Solicite ajustes** se necessário (comente no PR)
6. **Merge** quando estiver satisfeito

---

### Como revisar um PR do Agent (checklist de qualidade)

Quando o Agent abre um PR, revise com cuidado:

#### Corretude

- [ ] O código compila sem erros?
- [ ] Os testes passam? (`./mvnw test`)
- [ ] A funcionalidade funciona como descrito na Issue?

#### Arquitetura

- [ ] Segue a estrutura modular (domain/application/infrastructure)?
- [ ] Não há imports circulares entre módulos?
- [ ] A camada domain evita importar classes de infrastructure?

#### Qualidade

- [ ] Nomes de classes, métodos e variáveis estão claros?
- [ ] Tem tratamento de erro adequado?
- [ ] Tem validação de input (Bean Validation)?
- [ ] Não há credenciais hardcoded?

#### Testes

- [ ] Há testes unitários para a lógica de negócio?
- [ ] Há testes de integração para os endpoints?
- [ ] Os testes cobrem casos de erro (não só o caminho feliz)?

#### Documentação

- [ ] Novos endpoints aparecem no Swagger?
- [ ] Há JavaDoc nos métodos públicos?
- [ ] O README foi atualizado se necessário?

---

## Parte 2: Terraform e Infraestrutura (1 hora)

### Visão geral

Os módulos Terraform para deploy no Azure ficam em:

```
05-terraform-azure/
├── main.tf # Módulo raiz
├── variables.tf # Variáveis de entrada
├── outputs.tf # Saídas
└── modules/
 ├── resource-group/ # Resource group do Azure
 ├── container-registry/ # ACR para imagens Docker
 ├── container-apps/ # Azure Container Apps
 ├── postgresql/ # Azure Database for PostgreSQL
 ├── key-vault/ # Azure Key Vault para secrets
 └── monitoring/ # Application Insights + Log Analytics
```

### O que explorar

1. **Leia `main.tf`** — entenda como os módulos se conectam
2. **Olhe as variables** — quais parâmetros são configuráveis?
3. **Estude os outputs** — que informações o Terraform exporta?
4. **Veja o Key Vault** — como secrets são gerenciados?

### Terraform na prática

| Módulo           | O que provisiona | Recurso Azure                        |
| ---------------- | ---------------- | ------------------------------------ |
| `compute/`       | Backend Java     | App Service (B1 dev, P1v3 prod)      |
| `database/`      | Banco            | PostgreSQL Flexible Server           |
| `frontend/`      | Frontend Next.js | Static Web App                       |
| `registry/`      | Imagens Docker   | Azure Container Registry             |
| `security/`      | Secrets          | Key Vault                            |
| `observability/` | Monitoramento    | Application Insights + Log Analytics |
| `identity/`      | Identidade       | Azure AD / Entra ID                  |

#### Para explorar (não precisa aplicar):

```bash
cd 05-terraform-azure/envs/dev
terraform init # Inicializa providers
terraform plan # Mostra o que SERIA criado (sem aplicar)
```

Exemplo de saída de `terraform plan`:

```
Plan: 12 to add, 0 to change, 0 to destroy.

 + azurerm_resource_group.sifap
 + azurerm_postgresql_flexible_server.sifap
 + azurerm_service_plan.sifap
 + azurerm_linux_web_app.sifap_backend
 + azurerm_static_web_app.sifap_frontend
 + azurerm_key_vault.sifap
 + azurerm_application_insights.sifap
 + azurerm_container_registry.sifap
 ...
```

> [!CAUTION]
> **Escopo do workshop:** explore e entenda os módulos. **Não execute `terraform apply`** — isso criaria recursos Azure reais com custo real. `terraform plan` é suficiente para demonstrar conhecimento.

### Quando o Agent falha

O Copilot Agent não é perfeito. Problemas comuns:

| Sintoma                           | Causa provável                    | O que fazer                                                                             |
| --------------------------------- | --------------------------------- | --------------------------------------------------------------------------------------- |
| PR não compila                    | Issue faltou contexto técnico     | Adicione: arquitetura esperada, arquivos de referência, padrões a seguir                |
| Testes faltando no PR             | Issue não pediu testes            | Adicione checkbox: "Incluir testes unitários e de integração"                            |
| Imports cruzando bounded contexts | Agent ignora fronteiras de módulo | Rejeite o PR; adicione na Issue: "Respeitar fronteiras domain/application/infrastructure" |
| PR com lógica errada              | Requisito ambíguo                 | Reescreva o requisito em EARS e abra nova Issue                                         |
| Agent trava ou demora demais      | Codebase grande demais            | Estreite o escopo: aponte para arquivos específicos na Issue                            |

> [!TIP]
> **Regra de ouro:** quando o Agent erra, a causa quase sempre está na Issue. Melhore a Issue antes de tentar de novo.

### CI/CD: GitHub Actions

Os workflows de CI/CD ficam em:

```
.github/workflows/
├── ci.yml # Build + test em cada PR
├── cd-staging.yml # Deploy automático para staging
└── cd-production.yml # Deploy de produção (aprovação manual)
```

#### Workflow CI (ci.yml)

- Roda em cada Pull Request
- Etapas: checkout → setup Java 21 → build → test → lint
- Se falhar, o PR não pode ser mergeado

#### Workflow CD (cd-staging.yml)

- Roda após merge na branch `develop`
- Etapas: build Docker image → push para ACR → deploy para Container Apps (staging)

---

<details>
<summary><strong>Armadilhas comuns</strong> — clique para expandir</summary>

| ❌ Se você está fazendo isso             | ✅ Faça assim                                                    |
| ---------------------------------------- | ---------------------------------------------------------------- |
| Issue vaga ("adicione notificação")      | Issue completa com funcional + técnico + aceite                  |
| Aceitar PR do Agent sem revisar          | Revisar como se fosse PR humano. Revisão rápida ainda é revisão  |
| Disparar Agent para tarefa de 5 minutos  | Use Ask ou Plan para tarefas pequenas. Agent é para funcionalidades completas |
| Rodar `terraform apply` no workshop      | Só `plan`. Não criar recursos Azure reais                        |
| Hardcode de SMTP_HOST no application.yml | Sempre via env var, com fallback em Key Vault                    |

</details>

---

## Como saber que terminou (Definição de Pronto)

Ao final do Estágio 4, seu time deve ter:

- [ ] **2 Issues** criadas no formato correto para o Agent
- [ ] **2 PRs** gerados pelo Agent (um para cada Issue)
- [ ] **1 feature mergeada** — pelo menos um PR aprovado e mergeado
- [ ] **Relatório de experiência com o Agent** (arquivo: [`agent-experience-report.md`](agent-experience-report.md))
- [ ] `terraform plan` rodando sem erro em `05-terraform-azure/envs/dev/`
- [ ] CI verde na branch `develop` (build + test passando)

## Próximo passo

Demo (18:30) + Retrospectiva (19:10). Cada time tem ~3 minutos. O Par 1 (PO) conduz o demo; todos preparam 30 segundos cada. Veja [`../../07-playbook-facilitacao/DAY-SCRIPT.md`](../../../07-playbook-facilitacao/DAY-SCRIPT.md) para o roteiro completo.

<details>
<summary><strong>Prompts úteis para Copilot Chat</strong> — clique para expandir</summary>

1. _"Crie uma GitHub Issue para o Copilot Agent implementar [funcionalidade]. Use o formato com requisitos funcionais, requisitos técnicos e critérios de aceite."_
2. _"Revise este PR gerado pelo Agent e liste problemas de qualidade."_
3. _"Explique este módulo Terraform: [cole o código]."_
4. _"Que recursos Azure este Terraform vai criar?"_
5. _"Crie um diagrama dos recursos Azure definidos neste Terraform."_
6. _"Como este workflow CI/CD garante qualidade antes do deploy?"_
7. _"Sugira melhorias de segurança para esta configuração Terraform."_

</details>

## Dica de ouro

> [!TIP]
> O Agent é só tão bom quanto a Issue que você escreve. Gaste **mais tempo na Issue** e **menos tempo consertando o PR**. Uma Issue com contexto claro, requisitos específicos e referências a arquivos produz PRs muito melhores.

---

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="../03-implementacao/GUIDE.md"><strong>Estágio 3 — Implementação</strong></a><br/>
<sub>15:00–16:10 · Java 21 + Spring Boot + Next.js, com testes.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../00-SITEMAP.md"><strong>SITEMAP</strong></a><br/>
<sub>Mapa visual do kit + caminho recomendado por persona.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>

— Paula
