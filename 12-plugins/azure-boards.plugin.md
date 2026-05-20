<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Plugin do Azure Boards

![PLUGIN azure-boards](https://img.shields.io/badge/PLUGIN-azure-boards-1A1A1A?style=for-the-badge) ![OPCIONAL Estágio 4](https://img.shields.io/badge/OPCIONAL-Estágio%204-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Plugins](README.md) → **azure-boards**

> **Para quem é isto?** Para o time que quer usar este plugin específico.
>
> **O que você terá ao final desta leitura:**
>
> 1. O que o plugin faz
> 2. Como configurar
> 3. Exemplos de uso


## O que ele faz

Sincroniza itens de trabalho SDD com o Azure DevOps Boards. Cria Epics,
Features, User Stories e Tasks com rastreabilidade por REQ-ID.

## Use este plugin quando

- Sincronizar de TASKS.md para um projeto ADO com os itens de trabalho do workshop
- Criar a hierarquia Epic -> Feature -> Story alinhada a SPECIFICATION.md
- Manter critérios de aceitação sincronizados entre markdown e ADO

## Ferramentas fornecidas

### `create_work_item`

Cria um único Epic/Feature/User Story/Task com campos mapeados a partir do
frontmatter markdown.

### `sync_tasks_file`

Analisa TASKS.md e cria ou atualiza itens de trabalho; itens existentes são
correspondidos pelo REQ-ID na lista de tags.

### `link_hierarchy`

Cria links pai-filho entre Epic/Feature/Story/Task a partir da estrutura do markdown.

### `push_acceptance_criteria`

Copia critérios de aceitação de SPECIFICATION.md REQ-NNN para o campo
Critérios de aceite do item de trabalho correspondente.

### `comment_on_change`

Publica um comentário no item de trabalho resumindo um commit git que referencia
seu REQ-ID.

## Configuração

### Configuração obrigatória

- `organization`: nome da organização ADO.
- `project`: nome do projeto ADO.
- `pat`: Personal Access Token com escopo `Work Items (Read & Write)` (env var
  `AZURE_DEVOPS_PAT`).
- `area_path`: caminho de área padrão para itens criados.

### Configuração opcional

- `iteration_path`: iteração de sprint padrão.
- `type_map`: substitui o mapeamento de nível de heading markdown para tipo de
  item de trabalho. Padrão:
  - `# Epic` -> Epic
  - `## Feature` -> Feature
  - `### Story` -> User Story
  - `- [ ] Task` -> Task
- `dry_run`: pré-visualização sem gravar (padrão `true`).

## Padrão de uso

```text
@copilot using azure-boards plugin, sync TASKS.md to <target>.
```

O Copilot invoca o plugin, o plugin lê a fonte da verdade em markdown e aplica
as mudanças ao sistema de destino.

## Segurança

- Sempre leia credenciais de variáveis de ambiente. Nunca coloque um PAT inline
  em um prompt ou arquivo de configuração commitado no repo.
- Mantenha `dry_run` como `true` por padrão. Só defina como `false` depois de
  pré-visualizar a saída.
- Toda operação registra a trilha REQ-ID em que atuou, para que você possa
  correlacionar mudanças com o histórico da especificação.

## Antipadrões

- Usar o plugin como fonte da verdade. A fonte da verdade vive em markdown sob
  `specs/`; o plugin é um mecanismo de sincronização.
- Executar com `dry_run: false` antes de revisar a pré-visualização.
- Commitar PATs no repo.

## Gate de qualidade

Toda sincronização deve preservar a rastreabilidade por REQ-ID. Se um sistema de
destino não puder armazenar o REQ-ID (rastreador legado), o plugin se recusa a
executar.

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="README.md"><strong>Plugins</strong></a><br/>
<sub>Índice de plugins.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="github-issues.plugin.md"><strong>github-issues</strong></a><br/>
<sub>Plugin de GitHub Issues.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="README.md">Voltar ao Kit PT-BR</a></sub>

