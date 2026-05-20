---
description: "Guia de arquitetura para Modular Monolith — package-by-feature, bounded contexts, mapeamento JPA, Strangler Fig"
applyTo: '**/src/main/java/**,**/pom.xml,**/build.gradle*'
---

# Guia de Arquitetura Modular Monolith

Este arquivo é ativado quando você trabalha em arquivos-fonte Java ou configurações de build. Ele reforça a arquitetura-alvo: um **Modular Monolith** — não microservices.

## Princípio Central: Um Deployable, Muitos Módulos

O sistema-alvo é uma única aplicação Spring Boot com fronteiras internas de módulos claras. Cada bounded context é um módulo Maven (ou um package top-level) que possui suas camadas de domain, repository e service.

Por que Modular Monolith e não microservices:

- **Restrição do hackathon**: 8 horas não é tempo suficiente para gerenciar sistemas distribuídos, service discovery e comunicação inter-service.
- **Orçamento de complexidade**: Um monolith com boas fronteiras de módulo oferece 80% dos benefícios de microservices (autonomia da equipe, ownership claro) com 20% do custo operacional.
- **Caminho de migração**: Um Modular Monolith bem estruturado pode ser decomposto em microservices depois, se necessário. O inverso é muito mais difícil.

## Estrutura Package-by-Feature

Organize código por capacidade de negócio, não por camada técnica:

```
src/main/java/com/example/app/
├── payment/                    # Bounded context: Payment
│   ├── PaymentController.java  # REST endpoint
│   ├── PaymentService.java     # Business logic
│   ├── PaymentRepository.java  # Data access
│   ├── Payment.java            # JPA entity
│   └── PaymentDto.java         # DTO (Java record)
├── enrollment/                 # Bounded context: Enrollment
│   ├── EnrollmentController.java
│   ├── ...
├── shared/                     # Shared kernel
│   ├── audit/                  # Transversal: audit trail
│   └── exception/              # Transversal: error handling
└── Application.java            # Spring Boot entry point
```

Regras:

- Um módulo **nunca** importa diretamente classes internas de outro módulo. Use interfaces ou events.
- O package `shared/` contém apenas concerns cross-cutting (audit, exceptions, base entities).
- Cada módulo tem seu próprio `*Repository`, `*Service` e `*Controller`.

## Fronteiras de Bounded Context

Ao decidir onde desenhar fronteiras de módulos, pergunte:

1. **Quem é dono destes dados?** Se duas features compartilham a mesma tabela, talvez pertençam ao mesmo contexto.
2. **O que muda junto?** Features modificadas no mesmo sprint pertencem juntas.
3. **O que pode falhar independentemente?** Se a falha da Feature A não deve quebrar a Feature B, elas pertencem a contextos separados.

Padrão comum em modernização de legado Natural/Adabas: cada arquivo Adabas (FNR) frequentemente mapeia para um bounded context, embora alguns arquivos sejam dados de referência compartilhados que pertencem a um shared kernel.

## Mapeamento JPA a partir de Adabas FDT

### Campos Simples

| Formato Adabas | Tipo Java | Annotation JPA |
|---|---|---|
| `A` (alphanumeric) | `String` | `@Column(length = N)` |
| `N` (numeric, no decimal) | `Long` or `Integer` | `@Column` |
| `N` (numeric, with decimal) | `BigDecimal` | `@Column(precision = P, scale = S)` |
| `P` (packed decimal) | `BigDecimal` | `@Column(precision = P, scale = S)` |
| `D` (date) | `LocalDate` | `@Column` |
| `T` (time/datetime) | `LocalDateTime` | `@Column` |
| `B` (binary) | `byte[]` | `@Column` / `@Lob` |

### Campos MU (Multiple-Value) → JSONB

```java
@Column(columnDefinition = "jsonb")
@JdbcTypeCode(SqlTypes.JSON)
private List<String> alternateNames;  // Was MU field in Adabas
```

Ou com `@ElementCollection` se você precisar de capacidade de consulta:

```java
@ElementCollection
@CollectionTable(name = "person_alternate_names")
private List<String> alternateNames;
```

### Grupos PE (Periodic Groups) → @OneToMany

```java
@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
@JoinColumn(name = "person_id")
private List<AddressHistory> addressHistory;  // Was PE group
```

Onde `AddressHistory` é uma `@Entity` com sua própria tabela.

## Convenções Spring Boot 3.3

- **Constructor injection**: Sem `@Autowired` em campos. Use `@RequiredArgsConstructor` (Lombok) ou construtores explícitos.
- **Records para DTOs**: `public record PaymentDto(Long id, BigDecimal amount, LocalDate dueDate) {}`
- **Validação na camada de controller**: `@Valid @RequestBody PaymentDto dto` com annotations Bean Validation no DTO.
- **@Transactional somente na camada de service**: Nunca em repositories, nunca em controllers.
- **Optional para retornos anuláveis**: `Optional<Payment> findById(Long id)` — nunca retorne `null` de métodos públicos.
- **Sealed interfaces para type unions**: `sealed interface PaymentStatus permits Pending, Approved, Rejected {}`

## Error Handling Pattern

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(EntityNotFoundException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(detail);
    }
}
```

Use `ProblemDetail` (RFC 7807) para todas as respostas de erro.

## Padrão Strangler Fig

Quando o sistema moderno precisa coexistir com o legado:

1. **Facade**: Todas as requests passam por uma camada de roteamento
2. **New path**: Features novas ou migradas são tratadas pelos módulos Spring Boot
3. **Legacy path**: Features não migradas são proxied para o sistema legado
4. **Migração gradual**: À medida que cada feature é migrada, sua rota muda de legado para moderno

Esse padrão se aplica mesmo dentro do escopo do hackathon: as equipes talvez não migrem tudo, e isso é aceitável. A arquitetura deve suportar migração parcial de forma elegante.

## O Que NÃO Fazer

- **Sem microservices**: Não crie aplicações Spring Boot separadas para cada contexto
- **Sem stored procedures**: Toda lógica de negócio vive em Java, não em funções PostgreSQL
- **Sem concatenação de strings para SQL**: Use JPA/JPQL ou queries derivadas do Spring Data
- **Sem field injection com `@Autowired`**: Use constructor injection
- **Sem retornos `null`**: Use `Optional` para métodos que talvez não encontrem resultado
