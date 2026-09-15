# Issue #132: [51] [MVP][Estoque] Completar API de catálogo, movimentos e histórico com permissões

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/132
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T02:11:55Z
- **Updated at:** 2026-09-15T01:03:39Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF03; RNF03; UC06; US03.

**Referência do modelo:** Buscar EPI, consultar histórico, registrar movimento e comparar mínimo.

## O que entregar

Reutilizar API de EPI existente e completar consulta de histórico, dados do modelo aprovado e controle de acesso por operação.

## Critérios de aceite

- [ ] Gestor registra entrada/saída e mantém catálogo; Supervisor não ganha gestão de estoque por autorização ampla de /api/epis.
- [ ] Disponibilidade para empréstimo ao Supervisor continua no fluxo UC12, com permissões próprias.
- [ ] Movimento registra operador autenticado; cliente não escolhe autoria.
- [ ] Quantidade zero/negativa, saldo insuficiente, inexistência e tipo inválido não alteram banco.
- [ ] Histórico consultável corresponde às movimentações persistidas; OpenAPI e testes descrevem contratos reais.

## Dependências técnicas

- [#131](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/131)

## Decisões pendentes e limites

Campos, localização, CA e lote dependem #124. Não exigir tamanho mínimo arbitrário de motivo nem CA estritamente futuro sem decisão.

## Motivo da correção

Corrigidas permissões Gestor/Supervisor e nomes inventados. Não confundir saída comum de estoque com descarte RF14.

## Fontes verificáveis

- [Diagrama de classes](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/diagramas/classes/Diagrama%20de%20Classes%20-%20SafePlace.png).
- [Requisitos funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-funcionais.md) e [não funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-nao-funcionais.md).
- [MoSCoW](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/priorizacao-moscow.md) e [MVP, incluindo pendências da seção 11](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/mvp/especificacao-mvp-arquitetura.md).
- [Casos de uso](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/casos-de-uso/casos-de-uso.md) e [histórias de usuário](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/historias-de-usuario.md).
- [Base técnica consultada](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/backend/src/main/java/br/edu/safeplace/backend/domain/epi/Epi.java). Evidência do estado existente, não fonte de requisito.

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
Atendimento ao **RF03** (Controlar o estoque dos EPIs), **UC06** e **RNF03** (Controle de acesso por perfil):
- Disponibilizar endpoints REST seguros e padronizados para consulta do catálogo de EPIs, cadastro de novos equipamentos (exclusivo para Gestores e Supervisores) e registro de movimentações de estoque (entradas para reposição e baixas operacionais).

Esta entrega cria o Adaptador de Entrada Web (REST Controller) com validação declarativa de payloads (Bean Validation), mapeamento de DTOs para Casos de Uso de domínio e tratamento global de erros.

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 3 - APIs Inbound, Persistência e Alertas
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#92: Autenticação JWT e Filtro de Autorização RBAC)
  - [?] (#130: Domínio, Invariantes e Portas de Catálogo e Estoque de EPI)
  - [?] (#131: Persistência JPA de Catálogo e Estoque)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#133: Interface Frontend de Catálogo e Estoque de EPIs)
  - [?] (#105: Cache Local e Suporte Offline para Catálogo e Estoque de EPIs)

## 3. Entregáveis Técnicos e Arquiteturais (Arquitetura Hexagonal)
- **Adaptador Inbound Web (`infrastructure.adapters.input.web.epi`)**:
  - `EpiRestController`: Endpoints RESTful mapeados em `/api/epis`:
    - `POST /api/epis`: Cadastrar novo EPI no catálogo (`hasAnyRole('GESTOR', 'SUPERVISOR')`).
    - `GET /api/epis`: Listar catálogo com suporte a paginação e filtro por categoria/status.
    - `GET /api/epis/{id}`: Obter detalhes completos do EPI e saldo atual.
    - `POST /api/epis/{id}/movimentacoes`: Registrar entrada ou baixa de estoque com motivo e quantidade.
- **DTOs com Validação Estrita**:
  - `CreateEpiRequest`: Validação de nome obrigatório, CA não vazio e validade futura (`@NotBlank`, `@Future`).
  - `MovimentacaoEstoqueRequest`: Quantidade estritamente positiva (`@Positive`), tipo de movimentação válido e justificativa (`@Size(min = 5)`).
- **Tratamento de Exceções**: Mapear `SaldoInsuficienteException` para HTTP 422 Unprocessable Entity e `EpiNaoEncontradoException` para HTTP 404.

## 4. Testes Automatizados Obrigatórios
- **Testes de Controladores (`@WebMvcTest` ou `MockMvc`)**:
  - Testar cadastro com sucesso retornando HTTP 201 Created com header Location.
  - Testar rejeição de entrada de estoque com payload inválido (HTTP 400 Bad Request).
  - Testar tentativa de baixa superior ao saldo disponível retornando HTTP 422 com payload de erro descritivo.
  - Testar bloqueio de acesso não autenticado (HTTP 401 Unauthorized) e sem permissão RBAC (HTTP 403 Forbidden).

## 5. Artefatos Demonstráveis para Avaliação
- Endpoints documentados e operacionais via Swagger UI / OpenAPI em `/swagger-ui.html`.
- Suíte completa de testes MockMvc cobrindo cenários de sucesso, erro de validação e segurança.
- DTOs limpos desacoplados das entidades de persistência JPA.

## 6. Governança do Modelo Orientado a Cronograma (Schedule-Driven Development)
Como o projeto adota o **Modelo Orientado a Cronograma**, o prazo semanal (timebox) é **fixo e inegociável**:
- **Timebox Fixo**: A entrega desta issue deve ser concluída impreterivelmente até o final da semana correspondente para avaliação do professor.
- **Estratégia Anti-Bloqueio (In-Memory Stubs)**: Se a persistência JPA [45] estiver em andamento, teste e demonstre os controllers utilizando um mock/stub da porta de entrada `GerenciarEstoqueUseCase`.
- **Regra de Estabilidade de Integração (Trunk Stability)**: O endpoint deve subir sem falhas e com contratos de API estáveis para consumo pelo frontend.
- **Diretriz de Redução de Escopo (Scope Pruning)**: Se houver restrição temporal, priorizar os endpoints de listagem, consulta e movimentação básica de estoque, postergando filtros secundários avançados.

## 7. Critérios de Aceite para Nota Máxima (Final do Timebox)
- [ ] Endpoints `/api/epis` e `/api/epis/{id}/movimentacoes` implementados com segurança RBAC.
- [ ] Bean Validation rejeitando entradas inválidas com mensagens de erro padronizadas.
- [ ] Testes MockMvc validando todos os status codes previstos (200, 201, 400, 403, 404, 422).
- [ ] PR submetido, com build verde e revisado dentro do timebox semanal da Semana 3.


</details>

