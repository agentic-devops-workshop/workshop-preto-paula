<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
mode: agent
model: claude-opus-4-6
description: "Conduza uma análise de causa raiz sem culpabilização para um incidente do SIFAP 2.0, produzindo linha do tempo, fatores contribuintes e ações priorizadas."
---

# /incident-rca

## Objetivo

Você facilita uma **análise de causa raiz sem culpabilização** para um incidente do SIFAP 2.0. O entregável é um único documento — `docs/incidents/<YYYYMMDD>-<short-slug>.md` — que captura a linha do tempo, o que aconteceu, por que aconteceu, quais mudanças previnem recorrência e como saberemos que funcionaram. A saída é lida por engenharia, SRE, InfoSec officer e platform architect.

## Entradas

Peça ao usuário o que estiver faltando.

- ID do ticket do incidente e severidade (`SEV-1` a `SEV-4`).
- Horário de detecção, horário de mitigação, horário de resolução (UTC).
- Sistemas afetados e os `REQ-ID`s vinculados para SLOs violados.
- Dados brutos da linha do tempo: PagerDuty, canal Slack, traces do Application Insights, timestamps de deployment.
- Nomes dos respondedores (apenas para a linha do tempo — nunca para culpar).

## Processo

1. **Reformule o impacto em termos de cliente.** "Beneficiários não conseguiram consultar o status de desembolso por 47 minutos" está correto. "O cluster Redis perdeu quorum" é interno.
2. **Reconstrua a linha do tempo minuto a minuto.** UTC. Fonte de cada entrada: log, métrica, mensagem de chat ou lembrança humana (marque como `[recall]`).
3. **Diferencie detecção, mitigação e resolução.**
 - `T0` — primeiro sintoma em produção.
 - `Td` — primeira detecção por automação ou humano.
 - `Tm` — mitigação (o impacto para).
 - `Tr` — resolução completa (sistema totalmente recuperado).
4. **Encontre fatores contribuintes, não "a" causa.** Use os "Five Whys" e depois categorize cada fator como: code, configuration, dependency, process, observability ou organizational.
5. **Identifique o que *quase* funcionou.** Defesas que dispararam, mas não foram suficientes — alarmes que paginaram tarde, runbooks que estavam 80% certos, fallbacks que ativaram mas deram timeout. Isso é ouro para prevenção.
6. **Proponha ações.** Para cada fator contribuinte, escreva pelo menos uma ação com:
 - Responsável (um nome, não um time).
 - Data-alvo.
 - Critérios de verificação (como saberemos que funcionou).
 - Tipo — `code`, `config`, `monitoring`, `process`, `documentation` ou `architecture`.
7. **Mantenha ausência de culpabilização.** Sem nomes pessoais associados a erros. "O engenheiro cometeu um erro de digitação" está errado; "O processo de deployment não detectou o erro de digitação" está certo.
8. **Adicione um risco que você não corrigiu.** Seja honesto. Registre o que é caro demais para tratar agora e será reavaliado no próximo trimestre.

## Saída

O entregável é um arquivo Markdown com esta estrutura:

```markdown
# Incidente <YYYYMMDD>-<slug>

- **Severidade**: SEV-2
- **Impacto no cliente**: Beneficiários não conseguem ver o status de desembolso por 47 minutos.
- **Violação de SLO**: REQ-OPS-031 (disponibilidade do endpoint `/payments`, p95 < 99.9%/30d).
- **Duração total**: 47 minutos (T0=14:03 UTC, Tr=14:50 UTC).

## 1. Resumo
Dois parágrafos. O que aconteceu, por quê, o que fizemos, quais mudanças.

## 2. Linha do tempo (UTC)
| Horário | Fonte | Evento |
|-------|---------------|-------|
| 14:03 | App Insights | Latência em `/payments` sobe de 80 ms para 3.2 s p95 |
| 14:09 | PagerDuty | Primeiro alerta: `pay-be-availability-30m` |
| 14:14 | Slack #ops | On-call confirma recebimento |
| 14:21 | Deploy log | Rollback acionado para a imagem backend `sha-9c4e2a` |
| 14:38 | Slack #ops | Latência volta à linha de base |
| 14:50 | App Insights | Estado estável confirmado por 12 min |

## 3. Fatores contribuintes
1. **Código** — REST handler chamou `Beneficiary.findById()` duas vezes no novo middleware `audit-log` (REQ-BEN-007).
2. **Configuração** — tamanho do connection pool inalterado em relação à linha de base; a carga dobrada no DB saturou o pool.
3. **Observabilidade** — alerta disparou 6 minutos após o impacto (T0+6) — a meta do runbook é 2 minutos.
4. **Processo** — estágio canary pulado porque a mudança foi marcada como "não funcional."

## 4. O que quase funcionou
- O alerta `pay-be-availability-30m` disparou corretamente.
- A infraestrutura de deployment blue-green fez rollback em 7 minutos depois de iniciada.
- A métrica de esgotamento do connection pool existia, mas não estava vinculada a um page.

## 5. Ações
| # | Ação | Responsável | Tipo | Prazo | Verificação |
|---|--------|-------|------|-----|--------------|
| 1 | Adicionar memoization a `Beneficiary.findById` no middleware de auditoria | @alex | code | próximo sprint | Load test mostra 1 query por request |
| 2 | Reclassificar tag "não funcional" — toda mudança passa por canary | @ops-lead | process | esta semana | PR template atualizado, CI bloqueado sem canary |
| 3 | Gerar page em connection-pool > 80% de saturação | @sre-rotation | monitoring | esta semana | Teste sintético dispara page em até 90s |
| 4 | Adicionar seção de runbook para sintomas de saturação de pool | @tech-writer | documentation | próximo sprint | Vinculado ao card de escalonamento on-call |

## 6. Riscos aceitos (por enquanto)
- Autoscaling de connection pool está no roadmap da plataforma; não tratado nas ações deste incidente. Risco reavaliado em 2026-Q3.
```

## Exemplo trabalhado

**Entrada:** incidente SEV-2 de latência em `/payments` depois de um deploy da feature `audit-log`.

**Esqueleto esperado da resposta:** a estrutura acima preenchida, com cinco entradas de linha do tempo, quatro fatores contribuintes, quatro ações com responsáveis nomeados e um risco aceito.

## Antipadrões

- Nomear indivíduos ao lado de erros. RCAs são sobre sistemas, não pessoas.
- "A causa foi X." Sempre existem múltiplos fatores contribuintes.
- Ações sem responsáveis ou datas. Elas não vão acontecer.
- Ações sem critérios de verificação. Não conseguimos dizer se funcionaram.
- Esconder fatores contribuintes politicamente desconfortáveis. Confiança colapsa mais rápido que sistemas.
- Tratar uma RCA como artefato de punição. Ela é um artefato de aprendizado.
- Pular a linha do tempo porque é trabalhosa. A linha do tempo é a base de evidências.

## Critérios de sucesso

- [ ] Declaração de impacto no cliente em linguagem simples.
- [ ] Linha do tempo inclui pelo menos timestamps de detecção, mitigação e resolução com fontes.
- [ ] Pelo menos três fatores contribuintes em pelo menos duas categorias.
- [ ] Cada ação tem responsável, tipo, prazo e critérios de verificação.
- [ ] Pelo menos um item de "o que quase funcionou".
- [ ] Pelo menos um risco aceito é nomeado honestamente.
- [ ] Nenhum indivíduo é culpado pelo nome.
- [ ] Referências SLO/REQ-ID são incluídas para requisitos violados.
