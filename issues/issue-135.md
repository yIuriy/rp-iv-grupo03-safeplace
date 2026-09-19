# Issue #135: [69] [Backlog][Tarefas] Associar EPIs obrigatórios conforme RF12 e UC08

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/135
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T02:12:02Z
- **Updated at:** 2026-09-15T01:03:44Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** Backlog; fora da implementação do MVP.

**Requisitos e histórias:** RF12 — Deveria ter; UC08; US12.

**Referência do modelo:** Tarefa.associarEPIs e verificarEPIsObrigatoriosTarefa; associação Tarefa–EPI.

## O que entregar

Preservar vínculo entre tarefa e equipamento como funcionalidade futura, separado dos EPIs exigidos para acesso à área em UC03.

## Critérios de aceite

- [ ] Modelo futuro e operações seguem Tarefa/EPI, sem novo TipoEpi obrigatório.
- [ ] Regras do UC08 são rastreadas e conflitos de parametrização são decididos antes de codificar.
- [ ] Não deduzir matriz normativa de exemplos nem impor regra de altura como requisito já aprovado.
- [ ] RF06/classificação e RF10/capacitações do MVP não ficam bloqueados por esta entrega.

## Dependências técnicas

Nenhuma dependência técnica obrigatória adicional para iniciar a análise/correção descrita. Observar as decisões e dependências condicionais abaixo.

## Decisões pendentes e limites

Fora do MVP; quando priorizada depende de tarefas e EPIs aprovados.

## Motivo da correção

Removido rótulo MVP e regras de risco/altura inventadas. A ausência de vínculo no código atual é lacuna de backlog.

## Fontes verificáveis

- [Diagrama de classes](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/diagramas/classes/Diagrama%20de%20Classes%20-%20SafePlace.png).
- [Requisitos funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-funcionais.md) e [não funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-nao-funcionais.md).
- [MoSCoW](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/priorizacao-moscow.md) e [MVP, incluindo pendências da seção 11](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/mvp/especificacao-mvp-arquitetura.md).
- [Casos de uso](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/casos-de-uso/casos-de-uso.md) e [histórias de usuário](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/historias-de-usuario.md).
- [Base técnica consultada](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/backend/src/main/java/br/edu/safeplace/backend/domain/tarefa/Tarefa.java). Evidência do estado existente, não fonte de requisito.

## Regras de consistência desta revisão

- O modelo aprovado orienta a implementação. Campos e operações em conflito aguardam decisão registrada; código existente não aprova uma regra de negócio.
- Preservar identidade das classes, atributos, associações e semântica das operações. Adaptação para portas/adaptadores deve ter correspondência explícita; diferenças apenas de nomes técnicos não justificam reconstrução do sistema.
- A MoSCoW define o recorte. RF21 e RNF08 continuam obrigatórios mesmo com decisões pendentes. Dicionários marcados como proposta ou "A confirmar" não são requisitos aprovados.
- Reutilizar o que existe e completar o fluxo integrado; teste isolado ou repositório em memória não comprova persistência/entrega funcional. Verificar testes pertinentes, integração com PostgreSQL/API e interface quando aplicável, declarando falhas, skips e limitações.
- Migrações novas usam versão livre e preservam dados existentes; não reescrever migrações aplicadas nem inventar dados históricos ausentes.
- Esta edição corrige escopo e critérios. Não altera responsáveis, comentários, estado ou datas do projeto e não promete nota, prazo ou percentual de cobertura sem fonte acadêmica. O cronograma anterior é preservado no histórico; não foi revalidado como critério técnico nesta revisão.

<details>
<summary>Texto anterior preservado como histórico — não usar como escopo vigente</summary>

## 1. Contexto e Requisitos Atendidos
Atendimento ao **RF06** (Classificar o nível de periculosidade da tarefa), **RF12** (Conectar tarefas e EPIs) e **UC11**:
- Cada tarefa operacional cadastrada possui um grau de periculosidade específico (Grau 1 a 4) e riscos associados (altura, eletricidade, produtos químicos, ruído intenso).
- Para assegurar a integridade do trabalhador, o sistema deve impor a vinculação obrigatória de tipos de EPI indispensáveis para a execução de cada tarefa, impedindo que um colaborador seja alocado ou execute uma tarefa de risco sem a devida lista de proteção exigida.

Esta entrega constrói o serviço de domínio e o caso de uso de vinculação e validação entre Tarefas Operacionais e Equipamentos de Proteção Individual Obrigatórios.

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 6 - APIs de Empréstimo, Capacitações e Conformidade
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#130: Domínio de Catálogo de EPI)
  - [?] (#112: Domínio e Classificação de Periculosidade de Tarefas)
  - [?] (#113: Persistência JPA de Áreas e Tarefas)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#115: Mapa de Áreas de Risco e Painel de Tarefas no Frontend)
  - [?] (#126: Auditoria e Consulta de Aptidão de Colaborador por Tarefa)

## 3. Entregáveis Técnicos e Arquiteturais (Arquitetura Hexagonal)
- **Serviço de Domínio (`domain.tarefa.service`)**:
  - `VincularEpisObrigatoriosService`: Serviço que aplica a matriz de exigência normativa entre o nível de risco da tarefa e os tipos de EPIs mínimos requeridos (ex.: tarefas em altura acima de 2m exigem obrigatoriamente cinto paraquedista e trava-quedas).
  - Invariante de integridade: Nenhuma tarefa com periculosidade Alta ou Crítica pode ser salva ou ativada no catálogo de tarefas ativas sem conter ao menos um EPI de proteção mandatória associado.
- **Portas e Adaptador**:
  - `VincularEpisTarefaUseCase`: Porta de entrada com contrato `vincularEpisObrigatorios(TarefaId, List<TipoEpi>)`.
  - Endpoint REST em `TarefaRestController`: `PUT /api/tarefas/{id}/epis-obrigatorios` com validação de perfil de Supervisor/Gestor e DTO tipado.

## 4. Testes Automatizados Obrigatórios
- **Testes Unitários e MockMvc (`VincularEpisObrigatoriosServiceTest`, `TarefaControllerTest`)**:
  - Testar validação que rejeita ativação de tarefa crítica sem EPIs obrigatórios cadastrados.
  - Testar associação bem-sucedida de lista de tipos de EPI a uma tarefa existente.
  - Testar endpoint REST retornando HTTP 200 OK com payload atualizado da tarefa.
  - Testar retorno HTTP 400 Bad Request ao tentar enviar lista vazia para tarefas de risco elevado.

## 5. Artefatos Demonstráveis para Avaliação
- Serviço de domínio com regras de negócio claras e assertivas ligando Tarefas a EPIs normativos.
- Endpoint documentado no Swagger permitindo consultar e atualizar a matriz de EPIs por tarefa.
- Testes automatizados demonstrando a consistência das regras de segurança ocupacional.

## 6. Governança do Modelo Orientado a Cronograma (Schedule-Driven Development)
Como o projeto adota o **Modelo Orientado a Cronograma**, o prazo semanal (timebox) é **fixo e inegociável**:
- **Timebox Fixo**: A entrega desta issue deve ser concluída impreterivelmente até o final da semana correspondente para avaliação do professor.
- **Estratégia Anti-Bloqueio (In-Memory Stubs)**: Caso a persistência JPA de Tarefas [61] esteja sofrendo refatoração, o serviço e seus testes devem rodar com um stub em memória de `TarefaRepositoryPort`.
- **Regra de Estabilidade de Integração (Trunk Stability)**: O PR deve passar na esteira CI sem quebrar endpoints existentes de tarefas ou EPIs.
- **Diretriz de Redução de Escopo (Scope Pruning)**: Se houver restrição temporal, validar apenas os tipos de EPIs primários (altura e eletricidade), deixando a parametrização de EPIs secundários opcionais para iterações futuras.

## 7. Critérios de Aceite para Nota Máxima (Final do Timebox)
- [ ] Regra de negócio impedindo ativação de tarefas de periculosidade elevada sem EPIs obrigatórios.
- [ ] Endpoint REST `/api/tarefas/{id}/epis-obrigatorios` operacional com validação de payload.
- [ ] Testes unitários e de integração cobrindo 100% dos caminhos felizes e de exceção da vinculação.
- [ ] PR submetido, com build verde e revisado dentro do timebox semanal da Semana 6.


</details>

