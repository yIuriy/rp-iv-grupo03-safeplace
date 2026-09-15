# Issue #97: [42] [MVP][Planos] Definir correspondência entre plano, ações, responsáveis e alertas

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/97
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** Rafaela Pacheco Nunes
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:20:57Z
- **Updated at:** 2026-09-15T01:02:09Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF07; UC07; US07.

**Referência do modelo:** PlanoDeAcao: medidasCorretivas, medidasPreventivas, prazo, status; validarPlano, atualizarPrazo, atualizarResponsavel e operações por idAcao.

## O que entregar

Resolver a diferença entre o plano do PNG e as ações individuais do UC07. Produzir contrato aprovado que permita execução e acompanhamento sem substituir os atributos existentes por uma classe inventada.

## Critérios de aceite

- [ ] Definir identidade/cardinalidade de plano e ação e seus vínculos com ocorrência, Colaborador e Setor.
- [ ] Definir estados e transições; não assumir PENDENTE/EM_ANDAMENTO/CONCLUIDO/CANCELADO nem dataConclusao obrigatória.
- [ ] Prazo não pode anteceder data atual ou ocorrência; prorrogação exige justificativa e preservação de histórico.
- [ ] Registrar como reconhecer ocorrência grave e exigir ação preventiva, sem inventar escala de gravidade.
- [ ] Definir antecedência configurável dos alertas e canal para notificar responsável sem conta; três dias são exemplo, não constante.

## Dependências técnicas

Nenhuma dependência técnica obrigatória adicional para iniciar a análise/correção descrita. Observar as decisões e dependências condicionais abaixo.

## Decisões pendentes e limites

Decisão de domínio antes de persistência/contratos finais em #98/#99. Pode usar as definições de ocorrência de #95 e Setor de #111 conforme forem aprovadas.

## Motivo da correção

Responsável em texto livre, novos campos e estados fixos eram prescritos sem aprovação. Esta issue passa a resolver o contrato antes da implementação.

## Fontes verificáveis

- [Diagrama de classes](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/diagramas/classes/Diagrama%20de%20Classes%20-%20SafePlace.png).
- [Requisitos funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-funcionais.md) e [não funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-nao-funcionais.md).
- [MoSCoW](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/priorizacao-moscow.md) e [MVP, incluindo pendências da seção 11](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/mvp/especificacao-mvp-arquitetura.md).
- [Casos de uso](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/casos-de-uso/casos-de-uso.md) e [histórias de usuário](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/historias-de-usuario.md).
- [Base técnica consultada](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/backend/src/main/java/br/edu/safeplace/backend/domain/ocorrencia/PlanoDeAcao.java). Evidência do estado existente, não fonte de requisito.

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
O **RF07** e o **UC07** determinam que para cada acidente ou incidente registrado deve ser possível definir e acompanhar um **Plano de Ação**, composto por medidas corretivas e preventivas. O responsável pela execução pode ser um colaborador ou um setor da empresa (que não necessariamente possui conta de login).

Esta issue cria a entidade de domínio rica de Plano de Ação e suas portas.

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 2 - APIs de Usuários e Persistência de Ocorrências
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#95: [MVP][Ocorrências] Modelar Domínio e Invariantes para Acidentes, Incidentes e Protocolo Único)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#98: [MVP][Ocorrências] Implementar Persistência JPA e Migração Flyway para Planos de Ação)
  - [?] (#99: [MVP][Ocorrências] Implementar Endpoints REST para Ocorrências, Triagem e Planos de Ação)

## 3. O Que Fazer
1. Criar a entidade de domínio `PlanoDeAcao` em `domain.ocorrencia`:
   - Atributos: `id`, `ocorrenciaId`, `descricao`, `responsavel` (texto livre ou nome de colaborador/setor), `dataPrazo`, `dataConclusao`, `status` (`PENDENTE`, `EM_ANDAMENTO`, `CONCLUIDO`, `CANCELADO`).
2. Implementar regras de negócio no domínio:
   - Transição de status validando se a data de conclusão foi informada ao marcar como `CONCLUIDO`.
   - Proibição de prazos no passado para novas ações.
   - Método para verificar se a ação está em atraso (`isEmAtraso()`).
3. Definir a porta de entrada `GerenciarPlanoAcaoUseCase`:
   - Adicionar plano de ação a uma ocorrência.
   - Atualizar status do plano de ação.
   - Listar planos de ação por ocorrência.
4. Definir a porta de saída `PlanoAcaoRepositoryPort`.

## 4. Como Fazer (Arquitetura Hexagonal)
- Manter independência total do domínio.
- A associação entre a ocorrência e seus planos de ação pode ser modelada como agregação no domínio ou consulta via porta de saída.


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
- [ ] Entidade `PlanoDeAcao` com ciclo de vida validado por regras de negócio.
- [ ] Suporte a responsáveis sem necessidade de conta no sistema.
- [ ] Portas de entrada e saída declaradas com clareza.
- [ ] Testes unitários cobrindo transições de status e verificação de prazos.
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

