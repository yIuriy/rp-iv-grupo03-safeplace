# Issue #107: [59] [MVP][Empréstimos] Persistir posse e movimentos sem perda ou duplicação

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/107
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:21:21Z
- **Updated at:** 2026-09-15T01:02:37Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF11; RNF05; UC12; US11.

**Referência do modelo:** Emprestimo, Colaborador, EPI e movimentos segundo #106/#124.

## O que entregar

Gravar entrega/devolução, operador e efeito no estoque de forma transacional, preservando a rastreabilidade ao consultar novamente.

## Critérios de aceite

- [ ] Empréstimo conserva equipamento, beneficiário, operador e datas previstas/reais.
- [ ] Duas entregas concorrentes não consomem a mesma disponibilidade; devolução repetida não duplica crédito.
- [ ] Falha desfaz empréstimo/movimento/saldo juntos.
- [ ] Itens devolvidos para manutenção não aparecem automaticamente disponíveis.
- [ ] Migração incremental preserva V7 já existente; PostgreSQL valida FKs e reconstrução.

## Dependências técnicas

- [#106](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/106)
- [#131](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/131)

## Decisões pendentes e limites

Contratos definidos em #124/#106; capacidade de auditoria #121 usada na conclusão integrada.

## Motivo da correção

Eliminada dependência artificial de #91 e número fixo V7; preservar dados e verificar concorrência.

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
Persistir os registros de empréstimo e devolução de EPIs no banco de dados relacional, mantendo integridade com as tabelas de `epis` e `usuarios` (colaboradores).

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 5 - Rastreabilidade, Substituição e Persistência de Áreas
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#91: [MVP][Usuários] Implementar Geração Automática de Credenciais e Hash de Senha para Supervisor)
  - [?] (#106: [MVP][EPI] Modelar Domínio de Empréstimo e Devolução de EPIs para Colaboradores)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#109: [MVP][EPI] Implementar Endpoints REST para Empréstimos, Devoluções e Projeções de Substituição)
  - [?] (#110: [MVP][Frontend] Construir Interface de Empréstimos, Devoluções e Alertas de Substituição Inteligente)

## 3. O Que Fazer
1. Criar migração Flyway `V7__create_emprestimos_epi.sql`:
   - Tabela `emprestimos_epi` com: `id`, `epi_id` (FK), `colaborador_id` (FK), `data_emprestimo`, `data_previsao_devolucao`, `data_devolucao`, `status`, `observacoes`, `criado_em`.
2. Criar entidade JPA `EmprestimoEpiEntity` em `adapters.out.persistencia`.
3. Implementar `EmprestimoEpiJpaAdapter` implementando `EmprestimoEpiRepositoryPort`:
   - Salvar empréstimo.
   - Buscar por ID.
   - Listar empréstimos ativos por colaborador.
   - Listar todos os empréstimos pendentes/atrasados.
4. Garantir atomicidade transacional entre o registro do empréstimo e a movimentação de estoque do EPI.

## 4. Como Fazer
- Mapeamento bidirecional estrito entre JPA e Domínio.
- Anotar operações no serviço de aplicação com `@Transactional`.


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
- [ ] Migração Flyway aplicada sem erros.
- [ ] Repositório JPA executa consultas e atualizações de devolução corretamente.
- [ ] Atomicidade garantida entre empréstimo e atualização de saldo do EPI.
- [ ] Testes de integração de banco cobrindo o ciclo completo de empréstimo e devolução.
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

