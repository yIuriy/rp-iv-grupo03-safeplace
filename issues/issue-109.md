# Issue #109: [64] [MVP][EPIs] Integrar APIs de empréstimos, devoluções e projeções

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/109
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:21:25Z
- **Updated at:** 2026-09-15T01:02:41Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF11/RF21; RNF03; UC10/UC12; US11/US21.

**Referência do modelo:** Emprestimo e ProjecaoSubstituicao.

## O que entregar

Conectar portas de empréstimo/devolução e projeção à API, mantendo contratos distintos e autorização conforme os respectivos UCs.

## Critérios de aceite

- [ ] Supervisor/Gestor registram entrega/devolução e consultam posse conforme UC12.
- [ ] Regras de saldo, CA, duplicidade e capacitação são executadas no servidor antes de confirmar entrega.
- [ ] Gestor solicita/consulta projeções e alertas de UC10; erro de cálculo permite nova tentativa.
- [ ] Resposta de devolução reflete destino aprovado e saldo persistido.
- [ ] API documenta DTOs, erros e perfis; teste integrado confirma efeitos em banco e histórico.

## Dependências técnicas

- [#107](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/107)
- [#108](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/108)
- [#117](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/117)

## Decisões pendentes e limites

Entrega/devolução pode avançar sem esperar algoritmo, mas parte de projeções requer #108. Bloqueio de treinamento deve estar integrado antes do aceite de empréstimo.

## Motivo da correção

Separados módulos e removida dependência circular com unidade física. Catálogo offline não é critério desta issue.

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
Disponibilizar via API REST as operações de empréstimo e devolução de EPIs para colaboradores, além de fornecer os dados de projeção e alertas preventivos de substituição (**RF11, RF21, UC10, UC12**).

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 6 - APIs de Empréstimo, Áreas e Domínio de Capacitações
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#92: [MVP][Segurança] Implementar Autenticação JWT e Filtro de Autorização RBAC)
  - [?] (#107: [MVP][EPI] Implementar Persistência JPA e Migração Flyway para Empréstimos e Devoluções)
  - [?] (#108: [MVP][EPI] Implementar Algoritmo de Substituição Inteligente e Alertas Preventivos de EPI)
  - [?] (#124: [MVP][EPI] Modelar Rastreabilidade por Unidade Física de EPI)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#110: [MVP][Frontend] Construir Interface de Empréstimos, Devoluções e Alertas de Substituição Inteligente)

## 3. O Que Fazer
1. Criar `EmprestimoEpiController`:
   - `POST /api/epis/emprestimos`: registra novo empréstimo vinculando EPI e colaborador.
   - `POST /api/epis/emprestimos/{id}/devolucao`: registra devolução do EPI.
   - `GET /api/epis/emprestimos/ativos`: lista empréstimos em andamento e atrasados.
   - `GET /api/epis/emprestimos/colaborador/{colaboradorId}`: lista histórico de EPIs na posse do colaborador.
2. Criar endpoint em `EpiController`:
   - `GET /api/epis/alertas-substituicao`: retorna a lista de alertas preventivos calculados pelo algoritmo de substituição inteligente.
3. Criar DTOs de entrada e saída documentados no Swagger OpenAPI.

## 4. Como Fazer (Arquitetura Hexagonal)
- Controladores orquestram casos de uso via portas de entrada.
- Proteção RBAC garantindo que apenas Supervisores e Gestores possam registrar empréstimos.


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
- [ ] Empréstimo e devolução alteram o saldo do estoque em tempo real.
- [ ] Endpoint de alertas retorna diagnósticos precisos de CA, estoque e vida útil.
- [ ] Testes de integração de API cobrindo fluxo ponta a ponta.
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

