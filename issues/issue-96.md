# Issue #96: [41] [MVP][Ocorrências] Preservar atributos e vínculos ao salvar e consultar ocorrências

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/96
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** Rafaela Pacheco Nunes
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:20:55Z
- **Updated at:** 2026-09-15T01:01:57Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF01/RF02/RF13/RF16; UC05/UC09; US01/US02/US13/US16.

**Referência do modelo:** Ocorrencia.dataOcorrencia, dataRegistro, statusOcorrencia; herança Acidente/Incidente e associações aprovadas.

## O que entregar

Corrigir mapeamento e migrações incrementais para salvar e reconstruir ocorrência sem perda de estado, datas ou vínculos. Reutilizar hierarquia e tabelas existentes.

## Critérios de aceite

- [ ] Salvar, limpar contexto de persistência e consultar preserva dataRegistro e status; consulta não gera novo horário nem reinicia em ABERTA.
- [ ] Persistir relator autenticado, envolvidos e referências aprovadas em #95; não derivar área fictícia do texto local.
- [ ] Protocolo de acompanhamento aprovado permanece único sob concorrência; não usar count()+1.
- [ ] Migração usa próxima versão livre e preserva dados; não converter dataOcorrencia DateTime para DATE nem preencher passado com autoria/datas inventadas.
- [ ] Testes com PostgreSQL cobrem reconstrução, relações, atualização/arquivamento e ausência de exclusão física.

## Dependências técnicas

- [#95](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/95)

## Decisões pendentes e limites

Correção dos campos já definidos no PNG pode começar imediatamente; protocolo e associações novas aguardam decisões de #95/#111.

## Motivo da correção

Descrição anterior reutilizava V4, reduzia data do fato a DATE e pressupunha schema incompatível. Adaptador atual perde status e dataRegistro.

## Fontes verificáveis

- [Diagrama de classes](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/diagramas/classes/Diagrama%20de%20Classes%20-%20SafePlace.png).
- [Requisitos funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-funcionais.md) e [não funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-nao-funcionais.md).
- [MoSCoW](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/priorizacao-moscow.md) e [MVP, incluindo pendências da seção 11](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/mvp/especificacao-mvp-arquitetura.md).
- [Casos de uso](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/casos-de-uso/casos-de-uso.md) e [histórias de usuário](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/historias-de-usuario.md).
- [Base técnica consultada](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/backend/src/main/java/br/edu/safeplace/backend/adapters/out/persistencia/OcorrenciaJpaAdapter.java). Evidência do estado existente, não fonte de requisito.

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
Persistir com segurança e integridade as ocorrências (acidentes e incidentes), assegurando chaves estrangeiras com a tabela de usuários/colaboradores, índice único para o protocolo e campos auditáveis.

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 2 - APIs de Usuários e Persistência de Ocorrências
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#91: [MVP][Usuários] Implementar Geração Automática de Credenciais e Hash de Senha para Supervisor)
  - [?] (#95: [MVP][Ocorrências] Modelar Domínio e Invariantes para Acidentes, Incidentes e Protocolo Único)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#98: [MVP][Ocorrências] Implementar Persistência JPA e Migração Flyway para Planos de Ação)
  - [?] (#99: [MVP][Ocorrências] Implementar Endpoints REST para Ocorrências, Triagem e Planos de Ação)

## 3. O Que Fazer
1. Criar migração Flyway `V4__enhance_ocorrencias.sql`:
   - Adicionar coluna `protocolo VARCHAR(30) NOT NULL UNIQUE`.
   - Adicionar colunas `data_fato DATE NOT NULL` e `data_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP`.
   - Relacionar `usuario_relator_id` (FK para `usuarios.id`) e `colaborador_envolvido_id` (FK opcional para `usuarios.id`).
   - Mapear atributos específicos de Acidente e Incidente (usando estratégia de tabela única com discriminador ou tabelas unidas).
2. Atualizar as entidades JPA em `adapters.out.persistencia`:
   - `OcorrenciaEntity`, `AcidenteEntity`, `IncidenteEntity`.
3. Implementar `OcorrenciaJpaAdapter` cumprindo `OcorrenciaRepositoryPort`:
   - Métodos para salvar, buscar por ID, buscar por protocolo e listar com paginação/filtros.
   - Mapeamento bidirecional rigoroso entre entidades JPA e domínio puro.

## 4. Como Fazer (Arquitetura Hexagonal)
- O modelo relacional (`OcorrenciaEntity`) jamais vaza para o domínio ou controladores.
- Garantir índices de performance para consultas por protocolo e data.


## 5. Exigência de Testes Automatizados (Critério Obrigatório para Nota Máxima)
Para garantir pontuação máxima na avaliação semanal do professor, o desenvolvedor deve entregar:
- **Testes Unitários de Domínio**: Cobertura das regras e invariantes usando JUnit 5 e AssertJ, sem mocks de banco ou contexto Spring.
- **Testes de Integração / API**: Validação com Spring `MockMvc` ou `DataJpaTest` cobrindo status HTTP (200, 201, 400, 403, 404, 422), integridade transacional e constraints do PostgreSQL.
- **Documentação Swagger**: Endpoints testados e validados no OpenAPI (`/swagger-ui/index.html`).


## 6. Governança do Modelo Orientado a Cronograma (Schedule-Driven Development)
Como o projeto adota o **Modelo Orientado a Cronograma**, o prazo semanal (timebox) é **fixo e inegociável**:
- **Timebox Fixo**: A entrega desta issue deve ser concluída impreterivelmente até o final da semana correspondente para avaliação do professor.
- **Estratégia Anti-Bloqueio (In-Memory Stubs)**: Caso uma issue predecessora atrase, o desenvolvedor **NÃO deve ficar bloqueado**. Deve criar uma implementação falsa em memória (In-Memory Fake/Mock) da Porta de Saída correspondente, garantindo que seu Caso de Uso ou Interface funcione e seja testado autonomamente.
- **Regra de Estabilidade de Integração (Trunk Stability)**: Nenhuma alteração pode quebrar a branch principal (`main` / `dev`). Ao final do timebox, a entrega deve ser submetida via Pull Request com testes verdes e build passando.
- **Diretriz de Redução de Escopo (Scope Pruning)**: Se houver risco iminente de estourar o timebox semanal, recursos secundários (como estilos visuais avançados ou filtros secundários) devem ser temporariamente postergados, priorizando o núcleo de domínio, os testes unitários e a API funcional.

## 7. Critérios de Aceite para Nota Máxima (Final do Timebox)
- [ ] Migração Flyway executada com sucesso sem quebrar dados legados.
- [ ] Protocolo único garantido no nível do banco via restrição `UNIQUE`.
- [ ] Adaptador JPA salva e recupera acidentes e incidentes convertendo perfeitamente para o domínio.
- [ ] Testes de integração de repositório validam inserção e busca por protocolo.
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

