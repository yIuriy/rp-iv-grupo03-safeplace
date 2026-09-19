# Issue #126: [74] [MVP][Tarefas] Integrar bloqueio de alocação ao fluxo aprovado

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/126
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:36:56Z
- **Updated at:** 2026-09-15T01:03:20Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF06/RF10; UC02/UC11; US06/US10.

**Referência do modelo:** Tarefa, Colaborador, Certificacao e Treinamento.

## O que entregar

Identificar o ponto de alocação mencionado nos UCs e documentar seu contrato mínimo; integrar nele o impedimento por falta de classificação e capacitação.

## Critérios de aceite

- [ ] Registrar ator, operação de alocação e origem das exigências antes de criar endpoint/tela.
- [ ] Usar regra de #117 no momento da confirmação; consulta prévia não autoriza operação futura por si só.
- [ ] Sem classificação ou capacitação obrigatória válida, alocação é impedida e motivo informado.
- [ ] Operação permitida e recusada têm efeitos verificáveis; mutações permitidas são auditadas.
- [ ] Não criar matriz de aptidão geral, gestão de jornada, escala de trabalho ou atestado de saúde como escopo presumido.

## Dependências técnicas

- [#117](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/117)

## Decisões pendentes e limites

Fluxo de alocação está incompleto nos modelos disponíveis; depende de decisão explícita. Não fazer endpoint meramente consultivo passar por bloqueio efetivo.

## Motivo da correção

Retirados listagem geral de aptos e auditoria preventiva de toda consulta, não exigidas como tais.

## Fontes verificáveis

- [Diagrama de classes](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/diagramas/classes/Diagrama%20de%20Classes%20-%20SafePlace.png).
- [Requisitos funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-funcionais.md) e [não funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-nao-funcionais.md).
- [MoSCoW](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/priorizacao-moscow.md) e [MVP, incluindo pendências da seção 11](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/mvp/especificacao-mvp-arquitetura.md).
- [Casos de uso](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/casos-de-uso/casos-de-uso.md) e [histórias de usuário](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/historias-de-usuario.md).
- [Base técnica consultada](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/backend/src/main/java/br/edu/safeplace/backend/domain/tarefa/Tarefa.java). Evidência do estado existente, não fonte de requisito.

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
Consolidar a integração entre a periculosidade da tarefa (**RF06 / UC11**) e o controle de capacitações obrigatórias (**RF10 / UC02**), fornecendo um endpoint unificado para consulta de aptidão operacional de colaboradores antes do início da jornada de trabalho.

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 7 - Frontend de Áreas, Bloqueio e Capacitações
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#112: [MVP][Tarefas] Modelar Domínio e Classificação de Periculosidade de Tarefas)
  - [?] (#117: [MVP][Capacitações] Implementar Regra de Bloqueio Impeditivo de Alocação por Certificação Vencida)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#120: [MVP][Frontend] Construir Painel de Certificações, Treinamentos e Indicadores de Bloqueio no Frontend)

## 3. O Que Fazer
1. Criar caso de uso `ConsultarAptidaoGeralColaboradorUseCase`:
   - Avalia pendências de certificação, treinamentos vencidos e bloqueios impeditivos para uma lista de tarefas.
   - Retorna matriz de aptidão do colaborador para a equipe operacional.
2. Adicionar endpoint REST:
   - `GET /api/tarefas/{id}/colaboradores-aptos`: lista colaboradores aptos a executar a tarefa.
3. Implementar logs de auditoria preventiva.

## 4. Como Fazer (Arquitetura Hexagonal)
- Injeção das portas de Tarefas e Capacitações no serviço de aplicação.


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
- [ ] Consulta de colaboradores aptos por tarefa funcional.
- [ ] Bloqueio impeditivo respeitado na filtragem da lista.
- [ ] Testes de integração cobrindo cenários com colaboradores qualificados e desqualificados.
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

