<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Relatório de Descoberta — Estágio 1: Arqueologia Digital

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **discovery-report**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Este documento consolida todas as descobertas do Estágio 1.
> Preencha cada seção com as conclusões do time. **Este é o input principal do Estágio 2** — sem ele, a especificação vira chute.

**Time**: [Nome do Time]
**Data**: 19/05/2026
**Edição**:
**Participantes**: [Liste os membros e suas personas]

---

## 1. Sumário Executivo

> Em 3 a 5 frases, resuma o que o time descobriu sobre o SIFAP legado.
> O que é este sistema? Qual sua criticidade? Qual o estado do código?

[Escreva aqui]

---

## 2. Visão Geral do Sistema

### 2.1 Propósito do SIFAP

[Descreva o que o sistema faz com base na análise do código]

### 2.2 Arquitetura Legada

[Descreva a arquitetura: quantos programas, DDMs, fluxos principais]

### 2.3 Usuários e Perfis

[Quem usa o sistema? Quais perfis de acesso existem?]

---

## 3. Principais Descobertas

### 3.1 Regras de Negócio Críticas

> Liste as 5 regras de negócio mais importantes encontradas.

1. [Regra + referência ao catálogo BR-XXX]
2.
3.
4.
5.

### 3.2 Dependências Complexas

> Quais programas estão mais acoplados? Onde há risco de efeito cascata?

[Descreva]

### 3.3 Dívida Técnica Identificada

> Que problemas no código legado vão complicar a migração?

- [ ] [Problema 1]
- [ ] [Problema 2]
- [ ] [Problema 3]

### 3.4 Gaps de Documentação

> O que a documentação existente NÃO cobre?

[Descreva]

---

## 4. Mistérios e Riscos

### 4.1 Mistérios Não Resolvidos

> Resuma os mistérios do arquivo `mysteries-found.md` que permanecem sem explicação.

| ID  | Descrição | Risco para Migração |
| --- | --------- | ------------------- |
|     |           |                     |

### 4.2 Riscos para o Estágio 2

> O que o time de especificação precisa saber antes de começar?

1. [Risco 1]
2. [Risco 2]
3. [Risco 3]

---

## 5. Recomendações

### 5.1 O que migrar primeiro

> Com base na priorização do Par 1 (Product Owner), quais funcionalidades devem ser migradas primeiro?

| Prioridade | Funcionalidade | Justificativa |
| ---------- | -------------- | ------------- |
| 1          |                |               |
| 2          |                |               |
| 3          |                |               |

### 5.2 O que descartar

> Funcionalidades que provavelmente não precisam ser migradas:

- [Funcionalidade]: [Motivo para descartar]

### 5.3 O que evoluir

> Funcionalidades que devem ser migradas E melhoradas:

- [Funcionalidade]: [Como melhorar]

---

## 6. Métricas do Estágio

| Métrica                       | Valor        |
| ----------------------------- | ------------ |
| Programas analisados          | \_\_\_ / 15  |
| DDMs mapeados                 | \_\_\_ / 4   |
| Regras de negócio encontradas | \_\_\_       |
| Regras escondidas encontradas | \_\_\_ / 10  |
| Easter eggs encontrados       | \_\_\_ / 3   |
| Termos no glossário           | \_\_\_       |
| Mistérios catalogados         | \_\_\_       |
| Tempo total gasto             | \_\_\_ horas |

---

## 7. Notas para o Próximo Estágio

> Deixe aqui mensagens para o time no Estágio 2 (Especificação Moderna):

[Escreva aqui]

---

## Definição de Pronto deste relatório

- [ ] Todas as seções acima preenchidas (sem placeholders).
- [ ] Pelo menos 5 regras críticas listadas em §3.1, cada uma referenciando uma `BR-XXX` do catálogo.
- [ ] Decisões de migrar/descartar/evoluir em §5 cobrem as 8+ funcionalidades principais.
- [ ] Métricas de §6 conferem com os outros artefatos (glossary.md, business-rules-catalog.md, mysteries-found.md).

— Paula


---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="mysteries-found.md"><strong>mysteries-found.md</strong></a><br/>
<sub>Lista de mistérios.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../02-spec-moderna/GUIDE.md"><strong>Estágio 2 — Spec</strong></a><br/>
<sub>Próximo estágio: spec moderna.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>

