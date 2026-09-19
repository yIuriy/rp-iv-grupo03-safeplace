# Issue #106: [54] [MVP][Empréstimos] Definir entrega, posse e devolução conforme UC12

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/106
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:21:18Z
- **Updated at:** 2026-09-15T01:02:25Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF11; UC12; US11.

**Referência do modelo:** Emprestimo: dataRetirada, dataDevolucaoPrevista, dataDevolucaoReal, status:ClassificacaoEmprestimo; vínculos Colaborador/EPI.

## O que entregar

Implementar o contrato aprovado de empréstimo para Colaborador cadastrado, com Supervisor/Gestor como operador e movimentos coerentes de estoque.

## Critérios de aceite

- [ ] Conservar identidade do equipamento e beneficiário sem criar conta para Colaborador.
- [ ] Entrega exige saldo, EPI em situação permitida e CA válido; registrar termo de cautela conforme UC12, sem presumir PDF/assinatura digital.
- [ ] Registrar data/hora e operador; pendências e atraso são consultáveis.
- [ ] Duplicidade de tipo em posse ativa gera alerta e exige confirmação/justificativa, não bloqueio absoluto inventado.
- [ ] Devolução registra estado apto/manutenção/indicação de descarte conforme UC12; não creditar automaticamente tudo como disponível nem executar RF14.
- [ ] Entrega com treinamento obrigatório vencido é impedida pela integração com capacitações.

## Dependências técnicas

- [#124](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/124)

## Decisões pendentes e limites

Matrícula/identificação em #90 e representação das exigências em #116/#117. Enumeradores do PNG estão vazios: estados precisam de contrato explícito.

## Motivo da correção

Retirados EmprestimoEpi/estados como obrigação nominal e recomposição indiscriminada de saldo. Modelo não depende de geração de senha.

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
O **RF11** e o **UC12** determinam o controle da rastreabilidade dos EPIs:
- Registrar empréstimos de EPIs para colaboradores cadastrados (sem conta de login).
- Registrar a devolução do equipamento, retornando-o ao estoque.
- Identificar empréstimos pendentes ou atrasados.
- Baixa automática no saldo disponível do EPI no momento do empréstimo e recomposição no momento da devolução.

Esta issue cria o modelo de domínio puro para rastreabilidade de empréstimos.

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 4 - Frontend de Ocorrências, Empréstimos e Áreas de Risco
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#90: [MVP][Usuários] Definir Domínio e Portas para Gestor, Supervisor e Colaborador)
  - [?] (#101: [MVP][EPI] Modelar Domínio de Manutenção de EPIs e Transições de Status com Validação de CA)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#107: [MVP][EPI] Implementar Persistência JPA e Migração Flyway para Empréstimos e Devoluções)
  - [?] (#108: [MVP][EPI] Implementar Algoritmo de Substituição Inteligente e Alertas Preventivos de EPI)
  - [?] (#124: [MVP][EPI] Modelar Rastreabilidade por Unidade Física de EPI)
  - [?] (#109: [MVP][EPI] Implementar Endpoints REST para Empréstimos, Devoluções e Projeções de Substituição)

## 3. O Que Fazer
1. Criar a entidade de domínio `EmprestimoEpi` em `domain.epi`:
   - Atributos: `id`, `epiId`, `colaboradorId`, `dataEmprestimo`, `dataPrevisaoDevolucao`, `dataDevolucaoEfetiva`, `status` (`EM_USO`, `DEVOLVIDO`, `ATRASADO`), `observacoes`.
2. Implementar regras invariantes no domínio:
   - Empréstimo exige saldo disponível do EPI (`quantidade > 0`).
   - Ao realizar o empréstimo, deduzir o estoque do EPI.
   - Ao registrar devolução, restaurar o estoque do EPI e definir `dataDevolucaoEfetiva`.
   - Método para checar se o empréstimo está atrasado baseado na data atual (`isAtrasado(LocalDate hoje)`).
3. Declarar as portas em `application.port`:
   - `port.in.ControlarEmprestimoEpiUseCase`: métodos para emprestar, devolver e listar pendências.
   - `port.out.EmprestimoEpiRepositoryPort`.

## 4. Como Fazer (Arquitetura Hexagonal)
- Entidades ricas e isoladas no domínio.
- Orquestração no caso de uso garantindo consistência entre o empréstimo e o estoque do EPI.


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
- [ ] Empréstimo debita estoque do EPI e devolução recompõe o estoque.
- [ ] Colaborador vinculado pelo seu identificador de cadastro.
- [ ] Regra de detecção de atraso coberta por testes unitários.
- [ ] Testes unitários cobrem cenários sem saldo de estoque (`SaldoInsuficienteException`).
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

