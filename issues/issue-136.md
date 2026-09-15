# Issue #136: [75] [Backlog][Frontend] Consultar e registrar inspeções conforme modelo aprovado

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/136
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T02:12:04Z
- **Updated at:** 2026-09-15T01:03:45Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** Backlog; fora da implementação do MVP.

**Requisitos e histórias:** RF09 — Deveria ter; US09.

**Referência do modelo:** Inspecao e ItemVerificado associados à área.

## O que entregar

Planejar interface futura de registro/consulta de inspeções sobre o fluxo aprovado na #134.

## Critérios de aceite

- [ ] Formulário/histórico refletem dados do PNG e fluxo aprovado, incluindo itens verificados.
- [ ] Atores e permissões são definidos antes de expor escrita.
- [ ] Não exigir badge de interdição, risco EXTREMO, piscar cores ou laudo obrigatório sem fonte.
- [ ] Não bloquear mapa de áreas, classificação de tarefas ou homologação MVP.

## Dependências técnicas

- [#134](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/134)

## Decisões pendentes e limites

Backlog; UI e contratos dependem da decisão de #134.

## Motivo da correção

Reclassificada de RF05/RF06 para RF09; retirada fiscalização/interdição inventada.

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

## 1. Contexto e Requisitos Atendidos
Atendimento ao **RF05** (Mapear as áreas de risco do ambiente de trabalho), **RF06** (Classificar o nível de periculosidade da tarefa) e **RNF11** (Interface responsiva e acessível):
- O Supervisor de segurança necessita de uma interface responsiva no frontend para registrar laudos de inspeção técnica nas áreas de trabalho, visualizar histórico de vistorias e atualizar o status operacional da área (Liberada, Alerta ou Interditada).

Esta entrega implementa as telas e modais de Registro e Consulta de Inspeções de Áreas no frontend React + TypeScript, com componentes acessíveis e visualização clara de riscos.

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 7 - Interface de Riscos e Bloqueios de Alocação
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#134: Domínio e Invariantes de Inspeções e Nível de Risco Ambiental)
  - [?] (#114: Endpoints REST para Áreas de Risco e Classificação de Tarefas)
  - [?] (#115: Mapa de Áreas de Risco e Painel de Tarefas no Frontend)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#129: Validação de Integridade End-to-End e Homologação do MVP)

## 3. Entregáveis Técnicos e Arquiteturais (Frontend React / TS)
- **Componentes de Interface (`frontend/src/components/risco` e `pages/risco`)**:
  - `InspecaoModal`: Modal acessível com formulário para registro de inspeção, seleção do nível de perigo ambiental, checklist de fatores de risco e parecer técnico conclusivo.
  - `HistoricoInspecoesDrawer`: Gaveta lateral responsiva que lista a linha do tempo cronológica de vistorias realizadas na área selecionada, com dados do supervisor responsável.
  - `StatusAreaBadge`: Indicador visual de destaque que alerta imediatamente áreas sob `INTERDITADA` (vermelho piscante/destacado) ou `ALERTA` (amarelo).
- **Serviço de Integração**: `inspecaoService.ts` com tipagem estrita e tratamento de erros de rede.
- **Acessibilidade**: Validação de foco acessível (focus-trap no modal), tags semânticas e compatibilidade com leitores de tela (RNF11).

## 4. Testes Automatizados Obrigatórios
- **Testes de Componentes com Vitest e React Testing Library**:
  - Testar abertura do modal de inspeção e validação de campos obrigatórios (parecer e status).
  - Testar envio bem-sucedido de nova vistoria com feedback visual (Toast de sucesso).
  - Testar renderização cronológica ordenada dos registros no drawer de histórico.
  - Testar exibição de alerta impeditivo visual quando a área estiver com status `INTERDITADA`.

## 5. Artefatos Demonstráveis para Avaliação
- Tela interativa demonstrando o fluxo completo de vistoria e atualização do estado da área de risco.
- Linha do tempo visual de inspeções passadas permitindo auditoria rápida das condições do ambiente de trabalho.
- Testes automatizados Vitest verdes sem falhas.

## 6. Governança do Modelo Orientado a Cronograma (Schedule-Driven Development)
Como o projeto adota o **Modelo Orientado a Cronograma**, o prazo semanal (timebox) é **fixo e inegociável**:
- **Timebox Fixo**: A entrega desta issue deve ser concluída impreterivelmente até o final da semana correspondente para avaliação do professor.
- **Estratégia Anti-Bloqueio (In-Memory Stubs)**: Se os endpoints REST de histórico sofrerem atraso, utilize mocks do Mock Service Worker (MSW) para simular o recebimento de laudos e testar a interface de forma autônoma.
- **Regra de Estabilidade de Integração (Trunk Stability)**: O código deve ser integrado à branch principal sem introduzir warnings no console ou falhas de lint.
- **Diretriz de Redução de Escopo (Scope Pruning)**: Se houver pressão de cronograma, foque no formulário essencial de inspeção e na atualização do status da área, postergando upload de fotos de vistoria para versões futuras.

## 7. Critérios de Aceite para Nota Máxima (Final do Timebox)
- [ ] Formulário modal de inspeção acessível e validando parecer técnico obrigatório.
- [ ] Visualização histórica de inspeções anteriores com autor e data da vistoria.
- [ ] Atualização reativa em tempo real do badge de status da área na interface do mapa.
- [ ] Testes automatizados com React Testing Library passando integralmente.
- [ ] PR submetido, com build verde e revisado dentro do timebox semanal da Semana 7.


</details>

