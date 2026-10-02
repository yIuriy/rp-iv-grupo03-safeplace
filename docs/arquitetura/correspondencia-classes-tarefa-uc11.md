# Correspondência entre Diagrama de Classes e UC11 (Tarefa, NivelPerigo e Autoria da Classificação)

## 1. Visão Geral e Contexto

Este documento registra o contrato da classificação de periculosidade das tarefas do SafePlace. Ele concilia a classe `Tarefa` e a enumeração `NivelPerigo` do **Diagrama de Classes** com o **UC11 (Classificar Nível de Periculosidade)** e a **US06**, atendendo à [issue #112](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/112) (RF06; UC11; US06).

- **Situação**: proposta de contrato submetida à aprovação do grupo na issue #112. A persistência de relações ([#113](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/113)), a API complementar ([#114](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/114)) e o bloqueio de alocação ([#117](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/117) e [#126](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/126)) devem seguir as decisões aprovadas. As pendências da seção 6 não foram decididas e não devem ser supridas por suposição.
- **Fontes**: [Diagrama de classes](../diagramas/classes/Diagrama%20de%20Classes%20-%20SafePlace.png), [UC03](../casos-de-uso/casos-de-uso.md#uc03--mapear-áreas-de-risco), [UC11](../casos-de-uso/casos-de-uso.md#uc11--classificar-nível-de-periculosidade), [US06](../historias-de-usuario.md#us06--classificar-a-periculosidade-das-tarefas), [RF06](../requisitos/requisitos-funcionais.md#rf06-o-sistema-deve-permitir-classificar-o-nível-de-periculosidade-da-tarefa-a-ser-realizada), [RNF03 e RNF05](../requisitos/requisitos-nao-funcionais.md) e o [glossário](../glossario.md#grau-de-risco).
- **Implementação de referência**: domínio em `backend/src/main/java/br/edu/safeplace/backend/domain/tarefa/` (`Tarefa`, `RegistroClassificacaoTarefa`) e `domain/comum/NivelPerigo`; caso de uso `TarefaUseCase`; persistência `TarefaJpaAdapter` com a migração `V23__registrar_responsavel_classificacao_tarefa.sql`; API `/api/tarefas`; tela `frontend/src/features/tarefas/`.

### 1.1. Modelo de referência

O PNG define:

- **`Tarefa`**: atributos `descricao : String` e `status : NivelPerigo`; operações `consultarTarefas() : List<Tarefa>`, `associarEPIs(listaIds : List<int>) : boolean` e `verificarEPIsObrigatoriosTarefa() : List<EPI>`.
- **Associações de `Tarefa`**: com `Setor` (`Setor` 0..* para `Tarefa` 1..*), com `AreaRisco` (`AreaRisco` 0..1 para `Tarefa` 0..*) e com `EPI` (0..* nos dois lados).
- **`NivelPerigo`**: enumeração usada por `Tarefa.status`, `AreaRisco.status`, `Setor.status` e `AlertaComportamento.nivelGravidade`.

### 1.2. Histórico da enumeração `NivelPerigo` no PNG

A revisão de escopo da issue #112 consultou o PNG no commit `34d6b6d` (14/09/2026). Naquela versão, `NivelPerigo` aparecia **sem literais**, e a issue determina não afirmar que a enumeração vazia define valores. Os literais `Baixo`, `Medio`, `Alto` e `Critico` foram acrescentados ao PNG no commit `b34c7a1` (20/09/2026), junto com a correspondência do DS09. Eles reproduzem o vocabulário de UC03 (Cenário Alternativo I: "baixo, médio, alto, crítico"), que já estava implementado desde a migração V6. Este contrato não trata o acréscimo ao PNG como aprovação por si só; a decisão está na seção 2.1.

---

## 2. Decisões de Modelo Registradas

### 2.1. Vocabulário único: `NivelPerigo` para áreas, setores e tarefas

- **Decisão**: há **um único vocabulário** de grau de risco, o de `NivelPerigo`: `BAIXO`, `MEDIO`, `ALTO` e `CRITICO`, exibidos como Baixo, Médio, Alto e Crítico. `Tarefa.status` usa esse vocabulário, como `AreaRisco.status` e `Setor.status`. Não se cria enumeração própria para tarefas.
- **Fundamento estrutural**: desde a versão `34d6b6d`, o PNG tipa `Tarefa.status` como `NivelPerigo`, o mesmo tipo de `AreaRisco.status`. A escolha de um tipo comum não depende dos literais da enumeração: uma lista separada para tarefas contrariaria a associação de tipos do modelo.
- **Correspondência explícita com UC11 e US06**: os termos leve, moderado, grave e crítico, usados em UC11 (cenário principal, passo 4) e US06 (dicionário), correspondem, pela posição na escala crescente de quatro níveis, a:

| UC11 / US06 | `NivelPerigo` | Rótulo na interface |
| --- | --- | --- |
| Leve | `BAIXO` | Baixo |
| Moderado | `MEDIO` | Médio |
| Grave | `ALTO` | Alto |
| Crítico | `CRITICO` | Crítico |

- **Alcance da correspondência**: serve para ler os textos de UC11 e US06. Leve, moderado e grave **não** são valores aceitos pela API nem pela interface; um valor fora de `NivelPerigo` é recusado com HTTP 400 (`TarefaControllerTest`). A correspondência não traz critério normativo para escolher cada nível (seção 2.5).
- **Após a aprovação**: UC11 e US06 devem passar a citar os níveis de `NivelPerigo`. Esta proposta não reescreve os textos dos casos de uso e das histórias; apenas aponta para este contrato.

### 2.2. Relações de `Tarefa` com `Setor`, `AreaRisco` e `EPI`

| Associação no PNG | Leitura | Situação |
| --- | --- | --- |
| `Setor` 0..* para `Tarefa` 1..* | Cada setor se relaciona com uma ou mais tarefas; cada tarefa, com zero ou mais setores. | **Não implementada.** Depende do contrato espacial de [#111](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/111) e da persistência de #113. O mínimo 1..* do lado da tarefa não é imposto ao cadastro de setor, que já existe sem tarefas; a divergência fica registrada para #111. |
| `AreaRisco` 0..1 para `Tarefa` 0..* | Cada tarefa pertence a no máximo uma área de risco; uma área tem zero ou mais tarefas. | **Não implementada.** Mesma dependência de #111 e #113. Não se deriva área ou setor de texto livre. |
| `Tarefa` 0..* para `EPI` 0..* (`associarEPIs`, `verificarEPIsObrigatoriosTarefa`) | EPIs exigidos para executar a tarefa. | **Fora do MVP** (RF12, UC08, backlog). Não é exigido e não é confundido com os EPIs de acesso à área (UC03). |

### 2.3. Classificação, reavaliação e autoria

- **Quem classifica**: o Gestor de Segurança, ator principal de UC11, define e reavalia. O Supervisor, ator secundário, apenas consulta (US06, regra 1). A regra de acesso já existente permite `GET /api/tarefas/**` ao Gestor e ao Supervisor e restringe `POST` e `PATCH` ao Gestor (RNF03).
- **Quando há classificação**: no cadastro com nível (`POST /api/tarefas` com `nivelPerigo`), na primeira classificação e em cada reavaliação (`PATCH /api/tarefas/{id}/classificacao`). Reavaliar para o mesmo nível é aceito e registrado, porque confirma a avaliação periódica (UC11, alternativo I). Uma tarefa classificada não volta a ficar sem grau de risco.
- **O que é registrado** (UC11, regra 1; US06, critério 2, e dicionário):
  - na própria tarefa: `status` (`nivelPerigo`), `dataClassificacao` (data e hora) e `responsavelClassificacao` (o Gestor de Segurança autenticado), persistidos em `tarefas.nivel_perigo`, `tarefas.data_classificacao` e `tarefas.responsavel_classificacao_id` (V23, chave estrangeira para `usuarios`);
  - no histórico: um `LogAuditoria` por classificação, com `responsavel` (e-mail do operador), `dataHora` e `justificativa` no formato "Classificação de periculosidade da tarefa: de {anterior} para {novo}; responsável técnico: {nome} (id {id})". O valor anterior é "sem classificação" na primeira vez. O domínio devolve esse dado como `RegistroClassificacaoTarefa`, sem criar entidade nova.
- **Responsável técnico**: é o Gestor de Segurança autenticado, resolvido pelo e-mail da sessão. Sem um Gestor identificável, a classificação é recusada (HTTP 403) e nada é gravado. O cadastro de tarefa **sem** nível não exige responsável.
- **Dados anteriores**: tarefas classificadas antes da V23 continuam com nível e data, mas com responsável nulo. Não se preenche autoria retroativa; a interface mostra "Responsável não registrado".

### 2.4. Tarefa sem classificação e bloqueio de alocação

- **Identificação**: `Tarefa.estaClassificada()` e o campo `classificada` da API; na interface, "Não classificada" e "Pendente de avaliação".
- **Bloqueio**: `Tarefa.validarClassificacaoParaAlocacao()` lança `TarefaSemClassificacaoException` (tratada como HTTP 409 por `TarefaTratadorExcecoes`) para tarefa sem grau de risco (UC11, exceção I; US06, critério 3).
- **Ponto de alocação**: não existe operação de alocação no sistema. A chamada dessa verificação no momento da confirmação da alocação pertence a #117 e #126, que devem usá-la na mutação final, não apenas em consulta prévia.

### 2.5. O que não é criado

- Nenhum atributo `titulo`, método `isAltoRisco()` ou regra normativa automática (por exemplo, derivar o nível ou exigências a partir de NR-1, NR-6 ou NR-9).
- **Exigências normativas** (UC11, passo 4; US06, dicionário "A confirmar"; UC11, regra 2): não são representadas. O PNG não tem atributo para elas, e a US06 marca conteúdo e critérios como a confirmar. A consulta do Supervisor mostra nível, data e responsável; as exigências registradas (US06, critério 4) aguardam a decisão da seção 6.
- O recálculo automático de requisitos de EPIs e certificações (UC11, alternativo I, passo 2) depende de RF12 e de #117, e não é feito aqui.

---

## 3. Matriz de Correspondência: Diagrama e UC11 vs. Arquitetura Hexagonal

| Elemento | Destino / Camada Hexagonal | Papel arquitetural e semântica |
| :--- | :--- | :--- |
| `Tarefa.descricao` | Domínio `Tarefa.descricao`; coluna `tarefas.descricao` | Obrigatória. |
| `Tarefa.status : NivelPerigo` | Domínio `Tarefa.nivelPerigo`; coluna `tarefas.nivel_perigo`; API `nivelPerigo` | Mesmo atributo com nome técnico diferente; nulo enquanto não classificada. |
| `consultarTarefas()` | Porta de entrada `ClassificarTarefaUseCase.listar` e `listarPorNivelPerigo`; `GET /api/tarefas?nivelPerigo=` | Matriz de periculosidade para Gestor e Supervisor. |
| Classificar e reavaliar (UC11, passos 3 a 6 e alternativo I) | `ClassificarTarefaUseCase.classificar`; domínio `Tarefa.classificar(nivel, responsavel)`; `PATCH /api/tarefas/{id}/classificacao` | Grava nível, data, hora e responsável técnico e devolve `RegistroClassificacaoTarefa`. |
| Registro com data, hora e responsável técnico (UC11, regra 1) | `LogAuditoriaRepositoryPort.salvar` e `tarefas.responsavel_classificacao_id` | Histórico com valor anterior e novo (RNF05). |
| Bloqueio de alocação (UC11, exceção I) | Domínio `Tarefa.validarClassificacaoParaAlocacao()` | Usado pelo ponto de alocação de #117 e #126. |
| `associarEPIs(...)` / `verificarEPIsObrigatoriosTarefa()` | Sem correspondência no MVP | RF12, backlog. |
| Associações com `Setor` e `AreaRisco` | Sem correspondência nesta entrega | Dependem de #111 e #113 (seção 2.2). |

---

## 4. Verificação

- **Domínio**: `TarefaTest` cobre classificação com responsável, reavaliação com valor anterior, reavaliação para o mesmo nível, recusa sem nível ou sem responsável, registro legado sem autoria e bloqueio de alocação.
- **Caso de uso**: `TarefaUseCaseTest` e `AuditoriaEvidenciaIntegracaoTest` cobrem responsável técnico, histórico com valor anterior e recusa sem Gestor identificável, sem gravar nada.
- **API**: `TarefaControllerTest` cobre RBAC (Gestor classifica; Supervisor consulta e não altera), resposta com o responsável, recusa de termos fora do vocabulário (HTTP 400) e HTTP 403 sem responsável.
- **PostgreSQL**: `AreaRiscoTarefaPersistenciaTest` aplica as migrações até a V23 e verifica, após limpar o contexto de persistência, o responsável gravado em `tarefas`, o histórico em `logs_auditoria` e a leitura de tarefa legada sem autoria.
- **Interface**: `tests/tarefas.spec.ts` verifica a lista de níveis, o responsável exibido após a classificação e a indicação de responsável não registrado.

---

## 5. Rastreabilidade com Casos de Uso e Requisitos

| Fonte | Item | Atendimento no contrato |
| --- | --- | --- |
| RF06 | Classificar o nível de periculosidade da tarefa | Classificação com vocabulário único (2.1, 2.3). |
| UC03 / UC11 | Vocabulário de risco divergente | Vocabulário único com correspondência explícita (2.1). |
| UC11, regra 1 / US06, regra 2 e critério 2 | Data, hora e responsável técnico em toda alteração | `dataClassificacao`, `responsavelClassificacao` e `LogAuditoria` com valor anterior (2.3). |
| UC11, atores / US06, regra 1 e critérios 1 e 4 | Gestor define; Supervisor consulta | RBAC existente e consulta com nível, data e responsável (2.3). |
| UC11, exceção I / US06, regra 3 e critério 3 | Alocação bloqueada sem classificação | `validarClassificacaoParaAlocacao` para #117 e #126 (2.4). |
| UC11, regra 2 / US06, regra 4 | Exigências normativas | Pendente (2.5, seção 6). |
| RNF03 / RNF05 | Perfis e rastreabilidade | Acesso por perfil e auditoria (2.3). |

---

## 6. Pendências Não Resolvidas por Este Contrato

| Ponto | Situação |
| --- | --- |
| Representação das exigências normativas | Resolvida na #117 via `ExigenciaCapacitacao` e migração V15. |
| Critérios para atribuir cada nível | US06, regra 4: ainda precisam ser detalhados. A correspondência da seção 2.1 não define critério. |
| Relações com `Setor` e `AreaRisco` | Dependem de #111 e #113, incluindo o mínimo 1..* do lado da tarefa na associação com `Setor`. |
| Ponto de alocação | Resolvido na #117: endpoint `POST /api/tarefas/{id}/alocacoes` operado por Gestor e Supervisor. |
| Consulta do histórico pela API | Resolvida na #114: `GET /api/tarefas/{id}/historico` devolve os registros de `logs_auditoria` da tarefa, do mais recente ao mais antigo, e é exclusiva do Gestor de Segurança. A interface mostra esse histórico na matriz de tarefas. |
| Atualização textual de UC11 e US06 | Após a aprovação do vocabulário (2.1). |
