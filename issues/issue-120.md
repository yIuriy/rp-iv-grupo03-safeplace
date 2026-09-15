# Issue #120: [76] [MVP][Frontend] Consultar capacitações e impedimentos dos colaboradores

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/120
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:21:51Z
- **Updated at:** 2026-09-15T01:03:09Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF10; RNF11; UC02; US10.

**Referência do modelo:** Certificacao, Treinamento, Colaborador.

## O que entregar

Entregar painel de consulta com detalhes, filtros de vencimento e pendências, integrado à API e aos impedimentos documentados.

## Critérios de aceite

- [ ] Gestor/Supervisor selecionam colaborador e consultam certificações/treinamentos.
- [ ] Mostrar nome, datas e demais atributos aprovados; não inventar validade uniforme de treinamento.
- [ ] Filtro distingue certificações em dia, próximas em 30 dias e vencidas; ausência de histórico é informada.
- [ ] Bloqueio é mostrado com motivo; ação não contorna validação do servidor.
- [ ] Cadastro somente após #116 e perfis aprovados; teclado e responsividade verificados.

## Dependências técnicas

- [#94](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/94)
- [#119](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/119)

## Decisões pendentes e limites

Offline depende #105/#127; alocação efetiva integra #126 quando definida.

## Motivo da correção

Retirados cache de capacitações e modal de cadastro como obrigações já aprovadas.

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
Construir a interface do módulo de Capacitações no frontend (`features/capacitacoes`), permitindo ao supervisor monitorar a regularidade das certificações de cada colaborador, visualizar alertas de vencimento em 30 dias e identificar visualmente o bloqueio de alocação em tarefas de risco (**RF10, UC02**).

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 8 - Frontend Final, Auditoria e Homologação
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#94: [MVP][Frontend] Implementar Telas de Login, Gestão de Usuários e Cache Offline Local)
  - [?] (#119: [MVP][Capacitações] Implementar Endpoints REST para Certificações, Treinamentos e Validação de Bloqueio)
  - [?] (#126: [MVP][Tarefas] Implementar Auditoria e Consulta de Aptidão de Colaborador por Tarefa)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#121: [MVP][Transversal] Implementar Trilha de Auditoria Imutável (RNF05))
  - [?] (#129: [MVP][Transversal] Validar Integridade End-to-End, Documentação Técnica e Homologação do MVP)

## 3. O Que Fazer
1. Desenvolver em `features/capacitacoes`:
   - Tabela de colaboradores com status geral de capacitação (Em Dia, Atenção / 30 dias, Vencido).
   - Painel de detalhes do colaborador exibindo a lista de certificações e treinamentos com datas de validade.
   - Modal de cadastro de nova certificação/treinamento para o colaborador.
2. Indicador visual de **Bloqueio de Alocação**:
   - Selo de impedimento visual quando uma certificação obrigatória estiver vencida.
3. Cache Offline Local (**RNF08**):
   - Cachear dados de certificações no cliente para consulta em campo sem internet.
4. Seguir acessibilidade WCAG 2.1 AA.

## 4. Como Fazer
- Utilizar componentes padronizados do Design System (`Dialog`, `Button`, `Feedback`, `Fields`).
- Centralizar chamadas de API em `features/capacitacoes/api.ts`.


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
- [ ] Visão clara das certificações e prazos por colaborador.
- [ ] Destaque para certificações a vencer em 30 dias e vencidas.
- [ ] Alerta impeditivo de alocação visível.
- [ ] Consulta offline de certificações em cache local funcional.
- [ ] Testes de componentes frontend passando com sucesso.
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

