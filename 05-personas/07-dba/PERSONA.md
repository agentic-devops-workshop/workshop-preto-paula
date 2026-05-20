<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Persona — DBA

> 🗺 **Você está aqui:** [Kit PT-BR](../../README.md) → [Personas](../OVERVIEW.md) → [Dba](README.md) → **PERSONA**


> **Para quem é isto?** Para a pessoa que vai vestir a persona **DBA** no workshop. Foco: schema, Flyway, queries, migrações.
>
> **O que você terá ao final desta leitura:**
>
> 1. Saberá em qual par está e qual fase do SDLC lidera
> 2. Conhecerá a missão da persona no Dia 2
> 3. Verá em qual estágio você lidera, apoia ou observa
> 4. Terá 3 prompts de Copilot prontos para usar
> 5. Saberá o default se travar ("se não souber o que fazer, faça X")

![Par 4 · Qualidade](https://img.shields.io/badge/PAR-Par%204%20%E2%80%A2%20Qualidade-00A4EF?style=for-the-badge) ![Lidera estágio 3](https://img.shields.io/badge/LIDERA%20EST%C3%81GIO-3-1A1A1A?style=for-the-badge) ![Apoia estágio —](https://img.shields.io/badge/APOIA-—-737373?style=for-the-badge)

## Onde você atua no SDLC

![Linha do tempo do dia mostrando onde esta persona atua](../../assets/timeline-stages.svg)

- **Par**: 4 · Qualidade (junto com QA Engineer)
- **Fases lideradas**: Implementação (S3) — schema + migrações
- **Recebe de**: Software Architect (bounded contexts) e Estágio 1 (4 DDMs)
- **Faz passagem para**: Developer (modelo pronto) e DevOps (provisioning do PostgreSQL)

## Quem é essa pessoa

Dono dos dados. No SIFAP legado isso significa entender os 4 DDMs Adabas com MU e PE, com desnormalização pragmática, com índices ancestrais. No SIFAP 2.0 significa desenhar um schema PostgreSQL 16 que preserva a integridade lógica do negócio sem herdar as cicatrizes do Adabas.

## Missão no workshop

Traduzir o modelo Adabas para um schema relacional que funciona. Garantir migrações idempotentes (Flyway). Desenhar índices e particionamento para o ciclo mensal caber em 2 horas. Proteger a rastreabilidade (audit store).

## Seu papel no framework Agentic Legacy Modernization

- **Agentes relevantes**: Analysis Agent (S1), Translation Agent (S3)
- **Fase do framework**: Assessment → Translation (camada de dados)
- **Seu papel**: traduzir DDMs Adabas → schema PostgreSQL preservando integridade

## Onde você aparece em cada estágio

| Estágio                | Você faz isso                                                                                       | Entregável que depende de você |
| ---------------------- | --------------------------------------------------------------------------------------------------- | ------------------------------ |
| 1. Arqueologia         | Lê os 4 DDMs. Mapeia MU/PE para entidades relacionais candidatas. Identifica campos-chave.          | Mapa DDM → entidade relacional |
| 2. Spec Moderna        | Desenha o modelo lógico de dados. Escreve o ADR de PostgreSQL (ADR 2 da referência).                | Modelo de dados + ADR 002      |
| 3. Implementação       | Escreve migrações Flyway. Define índices. Popula dados de teste. Responde dúvidas de JPA/Hibernate. | Schema PostgreSQL + seed       |
| 4. Evolução com Agent  | Revisa se o PR do Agent toca no schema com segurança (nova migração, não alteração retroativa).     | Integridade do schema          |

## Ferramentas e primitivas

- **Copilot Chat** para traduzir DDM Adabas → SQL PostgreSQL.
- **Copilot Plan** para planejar migrações em lote.
- **PostgreSQL MCP** (se disponível no devcontainer) para introspecção e queries.
- **GitHub Spec-Kit** — `/speckit.plan` consome seu modelo de dados e contratos.

## Cheat-sheets que você usa

- [`../09-cheat-sheets/spec-kit-workflow.md`](../../09-cheat-sheets/spec-kit-workflow.md) — como declarar modelo de dados para `/speckit.plan` e revisar com `/speckit.analyze`.
- [`../09-cheat-sheets/model-routing.md`](../../09-cheat-sheets/model-routing.md) — Sonnet 4.6 é suficiente para a maior parte do seu trabalho.

## Como você se sai bem

- Todas as migrações são reversíveis ou substituídas por nova migração em vez de alteradas.
- Você descobre (e documenta) quais MUs do Adabas precisam virar tabela relacionada, não coluna `JSONB`.
- Seus índices cobrem as queries críticas do ciclo mensal.
- A audit store é verdadeiramente append-only — sem DELETE em lugar nenhum.

## Como você se perde

- Desnormaliza por hábito de Adabas.
- Esquece de indexar e a query do ciclo fica lenta.
- Usa `JSONB` para tudo porque "é flexível".
- Deixa migração não-idempotente e o devcontainer de um colega quebra.

## Se você pegou duas personas

- **DBA + Developer** é comum; você escreve suas migrações e algumas queries.
- **DBA + DevOps Engineer** se o time tem perfil mais ops — você cuida do PostgreSQL e do Terraform que o provisiona.

## 3 prompts de exemplo

1. **(Chat)** _"Leia o DDM BENEFICIARIO.ddm do Adabas e traduza para schema PostgreSQL 16. O grupo PE (periódico) de dependentes deve virar tabela separada ou JSONB? Justifique."_
2. **(Plan)** _"Planeje uma migração Flyway V5\_\_add_indexes.sql com índices para: busca por CPF, listagem de pagamentos por ciclo+status, auditoria por tipo+data."_
3. **(Chat)** _"Revise este schema e identifique: campos sem NOT NULL que deveriam ter, foreign keys faltantes e índices para o ciclo mensal de 3,8M pagamentos."_

## Se travar (defaults de emergência)

- Não conhece o formato DDM? Abra [`../01-arqueologia/legado-sifap/adabas-ddms/BENEFICIARIO.ddm`](../../01-arqueologia/legado-sifap/adabas-ddms/BENEFICIARIO.ddm) — tem comentários explicando cada campo.
- Migração quebrou? NUNCA edite uma migração existente. Crie nova: `V5__fix_xxx.sql`.
- Qual índice criar? Regra: "Se aparece em WHERE ou JOIN e a tabela tem >100K linhas, crie índice."
- PostgreSQL offline? Verifique se o Docker está rodando: `docker ps | grep postgres`.

## Dependências — Quem depende de você

| Persona            | Relação           | Artefato                             |
| ------------------ | ----------------- | ------------------------------------ |
| Software Architect | VOCÊ depende dele | Fronteiras de contexto para o modelo |
| Developer          | Depende de VOCÊ   | Migrações prontas para código JPA    |
| DevOps Engineer    | Depende de VOCÊ   | Schema estável para Terraform        |
| QA Engineer        | Depende de VOCÊ   | Dados de teste (seed)                |

## Como você é avaliado

- Rubrica A3 (Integridade Técnica): migrações idempotentes, schema consistente com entidades JPA
- Rubrica A1 (Arqueologia): mapa DDM → entidade relacional documentado
- Critério: "Audit store é append-only. Nenhum DELETE no schema de auditoria."
---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="../06-developer/PERSONA.md"><strong>Developer</strong></a><br/>
<sub>Par 3 · Implementação · Java + Next.js + testes.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../08-qa-engineer/PERSONA.md"><strong>QA Engineer</strong></a><br/>
<sub>Par 4 · Qualidade · testes BDD e cobertura.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../../README.md">Voltar ao Kit PT-BR</a></sub>

— Paula
