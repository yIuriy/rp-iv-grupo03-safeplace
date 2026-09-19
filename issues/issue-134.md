# Issue #134: [63] [Backlog][Inspeções] Alinhar histórico de inspeções ao RF09 e ao modelo

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/134
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T02:11:59Z
- **Updated at:** 2026-09-15T01:03:42Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** Backlog; fora da implementação do MVP.

**Requisitos e histórias:** RF09 — Deveria ter; US09.

**Referência do modelo:** Inspecao.dataInspecao e ItemVerificado.descricao/conformidade/observacoes; relações do PNG.

## O que entregar

Manter inspeções como evolução futura, com escopo baseado em RF09 e modelo, sem torná-las pré-condição de atualização de áreas do MVP.

## Critérios de aceite

- [ ] Registrar associação entre inspeção, área e itens verificados conforme modelo aprovado.
- [ ] Usar dados de Inspecao/ItemVerificado, sem substituí-los por InspecaoArea e NivelPerigoAmbiental inventados.
- [ ] Definir fluxo e papéis antes da implementação futura.
- [ ] Não impor LIBERADA/ALERTA/INTERDITADA, EXTREMO ou bloqueio automático sem requisito/decisão.
- [ ] Nenhuma issue MVP é bloqueada por esta entrega.

## Dependências técnicas

Nenhuma dependência técnica obrigatória adicional para iniciar a análise/correção descrita. Observar as decisões e dependências condicionais abaixo.

## Decisões pendentes e limites

Fora do MVP pela MoSCoW. Funcionalidade futura depende de detalhamento; não está pronta para execução automática.

## Motivo da correção

Reclassificada de RF05/UC03 para RF09; inspeção obrigatória antes de qualquer alteração de risco não possui fonte.

## Fontes verificáveis

- [Diagrama de classes](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/diagramas/classes/Diagrama%20de%20Classes%20-%20SafePlace.png).
- [Requisitos funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-funcionais.md) e [não funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-nao-funcionais.md).
- [MoSCoW](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/priorizacao-moscow.md) e [MVP, incluindo pendências da seção 11](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/mvp/especificacao-mvp-arquitetura.md).
- [Casos de uso](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/casos-de-uso/casos-de-uso.md) e [histórias de usuário](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/historias-de-usuario.md).
- [Base técnica consultada](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/backend/src/main/java/br/edu/safeplace/backend/domain/area_risco/AreaRisco.java). Evidência do estado existente, não fonte de requisito.

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
Atendimento ao **RF05** (Mapear as áreas de risco do ambiente de trabalho) e **UC03** (Cadastro, consulta e atualização de áreas de risco):
- O mapeamento de áreas de risco exige acompanhamento periódico das condições ambientais e do nível de perigo físico, químico, biológico e ergonômico.
- Cada alteração no grau de perigo de uma área deve ser precedida por uma inspeção registrada por Supervisor com data, apontamento técnico e parecer conclusivo.

Esta entrega estabelece o modelo de domínio puro para Avaliação e Inspeções Periódicas de Áreas de Risco, Value Objects, invariantes e as portas de entrada e saída.

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 5 - Cache Offline, Substituição Inteligente e Periculosidade
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#111: Domínio e Portas para Mapeamento de Áreas de Risco)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#114: Endpoints REST para Áreas de Risco e Classificação de Tarefas)
  - [?] (#125: Verificação de Conformidade de EPI para Acesso a Setores de Risco)
  - [?] (#136: Interface Frontend para Registro de Inspeções e Avaliação de Risco)

## 3. Entregáveis Técnicos e Arquiteturais (Arquitetura Hexagonal)
- **Entidades e Value Objects Puros (Pacote `domain.arearisco.inspecao`)**:
  - `InspecaoArea`: Entidade encapsulando identificador, ID da área inspecionada, supervisor responsável, data da inspeção, parecer técnico e status da área (`LIBERADA`, `ALERTA`, `INTERDITADA`).
  - `NivelPerigoAmbiental`: Value Object definindo o grau de severidade (`BAIXO`, `MEDIO`, `ALTO`, `EXTREMO`) com regras de agravamento de periculosidade.
  - Invariante de interdição: Caso o parecer aponte risco iminente de acidente grave, a área deve transicionar obrigatoriamente para `INTERDITADA`, bloqueando operações até nova inspeção liberatória.
- **Portas Hexagonais**:
  - `InspecaoRepositoryPort`: Porta de saída para registrar inspeção e consultar histórico cronológico de uma área.
  - `RegistrarInspecaoUseCase`: Porta de entrada para registrar laudo de inspeção e atualizar status da área.

## 4. Testes Automatizados Obrigatórios
- **Testes Unitários de Domínio (`InspecaoAreaTest`, `NivelPerigoAmbientalTest`)**:
  - Testar transição de status de área baseada no laudo da inspeção.
  - Testar invariante que impede registro de inspeção retroativa com datas inconsistentes.
  - Testar bloqueio automático de área ao detectar severidade de risco extremo.
  - Cobertura de 100% nas regras com JUnit 5 e AssertJ.

## 5. Artefatos Demonstráveis para Avaliação
- Classes de domínio puras desacopladas de qualquer anotação JPA ou Spring.
- Suíte completa de testes unitários garantindo que transições de estado ambientais respeitam invariantes rígidas.
- Interfaces de portas documentadas com contratos claros de entrada e saída.

## 6. Governança do Modelo Orientado a Cronograma (Schedule-Driven Development)
Como o projeto adota o **Modelo Orientado a Cronograma**, o prazo semanal (timebox) é **fixo e inegociável**:
- **Timebox Fixo**: A entrega desta issue deve ser concluída impreterivelmente até o final da semana correspondente para avaliação do professor.
- **Estratégia Anti-Bloqueio (In-Memory Stubs)**: Esta modelagem depende conceitualmente apenas do agregado de Área de Risco [55]; caso haja ajustes na interface, utilize a identificação `AreaRiscoId` via Value Object desacoplado.
- **Regra de Estabilidade de Integração (Trunk Stability)**: O PR deve conter apenas código puro de domínio e testes unitários verdes, sem dependências externas.
- **Diretriz de Redução de Escopo (Scope Pruning)**: Se houver limitação de tempo, foque na entidade de inspeção e na transição de status da área, simplificando os tipos detalhados de agentes nocivos.

## 7. Critérios de Aceite para Nota Máxima (Final do Timebox)
- [ ] Entidade `InspecaoArea` e Value Object `NivelPerigoAmbiental` implementados em POJOs puros.
- [ ] Invariantes de interdição e atualização de risco rigorosamente testadas com JUnit 5.
- [ ] Portas `InspecaoRepositoryPort` e `RegistrarInspecaoUseCase` definidas com contratos de tipos explícitos.
- [ ] PR submetido, com build verde e revisado dentro do timebox semanal da Semana 5.


</details>

