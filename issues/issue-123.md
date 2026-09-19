# Issue #123: [50] [MVP][Estoque] Exibir saldo crítico conforme quantidade mínima

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/123
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:36:50Z
- **Updated at:** 2026-09-15T01:03:14Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF03; UC06; US03.

**Referência do modelo:** EspecificacaoEPI.quantidadeMinima; EPI.compararComQuantMinima/buscarEPIsAbaixoDaQuantidadeMinima.

## O que entregar

Usar comparação de saldo com mínimo na consulta e após movimentação, disponibilizando alerta visual do estoque crítico.

## Critérios de aceite

- [ ] Saldo menor ou igual ao mínimo é crítico, inclusive igualdade.
- [ ] Saldo acima do mínimo não permanece crítico após entrada.
- [ ] Valor exibido corresponde ao saldo persistido e especificação aprovada.
- [ ] Consulta e UI #133 usam mesma regra e indicam situação sem depender apenas de cor.
- [ ] Teste com saldo 10, saída/entrada e cruzamento do mínimo comprova atualização.

## Dependências técnicas

- [#131](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/131)

## Decisões pendentes e limites

Não depende da manutenção. Configuração/origem do mínimo segue modelo #124.

## Motivo da correção

Não criar e-mail, compra automática, déficit obrigatório ou sistema novo de notificações. Estoque crítico não substitui projeção RF21.

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
Conforme o **RF03** e **UC06**, o sistema deve emitir alertas automáticos quando o saldo de um EPI atingir ou ficar abaixo do estoque mínimo configurado (`estoqueMinimo`), sinalizando a necessidade urgente de reposição antes da indisponibilidade operacional.

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 3 - Frontend de Usuários e Gestão de Manutenção
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#101: [MVP][EPI] Modelar Domínio de Manutenção de EPIs e Transições de Status com Validação de CA)
  - [?] (#102: [MVP][EPI] Implementar Persistência JPA e Migração Flyway para Histórico de Manutenção)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#104: [MVP][Frontend] Construir Visualização e Gestão de Manutenção de EPIs no Frontend)
  - [?] (#108: [MVP][EPI] Implementar Algoritmo de Substituição Inteligente e Alertas Preventivos de EPI)

## 3. O Que Fazer
1. Criar serviço de domínio / aplicação `AlertaEstoqueService`:
   - Consultar EPIs onde `quantidade <= estoqueMinimo`.
   - Gerar DTOs de alerta de estoque crítico com quantidade atual, estoque mínimo e déficit.
2. Adicionar endpoint REST em `EpiController`:
   - `GET /api/epis/alertas-estoque-critico`: lista todos os equipamentos com risco iminente de desabastecimento.
3. Integrar com as movimentações de saída para disparar verificação imediata de saldo crítico.

## 4. Como Fazer (Arquitetura Hexagonal)
- O cálculo do estado crítico é derivado do método `isEstoqueCritico()` da entidade rica `Epi`.
- Resposta via DTO de saída estruturado.


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
- [ ] Endpoint `/api/epis/alertas-estoque-critico` retorna os itens deficitários.
- [ ] Disparo de alerta quando uma movimentação de saída atinge o limiar mínimo.
- [ ] Testes unitários e de integração validando alertas em diferentes níveis de saldo.
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

