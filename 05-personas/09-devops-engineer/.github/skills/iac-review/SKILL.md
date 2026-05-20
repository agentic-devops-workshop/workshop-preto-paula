<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
name: Revisão de IaC
description: "Use ao revisar Terraform, Bicep ou CloudFormation, verificar drift ou fortalecer código de infraestrutura. Aciona com 'review terraform', 'review bicep', 'IaC review', 'drift detection', 'state file'."
---

# Revisão de IaC

## Quando invocar
- "Revise este módulo Terraform."
- "Por que nosso plan está mostrando drift?"
- "Este Bicep está pronto para produção?"

## Checklist de revisão
### Estrutura
- [ ] Modules são **composable** e têm uma única responsabilidade (um módulo = uma stack lógica, não um resource).
- [ ] **Sem valores hardcoded** — tudo parametrizado com defaults sensatos.
- [ ] **Entradas documentadas** (`description`, `type`, regras de `validation`); saídas também.
- [ ] **README** na raiz do módulo com exemplo de uso.

### State e backends
- [ ] **Remote state** com locking (S3+DynamoDB, Azure Storage com blob lease, GCS).
- [ ] State **nunca é commitado** no git; `.gitignore` cobre `*.tfstate*`.
- [ ] State separado por ambiente; sem acoplamento implícito entre envs.
- [ ] Acesso ao state controlado por IAM, não por credenciais compartilhadas.

### Segurança
- [ ] Sem secrets no código ou em defaults de variables — use Key Vault / Secrets Manager / SOPS.
- [ ] IAM com least privilege; sem `*:*` ou `Resource: "*"` salvo com justificativa.
- [ ] Criptografia em repouso e em trânsito habilitada em todos os data stores.
- [ ] Acesso público explicitamente negado salvo quando intencional; registre isso no README do módulo nesse caso.
- [ ] `tfsec` / `checkov` / `PSRule` limpos, ou exceções documentadas.

### Segurança de mudanças
- [ ] `terraform plan` incluído nos PRs como comentário (Atlantis / tfcmt / GH Actions).
- [ ] `prevent_destroy` em resources com state (databases, KV, storage accounts).
- [ ] Versões de provider **fixadas** (`~>` com major e minor explícitos).
- [ ] Versões de module fixadas.
- [ ] Diffs destrutivos exigem um segundo aprovador.

### Drift
- [ ] Detecção de drift agendada (`terraform plan -detailed-exitcode` diário, ou Driftctl).
- [ ] Drift cria um ticket automaticamente; nunca fica silencioso.
- [ ] Sem mudanças manuais no console sem codificação posterior.

## Achados comuns
- **`count` usado para listas que reordenam** → use `for_each` com chaves estáveis.
- **`depends_on` por toda parte** → geralmente sinal de deps implícitas ausentes; remova salvo quando realmente necessário.
- **Data sources usados para valores disponíveis em plan time** → chamadas de API desnecessárias, CI instável.
- **Diferenças de ambiente por interpolação de string de `terraform.workspace`** → frágil; use tfvars ou stacks separadas.

## Referências
- [Terraform Style Guide](https://developer.hashicorp.com/terraform/language/style)
- [Azure Verified Modules](https://azure.github.io/Azure-Verified-Modules/)
- [tfsec](https://aquasecurity.github.io/tfsec/), [checkov](https://www.checkov.io/)
