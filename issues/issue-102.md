# Issue #102: [43] [MVP][Manutenção] Gravar histórico e estado do EPI de forma durável

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/102
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:21:09Z
- **Updated at:** 2026-09-15T01:02:18Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF04; RNF05; UC01; US04.

**Referência do modelo:** ManutencaoEPI e associação com EPI.

## O que entregar

Substituir o repositório operacional em memória por persistência real, preservando histórico e estado do equipamento na mesma operação.

## Critérios de aceite

- [ ] Histórico permanece após reiniciar aplicação e banco.
- [ ] Manutenção e status confirmam ou falham juntos; rollback não deixa registro só em memória.
- [ ] Mapeamento conserva data, descrição, resultado aprovado e EPI correto; operador autenticado é rastreável.
- [ ] Migração não reutiliza V6; catálogo e dados existentes são preservados.
- [ ] Reprovação não executa descarte definitivo nem mistura estado de uma unidade com saldo coletivo.

## Dependências técnicas

- [#124](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/124)

## Decisões pendentes e limites

O contrato aprovado em #124 orienta ajuste da base #101. Auditoria usa #121.

## Motivo da correção

Persistência atual é InMemoryManutencaoEpiRepository; nova tabela deve usar versão livre, não V6 ocupada por áreas.

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
Persistir o histórico de manutenções preventivas e corretivas dos EPIs no PostgreSQL, assegurando a rastreabilidade das intervenções e o estado atual do equipamento.

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 2 - APIs de Usuários e Persistência de Ocorrências
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#101: [MVP][EPI] Modelar Domínio de Manutenção de EPIs e Transições de Status com Validação de CA)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#103: [MVP][EPI] Implementar Endpoints REST para Controle e Registro de Manutenção de EPIs)
  - [?] (#123: [MVP][EPI] Implementar Alertas Automáticos de Estoque Crítico e Reposição)
  - [?] (#104: [MVP][Frontend] Construir Visualização e Gestão de Manutenção de EPIs no Frontend)

## 3. O Que Fazer
1. Criar migração Flyway `V6__create_manutencoes_epi.sql`:
   - Tabela `manutencoes_epi` com colunas: `id`, `epi_id` (FK para `epis.id`), `data_manutencao`, `tipo_manutencao`, `descricao`, `resultado`, `responsavel`, `criado_em`.
2. Criar entidade JPA `ManutencaoEpiEntity` em `adapters.out.persistencia`.
3. Atualizar `EpiEntity` para refletir alterações de status persistidas.
4. Implementar `ManutencaoEpiJpaAdapter` implementando `ManutencaoEpiRepositoryPort`:
   - Salvar registro de manutenção.
   - Listar histórico de manutenções por `epiId`.
   - Listar EPIs atualmente em manutenção.

## 4. Como Fazer
- Mapeamento bidirecional rigoroso entre `ManutencaoEpiEntity` e `ManutencaoEpi` de domínio.
- Gerenciar transações (`@Transactional`) para sincronizar a criação do log de manutenção e a atualização do status do EPI.


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
- [ ] Migração Flyway aplicada com sucesso.
- [ ] Registros de manutenção vinculados corretamente aos EPIs.
- [ ] Status do EPI atualizado de forma atômica no banco de dados.
- [ ] Testes de integração de persistência cobrindo histórico e transição de status.
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

