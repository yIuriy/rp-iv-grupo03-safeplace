# Issue #121: [78] [MVP][Auditoria] Registrar alterações com autoria e retenção mínima de cinco anos

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/121
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:21:53Z
- **Updated at:** 2026-09-15T01:03:11Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RNF05; UC03/UC05/UC06/UC07/UC09/UC11/UC12.

**Referência do modelo:** LogAuditoria: tipoOperacao,dataHora,responsavel,justificativa; operações aprovadas mapeadas para porta de saída.

## O que entregar

Implementar capacidade de auditoria desde as primeiras mutações e integrá-la progressivamente a todas as operações de criação/edição/exclusão permitidas no MVP.

## Critérios de aceite

- [ ] Registro identifica operador autenticado, operação, entidade/dado alterado e data/hora; justificativa quando exigida.
- [ ] Histórico imutável tem proteção verificável e política documentada de retenção mínima de cinco anos.
- [ ] Falha de negócio não produz registro enganoso de sucesso; consistência com mutação é testada.
- [ ] Cobrir usuários, ocorrências/arquivamento, planos, estoque, manutenção, empréstimos, áreas/tarefas e capacitações à medida que integrarem.
- [ ] Não armazenar senha/token em payload de auditoria; não criar exclusão de ocorrência para testar log.
- [ ] Fornecer teste de alteração seguida de consulta de evidência; homologação global fica em #129.

## Dependências técnicas

Nenhuma dependência técnica obrigatória adicional para iniciar a análise/correção descrita. Observar as decisões e dependências condicionais abaixo.

## Decisões pendentes e limites

Capacidade básica pode começar com autenticação já existente; integração completa acompanha issues de mutação. Não aguarda todas as telas ou offline.

## Motivo da correção

Removida dependência circular de todas as issues; AOP, IP e JSON são escolhas técnicas, não exigências do PNG.

## Fontes verificáveis

- [Diagrama de classes](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/diagramas/classes/Diagrama%20de%20Classes%20-%20SafePlace.png).
- [Requisitos funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-funcionais.md) e [não funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-nao-funcionais.md).
- [MoSCoW](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/priorizacao-moscow.md) e [MVP, incluindo pendências da seção 11](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/mvp/especificacao-mvp-arquitetura.md).
- [Casos de uso](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/casos-de-uso/casos-de-uso.md) e [histórias de usuário](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/historias-de-usuario.md).
- [Base técnica consultada](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/backend/src/main/java/br/edu/safeplace/backend/domain/auditoria). Evidência do estado existente, não fonte de requisito.

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
Para cumprir o **RNF05 (Rastreabilidade de ações / Auditoria)** e fechar a entrega do MVP:
- Todas as operações que alteram dados no sistema (criação, edição e exclusão de usuários, ocorrências, EPIs, movimentações, áreas de risco, tarefas e capacitações) devem gerar registros de log de auditoria imutáveis.
- O registro deve identificar: usuário executor autenticado, timestamp exato, operação executada, entidade afetada e payload/dado alterado, com política de retenção mínima de 5 anos.
- Esta issue realiza a implementação transversal no backend e executa a verificação completa e integrada de todos os fluxos do MVP (Backend, Frontend e Suporte Offline RNF08).

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 8 - Frontend Final, Auditoria e Homologação
- **Dependências diretas (Pré-requisitos)**:
  - [34] a [?] (#90 a #120, #122 a #127)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#129: [MVP][Transversal] Validar Integridade End-to-End, Documentação Técnica e Homologação do MVP)

## 3. O Que Fazer
1. Backend: Implementar infraestrutura de auditoria imutável:
   - Migração Flyway `V10__create_auditoria.sql`: tabela `logs_auditoria` com `id`, `usuario_id`, `usuario_email`, `acao`, `entidade`, `entidade_id`, `detalhes_json`, `ip_origem`, `criado_em`.
   - Criar interceptor / aspecto Spring AOP (`@AuditarOperacao` ou `AuditListener`) para registrar automaticamente todas as mutações nos controladores ou casos de uso.
   - Garantir imutabilidade no banco (proibir `UPDATE` e `DELETE` na tabela de auditoria via regra de banco ou aplicação).
2. Verificação Integrada End-to-End:
   - Validar testes unitários e de integração de todos os módulos (`./mvnw test`).
   - Validar testes e linters do frontend (`npm run lint`, `npm test`, `npm run build`).
   - Verificar funcionamento do cache offline local (**RNF08**) nos navegadores suportados.
   - Atualizar documentação técnica OpenAPI e guias de execução (**RNF15**).

## 4. Como Fazer
- Implementação limpa via Spring Aspect ou JPA Entity Listeners, desacoplada das entidades de domínio.
- Proteger a tabela de auditoria contra qualquer manipulação posterior.


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
- [ ] Operações de escrita em todos os módulos geram registro de auditoria imutável com identificação do usuário e timestamp.
- [ ] Suite completa de testes automatizados do backend e frontend executando com 100% de aprovação.
- [ ] Consulta offline validada no frontend para colaboradores, EPIs e áreas de risco.
- [ ] Sistema pronto para demonstração acadêmica e homologação do MVP.
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

