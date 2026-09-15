# Issue #118: [72] [MVP][Capacitações] Persistir certificações e treinamentos conforme modelo

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/118
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:21:46Z
- **Updated at:** 2026-09-15T01:03:05Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF10; UC02; US10.

**Referência do modelo:** Certificacao e Treinamento associados a Colaborador.

## O que entregar

Persistir contratos aprovados em #116 e permitir consulta de histórico e vencimentos sem perder atributos do diagrama.

## Critérios de aceite

- [ ] Emissão/vencimento da certificação e conteúdo/instrutor/realização/carga horária de treinamento são reconstruídos corretamente.
- [ ] Vínculo com Colaborador não exige conta/senha.
- [ ] Consultas distinguem ausência, vencido e próximo de vencer conforme UC02.
- [ ] Migração incremental não fixa V9 nem adiciona validade genérica sem decisão.
- [ ] Testes PostgreSQL validam relações, datas e dados existentes; gravações auditáveis integram #121.

## Dependências técnicas

- [#116](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/116)

## Decisões pendentes e limites

Não depende de geração de senha #91. Escrita depende do fluxo de cadastro aprovado.

## Motivo da correção

Corrigida perda de atributos de Treinamento e campos extras tratados como requisitos.

## Fontes verificáveis

- [Diagrama de classes](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/diagramas/classes/Diagrama%20de%20Classes%20-%20SafePlace.png).
- [Requisitos funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-funcionais.md) e [não funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-nao-funcionais.md).
- [MoSCoW](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/priorizacao-moscow.md) e [MVP, incluindo pendências da seção 11](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/mvp/especificacao-mvp-arquitetura.md).
- [Casos de uso](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/casos-de-uso/casos-de-uso.md) e [histórias de usuário](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/historias-de-usuario.md).
- [Base técnica consultada](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/backend/src/main/java/br/edu/safeplace/backend/domain/capacitacao). Evidência do estado existente, não fonte de requisito.

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
Persistir certificações e treinamentos dos colaboradores no banco de dados relacional, permitindo consultas eficientes de vencimentos e bloqueios operacionais.

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 7 - Frontend de Áreas, Bloqueio e Capacitações
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#91: [MVP][Usuários] Implementar Geração Automática de Credenciais e Hash de Senha para Supervisor)
  - [?] (#116: [MVP][Capacitações] Modelar Domínio de Certificações, Treinamentos e Alerta de 30 Dias)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#119: [MVP][Capacitações] Implementar Endpoints REST para Certificações, Treinamentos e Validação de Bloqueio)

## 3. O Que Fazer
1. Criar migração Flyway `V9__create_capacitacoes.sql`:
   - Tabela `certificacoes` (`id`, `colaborador_id` FK, `nome_certificacao`, `entidade_emissora`, `data_emissao`, `data_validade`, `criado_em`).
   - Tabela `treinamentos` (`id`, `colaborador_id` FK, `nome_treinamento`, `carga_horaria`, `data_realizacao`, `data_validade`, `criado_em`).
2. Criar entidades JPA `CertificacaoEntity` e `TreinamentoEntity` em `adapters.out.persistencia`.
3. Implementar `CapacitacaoJpaAdapter` implementando `CapacitacaoRepositoryPort`:
   - Salvar certificação e treinamento.
   - Listar capacitações por colaborador.
   - Listar certificações com vencimento nos próximos 30 dias ou já vencidas.

## 4. Como Fazer
- Mapeamento bidirecional rigoroso entre JPA e Domínio.
- Criação de índices de busca por data de validade para otimização de consultas preventivas.


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
- [ ] Migração Flyway executada com sucesso.
- [ ] Repositório JPA realiza consultas cronológicas de vencimento com exatidão.
- [ ] Testes de integração de banco cobrindo persistência e listagem por colaborador.
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

