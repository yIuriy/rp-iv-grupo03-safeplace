# Correspondência entre Diagrama de Classes e UC07 (Plano de Ação, Ações, Responsáveis e Alertas)

## 1. Visão Geral e Contexto

Este documento registra o contrato do plano de ação do SafePlace. Ele concilia a classe `PlanoDeAcao` do **Diagrama de Classes** com as ações individuais descritas no **UC07 (Definir Plano de Ação por Acidente / Incidente)**, atendendo à [issue #97](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/97) (RF07; UC07; US07).

- **Situação**: contrato aprovado e implementado (RF07, UC07, US07). A persistência foi entregue na issue #98, a API na issue #99 e os alertas de prazo, o aviso de reatribuição, o responsável único no banco e a normalização dos status legados na issue #196. As pendências da seção 6 não foram decididas e não devem ser supridas por suposição.
- **Fontes**: [Diagrama de classes](../diagramas/classes/Diagrama%20de%20Classes%20-%20SafePlace.png), [UC07](../casos-de-uso/casos-de-uso.md#uc07--definir-plano-de-ação-por-acidente--incidente), [US07](../historias-de-usuario.md#us07--definir-e-acompanhar-planos-de-ação), [RF07](../requisitos/requisitos-funcionais.md#rf07-o-sistema-deve-permitir-informar-o-plano-de-ação-para-cada-tipo-de-acidente-ocorrido), [RNF05](../requisitos/requisitos-nao-funcionais.md) e a [correspondência Classes vs DS09](correspondencia-classes-ds09.md).
- **Implementação de referência**: núcleo de domínio em `backend/src/main/java/br/edu/safeplace/backend/domain/ocorrencia/` (`PlanoDeAcao`, `StatusPlanoDeAcao`, `ProrrogacaoPrazo`, `AvisoAcao`, `MotivoAvisoAcao` e exceções), coberto por `PlanoDeAcaoTest`. O domínio não depende de JPA nem de Spring.

### 1.1. Modelo de referência

Desde a revisão de 16/10/2026, o PNG define `PlanoDeAcao` com:

- **Atributos**: `medidasCorretivas : String`, `prazo : Date`, `statusPlanoDeAcao : StatusPlanoDeAcao`, `medidasPreventivas : String`, `alertaAtivo : boolean` e `antecedenciaAlertaDias : int`.
- **Operações**: `validarPlano(dadosPlano, ocorrencia, dataAtual)`, `validarAtualizacaoStatus(idAcao, novoStatus)`, `validarProrrogacao(idAcao, novoPrazo, justificativa)`, `atualizarResponsavel(idAcao, novoResponsavel : Colaborador)`, `atualizarStatus(novo : StatusPlanoDeAcao)`, `prorrogarPrazo(novoPrazo, justificativa) : ProrrogacaoPrazo`, `configurarAlerta(ativo, antecedenciaDias)` e `isEmAtraso(data) : boolean`.
- **Associações**: `Ocorrencia` (1) para `PlanoDeAcao` (0..*); `GestorDeSeguranca` (0..*) para `PlanoDeAcao` (1..*); `Setor` (0..1) como responsável; `ProrrogacaoPrazo` (0..*) e `AvisoAcao` (0..*).

A versão anterior do PNG tinha as operações de gravação e consulta `salvarVinculado`, `buscarPlano`, `salvarStatusMedida`, `buscarPorFiltros`, `buscarAcao` e `atualizarPrazo`. Elas foram retiradas porque pertencem aos casos de uso e às portas de saída; a correspondência com o código permanece na seção 3. A diferença que motivou este contrato era que o PNG tinha um único `prazo` e um único `status` por `PlanoDeAcao`, enquanto UC07 acompanha prazo, responsável, situação e alerta **por ação**.

---

## 2. Decisões de Modelo Registradas

### 2.1. Identidade e cardinalidade: cada `PlanoDeAcao` é uma ação

- **Decisão**: cada instância de `PlanoDeAcao` representa **uma ação** do UC07. O `idAcao` das operações do diagrama é o identificador de `PlanoDeAcao`, e `idPlano` (em `buscarPlano`) designa o mesmo identificador.
- **Fundamento no modelo**: `buscarAcao(idAcao)` devolve `PlanoDeAcao`; `validarProrrogacao`, `atualizarPrazo`, `atualizarResponsavel` e `validarAtualizacaoStatus` recebem `idAcao` e alteram os atributos de valor único `prazo` e `status`. Só são coerentes se cada `PlanoDeAcao` tiver um prazo e uma situação, ou seja, se for uma ação.
- **Plano da ocorrência**: é o conjunto das instâncias de `PlanoDeAcao` vinculadas a uma ocorrência, conforme a multiplicidade `Ocorrencia` (1) para `PlanoDeAcao` (0..*). Não se cria classe `Acao`, `ItemPlano` ou equivalente, e os atributos do PNG são preservados.
- **Tipo da ação** (US07: preventiva ou corretiva): deriva dos atributos existentes, sem campo novo. A ação é corretiva quando `medidasCorretivas` está preenchido e preventiva quando `medidasPreventivas` está preenchido. Cada ação precisa de ao menos uma das duas medidas e pode conter ambas, que compartilham prazo, responsável e situação. No domínio: `possuiMedidaCorretiva()` e `possuiMedidaPreventiva()`.

### 2.2. Vínculos com Ocorrência, Colaborador, Setor e Gestor

| Vínculo | Cardinalidade | Regra |
| --- | --- | --- |
| Ocorrência | Cada ação pertence a exatamente 1 ocorrência; uma ocorrência tem 0..* ações. | A ocorrência deve estar registrada (possuir identificador). A ação guarda `idOcorrencia`. O vínculo é por referência: a ação não é excluída junto com a ocorrência, que apenas é arquivada (UC05). |
| Responsável: `Colaborador` | 0..1 por ação | O PNG tipa `atualizarResponsavel` com `Colaborador`. `Supervisor` e `GestorDeSeguranca` são subclasses de `Colaborador` e também podem ser designados. |
| Responsável: `Setor` | 0..1 por ação | UC07 e US07 admitem setor como responsável. Usa a classe `Setor` existente ([#111](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/111)); não há texto livre. |
| Invariante de responsável | Exatamente 1 responsável por ação | Um, e somente um, entre `Colaborador` e `Setor`, previamente cadastrado. Ser responsável não cria conta, senha ou permissão (RF23, RNF03). Desde a V33, o banco garante a regra (`chk_planos_responsavel`); a restrição da V22 também aceitava os dois nulos. |
| `GestorDeSeguranca` | Associação do PNG | Identifica quem definiu a ação. A autoria de cada alteração fica em `LogAuditoria.responsavel` (RNF05); o campo de domínio `gestor` é opcional para permitir registros anteriores ao contrato. |

### 2.3. Estados e transições

No PNG, o atributo é `statusPlanoDeAcao : StatusPlanoDeAcao`. No código, o valor é gravado como texto e interpretado por `StatusPlanoDeAcao`, que define o vocabulário aceito:

| Valor | Significado | Transições permitidas |
| --- | --- | --- |
| `ABERTA` | Ação cadastrada cuja execução ainda não foi confirmada. Estado inicial obrigatório de toda nova ação. | `ABERTA` → `CONCLUIDA` |
| `CONCLUIDA` | Execução da ação confirmada pelo Gestor. Estado final. | Nenhuma |

- **Não adotados**: `PENDENTE`, `EM_ANDAMENTO` e `CANCELADO` não têm fonte no PNG nem em UC07/US07. Uma ação concluída não pode ser prorrogada, reatribuída nem ter o alerta reconfigurado.
- **Sem `dataConclusao`**: a data e o autor da conclusão são os do registro `LogAuditoria` da transição, sem criar atributo obrigatório novo.
- **Atraso e prazo próximo não são estados gravados**: UC07 fala em ação "com prazo a expirar ou em atraso". Essas situações são derivadas do `prazo` e da data consultada (`isEmAtraso(data)`, `deveEmitirAlerta(data)`), sem job que altere o `status`. Uma ação `ABERTA` com prazo vencido continua `ABERTA` e aparece como em atraso. A API devolve os dois valores já calculados pelo servidor (`emAtraso` e `alertaDevido`), e o navegador não refaz o cálculo (#196).
- **Garantia no banco**: desde a V33, `status` é obrigatório e restrito a `ABERTA` e `CONCLUIDA` (`chk_planos_status`).
- **Correspondência**: `validarAtualizacaoStatus` → `PlanoDeAcao.validarAtualizacaoStatus(StatusPlanoDeAcao)`; a mudança é feita por `atualizarStatus(...)` e gravada pela porta de saída `PlanoDeAcaoRepository.salvar` (#98), correspondente à operação `salvarStatusMedida` retirada do PNG.

### 2.4. Ocorrência grave e ação preventiva obrigatória

- **Reconhecimento**: reutiliza a decisão 2.7 da [correspondência Classes vs DS09](correspondencia-classes-ds09.md). É grave o `Acidente` cuja `gravidade : NivelPerigo` é `ALTO` ou `CRITICO` (`Ocorrencia.isGrave()`, que `Acidente` sobrescreve). Nenhuma escala nova foi criada.
- **Incidente**: `Incidente.potencialDano` é texto livre e o modelo não tem escala de gravidade para incidentes. O texto não é convertido em gravidade por suposição: `Incidente.isGrave()` devolve falso e a regra 1 de UC07 não é aplicada automaticamente a incidentes. Esta limitação está na seção 6.
- **Regra**: a cadeia `PlanoValidador` (camada `validacao`, issue #266; antes, o estático `PlanoDeAcao.validarPlano`) recebe o conjunto de ações da ocorrência (as já gravadas somadas às novas). Rejeita conjunto vazio e ações de outra ocorrência. Se a ocorrência for grave, exige ao menos uma ação com `medidasPreventivas`; caso contrário, lança `AcaoPreventivaObrigatoriaException`, a pendência prevista no critério 2 de US07. Ações concluídas também contam.

### 2.5. Prazo, prorrogação e histórico

- **Cadastro** (UC07, exceção I; US07): o `prazo` é obrigatório e não pode ser anterior à data atual nem à data do fato (`dataOcorrencia`). O prazo igual à data atual é aceito. A violação lança `PrazoAcaoInvalidoException` e nada é gravado. As regras ficam na cadeia `AcaoPlanoValidador` (issue #266), que recebe a data atual como em `validarPlano(..., dataAtual)` do PNG.
- **Prorrogação** (UC07, alternativo I): só em ação `ABERTA`. Exige justificativa técnica não vazia, e o novo prazo deve ser posterior ao prazo atual e não anterior à data atual. Por ser posterior ao prazo vigente, também não antecede a ocorrência.
- **Histórico**: `prorrogarPrazo(...)` devolve `ProrrogacaoPrazo(idAcao, prazoAnterior, novoPrazo, justificativa)`. É o dado a registrar no histórico, e o `LogAuditoria` já possui `justificativa`. O valor anterior nunca é sobrescrito sem registro. A forma de gravação, na tabela de auditoria ou em histórico próprio, é decidida em #98, desde que a consulta preserve cada prorrogação.
- **Alerta reajustado**: a data do alerta é calculada a partir do prazo (seção 2.6), portanto se reajusta sozinha após a prorrogação.
- **Correspondência**: `validarProrrogacao` → `PlanoDeAcao.validarProrrogacao(...)`; `prorrogarPrazo` → `PlanoDeAcao.prorrogarPrazo(...)` (substitui `atualizarPrazo` no PNG).

### 2.6. Alertas e canal de notificação do responsável sem conta

**Antecedência configurável** (UC07, regra 2; US07, regra 3):

- Cada ação tem `alertaAtivo` (sim ou não) e `antecedenciaAlertaDias` (inteiro ≥ 0, obrigatório quando o alerta está ativo). Não há valor padrão nem constante no código; os três dias de UC07 são apenas exemplo. Com o alerta inativo, a antecedência é descartada.
- `dataAlerta = prazo − antecedenciaAlertaDias` (`getDataAlerta()`). O alerta é devido a partir dessa data enquanto a ação estiver `ABERTA` (`deveEmitirAlerta(data)`), inclusive depois do prazo. A conclusão encerra o alerta.
- `configurarAlerta(ativo, antecedencia)` permite ao Gestor ativar, desativar ou alterar a antecedência de uma ação aberta.
- **Verificação periódica e emissão única** (#196): a rotina `AlertaPrazoAcaoAgendador` chama `EmitirAlertasPrazoUseCase.emitir(hoje)`, que percorre as ações abertas com alerta ativo e entrega pela porta `NotificadorAcao` o `AvisoAcao` de motivo `PRAZO` de cada ação cujo alerta já é devido. A chave de deduplicação é `(idAcao, prazo)`, garantida pelo índice único parcial `ux_notificacoes_acao_prazo`: repetir a verificação não duplica o aviso, e uma prorrogação cria novo prazo e rearma o alerta. Por isso a rotina pode rodar com frequência: o padrão é de hora em hora (`safeplace.plano-acao.alertas.cron`, variável `SAFEPLACE_ALERTAS_PLANO_CRON`), e um servidor desligado no horário recupera os alertas na execução seguinte. A propriedade `safeplace.agendamento.ativo` desliga as rotinas, o que a suíte de testes faz.

**Canal para o responsável sem conta** (UC07, alternativo II; US07, regra 5):

- O Colaborador não acessa o sistema e o `Setor` não tem atributo de contato. O aviso (`AvisoAcao`, com motivo `PRAZO` ou `REATRIBUICAO`, a ação, a ocorrência, o responsável, o prazo e a data do aviso) é entregue como **aviso interno** aos usuários com acesso responsáveis pelo acompanhamento. No MVP, esse usuário é o Gestor de Segurança, ator do UC07. O aviso identifica o encarregado, e a comunicação a ele ocorre fora do sistema, sem criar conta nem credencial.
- Na Arquitetura Hexagonal, o aviso sai por uma porta de saída de notificação (seção 3). E-mail, SMS ou outro meio externo **não são presumidos**: o atributo `email` de `Colaborador` é opcional e o `Setor` não tem contato. Outro canal pode ser acrescentado depois como novo adaptador da mesma porta, com aprovação do grupo.
- **Reatribuição**: `atualizarResponsavel(Colaborador | Setor, dataAtual)` exige ação aberta e responsável cadastrado diferente do atual, e devolve o `AvisoAcao` para o novo encarregado, que o caso de uso entrega pela `NotificadorAcao` depois de gravar a ação (#196). Cada troca gera um aviso, sem deduplicação. O responsável anterior fica registrado na auditoria da alteração.
- **Registro e consulta** (#196): o adaptador `NotificacaoAcaoInternaAdapter` grava cada aviso em `notificacoes_acao` (ação, ocorrência, motivo, tipo, identificador e nome do responsável, prazo, data do aviso e mensagem) e o Gestor de Segurança os consulta em `GET /api/notificacoes/acoes`, do mais recente para o mais antigo. A mensagem identifica o encarregado para que o Gestor o comunique; o Supervisor recebe 403.

### 2.7. Auditoria, autoria e preservação

- Cadastro, conclusão, prorrogação, reatribuição e configuração de alerta são alterações rastreáveis (RNF05; [#121](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/121)) com operador autenticado, data e hora. A prorrogação registra a justificativa.
- Não há exclusão física de ação nem exclusão em cascata a partir da ocorrência. O arquivamento da ocorrência conserva as ações (#98).

### 2.8. Estado existente anterior ao contrato

- **Plano embutido no cadastro**: hoje `ocorrencias.plano_de_acao_id` aponta para um único registro de `planos_de_acao`, recebido em `POST /api/ocorrencias`, e `Ocorrencia.getPlanoDeAcao()` expõe esse registro. Ele é mantido sem alteração de comportamento até #98/#99. O domínio o reconstitui pelo construtor de cinco argumentos, sem ocorrência, responsável nem alerta.
- **Texto de `status` legado**: a API anterior aceitava qualquer texto (por exemplo, `PENDENTE` e `EM_ANDAMENTO` nos testes). A V22 (#98) evoluiu `planos_de_acao` sem recriá-la e preencheu `ocorrencia_id` a partir de `ocorrencias.plano_de_acao_id`.
- **Decisão sobre os textos legados (V33, #196)**: só se convertem textos com o mesmo sentido do vocabulário aprovado. Execução ainda não confirmada (`ABERTA`, `ABERTO`, `PENDENTE`, `EM_ANDAMENTO`, `EM ANDAMENTO`) vira `ABERTA`; execução confirmada (`CONCLUIDA`, `CONCLUIDO`, `FINALIZADA`, `FINALIZADO`, com ou sem acento) vira `CONCLUIDA`. Maiúsculas e espaços nas pontas são ignorados. Texto sem correspondência (por exemplo, `CANCELADO`), situação vazia e ação sem exatamente um responsável não são resolvidos por suposição: a migração para antes de alterar o esquema e lista os ids, para que o Gestor decida a situação ou o responsável de cada um. Responsável, autoria e datas ausentes não são inventados.
- **Vínculo embutido somente leitura (#196)**: `ocorrencias.plano_de_acao_id` continua lido, mas gravar a ocorrência não regrava mais o registro vinculado. Antes, a cascata da ocorrência regravava a ação com ocorrência, responsável e alerta nulos na edição, na triagem ou no arquivamento.

---

## 3. Matriz de Correspondência: Operações do Diagrama vs. Arquitetura Hexagonal

Os elementos da camada de persistência foram implementados na issue #98 (`PlanoDeAcaoRepository`, `PlanoDeAcaoJpaAdapter`, `prorrogacoes_prazo`). A porta de entrada e a API foram entregues na issue #99 (`GerenciarPlanoDeAcaoUseCase`, `PlanoDeAcaoService` e `PlanoDeAcaoController`).

| Operação no Diagrama | Destino / Camada Hexagonal | Papel arquitetural e semântica |
| :--- | :--- | :--- |
| `validarPlano(dadosPlano, ocorrencia, dataAtual)` | Camada `validacao` (issue #266): `AcaoPlanoValidador` valida cada ação e `PlanoValidador` valida o conjunto, antes de gravar | Vínculo com ocorrência registrada, medida, responsável único, prazo não retroativo, alerta e regra da ocorrência grave. Lança as exceções de domínio em vez de devolver `boolean`. |
| `salvarVinculado(idOcorrencia, dadosPlano)` (retirada do diagrama em 16/10/2026) | Porta de entrada `GerenciarPlanoDeAcaoUseCase.cadastrar` (implementada em `PlanoDeAcaoService`, #99) e porta de saída `PlanoDeAcaoRepository.salvar` (implementada em `PlanoDeAcaoJpaAdapter`, #98) | Grava as ações vinculadas à ocorrência numa transação, com auditoria. |
| `buscarPlano(idPlano)` / `buscarAcao(idAcao)` (retirada do diagrama em 16/10/2026) | `PlanoDeAcaoRepository.buscarPorId` (implementada em `PlanoDeAcaoJpaAdapter`, #98) | Mesmo identificador (seção 2.1). |
| `buscarPorFiltros(criterios)` (retirada do diagrama em 16/10/2026) | `PlanoDeAcaoRepository.listarPorOcorrencia` (implementada em `PlanoDeAcaoJpaAdapter`, #98) | Consulta do andamento (US07, critério 1; UC05, alternativo I, passo 9). |
| `validarAtualizacaoStatus(idAcao, novoStatus)` | Domínio: `PlanoDeAcao.validarAtualizacaoStatus(StatusPlanoDeAcao)` | Apenas `ABERTA` → `CONCLUIDA`. |
| `atualizarStatus(novo)`; antes `salvarStatusMedida(idAcao, novoStatus)` | Domínio: `PlanoDeAcao.atualizarStatus(...)`, gravado por `PlanoDeAcaoRepository.salvar` (`PlanoDeAcaoJpaAdapter`, #98) | Grava a nova situação e registra a auditoria. |
| `validarProrrogacao(idAcao, novoPrazo, justificativa)` | Domínio: `PlanoDeAcao.validarProrrogacao(...)` | Justificativa obrigatória; novo prazo posterior ao atual e não retroativo. |
| `prorrogarPrazo(novoPrazo, justificativa)`; antes `atualizarPrazo(idAcao, novoPrazo)` | Domínio: `PlanoDeAcao.prorrogarPrazo(...)` → `ProrrogacaoPrazo`, gravado por `PlanoDeAcaoRepository.salvar` (#98) | Altera o prazo e devolve o registro para o histórico e a auditoria. |
| `atualizarResponsavel(idAcao, novoResponsavel : Colaborador)` | Domínio: `PlanoDeAcao.atualizarResponsavel(Colaborador \| Setor, dataAtual)` → `AvisoAcao`, persistido por `PlanoDeAcaoRepository.salvar` (#98) | Reatribui e produz o aviso ao novo encarregado. A sobrecarga com `Setor` atende UC07/US07. |
| `configurarAlerta(ativo, antecedenciaDias)` e `isEmAtraso(data)` (UC07, regra 2) | Domínio: `configurarAlerta`, `isEmAtraso`, `getDataAlerta`, `deveEmitirAlerta`, `emitirAvisoPrazo`; persistido por `PlanoDeAcaoRepository.salvar` (#98); porta de saída `NotificadorAcao.notificar(AvisoAcao)`, implementada em `NotificacaoAcaoInternaAdapter` (#196) | Antecedência por ação, sem constante. Canal da seção 2.6. |
| Verificação periódica dos alertas (UC07, regra 2; US07, critério 5) | Adaptador de entrada `AlertaPrazoAcaoAgendador`, porta de entrada `EmitirAlertasPrazoUseCase` (`NotificacaoAcaoService`) e `PlanoDeAcaoRepository.listarAbertasComAlerta` (#196) | Emissão única por `(idAcao, prazo)`; seção 2.6. |
| Consulta dos avisos | Porta de entrada `ConsultarNotificacoesAcaoUseCase` e `NotificacaoAcaoController` (`GET /api/notificacoes/acoes`) (#196) | Aviso interno ao Gestor; seção 2.6. |
| Ocorrência grave | Domínio: `Ocorrencia.isGrave()` / `Acidente.isGrave()` | `gravidade` `ALTO` ou `CRITICO`; incidente sem escala aprovada. |

---

## 4. Situação de Implementação (#98, #99 e #196)

- **#98 (Entregue)**: migração Flyway `V22__evoluir_persistencia_planos_de_acao.sql` evoluindo `planos_de_acao` com `ocorrencia_id` (sem `ON DELETE CASCADE`), responsável `colaborador_id` ou `setor_id` com restrição `CHECK`, `alerta_ativo`, `antecedencia_alerta_dias` (≥ 0) e `gestor_id`. Criada tabela `prorrogacoes_prazo` para histórico estruturado de prazos com justificativas. Entidades JPA `PlanoDeAcaoEntity` e `ProrrogacaoPrazoEntity`, porta `PlanoDeAcaoRepository` e adaptador `PlanoDeAcaoJpaAdapter` com auditoria integrada (`LogAuditoriaRepository`). Coberto por testes unitários e de integração PostgreSQL.
- **#99 (Entregue - API REST e Casos de Uso)**:
  - Porta de entrada `GerenciarPlanoDeAcaoUseCase` e serviço de aplicação `PlanoDeAcaoService`.
  - Operações expostas via `PlanoDeAcaoController`:
    - `POST /api/ocorrencias/{idOcorrencia}/planos-acao`: cadastro de ação vinculada (validação de ocorrência grave e prazos retroativos).
    - `GET /api/ocorrencias/{idOcorrencia}/planos-acao`: listagem de ações vinculadas à ocorrência.
    - `GET /api/planos-acao/{id}`: consulta de ação por identificador.
    - `PATCH /api/planos-acao/{id}/status`: transição de status (`ABERTA` → `CONCLUIDA`).
    - `POST /api/planos-acao/{id}/prorrogacoes`: prorrogação com justificativa obrigatória e persistência em `prorrogacoes_prazo`.
    - `GET /api/planos-acao/{id}/prorrogacoes`: histórico de prorrogações da ação, na ordem em que foram feitas.
    - `PATCH /api/planos-acao/{id}/responsavel`: reatribuição de responsável (colaborador ou setor), com aviso ao novo encarregado (#196).
    - `PATCH /api/planos-acao/{id}/alerta`: configuração de alerta e antecedência em dias. A documentação anterior citava `PUT`; a #196 alinhou o documento ao código, que usa `PATCH` como as demais alterações parciais da ação (situação e responsável).
  - Tratamento de exceções via `PlanoDeAcaoExceptionHandler`: 404 para ação inexistente (`AcaoPlanoNaoEncontradaException`, #196) e 400 para `AcaoPreventivaObrigatoriaException`, `PrazoAcaoInvalidoException` e `PlanoDeAcaoInvalidoException`.
  - Segurança e RBAC configurados em `SecurityConfig`: mutações restritas a `GESTOR_SEGURANCA`; consultas de acompanhamento permitidas para `GESTOR_SEGURANCA` e `SUPERVISOR` (UC09).
  - Coberto por testes unitários (`PlanoDeAcaoServiceTest`), testes web (`PlanoDeAcaoControllerTest`) e testes de segurança RBAC (`SegurancaOcorrenciaPlanoRbacTest`).
- **#196 (Entregue - alertas, avisos e integridade)**:
  - Migração `V33__alertas_e_responsavel_planos_de_acao.sql`: normalização dos status legados (seção 2.8), `status` obrigatório com `chk_planos_status`, `chk_planos_responsavel` exigindo exatamente um responsável e tabela `notificacoes_acao` com o índice único parcial `ux_notificacoes_acao_prazo`.
  - Alertas de prazo emitidos no backend pela rotina agendada e aviso de reatribuição entregue (seção 2.6). As respostas da ação trazem `alertaDevido` e `emAtraso` calculados pelo servidor.
  - `GET /api/notificacoes/acoes`: avisos de prazo e de reatribuição, exclusivo do Gestor de Segurança.
  - Coberto por `NotificacaoAcaoTest`, `NotificacaoAcaoServiceTest`, `AlertaPrazoAcaoAgendadorTest`, `PlanoDeAcaoServiceTest`, `PlanoDeAcaoControllerTest`, `SegurancaOcorrenciaPlanoRbacTest` e pelos testes de integração `PlanoDeAcaoPersistenciaIntegracaoTest` e `OcorrenciaPersistenciaIntegracaoTest`.

---

## 5. Rastreabilidade com Casos de Uso e Requisitos

| Fonte | Item | Atendimento no contrato |
| --- | --- | --- |
| RF07 | Plano de ação para cada acidente ou incidente | Ações `PlanoDeAcao` 0..* por ocorrência (2.1, 2.2). |
| UC07, cenário principal | Ações, prazos, responsáveis e gatilhos de alerta | `definir(...)` e `configurarAlerta(...)` (2.2, 2.5, 2.6). |
| UC07, regra 1 / US07, critério 2 | Ocorrência grave exige ação preventiva | `PlanoValidador` com `isGrave()` (2.4; issue #266). |
| UC07, regra 2 / US07, regra 3 e critério 5 | Antecedência configurável; três dias como exemplo | `antecedenciaAlertaDias` por ação, sem padrão (2.6). |
| UC07, alternativo I / US07, regra 4 e critério 4 | Prorrogação com justificativa, histórico e alerta reajustado | `prorrogarPrazo` → `ProrrogacaoPrazo`; data do alerta derivada (2.5). |
| UC07, alternativo II / US07, regra 5 | Reatribuição com notificação ao novo encarregado | `atualizarResponsavel` → `AvisoAcao`; canal interno (2.6). |
| UC07, exceção I / US07, critério 3 | Prazo anterior à data atual ou à ocorrência é bloqueado | `PrazoAcaoInvalidoException` (2.5). |
| UC05, alternativo I, passo 9 | Exibir status do plano de ação | Situação por ação e atraso derivado (2.3). |
| RNF05 | Rastreabilidade | Auditoria das alterações (2.7). |

---

## 6. Pendências Não Resolvidas por Este Contrato

| Ponto | Situação |
| --- | --- |
| Gravidade de incidente | O modelo não tem escala para `Incidente`. A regra de ação preventiva obrigatória só se aplica a acidentes até o grupo aprovar um critério. |
| Cancelamento ou substituição de ação registrada por engano | Não há fonte. Sem exclusão física, uma ação aberta permanece até ser concluída. |
| Novas ações em ocorrência arquivada | UC05 e UC07 não definem. Não há bloqueio no domínio. |
| Canal externo (e-mail, SMS) | Fora do MVP até decisão e infraestrutura. Entraria como novo adaptador da porta de notificação. |
| Destinatários do aviso interno além do Gestor | Depende das permissões do Supervisor definidas em #99/RNF03. |
