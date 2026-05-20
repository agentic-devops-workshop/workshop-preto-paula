<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Runbook

![DOC Runbook](https://img.shields.io/badge/DOC-Runbook-00A4EF?style=for-the-badge) ![DONO DevOps](https://img.shields.io/badge/DONO-DevOps-1A1A1A?style=for-the-badge) ![USE Quando subir/derrubar](https://img.shields.io/badge/USE-Quando%20subir/derrubar-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Docs](README.md) → **Runbook**

> **Para quem é isto?** Para o time durante o workshop e quem opera o ambiente local + CI/CD.
>
> **O que você terá ao final desta leitura:**
>
> 1. Comandos para subir/derrubar ambiente local
> 2. Como ler status do CI
> 3. O que fazer quando algo falha


> O que fazer quando algo roda (ou não roda). O DevOps Engineer é responsável por este arquivo.

## Local — primeira vez

```bash
./11-scripts/setup.sh # verifica ferramentas, clona referência, configura symlinks
docker compose up -d # sobe Postgres + backend + frontend
```

Depois:

- Backend health: <http://localhost:8080/actuator/health>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- Frontend: <http://localhost:3001>

As credenciais padrão estão documentadas em `prototype/README.md`.

## Local — diariamente

```bash
docker compose up -d # se estiver parado
./11-scripts/check.sh # antes do push
git status # nunca commite symlinks (01-arqueologia/legado-sifap/, prototype/, infra/)
```

## CI

Acionado automaticamente em push para `main`, `develop`, `spec/**`, `impl/**`.

| Fluxo de trabalho  | O que faz                                                                  | Quando                            |
| ------------------ | -------------------------------------------------------------------------- | --------------------------------- |
| `ci.yml`           | Backend `mvn verify`, frontend lint+test+typecheck, Terraform fmt+validate | Todo push e PR                    |
| `spec-quality.yml` | markdownlint + rastreabilidade de REQ-ID                                   | Quando `**.md` ou `specs/` mudam |

Verifique execuções com falha na aba Actions. Corrija localmente com `./11-scripts/check.sh`.

## Azure — Estágio 4

O Estágio 4 é quando a equipe aplica Terraform em uma assinatura sandbox fornecida pelas pessoas facilitadoras.

```bash
cd infra
terraform init
terraform plan -var-file=envs/dev/terraform.tfvars
terraform apply -var-file=envs/dev/terraform.tfvars
```

> Cada equipe tem uma cota de assinatura única. Marque todo recurso com `team=team-XX` ou seu apply falhará.

## Problemas comuns

| Sintoma                                   | Causa provável                     | Correção                                             |
| ----------------------------------------- | ---------------------------------- | ---------------------------------------------------- |
| `docker compose up` trava                 | Porta 5432 / 8080 / 3000 já em uso | `lsof -i :5432` e encerre o processo                 |
| `mvn verify` falha em Testcontainers      | Docker não está em execução        | Inicie o Docker Desktop                              |
| `pnpm test` falha em snapshots            | Componente mudou intencionalmente  | `pnpm test -- -u` para atualizar                     |
| `terraform apply` rejeitado               | Tag `team=` ausente                | Adicione a tag ao recurso com falha                  |
| GitHub Actions não consegue acessar Azure | Incompatibilidade na declaração de assunto OIDC | Rode novamente `az ad sp create-for-rbac` por equipe |

## Quando escalar para uma pessoa facilitadora

- Build ainda falhando após 20 minutos de depuração.
- A assinatura Azure parece suspensa.
- Qualquer coisa irreversível (por exemplo, `terraform destroy` executado por engano).

Use o formato de escalonamento de 3 linhas de [00-TEAM-FLOW.md §4](../00-TEAM-FLOW.md).

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="FAQ.md"><strong>FAQ</strong></a><br/>
<sub>Perguntas frequentes.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="troubleshooting.md"><strong>Troubleshooting</strong></a><br/>
<sub>Erros comuns e soluções.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="README.md">Voltar ao Kit PT-BR</a></sub>

