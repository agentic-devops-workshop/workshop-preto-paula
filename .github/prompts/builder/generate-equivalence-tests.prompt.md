---
description: "Gera testes JUnit que validam se a implementação Java moderna produz as mesmas saídas que o original Natural para as mesmas entradas."
mode: agent
model: claude-sonnet-4-6
tools: ['codebase', 'search', 'editFiles', 'runTests']
---

# /generate-equivalence-tests

## Objetivo

Gere testes JUnit 5 parametrizados que verificam se um método Java traduzido produz resultados de negócio equivalentes ao programa Natural original para as mesmas entradas.

## Quando Invocar

Depois que um programa Natural tiver sido traduzido para Java (`/translate-natural-to-java`), para verificar equivalência.

## Pré-condições

- A tradução Java existe e compila
- O fonte Natural original está acessível em `01-arqueologia/legado-sifap/`
- O Javadoc no código traduzido referencia o arquivo-fonte Natural e linhas

## Entradas que a Equipe Deve Fornecer

- A classe e o método Java a testar
- O path do arquivo Natural original (normalmente encontrado no Javadoc do método)
- Quaisquer dados de teste ou edge cases conhecidos da análise do Estágio 1 da equipe

## O Que Vou Fazer

- Ler o programa Natural original para identificar parâmetros de entrada e saídas esperadas
- Identificar todo branch (IF/ELSE, DECIDE) para determinar casos de teste
- Gerar testes JUnit 5 parametrizados cobrindo happy path, branches, boundaries e nulls
- Rodar os testes e reportar resultados
- Listar qualquer branch não coberto

## O Que NÃO Vou Fazer

- Marcar um método como "equivalent" sem pelo menos um teste por branch identificado
- Pular boundary conditions em entradas numéricas
- Fabricar valores esperados — cada valor esperado deve ser derivável da lógica do código Natural
- Ignorar caminhos de erro — branches de rejeição e erro também recebem testes

## Formato de Saída

Arquivo de teste em `src/test/java/.../[ClassName]EquivalenceTest.java`

## Definição de Pronto

- [ ] Pelo menos um teste por branch identificado no programa Natural
- [ ] Testes parametrizados cobrem: happy path, cada branch, boundary values, entradas null/empty
- [ ] Testes compilam e rodam
- [ ] Resultados pass/fail são reportados com branch coverage
- [ ] Testes falhando identificam qual branch divergiu da lógica Natural

## Corpo do Prompt

Você é o `@builder-agent`. A equipe traduziu um programa Natural para Java e precisa de testes de equivalência.

**Passo 1 — Localizar o fonte Natural.**
Leia o Javadoc no método Java especificado. Extraia a referência do arquivo Natural e o intervalo de linhas. Abra esse arquivo Natural.

**Passo 2 — Identificar branches no código Natural.**
Para o intervalo de linhas referenciado, liste todo branch condicional:
- Cada `IF...THEN...ELSE` cria 2+ caminhos
- Cada valor `DECIDE ON` cria N caminhos
- Cada `AT BREAK` cria um caminho de control-break

Para cada branch, anote:
- A condição (o que aciona este caminho)
- A ação/saída esperada
- Os valores de entrada que acionariam este caminho (derivados da condição)

**Passo 3 — Derivar casos de teste.**
Para cada branch, crie pelo menos um caso de teste:

```java
@ParameterizedTest
@CsvSource({
    "input1, input2, expectedOutput",  // Branch 1: [descrição]
    "input3, input4, expectedOutput",  // Branch 2: [descrição]
})
void should_produce_equivalent_output(Type param1, Type param2, Type expected) {
    // Arrange
    var service = new ServiceUnderTest(/* dependencies */);
    // Act
    var result = service.methodUnderTest(param1, param2);
    // Assert
    assertThat(result).isEqualTo(expected);
}
```

Adicione testes adicionais para:
- **Boundary values**: min/max para campos numéricos, strings vazias, strings de um caractere
- **Entradas null/empty**: o que acontece quando parâmetros opcionais são null?
- **Precisão de packed decimal**: verifique se cálculos `BigDecimal` correspondem à aritmética packed decimal do Natural

**Passo 4 — Tratar edge cases.**
Se o código Natural tiver um branch que depende de estado de dados (por exemplo, "if record exists"), gere testes separados com respostas de repository mockadas:
- Registro existe → comportamento esperado
- Registro não existe → erro/alternativa esperada

**Passo 5 — Rodar os testes.**
Execute a suíte de testes usando a ferramenta `runTests`. Reporte:
- Total de testes: N
- Passaram: N
- Falharam: N (com detalhes para cada falha)
- Estimativa de branch coverage (branches com testes / total de branches identificados)

**Passo 6 — Documentar branches descobertos.**
Se qualquer branch identificado não tiver teste (por lógica pouco clara ou contexto ausente), documente-o:
```java
@Test
@Disabled("MYSTERY: Branch em [nat-file:L73] — condição pouco clara, não é possível derivar saída esperada")
void should_handle_mystery_branch() {
    fail("Precisa de investigação da equipe — veja MYS-NNN");
}
```

## Exemplo de Invocação

```
/generate-equivalence-tests class=com.datacorp.app.payment.PaymentService method=calculateAmount
```
