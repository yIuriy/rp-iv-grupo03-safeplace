# Issue #119: [73] [MVP][Capacitações] Expor consulta, alertas e regras aprovadas pela API

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/119
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:21:48Z
- **Updated at:** 2026-09-15T01:03:07Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF10; RNF03; UC02; US10.

**Referência do modelo:** Operações de Certificacao/Treinamento e consulta por Colaborador.

## O que entregar

Integrar consultas de capacitações, filtros e alertas ao backend; oferecer cadastro somente após definição do fluxo e papéis em #116.

## Critérios de aceite

- [ ] Gestor/Supervisor consultam colaborador e detalhes de capacitações conforme UC02.
- [ ] API apresenta próximos vencimentos em 30 dias, vencidos e pendências.
- [ ] DTOs mantêm atributos e tipos aprovados; nenhuma credencial é exigida do beneficiário.
- [ ] Escrita não é liberada genericamente a ambos os perfis sem fluxo aprovado.
- [ ] Consulta de aptidão reutiliza regra #117; validar operação real continua obrigatório.

## Dependências técnicas

- [#118](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/118)
- [#117](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/117)

## Decisões pendentes e limites

Consulta pode avançar sem escrita; cadastro e alocação aguardam definições #116/#126.

## Motivo da correção

Removidos endpoints de cadastro com autorização inventada e validação de aptidão tratada como bloqueio suficiente.

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
Disponibilizar via API REST os endpoints para consulta e registro de certificações/treinamentos de colaboradores e para validação de bloqueio de alocação (**RF10, UC02**).

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 7 - Frontend de Áreas, Bloqueio e Capacitações
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#92: [MVP][Segurança] Implementar Autenticação JWT e Filtro de Autorização RBAC)
  - [?] (#117: [MVP][Capacitações] Implementar Regra de Bloqueio Impeditivo de Alocação por Certificação Vencida)
  - [?] (#118: [MVP][Capacitações] Implementar Persistência JPA e Migração Flyway para Certificações e Treinamentos)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#120: [MVP][Frontend] Construir Painel de Certificações, Treinamentos e Indicadores de Bloqueio no Frontend)

## 3. O Que Fazer
1. Criar `CapacitacaoController`:
   - `POST /api/capacitacoes/certificacoes`: cadastra nova certificação de colaborador.
   - `GET /api/capacitacoes/colaborador/{colaboradorId}`: consulta situação completa de certificações e treinamentos do colaborador.
   - `GET /api/capacitacoes/alertas-vencimento`: lista todas as certificações vencidas ou a vencer em 30 dias.
   - `POST /api/capacitacoes/validar-alocacao`: endpoint que recebe `colaboradorId` e `tarefaId`, retornando `200 OK` se apto ou `422 Unprocessable Entity` com detalhes caso bloqueado.
2. Criar DTOs de entrada e saída documentados no Swagger OpenAPI.
3. Adicionar tratamento global para `AlocacaoBloqueadaPorCertificacaoException`.

## 4. Como Fazer (Arquitetura Hexagonal)
- O controller traduz a requisição e invoca os casos de uso correspondentes.
- Endpoints protegidos exigindo perfil `SUPERVISOR` ou `GESTOR_SEGURANCA`.


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
- [ ] Endpoint de validação de alocação rejeita colaborador com capacitação vencida.
- [ ] Listagem de alertas de vencimento funcional.
- [ ] Testes de integração de API cobrindo registro e validação de alocação.
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

