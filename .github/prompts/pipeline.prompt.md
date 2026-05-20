<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
mode: agent
model: claude-sonnet-4-6
description: "Crie um pipeline CI/CD no GitHub Actions para o SIFAP 2.0 com build, testes, gates de segurança e promoção entre ambientes."
---

# /pipeline

## Objetivo

Você é o DevOps engineer criando (ou refatorando) um workflow de **GitHub Actions** para o SIFAP 2.0. O pipeline deve fazer build, testar, escanear e promover artefatos por `develop` → `stage` → `main` (= produção) com gates explícitos. O entregável fica em `.github/workflows/` e referencia workflows reutilizáveis em `.github/workflows/_reusable/` quando forem compartilhados.

## Entradas

Peça ao usuário o que estiver faltando.

- Alvo do pipeline — serviço backend Java, app frontend Next.js, módulo IaC ou orquestração end-to-end.
- Modelo de branches — `spec/*` → `develop` → `stage` → `main` (padrão).
- Ambientes configurados no GitHub (`dev`, `stage`, `prod`) com revisores obrigatórios.
- Container registry — Azure Container Registry (`acr.azurecr.io`).
- Requisitos de compliance — SBOM, imagens assinadas, attestation (`sigstore`).

## Processo

1. **Escolha a superfície de gatilhos.** `pull_request` para build + teste, `push` em branches protegidas para deploy, `workflow_dispatch` para rollback manual. Evite `pull_request_target` a menos que secrets sejam necessários para forks (raro neste projeto).
2. **Use OIDC para autenticação no Azure.** Nunca armazene secrets de service principal. Use `azure/login@v2` com credenciais federadas.
3. **Fixe actions por SHA**, não por tag. (Renovate ou Dependabot podem atualizar.)
4. **Organize os jobs por estágio.**
 - `build` — compilar e executar unit tests (Java: `./mvnw -B verify`; Node: `pnpm install --frozen-lockfile && pnpm build && pnpm test`).
 - `quality` — lint, type-check, license scan, upload de code coverage.
 - `security` — Trivy na imagem de container, OWASP Dependency Check, Gitleaks no diff.
 - `package` — build de container, push para ACR com tags `:sha-<short>` e `:latest`, gerar SBOM (`syft`), assinar com `cosign`.
 - `deploy-dev` — automático em push para `develop`, usa o ambiente GitHub `dev`.
 - `deploy-stage` — automático em push para `stage`, requer uma aprovação.
 - `deploy-prod` — automático em push para `main`, requer duas aprovações e uma referência válida a change ticket.
5. **Use cache com responsabilidade.** Maven: `actions/cache@<sha>` com chave baseada no hash de `pom.xml`. Node: `pnpm/action-setup@<sha>` com cache de store embutido. Cache de camadas do Buildx para builds de container.
6. **Defina timeouts e concorrência.** `timeout-minutes: 30` por job, `concurrency: { group: ${{ github.workflow }}-${{ github.ref }}, cancel-in-progress: true }`.
7. **Aplique gates por regras de branch protection.** Checks obrigatórios: `build`, `quality`, `security`. Stage e prod exigem revisão de deployment.
8. **Emita rastreabilidade.** Taggeie a imagem implantada com o SHA do merge commit e os `REQ-ID`s relacionados da descrição do PR; exponha-os na descrição do deployment no GitHub.

## Saída

Sua resposta final deve incluir:

- **Caminho e nome do arquivo de workflow** — por exemplo `.github/workflows/backend-payments.yml`.
- **YAML completo** — pronto para colar, com comentários explicando escolhas não óbvias.
- **Secrets e variáveis obrigatórios do GitHub** — listados com seu propósito.
- **Configurações de branch protection** — checks obrigatórios e regras de revisores.
- **Diagrama de promoção** — texto curto ou sequência Mermaid do PR até prod.

### Esqueleto (backend Java)

```yaml
name: backend-payments
on:
 pull_request:
 paths: ['04-prototipo-sifap-moderno/backend/**']
 push:
 branches: [develop, stage, main]
 paths: ['04-prototipo-sifap-moderno/backend/**']

permissions:
 id-token: write
 contents: read
 packages: write

concurrency:
 group: ${{ github.workflow }}-${{ github.ref }}
 cancel-in-progress: true

jobs:
 build:
 runs-on: ubuntu-latest
 timeout-minutes: 20
 steps:
 - uses: actions/checkout@<sha>
 - uses: actions/setup-java@<sha>
 with: { java-version: '21', distribution: 'temurin', cache: 'maven' }
 - run: ./mvnw -B verify
 # ... uploads de coverage, licença e relatório de testes
 # ... quality, security, package, deploy-* jobs
```

## Exemplo trabalhado

**Entrada:** novo pipeline para o serviço backend `payments`, implantando em Azure Container Apps via ACR.

**Esqueleto esperado da resposta:**

> Workflow `backend-payments.yml` com 7 jobs (`build`, `quality`, `security`, `package`, `deploy-dev`, `deploy-stage`, `deploy-prod`).
>
> OIDC federado para o app do Entra ID `sp-sifap-cicd`, com escopo na subscription `sub-sifap-prod`.
>
> Secrets obrigatórios: `AZURE_TENANT_ID`, `AZURE_SUBSCRIPTION_ID`, `AZURE_CLIENT_ID`, `ACR_NAME`. Sem senhas.
>
> Branch protection: `build`, `quality`, `security` obrigatórios em todos os PRs para `develop`. Stage exige um revisor. Prod exige dois revisores do time `release-managers` e um change ticket vinculado no corpo do PR.
>
> Promoção: PR → develop (auto-deploy `dev`) → cherry-pick para `stage` (1 aprovador) → cherry-pick para `main` (2 aprovadores).

## Antipadrões

- Armazenar secrets do Azure diretamente em GitHub Secrets quando OIDC funciona. OIDC é o padrão.
- Fixar em `@v3` em vez de um SHA. Tags podem ser movidas.
- Um único mega-job que faz build + teste + deploy. Difícil de depurar, difícil de tentar novamente.
- `pull_request_target` sem restringir paths. Risco de segurança em forks.
- Cache entre branches sem invalidação. Builds obsoletos.
- Deploy direto para prod sem gate de aprovação. Cedo ou tarde, um PR ruim chega à produção.
- Pular assinatura de imagem ou SBOM. Obrigatório para workloads regulados.
- Hard-code do nome do registry no YAML. Use uma variável de repositório ou ambiente.

## Critérios de sucesso

- [ ] Autenticação OIDC; nenhum secret do Azure armazenado no GitHub.
- [ ] Cada action fixada a um commit SHA com comentário nomeando a versão.
- [ ] `build`, `quality` e `security` são checks obrigatórios nos PRs.
- [ ] A imagem recebe tag `sha-<short>` e é assinada com cosign.
- [ ] Deployments de stage e prod exigem aprovações conforme descrito.
- [ ] O concurrency group impede dois deploys para o mesmo env ao mesmo tempo.
- [ ] `timeout-minutes` definido em todos os jobs.
- [ ] Requisito de descrição do PR aplicado para deploys em prod (`REQ-ID`s vinculados e change ticket).
