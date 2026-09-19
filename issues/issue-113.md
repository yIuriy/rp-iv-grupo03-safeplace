# Issue #113: [61] [MVP][Áreas e Tarefas] Completar persistência de relações e histórico

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/113
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:21:35Z
- **Updated at:** 2026-09-15T01:02:48Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF05/RF06; RNF05; UC03/UC11; US05/US06.

**Referência do modelo:** Setor, AreaRisco, Tarefa e NivelPerigo segundo #111/#112.

## O que entregar

Evoluir persistência existente de áreas/tarefas para refletir contratos aprovados, atualização, relações e histórico.

## Critérios de aceite

- [ ] Não recriar tabelas/classes já entregues em V6/V7; migrações usam próxima versão livre.
- [ ] Consulta após gravação preserva setor/área, EPIs de acesso e classificação aprovados.
- [ ] Código único e vínculos existentes são validados no banco.
- [ ] Alteração de limites e reclassificação preservam histórico e operador.
- [ ] Testes PostgreSQL cobrem salvar, atualizar, filtrar e reconstruir, além de rollback.

## Dependências técnicas

- [#111](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/111)
- [#112](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/112)

## Decisões pendentes e limites

Auditoria integra #121; decisões dos dois modelos precedem respectivas migrações.

## Motivo da correção

Retirados nomes de tabela/rota alternativos e campos não aprovados.

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
Criar as tabelas relacionais e adaptadores de persistência JPA para Áreas de Risco e Tarefas operacionais no PostgreSQL.

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 5 - Rastreabilidade, Substituição e Persistência de Áreas
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#111: [MVP][Áreas de Risco] Modelar Domínio e Portas para Mapeamento de Áreas de Risco)
  - [?] (#112: [MVP][Tarefas] Modelar Domínio e Classificação de Periculosidade de Tarefas)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#114: [MVP][Áreas e Tarefas] Implementar Endpoints REST para Áreas de Risco e Classificação de Tarefas)
  - [?] (#115: [MVP][Frontend] Construir Mapa de Áreas de Risco e Painel de Tarefas no Frontend)

## 3. O Que Fazer
1. Criar migração Flyway `V8__create_areas_e_tarefas.sql`:
   - Tabela `areas_de_risco` (`id`, `nome`, `codigo_setor` UNIQUE, `descricao`, `grau_perigo`, `agentes_risco`, `criado_em`).
   - Tabela associativa `area_risco_epis_obrigatorios` (`area_risco_id`, `epi_id`).
   - Tabela `tarefas` (`id`, `titulo`, `descricao`, `area_risco_id`, `nivel_periculosidade`, `exige_treinamento`, `treinamento_exigido`, `criado_em`).
2. Criar entidades JPA `AreaDeRiscoEntity` e `TarefaEntity` em `adapters.out.persistencia`.
3. Implementar `AreaDeRiscoJpaAdapter` e `TarefaJpaAdapter`:
   - Salvar, buscar por ID, listar com filtros de periculosidade.
   - Mapeamento bidirecional estrito com os modelos de domínio.

## 4. Como Fazer
- Mapeamento JPA com coleções gerenciadas adequadamente.
- Testes de integração de repositório cobrindo persistência com chaves estrangeiras.


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
- [ ] Migração Flyway aplicada com integridade referencial.
- [ ] Vínculo de EPIs obrigatórios de acesso à área persistido corretamente.
- [ ] Consultas de tarefas e áreas por nível de periculosidade funcionais.
- [ ] Testes de repositório passando sem falhas.
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

