<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Estágio 1 — Arqueologia Digital



> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **GUIDE**

> **Para quem é isto?** Para o par 1 (PO+RE) que lidera + todos os outros 4 pares trabalhando em paralelo. Este estágio é obrigatório.
>
> **O que você terá ao final desta leitura:**
>
> 1. Saberá quais 3 programas Natural seu par lê
> 2. Saberá como extrair regras de negócio com rastreabilidade `arquivo.NSN#L<linha>`
> 3. Entregará 5 artefatos: glossário, catálogo BR, mapa de dependências, mistérios, discovery report
> 4. Saberá o que fazer no Passagem #1 às 14:00

![ESTÁGIO 01 de 04](https://img.shields.io/badge/EST%C3%81GIO-01%20de%2004-F25022?style=for-the-badge) ![Duração 90 min](https://img.shields.io/badge/DURA%C3%87%C3%83O-90%20min-737373?style=for-the-badge) ![Líder Todos os pares](https://img.shields.io/badge/L%C3%8DDER-Todos%20os%205%20pares-1A1A1A?style=for-the-badge)

> ⏰ **Cronograma exato** vive em [`00-TEAM-FLOW.md`](../00-TEAM-FLOW.md) §2. Os badges aqui mostram apenas a **duração** do estágio.

> **Categoria:** Descoberta · **Quem trabalha agora:** Todos os 5 pares em paralelo

> 🧭 **Antes de entrar neste estágio** (1 minuto de leitura):
>
> - **Primeiro contato?** Leia [`../00-COMECE-AQUI.md`](../00-COMECE-AQUI.md) primeiro.
> - **Não programa em Natural?** [`../01-arqueologia/legado-sifap/COMO-LER-NATURAL.md`](legado-sifap/COMO-LER-NATURAL.md) ensina a extrair regras dos `.NSN` sem saber a sintaxe.
> - **Tropeçou em alguma sigla** (DDM, MU, PE, BR-NNN)? [`../07-conceitos/03-glossario-visual.md`](../07-conceitos/03-glossario-visual.md) explica em 3 linhas.
> - **Quer ver como fica o entregável**? [`../08-exemplos/business-rules-catalog-exemplo.md`](../08-exemplos/business-rules-catalog-exemplo.md) mostra um catálogo bem feito.
> - **Cheat-sheet do Copilot** (Ask vs Plan vs Agent): [`../09-cheat-sheets/copilot-3-modes.md`](../09-cheat-sheets/copilot-3-modes.md).

> [!IMPORTANT]
> **Esta é a única etapa do dia que você não pode pular.** Tudo o que vem depois — a especificação, o código, o deploy — depende do que sua dupla extrair daqui.
>
> Na edição anterior do workshop, vários times escreveram especificações sem ler o legado e descobriram tarde demais que perderam regras de negócio de 29 anos. Desta vez, o portão é obrigatório: o CI e a rubrica não deixam você seguir sem rastrear cada requisito até um arquivo `.NSN` ou `.ddm`.

**Você está no Estágio 1 (Descoberta do SDLC).** A saída deste estágio alimenta diretamente o Estágio 2 (Especificação). Sem entrega clara aqui, o passagem #1 falha e o time inteiro empaca.

## ⛳ Definition of Ready — antes de começar

> [!IMPORTANT]
> Não abra este estágio sem antes confirmar:
>
> - [ ] Setup técnico concluído (`00-SETUP.md` rodado, Docker no ar)
> - [ ] Você selecionou **`@archaeologist`** no Copilot Chat
> - [ ] Você já leu o `PERSONA.md` das suas 2 personas
> - [ ] Seu par está com você (não comece sozinho)
> - [ ] Você sabe quais 3 programas Natural seu par vai ler (tabela abaixo)
> - [ ] Aba aberta no [`07-conceitos/03-glossario-visual.md`](../07-conceitos/03-glossario-visual.md) para consulta
> - [ ] Garrafa de água perto :)

## Quem trabalha aqui (todos os 5 pares, em paralelo)

![Distribuição dos 5 pares: visão, arquitetura, implementação, qualidade e operações](../assets/personas-team.svg)

Todos os 5 pares trabalham **em paralelo**, cada um com seus 3 programas. Ninguém fica ocioso. Ao final, cada par contribui com sua parte para os 5 artefatos consolidados.

## O que você vai conseguir em 3 horas

Ao final do Estágio 1 sua dupla terá produzido **cinco artefatos verificáveis** dentro de [`01-arqueologia/`](.):

1. `glossary.md` — pelo menos 30 termos do domínio.
2. `business-rules-catalog.md` — no mínimo 15 regras de negócio, **cada uma com o programa-fonte** preenchido.
3. `dependency-map.md` — um diagrama Mermaid cobrindo todos os 15 programas Natural.
4. `mysteries-found.md` — pelo menos 5 regras escondidas com evidência (arquivo + linha).
5. `discovery-report.md` — síntese consolidada que vai virar input do Estágio 2.

Um facilitador (cordão azul) passa por volta de **13h50** e valida esses artefatos contra a [`LEGACY-EXPLORATION-CHECKLIST.md`](LEGACY-EXPLORATION-CHECKLIST.md). Se algo estiver vermelho, sua dupla não pode abrir o Estágio 2.

---

## Por que isso importa

Um sistema legado raramente tem documentação atualizada. O que ele tem é o **código**, e o código carrega regras de negócio que ninguém escreveu em nenhum outro lugar. Se você modernizar olhando só para o brief de modernização, você reescreve uma versão moderna **do brief**, não do sistema. E o sistema é o que está em produção.

O SIFAP tem 29 anos. Tem regras tributárias de 2003 ainda válidas. Tem cálculos de safra que só fazem sentido se você conhece a história. Tem um relatório que o TCU aceita há 23 anos com o mesmo layout. Você não pode modernizar o que você não leu.

A arqueologia digital existe para isso: extrair conhecimento do código antes de tocar nele.

---

## Como pensar nisso (modelo mental antes do passo a passo)

Pense no SIFAP como **uma cidade que vocês cinco vão escavar em 3 horas**. Cada par é uma equipe arqueológica responsável por um quarteirão. Ninguém tem tempo de escavar a cidade inteira sozinho, então a regra é simples:

- **Cada par fica com 3 programas.** Quinze programas divididos por cinco pares dá três cada. Sem orfãos.
- **Tudo que você acha vai num caderno comum** (os arquivos templates dentro deste folder). Outros pares vão ler o que você escreveu para construir a especificação depois.
- **Achou algo estranho? Anote.** Mistérios são pontos. Mas só vale se você documenta o lugar exato (arquivo + linha).
- **Não tente entender tudo.** Tente entender as **regras de negócio** que o programa implementa. A parte de IO, paginação, tratamento de erro do Natural — ignore. O que interessa é o `IF` que esconde uma regra.

O resultado bom não é "eu li tudo". O resultado bom é "extraí o que importa de 3 programas e deixei evidência para os outros usarem".

---

## Onde está o legado

Está dentro do próprio kit, em [`../01-arqueologia/legado-sifap/`](legado-sifap/):

| Recurso                          | Caminho                                                                    | Quantidade                  |
| -------------------------------- | -------------------------------------------------------------------------- | --------------------------- |
| Programas Natural                | [`../01-arqueologia/legado-sifap/natural-programs/`](legado-sifap/natural-programs/)               | 15 arquivos `.NSN`          |
| DDMs Adabas                      | [`../01-arqueologia/legado-sifap/adabas-ddms/`](legado-sifap/adabas-ddms/)                         | 4 arquivos `.ddm`           |
| Documentação parcial (1997-2018) | [`../01-arqueologia/legado-sifap/legacy-docs/`](legado-sifap/legacy-docs/)                         | 3 documentos desatualizados |
| README do sistema                | [`../01-arqueologia/legado-sifap/README.md`](legado-sifap/README.md)                               | 1 arquivo                   |

> Os documentos em `legacy-docs/` estão em português de propósito — são parte da imersão. Eles representam o que o cliente tinha guardado entre 1997 e 2018.

---

## Quem lê o quê (divisão obrigatória)

Cada par lidera 3 programas. **Nenhum programa pode ficar sem leitor.**

| Par                              | Programas                                      | Por que esses                                                                                                                  |
| -------------------------------- | ---------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------ |
| **1 · Visão** (PO + RE)          | `CADBENEF.NSN`, `CADDEPEND.NSN`, `CADPROG.NSN` | Cadastros são as **entidades** que viram subject das EARS. Se você não ler isso, não há REQ-ID para o resto.                   |
| **2 · Arquitetura** (EA + SA)    | `BATCHPGT.NSN`, `BATCHREL.NSN`, `BATCHCON.NSN` | Os batches revelam o **fluxo de negócio inteiro**. Bounded contexts saem daqui.                                                |
| **3 · Implementação** (TL + Dev) | `CALCBENF.NSN`, `CALCCORR.NSN`, `CALCDSCT.NSN` | Cálculos são o **núcleo financeiro**. Você vai reproduzi-los em Java no Estágio 3 — precisa saber exatamente o que eles fazem. |
| **4 · Qualidade** (DBA + QA)     | `VALBENEF.NSN`, `VALDOCS.NSN`, `VALELEG.NSN`   | Validações **viram testes**. Quem lê as regras de validação aqui está escrevendo a estratégia de testes do Estágio 3.          |
| **5 · Operação** (DevOps + TW)   | `CONSBENF.NSN`, `RELPGT.NSN`, `RELAUDIT.NSN`   | Consultas e relatórios revelam **o que o usuário vê** — informação que vai para o glossário e o runbook.                       |

> Os 4 DDMs ficam com o Par 4 (DBA + QA), com revisão dos demais. Cada DDM vira uma tabela PostgreSQL e a forma como você mapeia hoje define se haverá retrabalho no Estágio 3.

---

## Passo a passo (2 horas (1h + 30min) cronometradas)

### Bloco 1 — Reconhecimento (~30 min)

**O objetivo da primeira hora é apenas entender o terreno.** Você ainda não vai extrair regras, só vai criar o mapa.

1. **Todo o time, primeiros 15 minutos:** abra [`../01-arqueologia/legado-sifap/README.md`](legado-sifap/README.md) e leia o histórico do SIFAP. **Por que esse passo existe:** se você não sabe que o `RELPGT.NSN` é o relatório aceito pelo TCU desde 2003, você pode propor "modernizar o layout" e quebrar uma auditoria externa. Contexto evita decisão burra.

2. **Pares 1 e 5 em colaboração:** abra cada um dos 15 programas `.NSN` em modo leitura rápida (só os comentários e as constantes) e comece a popular [`glossary.md`](glossary.md). **Como pensar:** vocês não estão entendendo programas, estão **catalogando vocabulário**. Qualquer abreviação críptica (`DSCT`, `BENF`, `PE`, `MU`, `CTC`) é entrada de glossário. Meta: 30+ termos ao fim do dia.

3. **Par 2:** comece a desenhar o [`dependency-map.md`](dependency-map.md). Use o Copilot Chat com o prompt: _"Liste todas as ocorrências de CALLNAT nestes 15 arquivos e desenhe um diagrama Mermaid."_ Você vai descobrir, por exemplo, que `BATCHPGT` chama `VALELEG`, `CALCBENF`, `CALCCORR` e `CALCDSCT`. Esse grafo é a base dos bounded contexts do Estágio 2.

4. **Par 4 com Par 3 como revisor:** abra os 4 DDMs em [`../01-arqueologia/legado-sifap/adabas-ddms/`](legado-sifap/adabas-ddms/). Para cada DDM, liste todos os campos com tipo e tamanho, marcando `MU` (multi-valor) e `PE` (grupo periódico). **Por que isso importa:** esses dois construtos não existem em PostgreSQL relacional puro. Onde você ver `MU TELEFONES`, sabe que vai virar uma tabela `beneficiary_phone` no Estágio 3.

> **Ao fim da Hora 1**, faça um stand-up de 2 minutos: cada par diz em uma frase o que descobriu. Se um par está perdido, esse é o momento de pedir ajuda.

### Bloco 2 — Extração (~30 min)

**Agora vocês entram em modo extração.** Cada par lê profundamente seus 3 programas e documenta regras de negócio.

5. **Cada par em paralelo, 3 programas cada:** abra os arquivos `.NSN` da sua lista (ver tabela acima) e procure regras de negócio. **O que conta como regra de negócio:**

- Um `IF` que decide algo no domínio (ex.: _"se a UF é do Nordeste e o programa é Seca, valor base × 1.2"_)
- Uma constante numérica sem explicação (ex.: `0.075` no cálculo de imposto)
- Uma transição de status com regra (ex.: _"só de A para S, nunca de I para A"_)
- Um tratamento especial para um caso (ex.: _"se o CPF começa com 999, é teste"_)

**O que NÃO é regra de negócio:** paginação de relatório, formatação de saída, manipulação de cursor Adabas, abertura de arquivo. Ignore.

6. **Prompts úteis para Copilot Chat** (cole o conteúdo do `.NSN` no chat e use cada prompt depois):

- _"Explique este programa Natural linha por linha. Foque em decisões de negócio, ignore IO."_
- _"Quais regras de negócio este código implementa? Liste cada uma com a faixa de linhas."_
- _"Existe alguma constante numérica sem explicação? Para cada uma, sugira o que ela representa."_
- _"Há alguma condição que parece um workaround ou caso especial não documentado?"_
- _"Compare este programa com `outro-arquivo.NSN`. Existe lógica duplicada?"_

7. **Documentação imediata.** Toda regra encontrada vira **uma linha** em [`business-rules-catalog.md`](business-rules-catalog.md) com:

- `BR-NNN` (numeração sequencial)
- Descrição da regra em uma frase
- **`Programa Fonte` preenchido** com `01-arqueologia/legado-sifap/natural-programs/ARQUIVO.NSN#L<início>-L<fim>` (formato preferido) ou no mínimo o nome do arquivo
- Campos DDM envolvidos
- Nível de risco (CRÍTICO / ALTO / MÉDIO / BAIXO)

> [!WARNING]
> **Linhas com `Programa Fonte` vazio são inválidas e derrubam o time na rubrica.** Sem exceção.

### Bloco 3 — Síntese (~30 min)

**Agora vocês transformam descobertas em artefatos consolidados.** O par que liderou cada artefato finaliza; os outros revisam.

8. **Par 5 (Tech Writer lidera):** consolide o glossário. Cada termo precisa de definição em uma frase. Termos vindos do legado citam o programa onde aparecem. Meta: 30+ termos.

9. **Par 2 (Enterprise Architect lidera):** finalize o `dependency-map.md`. O diagrama Mermaid deve cobrir os **15 programas**, sem órfãos. Use cores diferentes para batch, online e relatórios.

10. **Par 1 (Requirements Engineer lidera):** consolide `business-rules-catalog.md`. Junte as regras dos 5 pares, deduplique, categorize. Confirme que **100% das linhas têm `Programa Fonte`**.

11. **Par 1 (Product Owner lidera):** priorize as regras em `discovery-report.md`. Quais 5–8 são as **essenciais** que o protótipo do Estágio 3 precisa preservar? Decisão difícil mas necessária — você não vai conseguir migrar tudo em 2 horas de código.

12. **Par 4 (QA Engineer lidera):** consolide `mysteries-found.md`. Liste pelo menos 5 mistérios com:

- O mistério (uma frase)
- Onde está (arquivo + faixa de linhas)
- Por que importa (o que quebra se a regra não for preservada)

---

## Exemplo concreto — do legado à linha do catálogo

Para fixar o padrão, veja um exemplo real. Suponha que ao ler `CALCDSCT.NSN` você encontre:

```natural
* CHECK DEDUCTION CAP
IF #TIPO-DSCT NE 'J'
 IF #VLR-TOTAL-DSCT > (#VLR-BRUTO * 0.30)
 COMPUTE #VLR-TOTAL-DSCT = #VLR-BRUTO * 0.30
 END-IF
END-IF
```

A regra que está escondida aqui é: **descontos têm teto de 30% do valor bruto, exceto descontos judiciais (tipo 'J'), que não têm teto.** Isso vai virar duas coisas:

**Uma linha em `business-rules-catalog.md`:**

| ID     | Regra                                                                                   | Programa Fonte                                   | Campos DDM                                                               | Nível de Risco | Notas                                                                   |
| ------ | --------------------------------------------------------------------------------------- | ------------------------------------------------ | ------------------------------------------------------------------------ | -------------- | ----------------------------------------------------------------------- |
| BR-013 | Desconto total não pode exceder 30% do valor bruto, exceto descontos judiciais (tipo J) | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L142-L148` | `PAGAMENTO.VLR-BRUTO`, `PAGAMENTO.VLR-TOTAL-DSCT`, `PAGAMENTO.TIPO-DSCT` | CRÍTICO        | Regra financeira, viola limite cria prejuízo. Tipo 'J' = exceção legal. |

**Uma futura REQ-ID no Estágio 2** (já no formato com `source_legacy`):

```yaml
REQ-PAY-013:
 pattern: state-driven
 text: "Enquanto o tipo de desconto NÃO for 'J' (judicial), o SIFAP deve limitar
 o desconto total a 30% do valor bruto do pagamento."
 source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L142-L148
 acceptance:
 - "Dado pagamento bruto R$ 1000 e descontos solicitados R$ 400 tipo I, o desconto aplicado é R$ 300."
 - "Dado pagamento bruto R$ 1000 e descontos R$ 400 tipo J, o desconto aplicado é R$ 400."
```

Repare na **rastreabilidade**: você lê uma linha do catálogo, vai direto para a linha exata do programa Natural, e do programa Natural à REQ-ID. Ninguém precisa adivinhar de onde a regra veio. Esse é o produto da arqueologia bem feita.

---

## Caça aos mistérios (pontuação extra)

Tem **10 regras de negócio escondidas**, **3 easter eggs** e **4 inconsistências** plantadas pela facilitadora no código. Veja a [`mysteries-checklist.md`](mysteries-checklist.md) — é a lista do que procurar (sem entregar a resposta).

**Cota mínima para passar:** 5 mistérios documentados em `mysteries-found.md` com arquivo + linha + impacto. Quem encontra mais que 8 ganha pontos na dimensão A1 da rubrica (até 32 pontos possíveis).

Se sua dupla travar depois de 90 minutos sem achar nenhum mistério, levante a mão — um facilitador (cordão azul) pode dar uma dica calibrada.

---

<details>
<summary><strong>Armadilhas comuns</strong> — clique para expandir</summary>

| ❌ Se você está fazendo isso                                   | ✅ Faça assim                                                                              |
| -------------------------------------------------------------- | ------------------------------------------------------------------------------------------ |
| Lendo os programas em ordem alfabética sem dividir entre pares | Cada par fica com 3 programas conforme a tabela. 15 ÷ 5 = 3.                               |
| Tentando entender cada `MOVE` e cada `READ LOGICAL`            | Foca só em `IF`, constantes, transições de status. Ignora IO.                              |
| Documentando regra sem citar arquivo e linha                   | Toda linha do catálogo precisa de `Programa Fonte`. Vazio = inválido.                      |
| Ignorando os 3 documentos antigos em `legacy-docs/`            | Eles estão desatualizados, mas mostram o que o time original _pensava_ na época. Vale ler. |
| Deixando para a Hora 3 começar a escrever no catálogo          | Documente conforme descobre. Memória de curto prazo trai.                                  |
| Considerando "regra de negócio" qualquer linha de código       | Regra de negócio = decisão do **domínio**, não da implementação.                           |

</details>

---

## Como saber que terminou (Definição de Pronto)

Antes do facilitador chegar às 13h50, sua dupla deve poder marcar **todas** essas caixas:

- [ ] [`glossary.md`](glossary.md) com 30+ termos, cada um com definição em 1 frase.
- [ ] [`business-rules-catalog.md`](business-rules-catalog.md) com ≥ 15 regras, **100% com `Programa Fonte` preenchido**.
- [ ] [`dependency-map.md`](dependency-map.md) com diagrama Mermaid cobrindo os 15 programas, sem órfãos.
- [ ] [`mysteries-found.md`](mysteries-found.md) com ≥ 5 mistérios, cada um com arquivo + linha + impacto.
- [ ] [`discovery-report.md`](discovery-report.md) totalmente preenchido, sem placeholders.

Se houver vermelho em qualquer item, o time **não abre o Estágio 2**. Não é punição — é proteção. Especificações sem base no legado quebram no Estágio 3.

---

## Próximo passo

Quando o facilitador validar o checklist às 13h50, o **Par 2 (Arquitetura)** lidera o Estágio 2 com os artefatos do Estágio 1 como entrada. O **Par 1 (Visão)** acompanha o sign-off de escopo. Os demais pares continuam contribuindo conforme [`02-spec-moderna/GUIDE.md`](../02-spec-moderna/GUIDE.md).

O `business-rules-catalog.md` que vocês produziram agora **é** o input do `SPECIFICATION.md`. Cada regra vira (ou não, com justificativa) uma REQ-ID. Sem o catálogo bem feito, o Estágio 2 vira chute.

---

## Referência rápida

```
Travou? → regra dos 20 minutos (00-TEAM-FLOW.md §6)
Não sei qual programa abrir? → tabela "Quem lê o quê" desta página
Catálogo válido? → toda linha precisa de Programa Fonte
EARS sem source_legacy? → CI bloqueia PR + rubrica derruba para Precário
Cheat-sheet do Copilot Chat? → 09-cheat-sheets/copilot-3-modes.md
Modelo certo (Haiku/Sonnet)? → 09-cheat-sheets/model-routing.md
```

---

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="../05-personas/OVERVIEW.md"><strong>OVERVIEW das 10 personas</strong></a><br/>
<sub>Tabela comparativa: par, líder de estágio, defaults de emergência.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../02-spec-moderna/GUIDE.md"><strong>Estágio 2 — Spec Moderna</strong></a><br/>
<sub>14:00–15:00 · Escrever EARS, ADRs e diagramas C4.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>

— Paula
