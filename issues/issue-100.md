# Issue #100: [52] [MVP][Frontend] Registrar e acompanhar ocorrências e planos de ação

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/100
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:21:04Z
- **Updated at:** 2026-09-15T01:02:14Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF01/RF02/RF07/RF13/RF16; RNF11; UC05/UC07/UC09; US01/US02/US07/US13/US16.

**Referência do modelo:** Ocorrencia, Acidente, Incidente, PlanoDeAcao e relações aprovadas.

## O que entregar

Entregar telas conectadas à API para registro e acompanhamento de ocorrências, com operações de planos restritas ao Gestor.

## Critérios de aceite

- [ ] Formulário distingue acidente/incidente, data do fato, Setor, envolvidos e descrição segundo contrato aprovado.
- [ ] Confirmação exibe protocolo de acompanhamento; detalhe mostra data do cadastro separada da data do fato.
- [ ] Gestor consulta/edita/triageia/arquiva e acompanha ações, prazos, responsáveis e alertas; Supervisor acompanha relatos conforme UC09.
- [ ] Recarregar página mantém dados e estados; notificação ao Gestor é demonstrável.
- [ ] Fluxos de erro, campos obrigatórios, teclado e responsividade verificados com API real.

## Dependências técnicas

- [#94](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/94)
- [#99](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/99)

## Decisões pendentes e limites

Plano usa #97. Consulta ou envio offline ficam condicionados à decisão #105 e implementação #127.

## Motivo da correção

Retirados timeline com estados não aprovados e cache obrigatório de ocorrências. Não expor anexos ou geração CAT no MVP.

## Fontes verificáveis

- [Diagrama de classes](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/diagramas/classes/Diagrama%20de%20Classes%20-%20SafePlace.png).
- [Requisitos funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-funcionais.md) e [não funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-nao-funcionais.md).
- [MoSCoW](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/priorizacao-moscow.md) e [MVP, incluindo pendências da seção 11](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/mvp/especificacao-mvp-arquitetura.md).
- [Casos de uso](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/casos-de-uso/casos-de-uso.md) e [histórias de usuário](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/historias-de-usuario.md).
- [Base técnica consultada](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/backend/src/main/java/br/edu/safeplace/backend/adapters/out/persistencia/OcorrenciaJpaAdapter.java). Evidência do estado existente, não fonte de requisito.

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
Desenvolver a interface completa de gestão de ocorrências no frontend (`OccurrencesPage.tsx`), permitindo que supervisores e gestores registrem acidentes/incidentes, visualizem o protocolo gerado, realizem a triagem e definam os planos de ação corretivos (**RF01, RF02, RF07, RF13, RF16**), com suporte a consulta offline (**RNF08**).

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 4 - Frontend de Ocorrências, Empréstimos e Áreas de Risco
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#94: [MVP][Frontend] Implementar Telas de Login, Gestão de Usuários e Cache Offline Local)
  - [?] (#99: [MVP][Ocorrências] Implementar Endpoints REST para Ocorrências, Triagem e Planos de Ação)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#127: [MVP][Frontend] Implementar Sincronização Resiliente e Detecção de Conexão Offline)
  - [?] (#128: [MVP][Ocorrências] Implementar Consulta Avançada por Protocolo, Período e Exportação de Sumário)

## 3. O Que Fazer
1. Desenvolver em `features/ocorrencias`:
   - Formulário de Novo Registro com seletor claro de tipo: **Acidente** (com opções de Causa: Fator Humano vs Falha de EPI) ou **Incidente**.
   - Campos para data do fato, colaboradores envolvidos e descrição do evento.
   - Modal de Confirmação com exibição em destaque do **Número de Protocolo** gerado.
2. Criar visualização de detalhes da ocorrência:
   - Timeline de status (`ABERTA`, `EM_TRIAGEM`, `ARQUIVADA`).
   - Aba ou seção para listagem e cadastro de **Planos de Ação** associados (descrição, responsável, prazo e status).
3. Implementar Cache Offline Local (**RNF08**):
   - Salvar ocorrências carregadas no cache local para visualização sem internet no canteiro.
   - Exibir alerta amigável caso o usuário tente registrar nova ocorrência offline (conforme escopo documental do MVP).
4. Assegurar conformidade com acessibilidade WCAG 2.1 AA (**RNF11**).

## 4. Como Fazer
- Utilizar os componentes do design system (`Dialog`, `Tabs`, `Button`, `Fields`, `Feedback`).
- Isolar a camada de comunicação com a API em `features/ocorrencias/api.ts`.


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
- [ ] Registro de Acidente e Incidente funcional com exibição imediata do protocolo.
- [ ] Planos de ação podem ser cadastrados e atualizados na visualização da ocorrência.
- [ ] Ocorrências consultadas ficam acessíveis mesmo sem conexão à rede.
- [ ] Testes de componentes frontend cobrindo fluxos de sucesso e validações.
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

