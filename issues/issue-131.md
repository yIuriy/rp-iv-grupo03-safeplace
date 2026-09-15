# Issue #131: [45] [MVP][Estoque] Corrigir persistência e concorrência de saldo e histórico

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/131
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T02:11:53Z
- **Updated at:** 2026-09-15T01:03:37Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF03; RNF05; UC06; US03.

**Referência do modelo:** EPI e MovimentacaoEstoque; vínculos com lote/modelo conforme #124.

## O que entregar

Completar o adaptador existente para preservar dados e gravar saldo e movimentação de forma coerente, inclusive sob concorrência.

## Critérios de aceite

- [ ] Saldo e histórico confirmam/falham na mesma transação; duas saídas não causam atualização perdida.
- [ ] Teste concorrente em PostgreSQL comprova correspondência entre saldo inicial, entradas/saídas confirmadas e saldo final.
- [ ] Data/hora, quantidade, motivo e operador autenticado são persistidos; lote segue contrato aprovado.
- [ ] Busca por CA suporta cardinalidade aprovada de modelos/lotes; não presumir CA único por EPI.
- [ ] Migrações evoluem epis e movimentações existentes, preservando V1–V7; não criar tb_epi paralela.

## Dependências técnicas

Nenhuma dependência técnica obrigatória adicional para iniciar a análise/correção descrita. Observar as decisões e dependências condicionais abaixo.

## Decisões pendentes e limites

Correções transacionais e de histórico podem começar. Estrutura EPI/lote/CA depende #124; auditoria usa #121.

## Motivo da correção

Não substituir PostgreSQL por H2 como única prova de integridade; retiradas tabelas e namespaces novos sem necessidade.

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
Atendimento ao **RF03** (Controlar o estoque dos EPIs) e ao **UC06**:
- O sistema SafePlace necessita de persistência relacional com integridade transacional ACID para controle do saldo físico de EPIs e registro cronológico auditável de movimentações de estoque (entradas, saídas e ajustes).

Esta entrega materializa o Adaptador de Saída JPA para o domínio de Catálogo e Estoque de EPIs, além da migração Flyway correspondente, garantindo isolamento da infraestrutura de banco de dados em relação ao núcleo de domínio.

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 2 - Persistência Inicial e Fluxos Centrais
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#130: Domínio, Invariantes e Portas de Catálogo e Estoque de EPI)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#123: Alertas Automáticos de Estoque Crítico e Reposição)
  - [?] (#132: Controladores Inbound REST e DTOs de Catálogo e Estoque)
  - [?] (#133: Interface Frontend de Catálogo e Estoque de EPIs)

## 3. Entregáveis Técnicos e Arquiteturais (Arquitetura Hexagonal)
- **Migração de Banco de Dados Flyway**:
  - Script SQL de migração criando as tabelas `tb_epi` (id, nome, ca_numero, ca_validade, quantidade_estoque, estoque_minimo, categoria) e `tb_movimentacao_estoque` (id, epi_id, tipo_movimentacao, quantidade, motivo, data_hora, usuario_id).
  - Constraints relacionais, integridade referencial e índices em `ca_numero` e `epi_id`.
- **Adaptador de Persistência JPA (Pacote `infrastructure.adapters.output.persistence`)**:
  - `EpiEntity` e `MovimentacaoEstoqueEntity` com anotações JPA/Hibernate.
  - `SpringDataEpiRepository` estendendo `JpaRepository`.
  - `EpiPersistenceAdapter` implementando a porta `EpiRepositoryPort`, realizando mapeamento bidirecional seguro (Mapper) entre entidades de banco e agregados de domínio puro.

## 4. Testes Automatizados Obrigatórios
- **Testes de Integração com Banco (`@DataJpaTest`)**:
  - Testar persistência, busca por ID, consulta por número de CA e listagem de EPIs com banco em memória H2 ou Testcontainers.
  - Testar atualização atômica de saldo e persistência em cascata de movimentações de estoque.
  - Testar integridade referencial e violação de constraints únicas.

## 5. Artefatos Demonstráveis para Avaliação
- Script de migração Flyway aplicado com sucesso na inicialização do backend.
- Adaptador JPA desacoplado implementando rigorosamente a interface da porta de domínio.
- Suíte de testes `@DataJpaTest` validando transacionalidade e mapeamento sem vazamento de ORM para o domínio.

## 6. Governança do Modelo Orientado a Cronograma (Schedule-Driven Development)
Como o projeto adota o **Modelo Orientado a Cronograma**, o prazo semanal (timebox) é **fixo e inegociável**:
- **Timebox Fixo**: A entrega desta issue deve ser concluída impreterivelmente até o final da semana correspondente para avaliação do professor.
- **Estratégia Anti-Bloqueio (In-Memory Stubs)**: Caso a issue predecessora de domínio [39] sofra algum ajuste, utilize o contrato da porta `EpiRepositoryPort` já especificado e implemente o adapter contra esse contrato.
- **Regra de Estabilidade de Integração (Trunk Stability)**: A branch deve ser integrada via PR com migração Flyway idempotente e testes verdes.
- **Diretriz de Redução de Escopo (Scope Pruning)**: Caso o tempo se esgote, priorizar o CRUD básico do EPI e o registro de entrada/saída direta, postergando queries analíticas complexas de movimentações passadas.

## 7. Critérios de Aceite para Nota Máxima (Final do Timebox)
- [ ] Tabelas `tb_epi` e `tb_movimentacao_estoque` criadas com sucesso via migração Flyway.
- [ ] Adaptador `EpiPersistenceAdapter` implementando `EpiRepositoryPort` com mapeamento limpo.
- [ ] Testes de integração `@DataJpaTest` cobrindo operações CRUD e atualização de saldo com 100% de sucesso.
- [ ] PR submetido, com build verde e revisado dentro do timebox semanal da Semana 2.


</details>

