<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
name: Lint de estilo de documentação
description: 'Use ao revisar documentação quanto a estilo, clareza, linguagem inclusiva ou conformidade com os guias de estilo Microsoft/Google. Dispara com "doc review", "style guide", "plain language", "inclusive language", "readability".'
---

# Lint de estilo de documentação

## Quando invocar
- "Faça lint deste README contra nosso guia de estilo."
- "Reescreva esta documentação de API em linguagem simples."
- "Verifique termos excludentes e jargão."

## Regras

### Voz e tom
- **Voz ativa**. "O sistema armazena o arquivo", não "O arquivo é armazenado pelo sistema."
- **Tempo presente**. "Retorna uma resposta JSON", não "Retornará uma resposta JSON."
- **Segunda pessoa** ("você") para guias práticos; **terceira pessoa** para referência.
- **Títulos em sentence case** (não Title Case).

### Clareza
- Uma ideia por frase.
- Máximo de 25 palavras por frase como regra prática.
- Máximo de cinco frases por parágrafo.
- Evite palavras amortecedoras ("just", "simply", "easily"): elas mentem para a pessoa leitora.
- Sem travessões longos. Use vírgulas, parênteses ou dois-pontos.

### Linguagem inclusiva
Substitua:
- "master/slave" -> "primary/replica" ou "leader/follower"
- "whitelist/blacklist" -> "allowlist/blocklist"
- "guys" -> "folks", "everyone", "team"
- "crazy/insane" (como intensificador) -> "significant", "unusual"
- "dummy" (nomes de variáveis) -> "example", "sample"
- "sanity check" -> "quick check", "verification"

### Estrutura
- **Comece pelo resultado**, não pelo contexto. A pessoa leitora sabe por que deve continuar lendo.
- **Diga o que ela vai aprender** no topo.
- **Resuma no final** em documentos longos.
- **Use títulos segmentados** para facilitar a leitura rápida.

### Links
- O texto do link descreve o destino. Nunca use "click here" ou "this link".
- URLs absolutas para fontes externas; relativas para conteúdo interno.
- Verifique links no CI.

### Exemplos de código
- Todo snippet executável deve ser testado.
- Use exemplos realistas, não `foo/bar/baz`.
- Destaque placeholders com clareza: `<YOUR-API-KEY>`.

### Números e unidades
- Algarismos para 10+, palavras para zero a nove (estilo Microsoft).
- Unidades métricas; inclua conversão se o público for misto.
- Sempre especifique a unidade: "100 MB", não "100".

## Etapas de revisão
1. **Leia uma vez como a pessoa leitora pretendida**. Tem o tamanho certo? O nível de detalhe certo?
2. **Execute verificações automatizadas**: Vale, Alex.js, markdownlint.
3. **Aplique as regras de estilo** seção por seção.
4. **Teste todo exemplo de código**.
5. **Pergunte**: uma nova contratação no dia 1 entenderia isto?

## Template de saída
```markdown
## Revisão de Estilo - <Doc>

### Resumo
- Legibilidade (grau Flesch-Kincaid): 11 (alvo: <=12)
- Voz passiva: 8% (alvo: <10%)
- Problemas de linguagem inclusiva: 2
- Links quebrados: 0
- Exemplos de código não testados: 3

### Recomendações (top 10)
| ID | Local | Problema | Correção |
|----|----------|-------|-----|
| 01 | Seção de instalação | Voz passiva | Reescrever em voz ativa |
| 02 | Troubleshooting | "guys" | Substituir por "team" |
| 03 | Referência de API | "simply call" | Remover "simply" |
```

## Antipadrões
- Revisar sem executar linters automatizados primeiro.
- Estilo acima de substância.
- Reescrever a voz da pessoa autora em vez de lapidá-la.
- Ignorar acessibilidade (texto alternativo, níveis de título, texto de link).

## Gate de qualidade
Todo documento deve passar nos linters automatizados antes da revisão humana.
