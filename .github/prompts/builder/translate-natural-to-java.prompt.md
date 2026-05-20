---
description: "Traduz um programa Natural para Java 21 + Spring Boot 3.3 idiomático, preservando semântica de negócio."
mode: agent
model: claude-sonnet-4-6
tools: ['codebase', 'search', 'editFiles', 'fetch']
---

# /translate-natural-to-java

## Objetivo

Traduza um programa Natural para Java 21 + Spring Boot 3.3 idiomático, preservando semântica de negócio (não sintaxe). A saída é Java compilável com Javadoc rastreando de volta ao fonte Natural.

## Quando Invocar

No início do Estágio 3, quando a equipe começa a implementar bounded contexts a partir do design do Estágio 2.

## Pré-condições

- `02-spec-moderna/modular-monolith-design.md` existe com a estrutura de packages definida
- `02-spec-moderna/SPECIFICATION.md` existe com requisitos EARS
- O bounded context e package-alvo são conhecidos
- O arquivo-fonte Natural está acessível em `01-arqueologia/legado-sifap/`

## Entradas que a Equipe Deve Fornecer

- O path do arquivo do programa Natural (por exemplo, `01-arqueologia/legado-sifap/programs/PGXXXXXX.nat`)
- O bounded context e package Java de destino
- Quaisquer requisitos EARS relacionados (REQ-IDs)

## O Que Vou Fazer

- Ler o programa Natural bloco por bloco
- Identificar o propósito de negócio de cada bloco procedural
- Traduzir para Java 21 idiomático (records para DTOs, sealed interfaces, constructor injection)
- Gerar Javadoc com link para o arquivo-fonte Natural e intervalo de linhas
- Sinalizar orphan logic (código sem requisito EARS correspondente) para decisão da equipe
- Criar stubs de teste unitário para cada método traduzido

## O Que NÃO Vou Fazer

- Espelhar sintaxe Natural linha por linha em Java ("JOBOL" — Java que parece Natural)
- Mesclar silenciosamente múltiplos conceitos Natural em uma classe Java
- Inventar significado de negócio para código pouco claro — orphan logic é sinalizada, não interpretada
- Pular a leitura dos requisitos EARS primeiro — todo bloco traduzido deve mapear para um REQ-ID

## Formato de Saída

Arquivos Java sob o package `src/main/java/` apropriado, mais stubs de teste sob `src/test/java/`. Cada arquivo inclui Javadoc citando o fonte Natural.

## Definição de Pronto

- [ ] Arquivos Java compilam sem erros
- [ ] Todo método público tem Javadoc citando o arquivo-fonte Natural e intervalo de linhas
- [ ] Toda regra de negócio dos requisitos EARS relevantes tem um método correspondente
- [ ] Orphan logic (código sem REQ) está documentada com `// ORPHAN: [file:line] — decisão da equipe necessária`
- [ ] Existem stubs de teste unitário para todo método público
- [ ] Sem port linha por linha de Natural — a tradução usa idiomas Java 21

## Corpo do Prompt

Você é o `@builder-agent`. A equipe escolheu um programa Natural para traduzir para Java.

**Passo 1 — Ler primeiro os requisitos EARS.**
Antes de tocar no arquivo Natural, leia `02-spec-moderna/SPECIFICATION.md` e identifique todos os requisitos relevantes para este programa. Liste-os. Esses requisitos definem o que o código Java *deve* fazer.

**Passo 2 — Ler o programa Natural.**
Abra o arquivo especificado. Leia a seção `DEFINE DATA` para entender o modelo de dados. Depois leia a lógica principal bloco por bloco:
- Para cada `IF...THEN...ELSE...END-IF`, identifique a decisão de negócio
- Para cada `READ` ou `FIND`, identifique o padrão de acesso a dados
- Para cada `CALLNAT`, anote a dependência (mas não traduza o alvo — isso é uma invocação separada)
- Para cada `PERFORM`, identifique a sub-rotina interna

**Passo 3 — Mapear blocos para requisitos.**
Para cada bloco identificado, encontre o requisito EARS que ele implementa. Se um bloco não tiver requisito correspondente, marque como orphan logic:
```java
// ORPHAN: [natural-file.nat:L42-58] — Nenhum REQ correspondente. Decisão da equipe: manter, modificar ou remover?
```
Pergunte à equipe o que fazer com orphan logic antes de prosseguir.

**Passo 4 — Traduzir para Java.**
Para cada bloco com requisito correspondente, escreva o equivalente Java:
- Variáveis `DEFINE DATA LOCAL` → parâmetros de método ou variáveis locais com tipos adequados
- `IF...THEN...ELSE` → expressões Java `if/else` ou `switch` (pattern matching do Java 21 quando apropriado)
- `READ LOGICAL BY` → Spring Data JPA `findBy*` method
- `FIND WITH` → JPA `@Query` com parâmetros nomeados
- `CALLNAT` → chamada de método de service (injetar a dependência)
- Cálculos packed decimal → `BigDecimal` com escala e modo de arredondamento explícitos
- Operações de string → métodos Java `String`, anotando diferenças de charset

Use idiomas Java 21:
- Records para DTOs e value objects
- Sealed interfaces para uniões discriminadas (por exemplo, payment statuses)
- `Optional` para retornos anuláveis
- Constructor injection (sem `@Autowired` em campos)
- `@Valid` para validação de entrada na camada de controller
- `@Transactional` somente em métodos de service, nunca repositories

**Passo 5 — Gerar Javadoc.**
Todo método público recebe Javadoc que inclui:
```java
/**
 * [Descrição de negócio].
 *
 * <p>Traduzido de: {@code [natural-file.nat:L42-58]}</p>
 * <p>Implementa: REQ-NNN</p>
 */
```

**Passo 6 — Criar stubs de teste.**
Para cada método público, gere um stub de teste em `src/test/java/`:
```java
@Test
void should_[expected]_when_[condition]() {
    // Arrange: [descreva o setup com base nos parâmetros de entrada Natural]
    // Act: [chame o método traduzido]
    // Assert: [verifique contra os critérios de aceitação EARS]
    fail("TODO: implement — see REQ-NNN critérios de aceite");
}
```

**Passo 7 — Verificar compilação.**
Tente compilar os arquivos gerados. Reporte quaisquer erros de compilação e corrija-os.

Se uma construção Natural não tiver um idioma Java limpo, apresente 2 alternativas à equipe e deixe que escolham. Não escolha silenciosamente.

## Exemplo de Invocação

```
/translate-natural-to-java file=01-arqueologia/legado-sifap/programs/PGMAIN01.nat context=payment package=com.datacorp.app.payment
```
