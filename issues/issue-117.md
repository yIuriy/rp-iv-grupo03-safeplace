# Issue #117: [71] [MVP][Capacitações] Aplicar impedimentos de alocação e entrega de EPI

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/117
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:21:44Z
- **Updated at:** 2026-09-15T01:03:04Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF06/RF10/RF11; UC02/UC11/UC12; US06/US10/US11.

**Referência do modelo:** Certificacao, Treinamento, Tarefa, Colaborador e EPI.

## O que entregar

Aplicar regras documentadas de capacitação no ponto em que alocação ou entrega é confirmada, usando exigências aprovadas e atuais.

## Critérios de aceite

- [ ] Tarefa sem classificação não aceita alocação.
- [ ] Certificação obrigatória vencida impede alocação; pendências de treinamento seguem UC02.
- [ ] Entrega de EPI com treinamento obrigatório vencido é bloqueada conforme UC12.
- [ ] Consulta prévia de aptidão não substitui validação na mutação final.
- [ ] Retorno explica pendência; não criar catálogo de NRs, multiplicadores ou exceções automáticas sem fonte.
- [ ] Testes de integração demonstram que operação bloqueada não grava nem movimenta estoque.

## Dependências técnicas

- [#112](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/112)
- [#116](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/116)

## Decisões pendentes e limites

Ponto de alocação e representação das exigências precisam ser identificados em #116/#126. Entrega integra #109. Não usar RF12 como dependência obrigatória.

## Motivo da correção

Ampliação necessária para cobrir UC12 sem inventar módulo de escalas; validador isolado não satisfaz bloqueio efetivo.

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
Conforme o **RF10**, **UC02 (Exceção I)** e **UC11**, o SafePlace deve **bloquear a alocação de colaboradores com certificações vencidas em tarefas de risco** (ex.: alocar colaborador com NR-35 vencida em trabalho em altura).

Esta issue desenvolve o serviço de domínio que aplica o bloqueio de segurança impeditivo.

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 7 - Frontend de Áreas, Bloqueio e Capacitações
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#112: [MVP][Tarefas] Modelar Domínio e Classificação de Periculosidade de Tarefas)
  - [?] (#116: [MVP][Capacitações] Modelar Domínio de Certificações, Treinamentos e Alerta de 30 Dias)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#119: [MVP][Capacitações] Implementar Endpoints REST para Certificações, Treinamentos e Validação de Bloqueio)
  - [?] (#126: [MVP][Tarefas] Implementar Auditoria e Consulta de Aptidão de Colaborador por Tarefa)
  - [?] (#120: [MVP][Frontend] Construir Painel de Certificações, Treinamentos e Indicadores de Bloqueio no Frontend)

## 3. O Que Fazer
1. Criar serviço de domínio `ValidadorAptidaoColaboradorService` em `domain.capacitacao`:
   - Método `validarAptidaoParaTarefa(Colaborador colaborador, Tarefa tarefa, List<Certificacao> certificacoes)`:
     - Verifica se a tarefa exige treinamento/certificação obrigatória.
     - Se sim, verifica se o colaborador possui a certificação correspondente.
     - Se a certificação estiver ausente ou vencida, lança `AlocacaoBloqueadaPorCertificacaoException` com justificativa normativa detalhada.
2. Definir a porta de entrada `ValidarAlocacaoColaboradorUseCase`.

## 4. Como Fazer (Arquitetura Hexagonal)
- O serviço de domínio atua como validador puro de regras de segurança entre agregados.
- Testes unitários exaustivos para colaboradores aptos e inaptos.


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
- [ ] Bloqueio impeditivo ativado sempre que houver certificação exigida vencida ou faltante.
- [ ] Mensagem de bloqueio clara indicando o motivo e a norma correspondente.
- [ ] Testes unitários cobrindo alocação permitida e múltiplos casos de bloqueio.
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

