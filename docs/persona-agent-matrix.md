<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Matriz Persona × Agente

![DOC Persona × Agente](https://img.shields.io/badge/DOC-Persona%20×%20Agente-00A4EF?style=for-the-badge) ![USE Quem faz o quê](https://img.shields.io/badge/USE-Quem%20faz%20o%20quê-1A1A1A?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Docs](README.md) → **Persona-Agent Matrix**

> **Para quem é isto?** Documentação transversal usada durante o workshop.
>
> **O que você terá ao final desta leitura:** contexto adicional sobre o tópico do título.


Esta matriz mapeia cada persona para cada agente, mostrando quem faz o que em cada estágio do workshop. Use-a para entender seu papel em cada momento do dia.

**Como ler este documento:**

1. Encontre a linha da sua persona
2. Leia horizontalmente para ver seu nível de intensidade em cada estágio
3. Para estágios em que você é **PROTAGONISTA** ou **Secundária**, leia as orientações detalhadas abaixo
4. Abra o README do kit de agente do estágio atual para ver o fluxo completo

## A Matriz

| #   | Persona               | @archaeologist  | @architect      | @builder        | @evolution      |
| --- | --------------------- | --------------- | --------------- | --------------- | --------------- |
| 01  | Product Owner         | Observadora        | Secundária       | Observadora        | Secundária       |
| 02  | Requirements Engineer | **PROTAGONISTA** | Secundária       | Observadora        | Observadora        |
| 03  | Enterprise Architect  | Secundária       | Secundária       | Observadora        | Observadora        |
| 04  | Software Architect    | Observadora        | **PROTAGONISTA** | Secundária       | Observadora        |
| 05  | Technical Lead        | Observadora        | Secundária       | Secundária       | **PROTAGONISTA** |
| 06  | Developer             | Observadora        | Observadora        | **PROTAGONISTA** | Secundária       |
| 07  | DBA                   | Secundária       | Observadora        | Secundária       | Observadora        |
| 08  | QA Engineer           | Observadora        | Observadora        | Secundária       | Secundária       |
| 09  | DevOps Engineer       | Observadora        | Observadora        | Secundária       | Secundária       |
| 10  | Tech Writer           | Secundária       | Observadora        | Observadora        | Secundária       |

**PROTAGONISTA** = Conduz o uso do agente; é responsável pelas entregas do estágio.
**Secundária** = Contribui ativamente; trabalha em par com a pessoa protagonista.
**Observadora** = Acompanha pelo chat; pronta para ajudar quando sua especialidade for necessária.

## Orientação por Célula

### Estágio 1 — @archaeologist

| Persona                                 | O que você faz                                                                                                                                                                                            |
| --------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Requirements Engineer** (PROTAGONISTA) | Conduz a exploração. Abra cada programa Natural, peça ao agente para ajudar a decodificá-lo e capture regras de negócio como requisitos em rascunho. Você é responsável pelo rascunho das regras de negócio. |
| Tech Writer (Secundária)                 | Construa o glossário de domínio em tempo real. Toda vez que a equipe encontrar um termo — nome de variável, rótulo de campo, propósito de sub-rotina — adicione-o ao glossário com uma definição.         |
| Enterprise Architect (Secundária)        | Foque no panorama geral: quais sistemas externos o código legado chama? Quais entradas batch vêm de onde? Comece a rascunhar o contexto do sistema.                                                       |
| DBA (Secundária)                         | Foque nos DDMs. Documente tipos de campo, descriptors, estruturas MU/PE e relações entre arquivos. Isso se torna seu mapa de dados.                                                                       |
| Product Owner (Observadora)                | Escute e valide. Quando a equipe propuser uma interpretação de regra de negócio, confirme ou questione com base em seu entendimento de domínio.                                                           |
| Todos os demais (Observadora)              | Acompanhem o chat. Quando alguém perguntar sobre um padrão na sua área de especialidade (por exemplo, Developer vê um cálculo que reconhece), manifeste-se.                                               |

### Estágio 2 — @architect

| Persona                              | O que você faz                                                                                                                                                                                                                      |
| ------------------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Software Architect** (PROTAGONISTA) | Lidera a definição de bounded contexts. Use o data map e o call graph do Estágio 1 para identificar limites naturais. Desenhe diagramas C4. Escreva os primeiros ADRs.                                                                |
| Requirements Engineer (Secundária)    | Transforme as regras de negócio do Estágio 1 em requisitos EARS formais com IDs `REQ-NNN`. Cada requisito precisa de critérios de aceite. Trabalhe com Software Architect para garantir que os requisitos mapeiem para bounded contexts. |
| Enterprise Architect (Secundária)     | Valide o diagrama de contexto do sistema. Garanta que pontos de integração (batch feeds, APIs externas, autenticação) estejam capturados. Revise ADRs quanto à consistência arquitetural.                                            |
| Product Owner (Secundária)            | Priorize requisitos. Com tempo limitado, a equipe não consegue implementar tudo. Ajude a decidir quais requisitos são obrigatórios versus desejáveis.                                                                                 |
| Technical Lead (Observadora)            | Comece a pensar na ordem de implementação. Qual bounded context a equipe deve construir primeiro? Quais são as dependências?                                                                                                        |
| Todos os demais (Observadora)           | Revisem a especificação emergente. Sinalizem qualquer coisa que pareça incompleta ou inconsistente da perspectiva de vocês.                                                                                                         |

### Estágio 3 — @builder

| Persona                        | O que você faz                                                                                                                                                                        |
| ------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Developer** (PROTAGONISTA)    | Escreve código. Use o agente builder para gerar entidades JPA, serviços Spring, controllers REST e páginas Next.js. Todo trecho de código rastreia para um `REQ-NNN`.                  |
| DBA (Secundária)                | É responsável pela camada de banco de dados. Revise mapeamentos de entidade, escreva migrações Flyway, valide se o schema PostgreSQL representa corretamente o data model do Estágio 2. |
| QA Engineer (Secundária)        | Escreva testes junto com Developer. Para cada serviço, produza pelo menos um teste happy-path e um error-path. Monitore cobertura e sinalize lacunas.                                 |
| Technical Lead (Secundária)     | Revise o código à medida que ele é produzido. Verifique violações de padrão (sem campos `@Autowired`, sem retornos `null`, sem `any` em TypeScript). Faça merge de PRs.               |
| Software Architect (Secundária) | Valide se a implementação corresponde ao design. Se Developer estiver desviando dos limites dos bounded contexts, sinalize cedo.                                                      |
| Todos os demais (Observadora)     | Disponíveis para perguntas. Developer pode precisar de esclarecimento de domínio que apenas Product Owner ou Requirements Engineer consegue fornecer.                                 |

### Estágio 4 — @evolution

| Persona                          | O que você faz                                                                                                                                                                 |
| -------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **Technical Lead** (PROTAGONISTA) | Escreva GitHub Issues para o Copilot Agent executar. Revise PRs gerados por IA. Decida o que fazer merge e o que rejeitar. É responsável por integração e prontidão para demo. |
| DevOps Engineer (Secundária)      | Escreva o fluxo de trabalho do GitHub Actions e os módulos Terraform. Garanta tags adequadas, gerenciamento de secrets e configuração de recursos.                              |
| QA Engineer (Secundária)          | Valide se o pipeline de CI inclui todos os gates de qualidade: lint, build, test. Revise resultados de testes de PRs gerados por IA.                                           |
| Developer (Secundária)            | Revise código gerado por IA quanto à corretude. Você conhece melhor a codebase — detecte erros lógicos que checks automatizados podem não pegar.                               |
| Tech Writer (Secundária)          | Refine o README, documente o roteiro de demo e garanta que as notas da retrospectiva capturem os aprendizados da equipe.                                                       |
| Product Owner (Secundária)        | Ajude a priorizar o que precisa funcionar para a demo versus o que pode ser adiado. Prepare a narrativa para a apresentação de 3 minutos.                                     |
| Todos os demais (Observadora)       | Contribuam com observações de retrospectiva. O que surpreendeu vocês? O que fariam de forma diferente?                                                                         |

## Sugestão de Ordem de Leitura

1. Leia o `PERSONA.md` do seu papel em [`05-personas/`](../05-personas/) — entenda pelo que você é responsável
2. Leia sua **linha nesta matriz** — entenda sua intensidade em cada estágio
3. Quando um novo estágio começar, abra o **README do agent kit** desse estágio em [`06-agentes-de-estagio/`](../06-agentes-de-estagio/)
4. Ative o **agent** do estágio atual e comece a trabalhar

## Referências

- Kits de agentes: [`06-agentes-de-estagio/`](../06-agentes-de-estagio/README.md)
- Arquitetura dos agentes: [`docs/4-agents-explained.md`](4-agents-explained.md)
- Persona kits consolidados: [`05-personas/`](../05-personas/)

---

| Anterior                     | Início                             | Próximo                                       |
| ---------------------------- | ---------------------------------- | --------------------------------------------- |
| [Início dos Docs](README.md) | [Início do Team Kit](../README.md) | [4 Agentes Explicados](4-agents-explained.md) |


---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="4-agents-explained.md"><strong>4 agents explained</strong></a><br/>
<sub>Os 4 agentes detalhados.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="sdlc-flow-guide.md"><strong>SDLC flow</strong></a><br/>
<sub>Fluxo do SDLC.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>

