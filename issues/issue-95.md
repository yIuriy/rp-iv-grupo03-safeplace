# Issue #95: [37] [MVP][Ocorrências] Reconciliar contratos do relato, protocolo e ciclo de acompanhamento

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/95
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** Dyonathan-Laner
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:20:52Z
- **Updated at:** 2026-09-15T01:01:56Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF01/RF02/RF13/RF16; UC05/UC09; US01/US02/US13/US16.

**Referência do modelo:** Ocorrencia, Acidente, Incidente, Setor, Colaborador; dataOcorrencia e dataRegistro distintas; statusOcorrencia.

## O que entregar

Registrar a correspondência entre PNG de classes e DS09 antes de ampliar contratos. Preservar herança, atributos e relações; distinguir quem registra de quem esteve envolvido, protocolo de acompanhamento de numeroProtocoloCAT.

## Critérios de aceite

- [ ] Registrar decisão sobre protocolo comum a acidente/incidente, cardinalidade de envolvidos, Setor/local e vínculo de EPI relacionado à falha.
- [ ] Preservar data/hora do fato e do cadastro; DS09 exportado deve transmitir a data do fato explicitamente.
- [ ] Definir estados/transições e representação de gravidade para UC07 sem deduzir literais da enum vazia no PNG.
- [ ] Cadastro não exige CAT, laudo, testemunha ou mídia; não impor investigação de causa raiz ao relato rápido.
- [ ] Associar cada operação do diagrama a porta/caso de uso/domínio sem eliminar sua semântica.

## Dependências técnicas

Nenhuma dependência técnica obrigatória adicional para iniciar a análise/correção descrita. Observar as decisões e dependências condicionais abaixo.

## Decisões pendentes e limites

Decisão de modelo requerida: PNG tem protocolo CAT em Acidente; DS09 gera protocolo em Ocorrencia. Parte de persistência já definida pode avançar em #96. Setor deve seguir #111.

## Motivo da correção

Removidos formato CAT-ANO-MES-SEQUENCIAL e cadeia ABERTA→EM_TRIAGEM→ARQUIVADA como requisitos presumidos. Código existente não aprova essas decisões.

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
O SafePlace tem como missão central o registro e acompanhamento de acidentes e incidentes de trabalho:
- **RF01**: Acidentes causados por fator humano/funcionário.
- **RF02**: Acidentes causados por falha de equipamento de segurança (EPI).
- **RF13**: Supervisor cria registros de acidentes com dados completos.
- **RF16**: Registro de incidentes (quase-acidentes) comunicados ou identificados.
- **UC09 / UC05**: Geração de protocolo único obrigatório, distinção clara entre a *data do fato* e a *data/hora do registro*, além da identificação do usuário relator e colaboradores envolvidos.

Esta issue estabelece o núcleo puro de domínio de ocorrências.

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 1 - Fundação de Domínio e Credenciais
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#90: [MVP][Usuários] Definir Domínio e Portas para Gestor, Supervisor e Colaborador)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#96: [MVP][Ocorrências] Implementar Adaptador de Persistência e Migrações Flyway de Ocorrências)
  - [?] (#97: [MVP][Ocorrências] Modelar Domínio e Ciclo de Vida de Planos de Ação)
  - [?] (#99: [MVP][Ocorrências] Implementar Endpoints REST para Ocorrências, Triagem e Planos de Ação)

## 3. O Que Fazer
1. Refinar a hierarquia de entidades puras em `domain.ocorrencia`:
   - `Ocorrencia` (abstrata): `id`, `descricao`, `dataOcorrencia`, `dataRegistro`, `statusOcorrencia` (`ABERTA`, `EM_TRIAGEM`, `ARQUIVADA`), `testemunhas`, `colaborador`, `midias`, `area`, `gestor`.
   - `Acidente`: herda de `Ocorrencia`; adiciona `causaRaiz` (`FATOR_HUMANO`, `FALHA_EPI`, `OUTRO`), `dano`, `tipo`, `numeroProtocolo`, `destino`.
   - `Incidente`: herda de `Ocorrencia`; adiciona `potencialDano` e `situacaoRisco`.
2. Implementar gerador de protocolo da CAT único e imutável exclusivo para a classe Acidente, preenchendo o atributo numeroProtocoloCAT no formato CAT-ANO-MES-SEQUENCIAL (ex.: CAT-2026-09-0001) no momento da consolidação dos dados.
3. Definir regras de transição de estado:
   - Transição de `ABERTA` -> `EM_TRIAGEM` -> `ARQUIVADA`.
   - Proibição de alteração do protocolo após emissão.
4. Definir as portas em `application.port`:
   - `RegistrarOcorrenciaUseCase` e `GerenciarOcorrenciaUseCase`.
   - `OcorrenciaRepositoryPort`.

## 4. Como Fazer (Arquitetura Hexagonal)
- Manter o domínio puro sem referências a anotações JPA.
- Validar que a data do fato não pode ser futura.
- Garantir testes unitários abrangentes para as regras de transição de status e integridade do protocolo.


## 5. Exigência de Testes Automatizados (Critério Obrigatório para Nota Máxima)
Para garantir pontuação máxima na avaliação semanal do professor, o desenvolvedor deve entregar:
- **Testes Unitários de Domínio**: Cobertura das regras e invariantes usando JUnit 5 e AssertJ, sem mocks de banco ou contexto Spring.
- **Testes de Integração / API**: Validação com Spring `MockMvc` ou `DataJpaTest` cobrindo status HTTP (200, 201, 400, 403, 404, 422), integridade transacional e constraints do PostgreSQL.
- **Documentação Swagger**: Endpoints testados e validados no OpenAPI (`/swagger-ui/index.html`).


## 6. Governança do Modelo Orientado a Cronograma (Schedule-Driven Development)
Como o projeto adota o **Modelo Orientado a Cronograma**, o prazo semanal (timebox) é **fixo e inegociável**:
- **Timebox Fixo**: A entrega desta issue deve ser concluída impreterivelmente até o final da semana correspondente para avaliação do professor.
- **Estratégia Anti-Bloqueio (In-Memory Stubs)**: Caso uma issue predecessora atrase, o desenvolvedor **NÃO deve ficar bloqueado**. Deve criar uma implementação falsa em memória (In-Memory Fake/Mock) da Porta de Saída correspondente, garantindo que seu Caso de Uso ou Interface funcione e seja testado autonomamente.
- **Regra de Estabilidade de Integração (Trunk Stability)**: Nenhuma alteração pode quebrar a branch principal (`main` / `dev`). Ao final do timebox, a entrega deve ser submetida via Pull Request com testes verdes e build passando.
- **Diretriz de Redução de Escopo (Scope Pruning)**: Se houver risco iminente de estourar o timebox semanal, recursos secundários (como estilos visuais avançados ou filtros secundários) devem ser temporariamente postergados, priorizando o núcleo de domínio, os testes unitários e a API funcional.

## 7. Critérios de Aceite para Nota Máxima (Final do Timebox)
- [ ] Protocolo único gerado automaticamente e imutável.
- [ ] Distinção explícita entre Acidente (fator humano vs falha de EPI) e Incidente.
- [ ] Data do fato e data do cadastro armazenadas separadamente.
- [ ] Testes unitários do domínio cobrem todas as regras e exceções invariantes.
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

