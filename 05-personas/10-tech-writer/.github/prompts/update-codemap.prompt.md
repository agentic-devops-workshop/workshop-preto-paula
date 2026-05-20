<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
mode: agent
model: claude-sonnet-4-6
description: "Gerar ou atualizar CODEMAP.md — um índice navegável da base de código do SIFAP 2.0 mostrando módulos, proprietários e pontos de entrada."
---

# /update-codemap

## Objetivo

Você produz ou atualiza `docs/CODEMAP.md`, um guia de navegação de uma página que uma nova pessoa do time consegue ler em 10 minutos e usar para encontrar qualquer módulo, seu proprietário, seus pontos de entrada e seus testes. O codemap **não** é documentação gerada automaticamente; ele é curado. Ele complementa `DESIGN.md` (arquitetura) e a spec (requisitos).

## Entradas

Peça à pessoa usuária o que estiver faltando.

- O caminho raiz do repositório (este workspace).
- Se deve atualizar no lugar (`update`) ou reconstruir (`rebuild`).
- A tabela de proprietários das personas, normalmente definida em `pt-br/05-personas/`.
- Uma versão anterior de `CODEMAP.md`, se existir.

## Processo

1. **Liste as pastas de serviço de nível superior.** Serviços backend em `04-prototipo-sifap-moderno/backend/src/main/java/br/gov/sifap/<service>/`, rotas frontend em `04-prototipo-sifap-moderno/frontend/app/<route>/`, infra em `05-terraform-azure/modules/<name>/`.
2. **Para cada módulo, capture cinco fatos.**
 - Propósito (uma frase).
 - Pontos de entrada públicos (endpoints REST, rotas de página, comandos CLI, entradas de IaC).
 - Estado persistente (tabelas, filas, blob containers).
 - Faixas de `REQ-ID` vinculadas (por exemplo, `REQ-PAY-001..024`).
 - Persona proprietária (`alex`, `sam`, `jordan`, `morgan`, `casey`).
3. **Encontre testes.** Para cada módulo, encontre o diretório de testes correspondente e crie um link para ele.
4. **Encontre o mapeamento legado.** Quando um módulo corresponder a um programa Natural de `02-cenario-sifap-legado/natural-programs/`, nomeie o programa (`CALCBENF.NSN` etc.). Isso explicita a linhagem da modernização.
5. **Encontre dependências não óbvias.** Imports entre módulos, bibliotecas compartilhadas (`commons-*`) e serviços externos do Azure. Destaque qualquer módulo que dependa de mais de três outros, pois isso é um cheiro de design.
6. **Ordene módulos por valor visível à pessoa usuária.** Caminhos críticos de uso primeiro (cadastro, desembolso, auditoria), módulos de suporte depois, infra por último.
7. **Renderize como um único arquivo markdown navegável.** Mantenha abaixo de 200 linhas. Se passar disso, divida sub-codemaps por área de serviço e crie links para eles.

## Saída

O entregável é `docs/CODEMAP.md` (ou subarquivos), com esta estrutura:

```markdown
# Mapa do Código do SIFAP 2.0

> Última atualização: <YYYY-MM-DD>. Donos: veja `pt-br/05-personas/`.

## 1. Guia de leitura
- Caminhos críticos: pagamentos, beneficiários, auditoria.
- Veja `DESIGN.md` para o racional arquitetural; veja `SPECIFICATION.md` para requisitos.

## 2. Serviços de backend

### payments — orquestração de desembolsos
- **Propósito**: emitir, tentar novamente e reconciliar desembolsos de beneficiários.
- **Path**: `04-prototipo-sifap-moderno/backend/src/main/java/br/gov/sifap/payments/`
- **Testes**: `04-prototipo-sifap-moderno/backend/src/test/java/br/gov/sifap/payments/`
- **Pontos de entrada**: `POST /api/v1/payments`, `GET /api/v1/payments/{id}`, `POST /api/v1/payments/{id}/retry`
- **Estado**: tabelas Postgres `payment`, `payment_attempt`, `disbursement_lock`. Fila Service Bus `payments-out`.
- **REQ-IDs**: REQ-PAY-001..024
- **Dono**: @alex
- **Linhagem legada**: `CALCBENF.NSN`, `CALCDSCT.NSN`, `BATCHPGT.NSN`
- **Dependências entre módulos**: `beneficiaries` (leitura), `audit` (escrita).

### beneficiaries — cadastro e ciclo de vida
- ...

## 3. Rotas de frontend

### /beneficiaries — listagem e detalhe
- **Path**: `04-prototipo-sifap-moderno/frontend/app/beneficiaries/`
- **Testes**: `04-prototipo-sifap-moderno/frontend/app/beneficiaries/__tests__/`
- **REQ-IDs**: REQ-UI-007..014
- **Dono**: @sam
- **Consome API de**: `payments`, `beneficiaries`
- ...

## 4. Módulos de infraestrutura

### postgres
- **Path**: `05-terraform-azure/modules/postgres/`
- **REQ-IDs**: REQ-OPS-014..018
- **Dono**: @jordan
- ...

## 5. Bibliotecas transversais
- `br.gov.sifap.shared.audit` — usada por pagamentos, beneficiários e programas.
- `br.gov.sifap.shared.money` — wrappers `BigDecimal` para cálculos de ICMS.

## 6. Pontos de atenção observados
- `beneficiaries` importa de 4 outros módulos — revisar para uma fronteira de responsabilidade mais estreita.

## 7. Como atualizar este arquivo
Rode `/update-codemap` após adicionar ou renomear qualquer módulo. Não gere automaticamente; faça curadoria.
```

## Exemplo trabalhado

**Entrada:** atualizar depois de adicionar o novo serviço `audit` e a migração Postgres `audit-log`.

**Esqueleto de resposta esperado:** a estrutura acima com a nova seção de `audit`, as dependências entre módulos de `payments` atualizadas e uma nova linha em "Pontos de atenção observados", se apropriado.

## Antipadrões

- Gerar a partir de `find . -type d`: isso é uma listagem de diretórios, não um mapa.
- Incluir todos os arquivos. O codemap nomeia módulos, não linhas.
- Listar endpoints como `*`. Seja específico.
- Esquecer a coluna de linhagem legada para SIFAP. Modernização sem linhagem fica invisível.
- Usar times como proprietários. A pessoa de plantão é a proprietária.
- Pular a seção "Smells observados". O codemap também é uma verificação de saúde.
- Deixar o arquivo sofrer drift por mais de 30 dias. Codemap desatualizado é pior que nenhum codemap.

## Critérios de sucesso

- [ ] Todo serviço backend, rota frontend e módulo IaC está listado.
- [ ] Cada entrada tem Finalidade, Path, Testes, Pontos de entrada, Estado, REQ-IDs, Owner.
- [ ] A linhagem legada está nomeada para qualquer módulo que mapeie para um programa Natural.
- [ ] Dependências entre módulos estão declaradas; módulos com > 3 dependências estão sinalizados.
- [ ] O arquivo permanece abaixo de 200 linhas (ou é dividido com subarquivos vinculados).
- [ ] A data de última atualização está definida como hoje.
- [ ] Os nomes das personas proprietárias correspondem a `pt-br/05-personas/`.
