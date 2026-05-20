<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
mode: agent
model: claude-opus-4-6
description: "Escreva um Registro de Decisão de Arquitetura (ADR) capturando contexto, opções, decisão e consequências para uma escolha arquitetural do SIFAP 2.0."
---

# /create-adr

## Objetivo

Você está escrevendo um **Registro de Decisão de Arquitetura** para o SIFAP 2.0 no formato usado em `specs/<NNN>-<feature>/ADRs/`. Um ADR é a resposta durável para "por que fizemos desse jeito?" Ele captura o contexto no momento da decisão, as opções consideradas, o caminho escolhido e as consequências. ADRs são imutáveis depois de aceitos — correções acontecem por meio de um *novo* ADR que substitui o anterior.

## Entradas

Peça ao usuário o que estiver faltando.

- O tópico da decisão em linguagem clara (por exemplo, "Como o SIFAP integrará com o wrapper legado de Adabas?").
- A pasta da feature onde o ADR fica (`specs/<NNN>-<feature>/ADRs/`).
- O próximo número de ADR — olhe os arquivos existentes para evitar colisões.
- Os `REQ-ID`s vinculados que a decisão afeta.
- Stakeholders que devem ser citados (architect, security, DevOps, product).
- Um rascunho da direção escolhida, mesmo que vago.

## Processo

1. **Escolha um título afiado.** Use um título guiado por verbo e com forma de decisão: "Integrar legado Adabas via adaptador REST" — não "Abordagem de integração" ou "Ideias sobre Adabas".
2. **Defina o status corretamente.**
 - `Proposed` — decisão rascunhada, aguardando revisão.
 - `Accepted` — aprovada pelo fórum de arquitetura, com data.
 - `Superseded by NNNN` — substituída por outro ADR.
 - `Rejected` — considerada e rejeitada (ainda registrada — evita rediscussão futura).
3. **Escreva o contexto com honestidade.** Quais forças estão em jogo agora? Restrições (Java 21, Postgres 16, somente Azure, regulatório)? Decisões existentes (ADRs anteriores)? Conhecidos e desconhecidos?
4. **Liste pelo menos três opções.** Inclua o status quo e uma opção "não fazer nada" se aplicável. Cada opção precisa de:
 - Descrição em uma linha.
 - Prós (máximo 3 bullets).
 - Contras (máximo 3 bullets).
 - Perfil de custo / risco em linguagem simples.
5. **Nomeie a decisão e a justificativa.** Um parágrafo para cada. Referencie a opção escolhida pelo nome.
6. **Capture consequências — positivas *e* negativas.** O que se torna possível? O que fica mais difícil? Quais novos riscos aparecem? Quais outras decisões agora ficam forçadas ou restringidas?
7. **Vincule para frente e para trás.** Cite os REQ-IDs, ADRs anteriores na mesma feature e itens inegociáveis de CONSTITUTION.md dos quais essa decisão depende.
8. **Registre a data e os signatários.** Data do fórum de arquitetura. Nomes dos aprovadores (technical lead, software architect, donos de persona afetados).

## Saída

O entregável é um único arquivo em `specs/<NNN>-<feature>/ADRs/<NNNN>-<title-slug>.md`:

```markdown
# ADR <NNNN> — Integrar legado Adabas via adaptador REST

- **Status**: Accepted
- **Data**: 2026-04-29
- **Aprovadores**: @morgan (technical lead), @paula (software architect), @alex (engineering)
- **REQs vinculados**: REQ-INT-001, REQ-INT-002, REQ-INT-005
- **ADRs vinculados**: ADR 0003 (escolheu Java 21), ADR 0006 (Postgres 16 como sistema de registro)
- **Substitui**: —

## 1. Contexto
O SIFAP precisa continuar lendo do banco de dados legado Adabas durante a janela de modernização (estimada em 18 meses). O código legado (programas Natural em `02-cenario-sifap-legado/natural-programs/`) é somente leitura. Pontes ODBC diretas foram descontinuadas pelo fornecedor e são proibidas pela InfoSec para novas integrações. A equipe tem experiência em Java + REST; ninguém na equipe escreve Natural com fluência.

## 2. Opções consideradas

### Opção A — Conexão ODBC direta a partir do Spring Boot
- Prós: Menor latência; modelo mental JDBC familiar.
- Contras: Descontinuada pelo fornecedor; não aprovada pela InfoSec; acopla a aplicação diretamente ao schema legado.
- Custo/risco: Baixo custo agora, alto custo para exceção de InfoSec, risco muito alto em mudança de schema legado.

### Opção B — Sidecar adaptador REST (escolhida)
- Prós: Oculta detalhes legados; pode ser substituído ou aposentado; testável; padrão aprovado pela InfoSec.
- Contras: Salto extra adiciona ~30 ms p95; exige um deployable separado; nova superfície de SLO.
- Custo/risco: Custo médio (um novo serviço); risco médio; substituível.

### Opção C — Replicar Adabas para Postgres via CDC
- Prós: Superfície Postgres unificada para o novo sistema; nenhuma chamada runtime ao legado.
- Contras: Modernização de 18 meses é curta demais para justificar tooling CDC; o legado ainda dirige as escritas.
- Custo/risco: Alto custo; alto risco se o tooling CDC não corresponder à semântica do Adabas.

## 3. Decisão
Vamos construir um sidecar adaptador REST fino que expõe uma API JSON sobre os dados legados do Adabas. O adaptador pertence à squad de integração, é implantado no Azure Container Apps e autenticado via managed identity.

## 4. Justificativa
A Opção B isola o legado do código novo, satisfaz a InfoSec e nos dá um caminho limpo de aposentadoria quando o sistema legado for finalmente desativado. O custo de latência é aceitável para os REQ-IDs afetados (nenhum deles está no orçamento de < 50 ms p95).

## 5. Consequências

### Positivas
- O código novo nunca importa tipos legados.
- O plano de aposentadoria se torna "excluir o adaptador" — limpo.
- O adaptador pode passar por teste de carga e rate limit independentemente.

### Negativas
- Mais um deployable para operar (implicações de REQ-OPS-014).
- Drift de schema entre o contrato do adaptador e os DDMs Adabas precisa ser detectado; nova suíte de testes de contrato obrigatória.
- O orçamento de latência dos endpoints afetados sobe em ~30 ms p95.

### Riscos
- Se o adaptador se tornar um gargalo sob carga de pico, precisaremos escalá-lo horizontalmente — planejar capacidade agora.
- Se a política de InfoSec mudar para proibir sidecars, precisaremos de um novo ADR.

## 6. Validação
- Orçamento de latência verificado pelo cenário k6 `legacy-read-burst`.
- Suíte de testes de contrato `adapter-pact` roda em todo PR.
- Uma retrospectiva está agendada no mês 3 para confirmar se as premissas continuam válidas.
```

## Exemplo trabalhado

**Entrada:** Decisão sobre stack de observabilidade — Application Insights vs Grafana Cloud vs in-house Prometheus.

**Resposta esperada:** um ADR seguindo o template, com três opções, um caminho escolhido, perfil calibrado de custo/risco e consequências nomeando instrumentação de SLO, roteamento de alertas e treinamento de operadores.

## Antipadrões

- ADRs pré-cozidos que apresentam apenas a opção escolhida. Sempre liste opções rejeitadas — metade do valor está aí.
- "Escolhemos X porque é melhor." Não é justificativa; descreva as forças.
- Consequências ausentes. ADRs sem consequências enganam o você do futuro.
- Reescrever um ADR aceito. Crie um novo com status `Supersedes NNNN`.
- ADRs sem datas. Sem valor para arqueologia.
- Não vincular nada. ADRs que não citam REQ-IDs ou ADRs anteriores são isolados e pouco confiáveis.
- "Não fazer nada" nunca considerado. Às vezes a resposta certa é "depois".

## Critérios de sucesso

- [ ] Nome de arquivo segue `<NNNN>-<title-slug>.md`, número não colide.
- [ ] Status é um de `Proposed`, `Accepted`, `Superseded by NNNN`, `Rejected`.
- [ ] Data e aprovadores registrados.
- [ ] Pelo menos três opções, cada uma com prós, contras e custo/risco.
- [ ] Decisão nomeia explicitamente a opção escolhida.
- [ ] Consequências incluem positivas, negativas e riscos.
- [ ] `REQ-ID`s vinculados e ADRs anteriores citados.
- [ ] Critérios de validação incluídos para que possamos checar esta decisão depois.
