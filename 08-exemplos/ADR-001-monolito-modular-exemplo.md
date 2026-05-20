<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ADR-001 — Adotar Monolito Modular em vez de Microsserviços

![EXEMPLO Pronto](https://img.shields.io/badge/EXEMPLO-Pronto-7FBA00?style=for-the-badge) ![USE Como referência](https://img.shields.io/badge/USE-Como%20referência-1A1A1A?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Exemplos](README.md) → **ADR-001-monolito-modular-exemplo**


> **Para quem é isto?** Para o EA/SA no Estágio 2 — referência de ADR profissional com alternativas rejeitadas.
>
> **O que você terá ao final desta leitura:**
>
> 1. Verá ADR com Status + Contexto + Decisão + Alternativas + Consequências
> 2. Entenderá por que alternativas A, B, C foram rejeitadas
> 3. Saberá registrar critérios de envelhecimento ("quando revisitar")
> 4. Reproduzirá o mesmo nível para seus ADRs

![EXEMPLO](https://img.shields.io/badge/EXEMPLO-ADR-001-00A4EF?style=for-the-badge) ![ESTÁGIO](https://img.shields.io/badge/ESTÁGIO-02%20·%20Spec%20Moderna-1A1A1A?style=for-the-badge) ![REFERÊNCIA](https://img.shields.io/badge/REFERÊNCIA-Monolito%20modular%20vs%20microservi%C3%A7os-737373?style=for-the-badge)

> **⚠️ Este é um EXEMPLO** para você ver como uma ADR fica pronta. Seu time vai escrever ADRs reais no Estágio 2 (mínimo 3) sobre as decisões que o time toma.

## Status

✅ **Aceito** · 19/05/2026 · Decisão sob responsabilidade do par 2 (EA + SA) com revisão do PO.

## Contexto

Estamos modernizando o SIFAP, um sistema de 29 anos com **4 domínios bem definidos** (beneficiário, pagamento, auditoria, administração) e ~4.2 milhões de beneficiários. Temos **8 horas** para entregar protótipo funcional + deploy via IaC.

Restrições conhecidas:

- **Time pequeno** (5 pessoas) sem experiência prévia com infraestrutura de microsserviços.
- **Janela curta** (1 dia) — não cabe estabelecer service mesh, observabilidade distribuída e contratos versionados.
- **Domínios fortemente acoplados pela auditoria** — quase toda mudança de entidade gera evento de auditoria. Comunicação cross-service via mensageria adicionaria latência sem benefício.
- **Operação ainda é on-prem para muitos clientes** — primeira versão Azure terá tráfego limitado nos primeiros 12 meses.

A decisão precisa equilibrar **clareza arquitetural** (4 bounded contexts visíveis) com **simplicidade de operação** (1 deployable).

## Decisão

Adotaremos **monolito modular** em Java 21 + Spring Boot 3.3, com a seguinte estrutura:

```
src/main/java/br/gov/client/sifap/
├── beneficiary/   ← módulo
│   ├── domain/         (puro, sem Spring)
│   ├── application/    (services)
│   └── infrastructure/ (controllers, repos)
├── payment/       ← módulo
├── audit/         ← módulo
└── admin/         ← módulo
```

Regras de fronteira:

1. **Nenhum módulo importa classes de `infrastructure` de outro módulo.**
2. Comunicação inter-módulo só via interfaces declaradas em `domain/`.
3. ArchUnit no CI verifica as fronteiras a cada PR.
4. **Banco compartilhado** com schemas separados por módulo (`beneficiary`, `payment`, `audit`, `admin`). Cada módulo só faz JOIN dentro do próprio schema.

A migração para microsserviços, se necessária no futuro, será **módulo por módulo**, extraindo `audit` primeiro (menor acoplamento de leitura).

## Alternativas consideradas (e rejeitadas)

### Alt. A · Microsserviços desde o dia 1

- ✅ Escalabilidade horizontal por domínio
- ✅ Deploy independente
- ❌ **Não cabe em 8h.** Service mesh, contratos OpenAPI versionados, observabilidade distribuída e CI/CD por serviço sozinhos consomem o dia.
- ❌ Time não tem skill operacional para 4 serviços em produção.
- ❌ Latência de chamadas síncronas entre audit/payment quebraria a SLA de p95 < 200ms.

**Rejeitado por incompatibilidade temporal e operacional.**

### Alt. B · Monolito tradicional (sem módulos)

- ✅ Mais simples de implementar
- ❌ **Perde clareza de domínio.** Os 4 bounded contexts identificados no Estágio 2 se misturariam em pacotes por camada (`controllers/`, `services/`, `repositories/`) — exatamente o anti-padrão que o legado tem.
- ❌ Refatoração futura para microsserviços ficaria muito custosa (sem fronteiras claras).

**Rejeitado por perda de rastreabilidade dos bounded contexts.**

### Alt. C · Serverless (Azure Functions)

- ✅ Custo baixo com tráfego intermitente
- ❌ **Cold-start incompatível** com janela batch noturna (geração de ciclo). 4M+ beneficiários processados em Function exigiria orchestrator complexo.
- ❌ Time não tem experiência. Adicionar complexidade nova em workshop curto = risco.

**Rejeitado por inadequação ao padrão batch.**

## Consequências

### Positivas

- **1 deployable** = 1 pipeline CI/CD, 1 imagem Docker, 1 ambiente para subir
- **Latência inter-módulo zero** (chamadas in-process)
- **Refatoração possível mais tarde** se um módulo precisar escalar separado
- ArchUnit garante que **não** vamos virar um monolito espaguete

### Negativas

- Um bug no módulo `payment` pode derrubar `beneficiary` (mesmo processo)
- Deploy precisa atualizar tudo — não dá pra liberar só `payment` sem `audit`
- Escalabilidade horizontal é "tudo ou nada" — não escala só o módulo carregado
- Se algum módulo precisar de stack diferente (ex.: Python para ML), monolito não acomoda

### Neutras / a observar

- O banco compartilhado por schema **funciona até ~50 escritas/seg por schema**. Acima disso, precisaremos avaliar split.
- Boundary tests do ArchUnit precisam ser mantidos pelo time — se relaxarmos, o ADR perde efeito.

## Como saber que esta decisão envelheceu mal

Se algum dos sinais abaixo aparecer em produção, revisitar este ADR:

- 🚨 Deploy de uma feature do módulo X causa rollback de módulo Y (acoplamento real existe)
- 🚨 Tempo de build > 10 minutos (monolito ficou grande demais)
- 🚨 Necessidade de escalar `audit` 5x mais que `payment` (assimetria justifica split)
- 🚨 Time cresce para > 15 pessoas e duas equipes começam a pisar uma na outra no mesmo módulo

Quando 2+ desses sinais coexistirem, abrir ADR-NNN para extrair o primeiro microsserviço.

## Referências

- *Building Evolutionary Architectures* (Ford, Parsons, Kua — 2017) — cap. 4, modular monolith
- *Microservices Patterns* (Richardson — 2018) — cap. 1, quando NÃO usar microsserviços
- [ADR Template](../02-spec-moderna/ADR-TEMPLATE.md)
- [Bounded Contexts identificados](../02-spec-moderna/templates/bounded-contexts.template.md)
- Discussão do time: `#hackathon-time-beta` 15:32–15:48 (síntese em `02-spec-moderna/scope-decisions.md`)

---

## O que torna este ADR "bom"

- ✅ **Status explícito** (Aceito / Proposto / Substituído)
- ✅ **Contexto com restrições reais** (não só "queremos uma arquitetura bonita")
- ✅ **3 alternativas rejeitadas**, cada uma com prós, contras e motivo de rejeição
- ✅ **Consequências positivas E negativas** — honesto, não vendedor
- ✅ **Critério de envelhecimento** ("se isso acontecer, revisitar") — único item que muitos ADRs esquecem
- ✅ Referências reais e localizáveis (livro + página, canal de chat + horário)
---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="SPECIFICATION-exemplo.md"><strong>SPECIFICATION (exemplo)</strong></a><br/>
<sub>Spec EARS com 8 REQ-IDs e source_legacy.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="V1__init_payment_module-exemplo.sql"><strong>Flyway V1 (exemplo)</strong></a><br/>
<sub>Migração com PE → tabela filha + auditoria imutável.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>
