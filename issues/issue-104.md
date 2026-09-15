# Issue #104: [53] [MVP][Frontend] Consultar histórico e registrar manutenção de EPIs

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/104
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:21:14Z
- **Updated at:** 2026-09-15T01:02:21Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF04; RNF11; UC01; US04.

**Referência do modelo:** EPI e ManutencaoEPI conforme #124.

## O que entregar

Permitir ao Gestor consultar EPI, histórico/datas, filtrar situação e registrar manutenção integrada à API.

## Critérios de aceite

- [ ] Exibir apenas ativos na listagem operacional e aplicar filtro por situação aprovada.
- [ ] Solicitar data, descrição e resultado conforme contrato; não criar ordem de serviço não prevista.
- [ ] Registro atualiza situação e histórico persistidos; falha não apresenta sucesso.
- [ ] Dados continuam após recarregar; inexistência de EPI permite nova busca.
- [ ] Navegação por teclado e indicação de estado não dependem só de cor.

## Dependências técnicas

- [#94](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/94)
- [#103](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/103)

## Decisões pendentes e limites

Não depende de alertas de reposição #123 para operar. Offline somente se aprovado em #105.

## Motivo da correção

Retiradas dependências artificiais e enumerações impostas pela implementação.

## Fontes verificáveis

- [Diagrama de classes](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/diagramas/classes/Diagrama%20de%20Classes%20-%20SafePlace.png).
- [Requisitos funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-funcionais.md) e [não funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-nao-funcionais.md).
- [MoSCoW](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/priorizacao-moscow.md) e [MVP, incluindo pendências da seção 11](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/mvp/especificacao-mvp-arquitetura.md).
- [Casos de uso](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/casos-de-uso/casos-de-uso.md) e [histórias de usuário](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/historias-de-usuario.md).
- [Base técnica consultada](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/backend/src/main/java/br/edu/safeplace/backend/application/usecase/ControlarManutencaoEpiService.java). Evidência do estado existente, não fonte de requisito.

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
Expandir o módulo de EPIs na interface gráfica (`EpiPage.tsx`), permitindo aos usuários acompanhar o status operacional dos equipamentos, filtrar itens em manutenção e submeter ordens de manutenção com seus resultados (**RF04, UC01**).

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 4 - Frontend de Ocorrências, Empréstimos e Áreas de Risco
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#94: [MVP][Frontend] Implementar Telas de Login, Gestão de Usuários e Cache Offline Local)
  - [?] (#103: [MVP][EPI] Implementar Endpoints REST para Controle e Registro de Manutenção de EPIs)
  - [?] (#123: [MVP][EPI] Implementar Alertas Automáticos de Estoque Crítico e Reposição)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#105: [MVP][Frontend] Implementar Cache Local e Suporte Offline para Catálogo e Estoque de EPIs)
  - [?] (#110: [MVP][Frontend] Construir Interface de Empréstimos, Devoluções e Alertas de Substituição Inteligente)

## 3. O Que Fazer
1. Atualizar `features/epis/EpiPage.tsx`:
   - Badges visuais de status: `DISPONIVEL`, `EM_MANUTENCAO`, `ESGOTADO`.
   - Adicionar aba ou botão para visualização do **Histórico de Manutenções** por EPI.
   - Modal para "Enviar para Manutenção" e "Registrar Conclusão de Manutenção" (aprovado/reprovado).
2. Adicionar filtros por status de conservação e validade do Certificado de Aprovação.
3. Exibir alerta visual em destaque quando um equipamento estiver com o CA vencido.
4. Seguir acessibilidade WCAG 2.1 AA.

## 4. Como Fazer
- Utilizar os componentes do Design System existente (`Tabs`, `Dialog`, `Fields`, `Feedback`).
- Centralizar requisições em `features/epis/api.ts`.


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
- [ ] Gestão visual do status de manutenção de cada EPI.
- [ ] Formulário de registro de manutenção com validação e feedback.
- [ ] Alerta em caso de CA vencido exibido na tela.
- [ ] Testes de interface garantindo renderização e submissão correta.
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

