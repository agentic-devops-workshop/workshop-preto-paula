<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Estágio 3 — Implementação (70 min)



> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 3](README.md) → **GUIDE**

> **Para quem é isto?** Para os pares 3 (TL+Dev) e 4 (DBA+QA) que lideram + par 5 que esqueleta o CI.
>
> **O que você terá ao final desta leitura:**
>
> 1. Backend Java + Spring Boot rodando com Testcontainers
> 2. Frontend Next.js usável com Server Components
> 3. Migração Flyway aplicada (mesmo padrão do `V1__init_payment_module-exemplo.sql`)
> 4. Cobertura de testes ≥70%
> 5. Cada commit cita `Implements REQ-XXX`

![ESTÁGIO 03 de 04](https://img.shields.io/badge/EST%C3%81GIO-03%20de%2004-7FBA00?style=for-the-badge) ![Duração 70 min](https://img.shields.io/badge/DURA%C3%87%C3%83O-70%20min-737373?style=for-the-badge) ![Líderes Pares 3 e 4](https://img.shields.io/badge/L%C3%8DDERES-Pares%203%20e%204-1A1A1A?style=for-the-badge)

> ⏰ **Cronograma exato** vive em [`00-TEAM-FLOW.md`](../00-TEAM-FLOW.md) §2. Os badges aqui mostram apenas a **duração** do estágio.

> **Categoria:** Implementação · **Quem trabalha agora:** Pares 3 + 4 lideram

> 🧭 **Antes de entrar neste estágio** (1 minuto de leitura):
>
> - **JPA, Flyway, Testcontainers, Server Component, Bean Validation, Swagger** — siglas novas? [`../07-conceitos/03-glossario-visual.md`](../07-conceitos/03-glossario-visual.md).
> - **Service Java pronto como referência:** [`../08-exemplos/PaymentService-exemplo.java`](../08-exemplos/PaymentService-exemplo.java) (implementa REQ-PAY-001/002 com rastreabilidade).
> - **Migração Flyway bem feita** (com PE → tabela filha): [`../08-exemplos/V1__init_payment_module-exemplo.sql`](../08-exemplos/V1__init_payment_module-exemplo.sql).
> - **Quando usar Plan vs Agent?** [`../09-cheat-sheets/copilot-3-modes.md`](../09-cheat-sheets/copilot-3-modes.md). Para features pequenas, Plan; Agent fica para o Estágio 4.
> - **Travou no setup?** Vá direto à seção `Troubleshooting` mais abaixo.

## ⛳ Definition of Ready — antes de começar

> [!IMPORTANT]
> Não abra este estágio sem antes confirmar:
>
> - [ ] Estágio 2 terminou (Passagem H2 aceita pelo PO)
> - [ ] Você selecionou **`@builder`** no Copilot Chat
> - [ ] `SPECIFICATION.md` tem REQ-IDs com `source_legacy:` válido
> - [ ] ADRs principais aprovadas (≥3 ADRs)
> - [ ] `docker compose up -d` está verde
> - [ ] Branch `impl/...` criada a partir de `develop` atualizada

## Quem trabalha aqui

![Linha do tempo do dia: pré-evento, 4 estágios e demo, com as três passagens H1, H2, H3](../assets/timeline-stages.svg)

## Objetivo

Estender o protótipo funcional do SIFAP 2.0 implementando as features priorizadas no Estágio 2. O protótipo já tem a estrutura base — seu time vai **adicionar features, corrigir bugs e escrever testes**. Cada feature precisa rastrear até uma REQ-ID.

## Por que isso importa

O Estágio 3 é onde a spec encontra a realidade. EARS escrita bonita no Estágio 2 fica preto-no-branco aqui: ou o teste passa, ou não passa. **Cada commit traz uma referência `Implements REQ-XXX:` no message.** Sem isso, a rastreabilidade morre e `/speckit.analyze` ajuda a encontrar drift entre spec, tasks e código antes do passagem.

## Como pensar nisso

Pense no protótipo como uma **cozinha aberta**: a estrutura está pronta (forno, geladeira, ingredientes), faltam pratos. Não reorganize a cozinha; cozinhe.

- O backend já tem 4 módulos (`beneficiary`, `payment`, `audit`, `admin`) com camadas `domain` / `application` / `infrastructure`.
- O frontend já tem layout, rotas básicas e Server Components.
- O banco já tem schema inicial via Flyway.

Sua tarefa: pegar as REQ-IDs do Estágio 2 e transformar cada uma em **endpoint + service + repository + migração + teste**. Não invente arquitetura nova no meio do estágio.

---

## Primeiros 15 minutos: subindo o ambiente

### 1. Suba o ambiente

```bash
# No raiz do repositório (04-prototipo-sifap-moderno/)
docker compose up -d
```

Isso sobe:

- **PostgreSQL 16** na porta 5432
- **Backend (Java 21 + Spring Boot 3)** na porta 8080
- **Frontend (Next.js 15)** na porta **3000** (local) ou **3001** (docker-compose do root)

> [!WARNING]
> Se você rodou `docker compose up` no **ROOT** do workspace (recomendado), o frontend está em **`http://localhost:3001`**. Se rodou de dentro de `04-prototipo-sifap-moderno/`, está em **`http://localhost:3000`**.

### 2. Verifique que tudo está no ar

- Backend health: http://localhost:8080/actuator/health
- Swagger UI: http://localhost:8080/swagger-ui.html
- Frontend: http://localhost:3001 (ou 3000 se subiu local)

### 3. Credenciais padrão

| Usuário     | Senha        | Perfil   | O que pode fazer                              |
| ----------- | ------------ | -------- | --------------------------------------------- |
| `admin`     | `client2026` | ADMIN    | Tudo: gerir usuários, configurações           |
| `operator1` | `client2026` | OPERATOR | Cadastrar beneficiários, registrar pagamentos |
| `auditor1`  | `client2026` | AUDITOR  | Consultar, gerar relatórios, auditar          |

### 4. Teste a API no Swagger

Abra http://localhost:8080/swagger-ui.html e teste:

1. `POST /api/v1/auth/login` com `{"username": "admin", "password": "client2026"}`
2. Copie o token JWT retornado
3. Clique em "Authorize" no Swagger e cole o token
4. Teste os endpoints de beneficiário e pagamento

---

## Estrutura do Backend

O backend segue uma arquitetura **modular monolith** com 4 módulos e 3 camadas cada:

```
src/main/java/br/gov/client/sifap/
│
├── beneficiary/ # Módulo: Beneficiários
│ ├── domain/ # Entidades e regras de negócio
│ ├── application/ # Services e DTOs
│ └── infrastructure/ # Controllers, Repositories, JPA Entities
│
├── payment/ # Módulo: Pagamentos
│ ├── domain/
│ ├── application/
│ └── infrastructure/
│
├── audit/ # Módulo: Auditoria
│ ├── domain/
│ ├── application/
│ └── infrastructure/
│
└── admin/ # Módulo: Administração
 ├── domain/
 ├── application/
 └── infrastructure/
```

### Camadas (de dentro para fora)

| Camada             | Responsabilidade                                      | Exemplos                                                  |
| ------------------ | ----------------------------------------------------- | --------------------------------------------------------- |
| **domain**         | Regras de negócio puras, sem dependência de framework | Enums de status, interfaces de repositório, value objects |
| **application**    | Casos de uso, orquestração                            | Services, DTOs de Request/Response                        |
| **infrastructure** | Detalhes técnicos, I/O                                | Controllers REST, JPA Entities, Spring Data Repositories  |

### Regra de ouro

A camada `domain` **nunca** importa classes de `infrastructure`. O fluxo é sempre: Controller → Service → Repository (interface em domain, implementação em infrastructure).

---

## Passo a passo: adicionar uma feature

Siga estes 5 passos para cada peça de funcionalidade:

### Passo 1: crie ou atualize a entidade de domínio

```java
// src/.../payment/domain/PaymentStatus.java
public enum PaymentStatus {
 PENDING, APPROVED, REJECTED, CANCELLED
}
```

### Passo 2: crie o service

```java
// src/.../payment/application/PaymentService.java
@Service
public class PaymentService {
 // Injete o repositório, implemente a lógica
}
```

### Passo 3: crie o controller

```java
// src/.../payment/infrastructure/PaymentController.java
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
 // Injete o service, exponha os endpoints
}
```

### Passo 4: crie a migração de banco

```sql
-- src/main/resources/db/migration/V2__add_payment_status.sql
ALTER TABLE payments ADD COLUMN status VARCHAR(20) DEFAULT 'PENDING';
```

> [!CAUTION]
> **Use Flyway. Nunca modifique migrações existentes.** Sempre crie novas (`V2__`, `V3__`, etc.). Editar uma migração antiga corrompe o histórico de schema e quebra deploys.

### Passo 5: escreva os testes

```java
// src/test/.../payment/application/PaymentServiceTest.java
@SpringBootTest
class PaymentServiceTest {
 @Test
 @DisplayName("REQ-PAY-XXX: descrição do cenário")
 void shouldCalculatePaymentCorrectly() {
 // Arrange, Act, Assert
 }
}
```

---

## Fluxo com Copilot Plan

Para implementar features rapidamente:

1. **Selecione os arquivos relevantes** no VS Code (Ctrl+click)
2. **Abra o Copilot em modo Plan**
3. **Descreva a mudança** em linguagem natural e peça um plano antes da execução:
   > "Adicione um endpoint PUT /api/v1/beneficiaries/{id}/status que permita
   > mudar o status do beneficiário. Valide que a transição é válida
   > (ACTIVE -> SUSPENDED é permitido, INACTIVE -> ACTIVE não é). Crie o teste."
4. **Revise o plano e o diff** antes de aceitar — verifique que segue a arquitetura
5. **Rode os testes** para confirmar

---

## Testes

### Rodar todos os testes

```bash
cd 04-prototipo-sifap-moderno/backend
./mvnw test
```

### Requisitos

- **Docker precisa estar rodando** — os testes usam Testcontainers para subir um PostgreSQL real
- Java 21 instalado (ou use o DevContainer)

### Tipos de teste esperados

| Tipo        | Onde                   | O que testa                   |
| ----------- | ---------------------- | ----------------------------- |
| Unit        | `*ServiceTest.java`    | Lógica de negócio isolada     |
| Integration | `*ControllerTest.java` | Endpoint completo (HTTP → DB) |
| Repository  | `*RepositoryTest.java` | Queries customizadas          |

---

## Frontend

### Rodar o frontend localmente

```bash
cd 04-prototipo-sifap-moderno/frontend
npm install
npm run dev
```

Abra http://localhost:3000

### Arquitetura do Frontend

O frontend usa **Next.js 15 com App Router** e **Server Components**:

```
src/app/
├── layout.tsx # Layout raiz
├── page.tsx # Página inicial
├── (auth)/
│ └── login/page.tsx # Login
└── (dashboard)/
 ├── beneficiaries/ # CRUD de beneficiários
 ├── payments/ # CRUD de pagamentos
 ├── audit/ # Logs de auditoria
 └── admin/ # Gestão de usuários
```

### Padrão Server Components

- **Server Components** (padrão): buscam dados no servidor, sem JavaScript no cliente
- **Client Components** (`"use client"`): só quando precisa de interatividade (forms, modais)

---

## Rastreabilidade: Requisito → Código → Teste

Para cada feature que você implementa, mantenha rastreabilidade com a spec:

| Requisito EARS                                                                    | Código                           | Teste                                        |
| --------------------------------------------------------------------------------- | -------------------------------- | -------------------------------------------- |
| REQ-BEN-01: "O SIFAP deve validar CPF com módulo 11"                              | `Cpf.java` (domain)              | `CpfTest.java` — 11 testes                   |
| REQ-PAY-03: "Quando um ciclo for gerado, criar pagamentos para beneficiários ACTIVE" | `PaymentCycleService.generate()` | `PaymentCycleServiceTest.generate_openCycle` |
| REQ-AUD-01: "Quando uma entidade for alterada, gravar um registro de auditoria"    | `AuditService.record()`          | `SifapApplicationIntegrationTest`            |

Quando adicionar uma feature, documente no commit: `Implements REQ-XXX`. Isso fecha o ciclo spec → código → teste.

---

## Exemplo concreto: implementando REQ-PAY-DSCT-01

Suponha que o Estágio 2 produziu este REQ-ID:

```yaml
REQ-PAY-DSCT-01:
  pattern: unwanted
  text: "O SIFAP não deve permitir que descontos não judiciais excedam 30% do valor bruto."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L142-L148
```

Sua implementação no Estágio 3:

1. **Service**: adicione método `calculateTotalDeductions` em `PaymentService.java`.
2. **Teste**: 3 cenários (não-judicial 35% → trunca; judicial 50% → aceita; mix 45% → aceita).
3. **Commit**: `feat(payment): cap non-judicial deductions at 30% — Implements REQ-PAY-DSCT-01`.
4. **PR**: link para a REQ-ID no corpo do PR.

Pronto. Rastreabilidade fechada.

---

<details>
<summary><strong>Armadilhas comuns</strong> — clique para expandir</summary>

| ❌ Se você está fazendo isso                                         | ✅ Faça assim                                                 |
| -------------------------------------------------------------------- | ------------------------------------------------------------- |
| Branch única gigante de 8 horas                                      | Commits pequenos, PRs pequenos. 1 feature = 1 PR              |
| Implementar sem teste, "depois eu faço"                              | Escreva o teste junto com o código. "Depois" não existe       |
| Editar uma migração Flyway antiga                                    | NUNCA. Sempre nova migração (V5**, V6**...)                   |
| Criar endpoint sem `@Valid` no DTO                                   | Bean Validation no controller. Sempre                         |
| Misturar lógica de domínio no controller                             | Controller chama service. Lógica fica em service ou domain    |
| Importar classes de `payment.infrastructure` em `beneficiary.domain` | Bounded contexts não se cruzam                                |
| Commit sem `Implements REQ-XXX`                                      | Rastreabilidade é o que valida o trabalho do estágio anterior |

</details>

---

<details>
<summary><strong>Troubleshooting</strong> — clique para expandir</summary>

| Problema                            | Solução                                                                                                             |
| ----------------------------------- | ------------------------------------------------------------------------------------------------------------------- |
| `docker compose up` falha           | Verifique: Docker Desktop ligado? Portas 5432/8080/3000 livres? Tente `docker compose down && docker compose up -d` |
| Backend não conecta no PostgreSQL   | Verifique que o container postgres está healthy: `docker compose ps`                                                |
| Frontend mostra "Failed to load"    | O backend está rodando? Teste: `curl http://localhost:8080/actuator/health`                                         |
| Login retorna "Invalid credentials" | Use: admin / client2026. Verifique que V4\_\_auth.sql rodou (Flyway)                                                |
| Teste falha com Testcontainers      | Docker Desktop precisa estar rodando. Alternativa: unit test com Mockito                                            |
| Migração falha no startup           | NUNCA edite uma migração existente. Crie uma nova (V5**, V6**...)                                                   |
| `mvn test-compile` erro de import   | Verifique que o pacote segue a estrutura: domain/ → application/ → infrastructure/                                  |
| Swagger UI não aparece              | Tente: http://localhost:8080/swagger-ui/index.html (caminho alternativo)                                            |

</details>

---

## Como saber que terminou (Definição de Pronto)

Ao final do Estágio 3, seu time deve ter:

- [ ] Backend funcionando com pelo menos 2 endpoints novos implementados
- [ ] Frontend com pelo menos 1 tela nova ou melhoria significativa
- [ ] Testes passando: `./mvnw test` sem falhas
- [ ] `docker compose up` funcionando — qualquer revisor consegue subir
- [ ] Swagger UI mostrando todos os endpoints documentados
- [ ] Pelo menos 1 regra de negócio do Estágio 1 implementada e testada
- [ ] Todos os commits têm `Implements REQ-XXX` no message
- [ ] Cobertura de testes: backend ≥ 70%, frontend ≥ 60% de linhas

## Próximo passo

No Passagem #3 (~17:00), o **Par 3 (Implementação)** entrega o código rodando para o **Par 5 (Operações)**, que vai cuidar de Terraform e CI/CD no Estágio 4. O Par 4 (Qualidade) continua os testes finais. Veja [`../04-evolucao/GUIDE.md`](../04-evolucao/GUIDE.md).

<details>
<summary><strong>Prompts úteis para Copilot Chat</strong> — clique para expandir</summary>

1. _"Crie um endpoint REST para [funcionalidade] seguindo a arquitetura existente."_
2. _"Escreva um teste de integração para o endpoint [endpoint]."_
3. _"Adicione Bean Validation ao DTO [classe]."_
4. _"Crie uma migração Flyway para adicionar [tabela/coluna]."_
5. _"Implemente a regra de negócio BR-XXX: [descrição da regra]."_
6. _"Crie um React Server Component para listar [entidade]."_
7. _"Adicione tratamento de erro para o caso de [cenário]."_
8. _"Refatore este service para separar a lógica de [responsabilidade]."_

</details>

## Dica de ouro

> [!TIP]
> Não tente implementar tudo. Foque em **qualidade sobre quantidade**. Um endpoint bem feito — com testes, validação e documentação — vale mais que 5 endpoints quebrados.

---

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="../02-spec-moderna/GUIDE.md"><strong>Estágio 2 — Spec Moderna</strong></a><br/>
<sub>14:00–15:00 · Escrever EARS, ADRs e diagramas C4.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../04-evolucao/GUIDE.md"><strong>Estágio 4 — Evolução</strong></a><br/>
<sub>16:10–16:50 · Copilot Agent + Terraform + CI/CD.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>

— Paula
