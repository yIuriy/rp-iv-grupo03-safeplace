# Issue #99: [48] [MVP][Ocorrências e Planos] Completar operações de acompanhamento pela API

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/99
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:21:02Z
- **Updated at:** 2026-09-15T01:02:12Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF01/RF02/RF07/RF13/RF16; RNF03/RNF05; UC05/UC07/UC09.

**Referência do modelo:** Operações de consulta/alteração de Ocorrencia e PlanoDeAcao conforme decisões #95/#97.

## O que entregar

Conectar persistência e domínio às operações de registro, detalhes, atualização, triagem, arquivamento e gestão de planos. Reutilizar endpoints existentes e documentar extensões coerentes.

## Critérios de aceite

- [ ] Registro preserva relator autenticado, data do fato e protocolo aprovado para acidente/incidente.
- [ ] Gestor conduz triagem/atualização/arquivamento e planos; Supervisor registra e acompanha seus relatos segundo UC09.
- [ ] Ações podem ser cadastradas, acompanhadas, prorrogadas com justificativa e reatribuídas com alerta conforme #97.
- [ ] Nova consulta confirma alteração persistida; impedir exclusão permanente.
- [ ] Notificação de novo relato ao Gestor é implementada conforme UC09/DS09, sem presumir e-mail/SMS como canal.
- [ ] Documentar contratos e validar autenticação, autorização, dados inválidos, não encontrado e persistência.

## Dependências técnicas

- [#96](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/96)
- [#98](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/98)

## Decisões pendentes e limites

Forma do protocolo/estados em #95 e alertas em #97 devem estar resolvidos para respectivos fluxos; auditoria integrada com #121.

## Motivo da correção

Cadastro/listagem existentes não cobrem gestão completa; removida exigência de CAT, investigação e exportação.

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
Expor via API REST todas as operações de registro e acompanhamento de acidentes, incidentes e planos de ação, integrando com o controle de acesso JWT (**RNF03**).

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 3 - Frontend de Usuários e Gestão de Manutenção
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#92: [MVP][Segurança] Implementar Autenticação JWT e Filtro de Autorização RBAC)
  - [?] (#96: [MVP][Ocorrências] Implementar Adaptador de Persistência e Migrações Flyway de Ocorrências)
  - [?] (#98: [MVP][Ocorrências] Implementar Persistência JPA e Migração Flyway para Planos de Ação)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#100: [MVP][Frontend] Construir Interface de Registro de Ocorrências, Triagem e Planos de Ação)
  - [?] (#128: [MVP][Ocorrências] Implementar Consulta Avançada por Protocolo, Período e Exportação de Sumário)

## 3. O Que Fazer
1. Atualizar `OcorrenciaController`:
   - `POST /api/ocorrencias`: registra acidente ou incidente (extraindo o usuário relator logado do JWT). Retorna `201 Created` com o protocolo gerado.
   - `GET /api/ocorrencias`: lista ocorrências com filtros (tipo, status, período).
   - `GET /api/ocorrencias/{id}` e `GET /api/ocorrencias/protocolo/{protocolo}`: consulta detalhes.
   - `PATCH /api/ocorrencias/{id}/status`: triagem e atualização de status (ex: arquivar ocorrência).
2. Criar endpoints aninhados para planos de ação:
   - `POST /api/ocorrencias/{id}/planos-acao`: adiciona uma ação à ocorrência.
   - `GET /api/ocorrencias/{id}/planos-acao`: lista ações daquela ocorrência.
   - `PATCH /api/planos-acao/{id}/status`: conclui ou atualiza o status de uma ação.
3. Criar DTOs de entrada e saída com documentação OpenAPI Swagger.

## 4. Como Fazer (Arquitetura Hexagonal)
- Os controllers atuam puramente como adaptadores inbound: deserializam requisição, invocam casos de uso através de portas de entrada e serializam DTOs de saída.
- Tratamento de exceções via `@ExceptionHandler` para `OcorrenciaNaoEncontradaException` e erros de validação.


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
- [ ] Endpoints protegidos por JWT e documentados no OpenAPI/Swagger.
- [ ] Retorno do protocolo gerado no momento da criação da ocorrência.
- [ ] Adição e acompanhamento de planos de ação funcionando via API.
- [ ] Testes de integração de endpoints (Spring MockMvc) com status HTTP corretos.
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

