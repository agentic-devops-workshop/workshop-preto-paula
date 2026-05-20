<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
mode: agent
model: claude-sonnet-4-6
description: "Crie ou refatore um único módulo Terraform para a infraestrutura Azure do SIFAP 2.0 com tags, variáveis, saídas e validação."
---

# /iac-module

## Objetivo

Você é o DevOps engineer produzindo ou atualizando um **único módulo Terraform** em `05-terraform-azure/modules/` para o SIFAP 2.0. Os módulos são delimitados a uma área de serviço do Azure (rede, computação, banco de dados, monitoramento, segurança). O entregável passa sem erros em `terraform fmt`, `terraform validate`, `tflint` e `checkov`, e vem com um exemplo.

## Entradas

Peça ao usuário o que estiver faltando.

- O nome do módulo e o serviço Azure (por exemplo, `postgres` para `azurerm_postgresql_flexible_server`).
- O `REQ-ID` vinculado em `SPECIFICATION.md` (normalmente um requisito não funcional ou operacional).
- Os ambientes-alvo (`dev`, `stage`, `prod`) e quaisquer sobrescritas específicas por ambiente.
- A linha de base existente: estamos criando um módulo novo ou modificando `05-terraform-azure/modules/<name>/`?

## Processo

1. **Leia a constituição e os módulos existentes.** Abra `specs/<NNN>-<feature>/CONSTITUTION.md` para ver os itens não negociáveis (managed identity, Key Vault para secrets, sem IPs públicos etc.) e os módulos existentes para copiar padrões.
2. **Escolha uma versão de provider com suporte da Microsoft.** Use `azurerm ~> 4.x` e fixe via `required_providers`. Referencie [Azure Verified Modules](https://aka.ms/avm) quando aplicável.
3. **Escreva o esqueleto do módulo.** No mínimo cinco arquivos:
 - `main.tf` — apenas resources, sem bloco `provider`.
 - `variables.tf` — cada entrada tipada e documentada, com blocos `validation` onde intervalos importarem.
 - `outputs.tf` — IDs, nomes e FQDNs de que os chamadores precisam, nunca secrets.
 - `versions.tf` — `terraform` e `required_providers`.
 - `README.md` — propósito, entradas, saídas e exemplo de uso.
4. **Aplique as tags padrão do SIFAP** a todo recurso taggable:
 ```hcl
 tags = merge(var.tags, {
 project = "sifap"
 environment = var.environment
 owner = var.owner
 module = "<name>"
 managed_by = "terraform"
 })
 ```
5. **Disciplina de secrets.** Nada de secrets em variables, nada de secrets em outputs, nada de secrets em state files quando pudermos evitar. Connection strings vêm de data sources `azurerm_key_vault_secret` ou são retornadas como URIs que chamadores podem resolver em runtime.
6. **Disciplina de identidade.** Use managed identities `system-assigned` ou `user-assigned`; nunca credenciais de service principal no código.
7. **Disciplina de rede.** Nada de `public_network_access_enabled = true` sem uma exceção em `CONSTITUTION.md`. Private endpoints por padrão.
8. **Adicione uma pasta `examples/`.** Um `examples/basic/main.tf` mínimo que consome o módulo — usado por `tflint` e pelos revisores.
9. **Valide localmente.** Rode:
 ```
 terraform fmt -check -recursive
 terraform init -backend=false
 terraform validate
 tflint --recursive
 checkov -d . --soft-fail false
 ```
 Todos devem passar.

## Saída

Sua resposta final deve incluir:

- **Resumo do módulo** — nome, propósito, REQ-ID.
- **Todos os cinco arquivos** com conteúdo completo.
- **Consumidor de exemplo** — `examples/basic/main.tf`.
- **Relatório de validação** — cole a saída de `fmt`, `validate`, `tflint`, `checkov`.
- **Nota de custo** — link para [preços do Azure](https://azure.microsoft.com/pricing/) da(s) SKU(s) escolhida(s) e uma estimativa mensal em uma linha por ambiente.

## Exemplo trabalhado

**Entrada:** novo módulo `postgres` encapsulando `azurerm_postgresql_flexible_server`, dev/stage/prod, REQ-OPS-014 ("o sistema deve persistir registros de pagamento em PostgreSQL 16 com point-in-time recovery").

**Esqueleto esperado da resposta:**

> Módulo `postgres` — Azure PostgreSQL Flexible Server para o SIFAP. Sustentado por REQ-OPS-014.
>
> `main.tf`: resource com `version = "16"`, `geo_redundant_backup_enabled = var.environment == "prod"`, private endpoint, chave gerenciada pelo cliente e identity system-assigned.
>
> `variables.tf`: `name`, `environment`, `sku_name`, `storage_mb`, `subnet_id`, `key_vault_key_id`, `tags` — cada uma com descrição, tipo e validação (`storage_mb >= 32768`).
>
> `outputs.tf`: `id`, `fqdn`, `principal_id` (para RBAC), sem senhas.
>
> Exemplo: uma SKU Burstable de 4 GB em `dev`, zona única.
>
> Validação: fmt limpo, validate limpo, 0 achados do tflint, 0 achados críticos do checkov, 1 informativo (padrão de retenção longa de snapshot — aceito).

## Antipadrões

- IDs de subscription, nomes de resource group ou região hard-coded. Passe-os como variables.
- Blocos `provider` dentro de modules. Providers pertencem aos root modules.
- `count = var.create ? 1 : 0` para tornar recursos opcionais. Use módulos separados ou flags de feature.
- Outputs que vazam secrets (`administrator_password`, connection strings).
- `public_network_access_enabled = true` sem uma exceção explícita em `CONSTITUTION.md`.
- Taggear alguns resources e outros não. Taggeie tudo que for taggable.
- Pular blocos `validation` em inputs numéricos — isso produz erros crípticos do Azure no momento do apply.
- Misturar atualização de módulo com mudança de feature no mesmo PR. Mudanças de módulo são entregues sozinhas.

## Critérios de sucesso

- [ ] `terraform fmt -check`, `terraform validate`, `tflint` e `checkov` passam.
- [ ] Todos os resources taggable carregam o conjunto padrão de tags.
- [ ] Nenhum secret em variables, outputs ou default values.
- [ ] Acesso de rede pública desabilitado, a menos que uma exceção em `CONSTITUTION.md` seja referenciada.
- [ ] Managed identity usada; nenhuma credencial de SP no código.
- [ ] Um consumidor em `examples/basic/` compila e valida.
- [ ] O README documenta entradas, saídas e um exemplo.
- [ ] O `REQ-ID` vinculado é citado no README do módulo.
