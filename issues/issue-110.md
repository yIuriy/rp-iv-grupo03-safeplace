# Issue #110: [65] [MVP][Frontend] Operar empréstimos, devoluções e consultar projeções

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/110
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** Amanda Dias
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:21:28Z
- **Updated at:** 2026-09-15T01:02:43Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF11/RF21; RNF11; UC10/UC12; US11/US21.

**Referência do modelo:** Emprestimo e ProjecaoSubstituicao conforme decisões registradas.

## O que entregar

Entregar interface de posse/devolução para Supervisor/Gestor e projeções para Gestor integrada às respectivas APIs.

## Critérios de aceite

- [ ] Entrega seleciona Colaborador e equipamento conforme identidade aprovada; informa data prevista quando aplicável.
- [ ] Mostrar posse/atrasos, impedimento por saldo/CA/capacitação e confirmação justificada de duplicidade.
- [ ] Devolução informa condição e confirma destino, sem tratar manutenção como disponibilidade.
- [ ] Projeções mostram data estimada e justificativa reais; falha não gera alerta fictício.
- [ ] Dados persistem após recarregar e fluxo é operável por teclado em dispositivos previstos.

## Dependências técnicas

- [#94](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/94)
- [#109](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/109)

## Decisões pendentes e limites

Parte de projeção aguarda #108; consulta offline depende de #105/#127.

## Motivo da correção

Retiradas prioridades arbitrárias e cache obrigatório de empréstimos.

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
Integrar na interface gráfica (`EpiPage.tsx` e abas complementares) o controle visual de empréstimos/devoluções de EPIs e o painel preventivo de alertas de substituição inteligente (**RF11, RF21**).

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 6 - APIs de Empréstimo, Áreas e Domínio de Capacitações
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#104: [MVP][Frontend] Construir Visualização e Gestão de Manutenção de EPIs no Frontend)
  - [?] (#109: [MVP][EPI] Implementar Endpoints REST para Empréstimos, Devoluções e Projeções de Substituição)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#121: [MVP][Transversal] Implementar Trilha de Auditoria Imutável (RNF05))

## 3. O Que Fazer
1. Desenvolver abas e modais em `features/epis`:
   - Modal de "Novo Empréstimo": seleção de EPI disponível e busca rápida de colaborador cadastrado (com preenchimento de prazo de devolução).
   - Tabela de "EPIs em Posse / Empréstimos Ativos" com destaque para devoluções em atraso.
   - Ação rápida de "Registrar Devolução" com confirmação.
2. Desenvolver o widget/painel de **Substituição Inteligente**:
   - Cards de alerta preventivo (alerta de CA próximo ao vencimento, alerta de vida útil, alerta de estoque crítico).
   - Indicador visual de prioridade (Alta, Média, Baixa).
3. Suporte offline: permitir consulta da lista de empréstimos ativos em cache local (**RNF08**).

## 4. Como Fazer
- Reutilizar componentes do design system (`Dialog`, `Button`, `Tabs`, `Feedback`).
- Tipagem TypeScript estrita nos contratos de API.


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
- [ ] Checkout e check-in de EPI por colaborador realizados intuitivamente.
- [ ] Alertas de substituição inteligente exibidos com clareza visual.
- [ ] Consulta offline de empréstimos ativos funcional.
- [ ] Testes de componentes frontend cobrindo as novas interações.
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

