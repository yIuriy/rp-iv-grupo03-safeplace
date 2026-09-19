# Issue #114: [66] [MVP][Áreas e Tarefas] Completar APIs de atualização e classificação

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/114
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:21:37Z
- **Updated at:** 2026-09-15T01:02:50Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF05/RF06; RNF03; UC03/UC11; US05/US06.

**Referência do modelo:** Operações de AreaRisco, Setor e Tarefa mapeadas para portas.

## O que entregar

Reutilizar /api/areas-risco e /api/tarefas e completar atualização de agentes/limites e classificação com autorização e histórico.

## Critérios de aceite

- [ ] Gestor cadastra/atualiza/classifica; Supervisor consulta.
- [ ] Consulta/detalhe/filtros devolvem vínculos reais aprovados.
- [ ] Atualizar área sem EPI obrigatório ou com código duplicado falha sem perda de dados.
- [ ] Reclassificação preserva data/hora/operador; consulta reflete novo estado.
- [ ] Contrato OpenAPI corresponde à API existente e às extensões, sem criar /api/areas-de-risco em paralelo.

## Dependências técnicas

- [#113](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/113)

## Decisões pendentes e limites

Contrato de #111/#112 orienta campos; não incluir inspeções ou vínculo tarefa–EPI.

## Motivo da correção

Base já existe; issue deve completar lacunas, não reconstruir com nomes diferentes.

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

## 1. Contexto e Objetivo
Expor via API REST os endpoints para mapeamento de áreas de risco e classificação de periculosidade das tarefas, com controle de permissões por perfil (**RF05, RF06, RNF03**).

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 6 - APIs de Empréstimo, Áreas e Domínio de Capacitações
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#92: [MVP][Segurança] Implementar Autenticação JWT e Filtro de Autorização RBAC)
  - [?] (#113: [MVP][Áreas e Tarefas] Implementar Persistência JPA e Migração Flyway para Áreas e Tarefas)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#115: [MVP][Frontend] Construir Mapa de Áreas de Risco e Painel de Tarefas no Frontend)
  - [?] (#119: [MVP][Capacitações] Implementar Endpoints REST para Certificações, Treinamentos e Validação de Bloqueio)

## 3. O Que Fazer
1. Criar `AreaDeRiscoController`:
   - `POST /api/areas-de-risco`: cadastra nova área (acesso restrito ao Gestor de Segurança).
   - `GET /api/areas-de-risco`: consulta mapa global de áreas de risco com filtros por grau de perigo.
   - `GET /api/areas-de-risco/{id}`: detalhes de setor e EPIs obrigatórios para acesso.
   - `PUT /api/areas-de-risco/{id}`: atualiza limites e agentes de risco.
2. Criar `TarefaController`:
   - `POST /api/tarefas`: cadastra e classifica tarefa.
   - `GET /api/tarefas`: lista tarefas cadastradas.
   - `GET /api/tarefas/{id}`: detalhes da tarefa e periculosidade.
3. Criar DTOs de entrada e saída com validações Jakarta e documentação Swagger OpenAPI.

## 4. Como Fazer (Arquitetura Hexagonal)
- Injeção das portas de entrada nos controllers.
- Anotações de segurança `@PreAuthorize` aplicando regras de RBAC por perfil.


## 5. Exigência de Testes Automatizados (Critério Obrigatório para Nota Máxima)
Para garantir pontuação máxima na avaliação semanal do professor, o desenvolvedor deve entregar:
- **Testes de Componentes React**: Testes automatizados com Vitest e React Testing Library cobrindo renderização, submissão de formulários, estados de carregamento (loading), mensagens de erro e feedback visual.
- **Validação de Acessibilidade (WCAG 2.1 AA)**: Verificação de contraste de cores, labels acessíveis em formulários e navegação por teclado.
- **Resiliência Offline (RNF08)**: Teste comprovando a consulta de dados a partir do cache local quando a rede estiver indisponível.


## 6. Governança do Modelo Orientado a Cronograma (Schedule-Driven Development)
Como o projeto adota o **Modelo Orientado a Cronograma**, o prazo semanal (timebox) é **fixo e inegociável**:
- **Timebox Fixo**: A entrega desta issue deve ser concluída impreterivelmente até o final da semana correspondente para avaliação do professor.
- **Estratégia Anti-Bloqueio (In-Memory Stubs)**: Caso uma issue predecessora atrase, o desenvolvedor **NÃO deve ficar bloqueado**. Deve criar uma implementação falsa em memória (In-Memory Fake/Mock) da Porta de Saída correspondente, garantindo que seu Caso de Uso ou Interface funcione e seja testado autonomamente.
- **Regra de Estabilidade de Integração (Trunk Stability)**: Nenhuma alteração pode quebrar a branch principal (`main` / `dev`). Ao final do timebox, a entrega deve ser submetida via Pull Request com testes verdes e build passando.
- **Diretriz de Redução de Escopo (Scope Pruning)**: Se houver risco iminente de estourar o timebox semanal, recursos secundários (como estilos visuais avançados ou filtros secundários) devem ser temporariamente postergados, priorizando o núcleo de domínio, os testes unitários e a API funcional.

## 7. Critérios de Aceite para Nota Máxima (Final do Timebox)
- [ ] Endpoints protegidos por JWT e documentados.
- [ ] Gestor pode cadastrar áreas; supervisores podem consultar.
- [ ] Validações de requisição rejeitam payloads sem EPIs de acesso obrigatório.
- [ ] Testes de integração de endpoints (MockMvc) cobrindo fluxos principais e permissões.
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

