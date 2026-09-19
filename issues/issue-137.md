# Issue #137: [81] [MVP][Qualidade] Verificar responsividade e acessibilidade dos fluxos essenciais

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/137
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T02:12:06Z
- **Updated at:** 2026-09-15T01:03:47Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RNF11 — Deve ter; RNF15.

**Referência do modelo:** Interfaces dos fluxos MVP e contratos aprovados.

## O que entregar

Verificar interface desktop/tablet/móvel e diretrizes WCAG 2.1 AA com evidências, combinando ferramentas disponíveis e revisão manual.

## Critérios de aceite

- [ ] Cobrir fluxos MVP de usuários, ocorrências/planos, EPIs/estoque/manutenção/empréstimos/projeções, áreas/tarefas e capacitações.
- [ ] Verificar teclado, foco, rótulos/erros, contraste e comunicação de estados, sem depender só de cores.
- [ ] Registrar dispositivos/cenários, resultados e limitações; ferramenta automática isolada não comprova toda conformidade.
- [ ] Falhas identificadas geram correção verificável; aceite final usa evidências reais.
- [ ] Não impor p95 <500 ms: RNF01 prevê até 3 s para interações e 1 s para consultas simples e está fora do MVP; RNF16 também é backlog.

## Dependências técnicas

- [#94](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/94)
- [#100](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/100)
- [#104](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/104)
- [#110](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/110)
- [#115](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/115)
- [#120](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/120)
- [#133](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/133)

## Decisões pendentes e limites

Pode começar incrementalmente por telas entregues; conclusão acompanha conjunto final. Nenhuma biblioteca de teste específica é requisito acadêmico presumido.

## Motivo da correção

Separada acessibilidade obrigatória de desempenho/compatibilidade ampla fora do MVP; retirada meta inventada de 500 ms.

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

## 1. Contexto e Requisitos Atendidos
Atendimento rigoroso ao **RNF01** (Tempo de resposta sob carga de operação acadêmica), **RNF11** (Interface responsiva e acessível conforme diretrizes WCAG 2.1 AA) e **RNF16** (Compatibilidade com navegadores):
- Para a avaliação e fechamento do MVP com pontuação máxima, o sistema deve apresentar evidências automatizadas de conformidade com padrões internacionais de acessibilidade para usuários com necessidades especiais, além de garantir que as rotas da API respondem dentro do limiar de performance aceitável.

Esta entrega implementa a suíte de auditoria contínua de acessibilidade digital (`axe-core`), scripts automatizados de benchmarking de tempo de resposta dos endpoints e documentação do laudo técnico de conformidade.

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 8 - Hardening do Sistema, Auditoria e Entrega do MVP
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#94: Telas de Login e Gestão de Usuários)
  - [?] (#100: Interface de Ocorrências e Triagem)
  - [?] (#133: Interface de Catálogo e Estoque)
  - [?] (#120: Painel de Certificações e Indicadores de Bloqueio)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#129: Validação de Integridade End-to-End e Homologação do MVP)

## 3. Entregáveis Técnicos e Arquiteturais
- **Suíte de Testes Automatizados de Acessibilidade (`frontend`)**:
  - Testes com `@axe-core/react` e `vitest-axe` executados em todas as telas principais (Login, Ocorrências, EPIs, Áreas de Risco e Certificações).
  - Verificação de contraste de cores, hierarquia de cabeçalhos semânticos (`<h1>` a `<h6>`), atributos `alt` em imagens e navegação completa via teclado.
- **Auditoria de Desempenho e Tempos de Resposta (Backend/API)**:
  - Script automatizado (via k6 ou script Python de benchmark) avaliando tempo de resposta médio das 10 principais rotas da API sob carga simultânea.
  - Evidência de que 95% das requisições (p95) respondem abaixo de 500ms em ambiente de teste local.
- **Relatório Técnico de Qualidade e Homologação**: Documento em `docs/qualidade/laudo-acessibilidade-performance.md` registrando métricas coletadas e conformidade normativa.

## 4. Testes Automatizados Obrigatórios
- **Testes de Acessibilidade Vitest-Axe**:
  - Asserção de zero violações críticas ou graves de acessibilidade WCAG 2.1 AA nos componentes principais.
- **Script de Benchmark Automatizado**:
  - Execução validando status codes 200/201 e tempos de latência em operações de leitura e escrita.

## 5. Artefatos Demonstráveis para Avaliação
- Laudo técnico de acessibilidade e performance versionado no repositório.
- Relatório de execução dos testes `axe-core` passando na esteira de CI.
- Gráficos ou tabelas de tempo de resposta dos endpoints demonstrando estabilidade e respeito ao RNF01.

## 6. Governança do Modelo Orientado a Cronograma (Schedule-Driven Development)
Como o projeto adota o **Modelo Orientado a Cronograma**, o prazo semanal (timebox) é **fixo e inegociável**:
- **Timebox Fixo**: A entrega desta issue deve ser concluída impreterivelmente até o final da semana correspondente para avaliação do professor.
- **Estratégia Anti-Bloqueio (In-Memory Stubs)**: Os testes de acessibilidade executam diretamente sobre os componentes de frontend renderizados; para páginas com dependências de dados, utilize mocks estáticos estruturados.
- **Regra de Estabilidade de Integração (Trunk Stability)**: A suíte de auditoria deve ser integrada sem quebrar o pipeline de testes existente.
- **Diretriz de Redução de Escopo (Scope Pruning)**: Priorizar as telas críticas do fluxo central do MVP (Login, Ocorrências e EPIs), gerando laudos parciais caso a carga de trabalho atinja o limite do timebox.

## 7. Critérios de Aceite para Nota Máxima (Final do Timebox)
- [ ] Suíte de testes `axe-core` integrada ao frontend com zero violações de acessibilidade WCAG 2.1 AA.
- [ ] Script de benchmark de performance comprovando tempos de resposta < 500ms nas rotas centrais.
- [ ] Relatório técnico documentado em markdown no repositório com evidências e gráficos.
- [ ] PR submetido, com build verde e revisado dentro do timebox semanal da Semana 8.


</details>

