# Issue #133: [57] [MVP][Frontend] Operar estoque e consultar histórico e nível crítico

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/133
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T02:11:57Z
- **Updated at:** 2026-09-15T01:03:40Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF03; RNF03/RNF11; UC06; US03.

**Referência do modelo:** EPI, EspecificacaoEPI e MovimentacaoEstoque.

## O que entregar

Entregar consulta de catálogo/saldo/histórico e registro de entrada/saída pelo Gestor, integrados à API e ao alerta crítico.

## Critérios de aceite

- [ ] Movimento válido atualiza saldo e histórico após nova consulta; retirada acima do saldo falha e mantém estado.
- [ ] Exibir operador, data/hora, quantidade e motivo do movimento conforme contrato.
- [ ] Igualdade e valores inferiores ao mínimo aparecem críticos; regra não é reimplementada de forma divergente.
- [ ] Campos de catálogo seguem #124; não criar recebimento de fornecedor ou código de barras não aprovado.
- [ ] Interface e backend aplicam mesmos papéis; teclado e responsividade demonstráveis.

## Dependências técnicas

- [#94](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/94)
- [#132](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/132)
- [#123](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/123)

## Decisões pendentes e limites

Offline só depois #105; consulta de empréstimo para Supervisor permanece fluxo próprio.

## Motivo da correção

Retirada movimentação por Supervisor e exigência de busca em tempo real sem fonte.

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
Atendimento ao **RF03** (Controlar o estoque dos EPIs) e **RNF11** (Interface responsiva e acessível):
- O Supervisor e o Gestor de Segurança precisam de uma interface web intuitiva e ágil para consultar a disponibilidade de EPIs, registrar recebimento de lotes e baixas de estoque, e visualizar alertas visuais de estoque baixo.

Esta entrega implementa as telas de catálogo e movimentação de estoque de EPIs no frontend React + TypeScript, com componentes reutilizáveis, feedback visual e conformidade de acessibilidade (WCAG 2.1 AA).

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 4 - Interfaces Operacionais e Domínios de Risco
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#94: Telas de Login, Gestão de Usuários e AuthContext)
  - [?] (#132: Controladores Inbound REST e DTOs de Catálogo e Estoque)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#105: Cache Local e Suporte Offline para Catálogo e Estoque de EPIs)
  - [?] (#110: Interface de Empréstimos, Devoluções e Alertas de Substituição)

## 3. Entregáveis Técnicos e Arquiteturais (Frontend React / TS)
- **Componentes de Interface (`frontend/src/components/epi` e `pages/epi`)**:
  - `EpiCatalogPage`: Tabela responsiva com listagem de EPIs, busca em tempo real, filtro por tipo e indicador de status (Em Estoque, Estoque Baixo, Esgotado).
  - `StockMovementModal`: Modal acessível para registrar entrada de estoque (reposição) ou baixa justificada, com validação de campos obrigatórios e quantidade positiva.
  - `StockLevelBadge`: Componente visual que destaca equipamentos abaixo do nível mínimo de segurança em amarelo/vermelho.
- **Serviço HTTP de Integração**: `epiService.ts` integrando com a API REST `/api/epis` com tipagem estrita TypeScript e interceptor de token JWT.
- **Acessibilidade e Responsividade**: Conformidade com navegação por teclado, labels semânticos (aria-label) e layout responsivo mobile/desktop (RNF11).

## 4. Testes Automatizados Obrigatórios
- **Testes de Componentes com Vitest e React Testing Library**:
  - Testar renderização do catálogo com dados mockados da API.
  - Testar disparo de formulário de movimentação de estoque e exibição de mensagem de sucesso (Toast).
  - Testar bloqueio de submissão quando a quantidade informada for inválida ou zerada.
  - Testar renderização correta do badge de estoque crítico quando o saldo for menor ou igual ao mínimo.

## 5. Artefatos Demonstráveis para Avaliação
- Telas funcionais no React renderizando catálogo e permitindo registrar entradas e saídas de estoque.
- Interface responsiva testada em resoluções de desktop e smartphone.
- Suíte de testes unitários Vitest verde e sem warnings no console.

## 6. Governança do Modelo Orientado a Cronograma (Schedule-Driven Development)
Como o projeto adota o **Modelo Orientado a Cronograma**, o prazo semanal (timebox) é **fixo e inegociável**:
- **Timebox Fixo**: A entrega desta issue deve ser concluída impreterivelmente até o final da semana correspondente para avaliação do professor.
- **Estratégia Anti-Bloqueio (In-Memory Stubs)**: Se a API backend estiver temporariamente indisponível na branch, utilize mocks locais com MSW (Mock Service Worker) para demonstrar e testar o fluxo de catálogo e estoque sem bloqueios.
- **Regra de Estabilidade de Integração (Trunk Stability)**: A aplicação frontend deve manter build verde (`npm run build`) sem erros de lint ou quebras de tipos TypeScript.
- **Diretriz de Redução de Escopo (Scope Pruning)**: Caso haja restrição temporal, priorizar a visualização em tabela e o modal de movimentação, postergando gráficos avançados de consumo para versões futuras.

## 7. Critérios de Aceite para Nota Máxima (Final do Timebox)
- [ ] Catálogo de EPIs interativo com paginação e visualização clara do saldo físico.
- [ ] Modal de movimentação de estoque funcional e validando dados de entrada.
- [ ] Badges visuais indicando estoque crítico de acordo com as regras de negócio.
- [ ] Testes automatizados com React Testing Library passando com sucesso.
- [ ] PR submetido, com build verde e revisado dentro do timebox semanal da Semana 4.


</details>

