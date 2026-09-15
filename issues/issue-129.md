# Issue #129: [80] [MVP][Entrega] Verificar rastreabilidade e evidências dos requisitos Deve ter

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/129
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:37:03Z
- **Updated at:** 2026-09-15T01:03:33Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RNF15; RFs/RNFs Deve ter na MoSCoW.

**Referência do modelo:** Modelo aprovado, operações dos diagramas e sua implementação verificável.

## O que entregar

Homologar entrega por requisito e cenário, com evidências reais de integração e documentação técnica atualizada.

## Critérios de aceite

- [ ] Matriz cobre RF01/02/03/04/05/06/07/10/11/13/16/21/23 e RNF03/05/08/11/15; pendências não são marcadas como concluídas.
- [ ] Cada fluxo aponta UC/US, modelo aprovado, API/tela e evidência; stubs e testes isolados são identificados.
- [ ] Verificar ciclo completo com PostgreSQL, migrations, autenticação, dados persistidos, histórico e UI.
- [ ] Relatar testes executados, falhas, skips e limitações; não presumir 40 fluxos nem nota máxima.
- [ ] Reconciliar documentação arquitetural, modelos/PNG/Astah e contratos; histórico Python/CultiPampa não define domínio SafePlace.
- [ ] MVP não depende de inspeções RF09, vínculo tarefa–EPI RF12, exportação RF18 ou CAT RF19.

## Dependências técnicas

- [#94](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/94)
- [#100](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/100)
- [#104](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/104)
- [#110](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/110)
- [#115](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/115)
- [#120](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/120)
- [#121](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/121)
- [#123](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/123)
- [#125](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/125)
- [#126](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/126)
- [#127](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/127)
- [#128](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/128)
- [#133](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/133)
- [#137](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/137)

## Decisões pendentes e limites

Homologação final depende das fatias MVP e suas decisões. Pode coletar evidências incrementalmente; não há aprovação automática por todos os testes existentes passarem.

## Motivo da correção

Retirada quantidade arbitrária de fluxos e dependência universal por números de ordenação. Atualizar evidência sem impor prazo acadêmico não confirmado.

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

## 1. Contexto e Objetivo
Fechamento do ciclo de desenvolvimento do MVP do SafePlace:
- Verificação end-to-end de todos os 40 fluxos implementados nas 8 semanas.
- Garantia de conformidade dos contratos OpenAPI/Swagger com os requisitos funcionais (**RNF15**).
- Verificação de execução limpa de testes automatizados de backend (`./mvnw test`), frontend (`npm test`, `npm run lint`) e containers Docker.

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 8 - Frontend Final, Auditoria e Homologação
- **Dependências diretas (Pré-requisitos)**:
  - Todas as issues anteriores ([34] a [72]).
- **Issues dependentes**: Nenhuma (Conclusão definitiva do MVP SafePlace).

## 3. O Que Fazer
1. Executar e documentar bateria completa de testes automatizados:
   - Testes de Domínio, Aplicação e Persistência JPA no Spring Boot.
   - Testes de componentes e acessibilidade WCAG no React.
2. Validar documentação OpenAPI versionada em `/docs` e `/v3/api-docs`.
3. Validar inicialização conjunta com Docker Compose (`docker compose up`).
4. Elaborar relatório final de conformidade do MVP relacionando requisitos `Deve ter` aos endpoints e telas entregues.

## 4. Critérios de Aceite
- [ ] 100% dos testes automatizados de backend e frontend passando sem erros.
- [ ] Docker Compose inicializa backend, frontend e banco PostgreSQL perfeitamente integrados.
- [ ] Documentação técnica OpenAPI completa e atualizada.
- [ ] MVP homologado e pronto para apresentação.


## 5. Exigência de Testes Automatizados (Critério Obrigatório para Nota Máxima)
Para garantir pontuação máxima na avaliação semanal do professor, o desenvolvedor deve entregar:
- **Testes Unitários de Domínio**: Cobertura das regras e invariantes usando JUnit 5 e AssertJ, sem mocks de banco ou contexto Spring.
- **Testes de Integração / API**: Validação com Spring `MockMvc` ou `DataJpaTest` cobrindo status HTTP (200, 201, 400, 403, 404, 422), integridade transacional e constraints do PostgreSQL.
- **Documentação Swagger**: Endpoints testados e validados no OpenAPI (`/swagger-ui/index.html`).

- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.


## 6. Governança do Modelo Orientado a Cronograma (Schedule-Driven Development)
Como o projeto adota o **Modelo Orientado a Cronograma**, o prazo semanal (timebox) é **fixo e inegociável**:
- **Timebox Fixo**: A entrega desta issue deve ser concluída impreterivelmente até o final da semana correspondente para avaliação do professor.
- **Estratégia Anti-Bloqueio (In-Memory Stubs)**: Caso uma issue predecessora atrase, o desenvolvedor **NÃO deve ficar bloqueado**. Deve criar uma implementação falsa em memória (In-Memory Fake/Mock) da Porta de Saída correspondente, garantindo que seu Caso de Uso ou Interface funcione e seja testado autonomamente.
- **Regra de Estabilidade de Integração (Trunk Stability)**: Nenhuma alteração pode quebrar a branch principal (`main` / `dev`). Ao final do timebox, a entrega deve ser submetida via Pull Request com testes verdes e build passando.
- **Diretriz de Redução de Escopo (Scope Pruning)**: Se houver risco iminente de estourar o timebox semanal, recursos secundários (como estilos visuais avançados ou filtros secundários) devem ser temporariamente postergados, priorizando o núcleo de domínio, os testes unitários e a API funcional.

- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

