# Issue #125: [68] [MVP][Áreas] Consultar EPIs obrigatórios para acesso à área

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/125
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:36:54Z
- **Updated at:** 2026-09-15T01:03:18Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF05; UC03; US05.

**Referência do modelo:** AreaRisco.verificarEPIsObrigatoriosArea e vínculos de proteção.

## O que entregar

Completar consulta das proteções exigidas pela área e garantir vínculo obrigatório no cadastro/atualização, sem criar controle de entrada física de pessoas.

## Critérios de aceite

- [ ] Toda área cadastrada mantém ao menos um EPI obrigatório, independentemente do grau.
- [ ] Supervisor consulta a lista de exigências do setor/área; Gestor mantém vínculos.
- [ ] Equipamento inexistente não pode ser vinculado; consulta usa referências reais.
- [ ] API e interface #115 apresentam dados persistidos e erros coerentes.

## Dependências técnicas

- [#111](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/111)
- [#113](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/113)

## Decisões pendentes e limites

Nenhuma dependência de empréstimo é necessária para consultar exigências. Controle de acesso físico por posse de EPI requer requisito próprio e não foi aprovado.

## Motivo da correção

UC03 não especifica verificar se pessoa está usando EPI nem endpoint APTO/INAPTO por empréstimos. Recorte corrigido para comportamento efetivamente documentado.

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
Conforme o **RF05** e **UC03**, cada setor físico de risco exige o uso obrigatório de EPIs específicos para acesso. Esta issue implementa o serviço que verifica se um colaborador possui os EPIs ativos necessários para ingressar em determinada área de risco.

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 6 - APIs de Empréstimo, Áreas e Domínio de Capacitações
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#106: [MVP][EPI] Modelar Domínio de Empréstimo e Devolução de EPIs para Colaboradores)
  - [?] (#111: [MVP][Áreas de Risco] Modelar Domínio e Portas para Mapeamento de Áreas de Risco)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#115: [MVP][Frontend] Construir Mapa de Áreas de Risco e Painel de Tarefas no Frontend)

## 3. O Que Fazer
1. Criar serviço de domínio `VerificadorAcessoAreaRiscoService`:
   - Recebe `colaboradorId` e `areaRiscoId`.
   - Consulta os EPIs obrigatórios para acesso àquela área.
   - Consulta os empréstimos ativos do colaborador e valida se ele está com todos os EPIs requeridos em mãos.
   - Retorna conformidade (`APTO` ou `INAPTO` com lista de EPIs faltantes).
2. Adicionar endpoint REST em `AreaDeRiscoController`:
   - `POST /api/areas-de-risco/{id}/verificar-acesso`: valida se o colaborador pode ingressar na área.

## 4. Como Fazer (Arquitetura Hexagonal)
- Lógica de conformidade pura desacoplada no domínio/aplicação.
- Resposta rica orientando o supervisor sobre quais proteções faltam.


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
- [ ] Verificação de proteção de acesso a áreas de risco validada.
- [ ] Endpoint retorna lista explícita de EPIs faltantes em caso de inaptidão.
- [ ] Testes unitários cobrindo colaboradores com proteção completa e parcial.
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

