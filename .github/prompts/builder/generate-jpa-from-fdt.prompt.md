---
description: "Gera classes de entidade JPA a partir de definições Adabas FDT, com JSONB para campos MU/PE."
mode: agent
model: claude-sonnet-4-6
tools: ['codebase', 'search', 'editFiles']
---

# /generate-jpa-from-fdt

## Objetivo

Faça parse de um arquivo DDM Adabas e gere uma classe de entidade JPA com mapeamentos de tipo corretos, JSONB para campos MU/PE e um script de migration Flyway correspondente.

## Quando Invocar

No início do Estágio 3, quando a equipe está configurando a camada de dados para um bounded context.

## Pré-condições

- `02-spec-moderna/bounded-contexts.md` existe (para saber qual context possui este DDM)
- O arquivo DDM está acessível em `01-arqueologia/legado-sifap/ddms/`
- A equipe decidiu o package-alvo a partir do design do modular monolith

## Entradas que a Equipe Deve Fornecer

- O path do arquivo DDM (por exemplo, `01-arqueologia/legado-sifap/ddms/DDMXXXXX.ddm`)
- O bounded context e package Java de destino
- Formato de data usado no sistema legado (por exemplo, `YYYYMMDD` packed, ou `YYYY-MM-DD` alpha)

## O Que Vou Fazer

- Fazer parse da estrutura FDT do arquivo DDM
- Mapear cada campo para o tipo Java/JPA apropriado
- Tratar campos MU como coleções mapeadas em JSONB ou @ElementCollection
- Tratar grupos PE como entidades embedded @OneToMany
- Gerar a migration Flyway criando a tabela PostgreSQL
- Sinalizar nomes de campos crípticos com marcadores FIXME

## O Que NÃO Vou Fazer

- Inventar significado de negócio para nomes de campos crípticos — adiciono marcadores FIXME
- Assumir formatos de data — a equipe deve confirmar
- Criar stored procedures — toda lógica de negócio fica em Java
- Pular campos MU/PE — eles são a parte mais difícil e devem ser tratados explicitamente

## Formato de Saída

Dois arquivos:
1. Entidade JPA em `src/main/java/[package]/domain/[EntityName].java`
2. Migration Flyway em `db/migration/V[NNN]__create_[table_name].sql`

## Definição de Pronto

- [ ] A entidade compila sem erros
- [ ] Todo campo FDT tem um campo Java correspondente com tipo correto
- [ ] Campos MU usam JSONB (`@JdbcTypeCode(SqlTypes.JSON)`) ou `@ElementCollection`
- [ ] Grupos PE usam `@OneToMany` com uma classe de entidade separada
- [ ] A migration Flyway é DDL PostgreSQL 16 válida
- [ ] Nomes de campos crípticos têm comentários `// FIXME: confirm semantics`
- [ ] Campos crípticos são adicionados ao catálogo de mistérios se ainda não estiverem lá

## Corpo do Prompt

Você é o `@builder-agent`. A equipe precisa criar uma entidade JPA a partir de um DDM Adabas.

**Passo 1 — Fazer parse do FDT.**
Abra o arquivo DDM especificado. Extraia toda definição de campo:
- Número de level (01 = top-level, 02+ = children)
- Short name (nome Adabas de 2 caracteres)
- Long name (se presente em comentários ou documentação)
- Format: A (alpha), N (numeric), P (packed), B (binary), D (date), T (time)
- Length
- Tipo de descriptor: DE (searchable), MU (multi-value), PE (periodic group), SU (super-descriptor)

Apresente o FDT parseado como tabela para a equipe revisar antes de gerar código.

**Passo 2 — Mapear tipos.**
Aplique estas regras de mapeamento:

| Adabas | Java | JPA | Observações |
|--------|------|-----|-------|
| A(n) | `String` | `@Column(length = n)` | |
| N(n) sem decimal | `Long` ou `Integer` | `@Column` | Use `Long` para IDs |
| N(n.m) | `BigDecimal` | `@Column(precision=n, scale=m)` | Sempre para dinheiro |
| P(n.m) | `BigDecimal` | `@Column(precision=n, scale=m)` | Packed decimal |
| D | `LocalDate` | `@Column` | Pergunte à equipe o formato de origem |
| T | `LocalDateTime` | `@Column` | |
| B(n) | `byte[]` | `@Lob` | Raro |
| Campo MU | `List<T>` | JSONB ou `@ElementCollection` | A equipe escolhe |
| Grupo PE | `List<EmbeddedEntity>` | `@OneToMany` | Classe de entidade separada |

Para campos MU, apresente ambas as opções:
1. **JSONB**: Mais simples, menos consultável → `@JdbcTypeCode(SqlTypes.JSON) private List<String> fieldName;`
2. **@ElementCollection**: Mais consultável, tabela separada → `@ElementCollection @CollectionTable(...)`

Deixe a equipe escolher por campo.

**Passo 3 — Tratar grupos PE.**
Para cada grupo PE, crie uma classe `@Entity` separada com:
- Sua própria tabela
- Uma back-reference `@ManyToOne` para a entidade pai
- Todos os campos dentro do grupo PE mapeados como no Passo 2
- Um campo de índice rastreando o número da ocorrência

**Passo 4 — Tratar super-descriptors.**
Para cada super-descriptor, adicione uma annotation `@Index` composta na entidade pai:
```java
@Table(indexes = @Index(columnList = "field_a, field_b"))
```

**Passo 5 — Sinalizar nomes crípticos.**
Para qualquer campo cujo nome Adabas de 2 caracteres não tenha equivalente claro em inglês:
```java
/** FIXME: confirmar semântica com a equipe para o campo Adabas XX */
@Column(name = "xx_value", length = 20)
private String xxValue;
```

Se o campo ainda não estiver em `01-arqueologia/mysteries-found.md`, anote para a equipe adicionar.

**Passo 6 — Gerar migration Flyway.**
Escreva um script DDL PostgreSQL 16:
- Nome da tabela derivado do nome da entidade (snake_case)
- Tipos de coluna correspondentes aos mapeamentos JPA
- Colunas JSONB para campos MU (se JSONB foi escolhido)
- Tabela separada para grupos PE com foreign key
- Primary key, indexes para descritores
- Constraints `CHECK` quando óbvias a partir do FDT (por exemplo, NOT NULL para campos obrigatórios)

Numere a migration: `V[NNN]__create_[table_name].sql`.

**Passo 7 — Verificar compilação.**
Garanta que a classe de entidade compile. Reporte quaisquer problemas.

## Exemplo de Invocação

```
/generate-jpa-from-fdt ddm=01-arqueologia/legado-sifap/ddms/DDM001.ddm context=payment package=com.datacorp.app.payment dateformat=YYYYMMDD
```
