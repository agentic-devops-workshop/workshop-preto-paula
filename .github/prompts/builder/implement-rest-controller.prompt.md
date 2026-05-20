---
description: "Implementa um controller Spring REST a partir de uma definição de endpoint OpenAPI, conectando-o aos services do bounded context."
mode: agent
model: claude-sonnet-4-6
tools: ['codebase', 'search', 'editFiles', 'runCommands']
---

# /implement-rest-controller

## Objetivo

Gere um controller REST Spring Boot a partir de uma definição de endpoint OpenAPI. O controller é um adapter fino — valida entrada, delega para um service e retorna a resposta. Sem lógica de negócio no controller.

## Quando Invocar

Depois que a camada de service para um bounded context existir, quando a equipe estiver pronta para expô-la como REST API.

## Pré-condições

- `02-spec-moderna/openapi.yaml` existe com a definição do endpoint
- A classe de service do bounded context existe (ou sua interface)
- Os DTOs request/response estão definidos (ou serão gerados como records)

## Entradas que a Equipe Deve Fornecer

- O endpoint a implementar (method + path de openapi.yaml)
- O bounded context e package de destino
- A classe de service para delegar

## O Que Vou Fazer

- Ler a definição OpenAPI para o endpoint especificado
- Gerar uma classe `@RestController` com annotations adequadas
- Criar DTOs record request/response com Jakarta Bean Validation
- Conectar o controller ao service via constructor injection
- Adicionar tratamento de erros `@ControllerAdvice` se ainda não estiver presente
- Rodar um build para verificar compilação

## O Que NÃO Vou Fazer

- Colocar lógica de negócio no controller — ele delega para a camada de service
- Pular validação de entrada — todo endpoint tem `@Valid` em seu request body
- Usar field injection com `@Autowired` — somente constructor injection
- Fazer hardcode de mensagens de erro — use respostas ProblemDetail RFC 7807
- Fabricar comportamento de endpoint não definido na spec OpenAPI

## Formato de Saída

Arquivos Java:
1. Controller em `src/main/java/[package]/api/[Name]Controller.java`
2. DTOs request/response em `src/main/java/[package]/api/dto/[Name]Request.java` e `[Name]Response.java`
3. Global exception handler em `src/main/java/[package]/shared/exception/GlobalExceptionHandler.java` (se não existir)

## Definição de Pronto

- [ ] O controller compila sem erros
- [ ] O `operationId` OpenAPI é referenciado no Javadoc
- [ ] O DTO request tem annotations Jakarta Bean Validation (`@NotNull`, `@Size` etc.)
- [ ] A resposta usa HTTP status codes corretos (201 para POST, 200 para GET, 204 para DELETE)
- [ ] Sem lógica de negócio no corpo do controller — apenas validação, delegação, mapeamento de resposta
- [ ] Respostas de erro usam `ProblemDetail` RFC 7807
- [ ] REQ-IDs relacionados estão documentados no Javadoc

## Corpo do Prompt

Você é o `@builder-agent`. A equipe precisa de um controller REST para um endpoint definido na spec OpenAPI.

**Passo 1 — Ler a definição OpenAPI.**
Abra `02-spec-moderna/openapi.yaml`. Encontre o endpoint especificado. Extraia:
- HTTP method e path
- Operation ID e summary
- Request body schema (se houver)
- Response schema
- Path/query parameters
- REQ-IDs relacionados (da description ou tags)

**Passo 2 — Gerar records request/response.**
Crie Java records para request e response:

```java
public record CreatePaymentRequest(
    @NotNull @Positive BigDecimal amount,
    @NotNull LocalDate dueDate,
    @Size(max = 200) String description
) {}

public record PaymentResponse(
    Long id,
    BigDecimal amount,
    LocalDate dueDate,
    String status
) {}
```

Use annotations Jakarta Bean Validation com base nos tipos de campos e quaisquer constraints no schema OpenAPI.

**Passo 3 — Gerar o controller.**
Crie a classe controller:

```java
@RestController
@RequestMapping("/api/v1/[context]")
@Tag(name = "[Context]", description = "[de OpenAPI]")
public class [Name]Controller {

    private final [Service] service;

    public [Name]Controller([Service] service) {
        this.service = service;
    }

    /**
    * [Resumo da operação de OpenAPI].
     *
     * <p>OpenAPI operationId: {@code [operationId]}</p>
    * <p>Implementa: REQ-NNN</p>
     */
    @PostMapping  // ou @GetMapping, etc.
    @Operation(summary = "[summary]", operationId = "[operationId]")
    public ResponseEntity<[Response]> [methodName](@Valid @RequestBody [Request] request) {
        var result = service.[method](/* map request to domain */);
        return ResponseEntity.status(HttpStatus.CREATED).body(/* map domain to response */);
    }
}
```

**Passo 4 — Garantir que existe tratamento de erro.**
Verifique se `GlobalExceptionHandler` existe no package shared. Se não, gere-o com handlers para:
- `MethodArgumentNotValidException` → 400 com detalhes de validação
- `EntityNotFoundException` → 404
- `IllegalStateException` → 409 (conflict)
- `Exception` → 500 (catch-all com mensagem de erro segura, sem stack trace exposto)

Todas as respostas de erro usam `ProblemDetail` (RFC 7807).

**Passo 5 — Verificar compilação.**
Rode `mvn compile` (ou comando de build equivalente). Reporte quaisquer erros e corrija-os.

Se a interface de service ainda não existir, gere uma interface mínima com a assinatura de método obrigatória e uma implementação TODO. A equipe preenche a lógica.

## Exemplo de Invocação

```
/implement-rest-controller endpoint="POST /api/v1/payments" context=payment service=PaymentService
```
