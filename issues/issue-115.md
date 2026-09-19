# Issue #115: [70] [MVP][Frontend] Mapear áreas e consultar/classificar tarefas

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/115
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:21:39Z
- **Updated at:** 2026-09-15T01:02:52Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF05/RF06; RNF11; UC03/UC11; US05/US06.

**Referência do modelo:** Setor, AreaRisco, Tarefa e EPIs exigidos para acesso à área.

## O que entregar

Integrar cadastro/edição/consulta de áreas e classificação de tarefas com API real e ações disponíveis segundo perfil.

## Critérios de aceite

- [ ] Gestor informa identificação, agentes, limites e EPIs conforme contrato #111; Supervisor consulta diretrizes.
- [ ] Filtro de risco usa vocabulário aprovado #112.
- [ ] Interface apresenta vínculos de proteção para área sem confundi-los com RF12.
- [ ] Alterações de área e tarefa aparecem após recarregar e mostram erros sem falso sucesso.
- [ ] Rótulos, foco, teclado e responsividade verificáveis; sem impor mapa geográfico não especificado.

## Dependências técnicas

- [#94](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/94)
- [#114](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/114)

## Decisões pendentes e limites

Consulta de EPIs obrigatórios integra #125. Offline depende de #105.

## Motivo da correção

Retirado cache de mapa como escopo automático e dependência de inspeções.

## Fontes verificáveis

- [Diagrama de classes](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/diagramas/classes/Diagrama%20de%20Classes%20-%20SafePlace.png).
- [Requisitos funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-funcionais.md) e [não funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-nao-funcionais.md).
- [MoSCoW](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/priorizacao-moscow.md) e [MVP, incluindo pendências da seção 11](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/mvp/especificacao-mvp-arquitetura.md).
- [Casos de uso](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/casos-de-uso/casos-de-uso.md) e [histórias de usuário](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/historias-de-usuario.md).
- [Base técnica consultada](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/backend/src/main/java/br/edu/safeplace/backend/domain/area_risco/AreaRisco.java). Evidência do estado existente, não fonte de requisito.

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
Desenvolver as telas `RiskAreasPage.tsx` e `TasksPage.tsx` no frontend React, permitindo a visualização das áreas de risco, identificação imediata dos EPIs obrigatórios de cada área e consulta do catálogo de tarefas operacionais, com suporte a consulta offline (**RF05, RF06, RNF08**).

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 7 - Frontend de Áreas, Bloqueio e Capacitações
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#94: [MVP][Frontend] Implementar Telas de Login, Gestão de Usuários e Cache Offline Local)
  - [?] (#114: [MVP][Áreas e Tarefas] Implementar Endpoints REST para Áreas de Risco e Classificação de Tarefas)
  - [?] (#125: [MVP][Áreas de Risco] Implementar Verificação de Conformidade de EPI para Acesso a Setores de Risco)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#121: [MVP][Transversal] Implementar Trilha de Auditoria Imutável (RNF05))

## 3. O Que Fazer
1. Implementar `RiskAreasPage.tsx`:
   - Grid de cartões de setores com cores de periculosidade (Baixo, Médio, Alto, Crítico).
   - Lista visual dos EPIs obrigatórios para entrar no setor.
   - Modal de cadastro/edição de área de risco (visível apenas para Gestor).
2. Implementar `TasksPage.tsx`:
   - Listagem de tarefas operacionais com selo de periculosidade e requisitos de treinamento.
   - Modal de criação de tarefas.
3. Implementar Cache Offline Local (**RNF08**):
   - Gravar o mapa de áreas de risco e tarefas em storage local para que o supervisor consulte as diretrizes de segurança sem internet.
4. Seguir diretrizes de acessibilidade WCAG 2.1 AA.

## 4. Como Fazer
- Utilizar componentes padronizados do design system (`Button`, `Dialog`, `Fields`, `Feedback`).
- Isolar chamadas de rede em `features/areas-risco/api.ts` e `features/tarefas/api.ts`.


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
- [ ] Áreas de risco exibidas com EPIs obrigatórios claramente destacados.
- [ ] Catálogo de tarefas operacionais funcional com filtro de risco.
- [ ] Consulta offline de áreas e tarefas disponível sem internet.
- [ ] Testes de componentes frontend cobrindo renderização e estados.
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

