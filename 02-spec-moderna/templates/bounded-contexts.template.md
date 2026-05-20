<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
title: "Modelo: Bounded Contexts"
description: "Esqueleto para definições de bounded contexts via /carve-bounded-contexts"
author: "Paula Silva, AI-Native Software Engineer, Americas Global Black Belt at Microsoft"
date: "2026-04-29"
version: "1.0.0"
status: "approved"
tags: ["template", "bounded-contexts", "architect", "stage-2"]
---

<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

<!-- Como usar: execute /carve-bounded-contexts. Clone o bloco de contexto para cada um. -->

# Mapa de Bounded Contexts

![TEMPLATE bounded-contexts](https://img.shields.io/badge/TEMPLATE-bounded-contexts-737373?style=for-the-badge) ![COPIE Não edite o original](https://img.shields.io/badge/COPIE-Não%20edite%20o%20original-1A1A1A?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../../README.md) → [Estágio 2](../README.md) → **Templates** → **bounded-contexts**

> 📋 **Este arquivo é um TEMPLATE.** Copie para o seu repositório de time e preencha com os dados do seu time. Não edite o original.


## Avaliações de Hipóteses

### <!-- placeholder: Nome --> — <!-- placeholder: ACEITO/REJEITADO -->

| Critério              | Avaliação            | Evidência            |
| --------------------- | -------------------- | -------------------- |
| Coesão                | <!-- placeholder --> | <!-- placeholder --> |
| Acoplamento           | <!-- placeholder --> | <!-- placeholder --> |
| Frequência de mudança | <!-- placeholder --> | <!-- placeholder --> |

## Bounded Contexts Finais

### <!-- placeholder: Nome do Contexto -->

- **Responsabilidade:** <!-- placeholder -->
- **Dados sob ownership:** <!-- placeholder -->
- **Interface pública:** <!-- placeholder -->
- **Por que é seu próprio contexto:** <!-- placeholder -->

## Comunicação Entre Contextos

| De  | Para | Mecanismo | Dados |
| --- | ---- | --------- | ----- |

```mermaid
flowchart LR
    CTX1["Contexto 1"] -->|"chama"| CTX2["Contexto 2"]
    classDef ctx fill:#0f172a,stroke:#334155,color:#e2e8f0
    class CTX1,CTX2 ctx
```

---

**Lembrete de Definição de Pronto:** Hipóteses avaliadas, rejeições documentadas, 2-5 contextos nomeados, Mermaid renderiza.


---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="../GUIDE.md"><strong>GUIDE do Estágio 2</strong></a><br/>
<sub>Passo a passo.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="ADR.template.md"><strong>ADR Template</strong></a><br/>
<sub>Template de ADR.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../../README.md">Voltar ao Kit PT-BR</a></sub>

