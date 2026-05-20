---
description: "Checklist de self-review para segurança e problemas OWASP Top 10 em uma feature recém-construída."
mode: ask
model: claude-sonnet-4-6
tools: ['codebase', 'search']
---

# /security-self-review

## Objetivo

Escaneie uma feature recém-construída em busca de problemas comuns de segurança alinhados ao OWASP Top 10. A saída é um relatório priorizado — o agente não corrige automaticamente, a equipe decide.

## Quando Invocar

Depois que um bounded context foi implementado (entities, services, controllers, tests) e antes de avançar para o Estágio 4.

## Pré-condições

- O código da feature existe e compila
- A equipe especifica quais controller(s), service(s) e entity(entities) revisar

## Entradas que a Equipe Deve Fornecer

- O escopo da feature: quais classes controller, service e entity revisar
- O nome do bounded context

## O Que Vou Fazer

- Escanear hardcoded secrets (strings que parecem keys, passwords, tokens)
- Verificar vetores de SQL injection (concatenação de strings em queries)
- Verificar annotations de authentication/authorization em endpoints
- Verificar coverage de validação de entrada
- Procurar dados sensíveis em logs ou respostas de erro
- Identificar rate limits ausentes em endpoints de escrita
- Sinalizar áreas de dependência onde um security scan real deve ser executado

## O Que NÃO Vou Fazer

- Rodar um security scanner real (faço análise estática lendo código)
- Corrigir problemas automaticamente — a equipe revisa e decide o que corrigir
- Fabricar ratings de severidade — cada rating é justificado pelo finding
- Garantir completude — isto é um self-review, não uma auditoria formal

## Formato de Saída

Um relatório Markdown em `03-implementacao/security-review-[context].md`:

```markdown
# Autoavaliação de Segurança — [Bounded Context]
## Resumo
Achados: N no total | Alta: N | Média: N | Baixa: N
## Achados
| # | Severidade | Categoria | Arquivo:Linha | Descrição | Remediação |
## Áreas que Precisam de Scan Externo
## Aprovação
```

## Definição de Pronto

- [ ] Todo endpoint de controller foi verificado quanto a annotations de auth
- [ ] Toda query foi verificada quanto a SQL injection
- [ ] Nenhum hardcoded secret encontrado (ou todos estão sinalizados)
- [ ] Coverage de validação de entrada é avaliada por endpoint
- [ ] O relatório tem ratings de severidade justificados por achados
- [ ] Pelo menos uma "área que precisa de scan externo" é identificada

## Corpo do Prompt

Você é o `@builder-agent` realizando um security self-review. Isto não é uma auditoria formal — é uma checagem rápida antes de a equipe avançar para o Estágio 4.

**Passo 1 — Escanear hardcoded secrets.**
Pesquise nos arquivos especificados padrões que sugiram hardcoded secrets:
- Strings contendo "password", "secret", "key", "token", "api_key" (case-insensitive)
- Strings que parecem tokens codificados em Base64 (strings alfanuméricas longas)
- Properties ou referências a variáveis de ambiente definidas com valores literais em vez de `${ENV_VAR}`
- Arquivos chamados `.env` commitados no repo

Para cada achado: path do arquivo, número da linha, o padrão suspeito (redigido se parecer um secret real), severidade (Alta).

**Passo 2 — Verificar SQL injection.**
Pesquise por:
- Concatenação de strings em SQL queries (`"SELECT..." + variable`)
- Annotations `@Query` com interpolação de string em vez de parâmetros nomeados
- Qualquer uso de `nativeQuery = true` (sinalizar para revisão manual, não rejeitar automaticamente)
- Uso de `JdbcTemplate` com concatenação de strings

Para cada achado: arquivo, linha, padrão vulnerável, remediação (usar named parameters ou derived queries).

**Passo 3 — Verificar authentication e authorization.**
Para cada endpoint `@RestController`:
- Verifique se `@PreAuthorize`, `@Secured` ou method-level security está presente
- Verifique se o controller está sob um path coberto por Spring Security filter chains
- Sinalizar qualquer endpoint publicamente acessível sem justificativa aparente

Para cada endpoint desprotegido: arquivo, linha, método e path do endpoint, severidade (High se modifica dados, Medium se read-only).

**Passo 4 — Verificar validação de entrada.**
Para cada endpoint que aceita request body:
- Verifique se `@Valid` está presente no parâmetro
- Verifique se o DTO de request tem annotations Bean Validation
- Procure qualquer campo do tipo `String` sem constraints `@Size` ou `@Pattern`

Para cada lacuna: arquivo, linha, campo sem validação, remediação.

**Passo 5 — Verificar exposição de dados sensíveis.**
Pesquise por:
- Statements de logging que podem emitir campos sensíveis (passwords, tokens, personal data)
- Respostas de erro que expõem stack traces ou detalhes internos
- DTOs de resposta que incluem campos como `password`, `token`, `ssn`

**Passo 6 — Identificar oportunidades de rate limit.**
Sinalize qualquer endpoint de escrita (POST, PUT, DELETE) sem rate limiting. Nota: talvez a equipe não implemente rate limiting no hackathon, mas isso deve ser documentado como preocupação de produção.

**Passo 7 — Compilar o relatório.**
Escreva em `03-implementacao/security-review-[context].md` com todos os findings ordenados por severidade (High primeiro). Inclua uma contagem de resumo e uma seção listando áreas onde um scanner real (SAST/DAST) deve ser rodado.

Este relatório não bloqueia o Estágio 4 — é informacional. A equipe decide quais findings corrigir agora e quais adiar.

## Exemplo de Invocação

```
/security-self-review context=payment files=PaymentController.java,PaymentService.java,Payment.java
```
