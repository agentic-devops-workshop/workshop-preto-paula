<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
name: Pipeline Hardening
description: "Use ao fortalecer um pipeline CI/CD, migrar para OIDC, assinar artefatos ou atender requisitos SLSA. Aciona com 'SLSA', 'supply chain', 'OIDC', 'sigstore', 'cosign', 'pipeline security', 'GHA hardening'."
---

# Fortalecimento de Pipeline

## Quando invocar
- "Fortaleça nosso pipeline GitHub Actions / Azure DevOps / GitLab."
- "Migre de secrets de longa duração para OIDC."
- "Alcance SLSA Level 2/3."
- "Assine nossas imagens de container."

## Modelo de ameaças (lista curta)
1. **Secrets roubados** de logs do pipeline ou runner comprometido.
2. **Dependência maliciosa** publicada upstream ou typosquatted.
3. **GitHub Action de terceiro / etapa compartilhada comprometida**.
4. **Artefato adulterado** entre build e deploy.
5. **Escalação de privilégio** por permissões amplas demais no pipeline.

## Controles (ordenados por ROI)
### Tier 1 — faça primeiro
- [ ] **OIDC para a cloud** — sem credenciais cloud de longa duração armazenadas como secrets. Identidade federada com tokens de curta duração.
- [ ] **Fixe actions de terceiros por SHA**, não por tag (`actions/checkout@<sha>` com um comentário mostrando a versão).
- [ ] **Bloco `permissions:`** em todo workflow, default `contents: read`, elevando apenas onde necessário.
- [ ] **Branch protection**: revisões obrigatórias, status checks obrigatórios, sem force-push, commits assinados na main.
- [ ] **Secret scanning + push protection** habilitados em toda a org.
- [ ] **Dependabot / Renovate** para dependências e actions.

### Tier 2 — integridade de supply chain
- [ ] **SBOM** gerado em todo build (Syft / CycloneDX).
- [ ] **Assinatura de artefatos** com Cosign (keyless via OIDC preferido).
- [ ] **Provenance** (SLSA v1.0 attestation) publicada com o artefato.
- [ ] **Verificar assinaturas no deploy** — o job de deploy recusa artefatos não assinados.
- [ ] **Varredura de vulnerabilidades** (Trivy / Grype) na imagem; falha em Critical/High com exceções justificadas.

### Tier 3 — maduro
- [ ] **Builds herméticos / reproduzíveis** quando viável.
- [ ] **Revisão por duas pessoas** para pipelines de release.
- [ ] **Runner hardening**: efêmero, network-egress restrito, sem state mutável compartilhado.

## Antipadrões
- Armazenar `AWS_ACCESS_KEY_ID` / `AZURE_CLIENT_SECRET` como secret de repositório quando OIDC está disponível.
- `permissions: write-all`.
- Tags flutuantes `@main` ou `@v3` em actions de terceiros.
- Implantar um artefato construído em outro pipeline sem verificar sua assinatura.
- Secrets impressos em logs por expansão de shell sem aspas.

## Referências
- [SLSA v1.0](https://slsa.dev/spec/v1.0/)
- [GitHub - Security hardening for GHA](https://docs.github.com/en/actions/security-guides/security-hardening-for-github-actions)
- [Sigstore / Cosign](https://docs.sigstore.dev/)
- [OpenSSF Scorecard](https://scorecard.dev/)
