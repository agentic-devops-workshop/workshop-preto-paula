<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Glossário de Domínio

![DOC Glossário canônico](https://img.shields.io/badge/DOC-Glossário%20canônico-00A4EF?style=for-the-badge) ![VARIANTE Versão visual em 07-conceitos](https://img.shields.io/badge/VARIANTE-Versão%20visual%20em%2007-conceitos-1A1A1A?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Docs](README.md) → **Glossário**

> **Para quem é isto?** Glossário canônico de termos do projeto (estruturado, sem analogias).
>
> **Quer a versão com analogias?** → [`../07-conceitos/03-glossario-visual.md`](../07-conceitos/03-glossario-visual.md)


> Fonte única da verdade para termos de domínio. Todo novo termo entra aqui ANTES de aparecer em código, specs ou tickets. O Tech Writer é responsável por este arquivo.

## Como adicionar um termo

1. Use este formato:

```markdown
### Termo

- **Fonte:** Programa Natural ou DDM onde o termo se origina (ex.: `CADBENEF.NSN:42`)
- **Definição:** Definição curta (1–2 frases)
- **Equivalente em inglês:** Se o termo legado estiver em português
- **Usado em:** REQ-IDs, arquivos, módulos
```

2. Mantenha os termos em ordem alfabética dentro de cada seção.
3. Se duas pessoas da equipe discordarem de uma definição, registre isso como uma decisão e acione o Requirements Engineer.

---

## A

### Adabas

- **Fonte:** Armazenamento de dados legado
- **Definição:** Banco de dados mainframe em estilo NoSQL multivalorado usado pelo sistema legado SIFAP. DDMs (Data Definition Modules) do Adabas descrevem registros.
- **Usado em:** todos os programas `.NSN` em `02-cenario-sifap-legado/natural-programs/`

## B

### Beneficiário (Beneficiary)

- **Fonte:** `BENEFICIARIO.ddm`
- **Definição:** Pessoa que recebe pagamentos de benefício social pelo SIFAP.
- **Equivalente em inglês:** Beneficiary
- **Usado em:** REQ-BEN-\*, `BeneficiaryEntity.java`

## C

### Ciclo (Cycle)

- **Fonte:** `BATCHPGT.NSN`
- **Definição:** Execução mensal de geração de pagamentos que produz registros de pagamento para todos os beneficiários elegíveis.
- **Equivalente em inglês:** Payment cycle
- **Usado em:** REQ-PAY-\*, `PaymentCycleService.java`

### CPF

- **Fonte:** CPF brasileiro
- **Definição:** Identificador nacional de contribuinte com 11 dígitos, usado como identificador principal de beneficiários. Validado com o algoritmo módulo-11.
- **Usado em:** toda a lógica de identificação de beneficiários

## D

### DDM (Data Definition Module)

- **Fonte:** Adabas
- **Definição:** Definição de schema para um registro Adabas. O SIFAP usa 4 DDMs: Beneficiary, Payment, Social Program, Audit.
- **Usado em:** arqueologia + mapeamento de schema

---

## Como facilitadores usam este arquivo

A pessoa facilitadora confere este glossário em toda transição de estágio. Um glossário com menos de 30 termos ao fim do Estágio 1 é um sinal de que a equipe não investigou com profundidade suficiente.

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="../07-conceitos/03-glossario-visual.md"><strong>Glossário Visual</strong></a><br/>
<sub>Versão com analogias.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="FAQ.md"><strong>FAQ</strong></a><br/>
<sub>Perguntas frequentes.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>

