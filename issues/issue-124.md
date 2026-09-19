# Issue #124: [62] [MVP][Decisão] Fixar identidade de EPI, lote, modelo, CA e manutenção

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/124
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:36:52Z
- **Updated at:** 2026-09-15T01:03:16Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF03/RF04/RF11/RF21; UC01/UC06/UC10/UC12; US03/US04/US11/US21.

**Referência do modelo:** EPI, EspecificacaoEPI, LoteEPI, ModeloEPI, MovimentacaoEstoque, ManutencaoEPI e associações do PNG.

## O que entregar

Resolver unidade representada por EPI e rastreabilidade antes de introduzir ItemEPI. Registrar modelo e contratos que mantenham localização, quantidade, lote, modelo, CA e histórico coerentes.

## Critérios de aceite

- [ ] Registrar se EPI representa unidade/tipo/agrupamento e como quantidade/status correspondem a empréstimo e manutenção.
- [ ] Preservar relações do PNG; decidir dados de lote/modelo necessários no MVP sem promover gestão de fornecedores RF20.
- [ ] Definir fonte única e tipo do CA; evitar CertificadoAprovacao paralelo a ModeloEPI sem correspondência.
- [ ] Resolver resultado booleano versus enum/texto e relação com situação do equipamento; separar manutenção de descarte definitivo.
- [ ] Conferir retorno de buscarHistorico e dados de lote/responsável das movimentações.
- [ ] ItemEPI, número de série e código de barras permanecem propostas; nenhuma é implementada automaticamente.

## Dependências técnicas

Nenhuma dependência técnica obrigatória adicional para iniciar a análise/correção descrita. Observar as decisões e dependências condicionais abaixo.

## Decisões pendentes e limites

Decisão humana necessária. Fornecedor, descarte e consulta externa de CA permanecem backlog; dados mínimos de CA necessários aos UCs exigem definição própria.

## Motivo da correção

Issue antiga declarava ItemEPI aprovado, invertia dependências e inventava código de barras. Decisão deve preceder #106/#108 e ajustes estruturais.

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
Para cumprir a rastreabilidade estrita do **RF11** e alinhar a proposta conceitual do diagrama de classes (separando `EPI` genérico de sua unidade física rastreável `ItemEpi` com identificador individual/lote):
- Permitir o rastreamento individual do equipamento entregue ao colaborador.
- Garantir histórico de posse por número de série ou lote do fabricante.

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 5 - Rastreabilidade, Substituição e Persistência de Áreas
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#101: [MVP][EPI] Modelar Domínio de Manutenção de EPIs e Transições de Status com Validação de CA)
  - [?] (#106: [MVP][EPI] Modelar Domínio de Empréstimo e Devolução de EPIs para Colaboradores)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#109: [MVP][EPI] Implementar Endpoints REST para Empréstimos, Devoluções e Projeções de Substituição)
  - [?] (#110: [MVP][Frontend] Construir Interface de Empréstimos, Devoluções e Alertas de Substituição Inteligente)

## 3. O Que Fazer
1. Criar entidade de domínio `ItemEpi` em `domain.epi`:
   - Atributos: `id`, `epiId`, `codigoIdentificador`, `numeroLote`, `status` (`NOVO`, `EM_USO`, `EM_MANUTENCAO`, `DESCARTADO`).
2. Vincular `EmprestimoEpi` opcionalmente a um `ItemEpi` específico.
3. Criar migração Flyway e adaptador JPA `ItemEpiJpaAdapter`.
4. Adicionar métodos de consulta por código de barras / identificador individual.

## 4. Como Fazer (Arquitetura Hexagonal)
- Manter o domínio puro.
- Relacionar `ItemEpi` à entidade de catálogo `Epi`.


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
- [ ] Unidade física de EPI modelada e persistida.
- [ ] Histórico de empréstimos rastreia o item específico ou lote.
- [ ] Testes de domínio e repositório cobrindo a rastreabilidade unitária.
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

