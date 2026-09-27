# Correspondência entre Diagrama de Classes e UC07 (Plano de Ação, Ações, Responsáveis e Alertas)

## 1. Visão Geral e Contexto

Este documento registra o contrato do plano de ação do SafePlace. Ele concilia a classe `PlanoDeAcao` do **Diagrama de Classes** com as ações individuais descritas no **UC07 (Definir Plano de Ação por Acidente / Incidente)**, atendendo à [issue #97](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/97) (RF07; UC07; US07).

- **Situação**: proposta de contrato submetida à aprovação do grupo na issue #97. A persistência ([#98](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/98)) e a API ([#99](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/99)) devem seguir as decisões aprovadas. As pendências da seção 6 não foram decididas e não devem ser supridas por suposição.
- **Fontes**: [Diagrama de classes](../diagramas/classes/Diagrama%20de%20Classes%20-%20SafePlace.png), [UC07](../casos-de-uso/casos-de-uso.md#uc07--definir-plano-de-ação-por-acidente--incidente), [US07](../historias-de-usuario.md#us07--definir-e-acompanhar-planos-de-ação), [RF07](../requisitos/requisitos-funcionais.md#rf07-o-sistema-deve-permitir-informar-o-plano-de-ação-para-cada-tipo-de-acidente-ocorrido), [RNF05](../requisitos/requisitos-nao-funcionais.md) e a [correspondência Classes vs DS09](correspondencia-classes-ds09.md).
- **Implementação de referência**: núcleo de domínio em `backend/src/main/java/br/edu/safeplace/backend/domain/ocorrencia/` (`PlanoDeAcao`, `StatusPlanoDeAcao`, `ProrrogacaoPrazo`, `AvisoAcao`, `MotivoAvisoAcao` e exceções), coberto por `PlanoDeAcaoTest`. O domínio não depende de JPA nem de Spring.

### 1.1. Modelo de referência

O PNG define `PlanoDeAcao` com:

- **Atributos**: `medidasCorretivas : String`, `prazo : Date`, `status : String`, `medidasPreventivas : String`.
- **Operações**: `validarPlano(dadosPlano, ocorrencia, dataAtual)`, `salvarVinculado(idOcorrencia, dadosPlano)`, `buscarPlano(idPlano)`, `validarAtualizacaoStatus(idAcao, novoStatus)`, `salvarStatusMedida(idAcao, novoStatus)`, `buscarPorFiltros(criterios)`, `buscarAcao(idAcao) : PlanoDeAcao`, `validarProrrogacao(idAcao, novoPrazo, justificativa)`, `atualizarPrazo(idAcao, novoPrazo)` e `atualizarResponsavel(idAcao, novoResponsavel : Colaborador)`.
- **Associações**: `Ocorrencia` (1) para `PlanoDeAcao` (0..*); `GestorDeSeguranca` (0..*) para `PlanoDeAcao` (1..*).

A diferença a resolver: o PNG tem um único `prazo` e um único `status` por `PlanoDeAcao`, enquanto UC07 acompanha prazo, responsável, situação e alerta **por ação**.

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
| Invariante de responsável | Exatamente 1 responsável por ação | Um, e somente um, entre `Colaborador` e `Setor`, previamente cadastrado. Ser responsável não cria conta, senha ou permissão (RF23, RNF03). |
| `GestorDeSeguranca` | Associação do PNG | Identifica quem definiu a ação. A autoria de cada alteração fica em `LogAuditoria.responsavel` (RNF05); o campo de domínio `gestor` é opcional para permitir registros anteriores ao contrato. |

### 2.3. Estados e transições

O atributo `status : String` do PNG é preservado como texto. O vocabulário aceito é dado por `StatusPlanoDeAcao`:

| Valor | Significado | Transições permitidas |
| --- | --- | --- |
| `ABERTA` | Ação cadastrada cuja execução ainda não foi confirmada. Estado inicial obrigatório de toda nova ação. | `ABERTA` → `CONCLUIDA` |
| `CONCLUIDA` | Execução da ação confirmada pelo Gestor. Estado final. | Nenhuma |

- **Não adotados**: `PENDENTE`, `EM_ANDAMENTO` e `CANCELADO` não têm fonte no PNG nem em UC07/US07. Uma ação concluída não pode ser prorrogada, reatribuída nem ter o alerta reconfigurado.
- **Sem `dataConclusao`**: a data e o autor da conclusão são os do registro `LogAuditoria` da transição, sem criar atributo obrigatório novo.
- **Atraso e prazo próximo não são estados gravados**: UC07 fala em ação "com prazo a expirar ou em atraso". Essas situações são derivadas do `prazo` e da data consultada (`isEmAtraso(data)`, `deveEmitirAlerta(data)`), sem job que altere o `status`. Uma ação `ABERTA` com prazo vencido continua `ABERTA` e aparece como em atraso.
- **Correspondência**: `validarAtualizacaoStatus` → `PlanoDeAcao.validarAtualizacaoStatus(StatusPlanoDeAcao)`; a mudança é feita por `atualizarStatus(...)` e gravada por `salvarStatusMedida`, que é responsabilidade da porta de saída (#98).

### 2.4. Ocorrência grave e ação preventiva obrigatória

- **Reconhecimento**: reutiliza a decisão 2.7 da [correspondência Classes vs DS09](correspondencia-classes-ds09.md). É grave o `Acidente` cuja `gravidade : NivelPerigo` é `ALTO` ou `CRITICO` (`Ocorrencia.isGrave()`, que `Acidente` sobrescreve). Nenhuma escala nova foi criada.
- **Incidente**: `Incidente.potencialDano` é texto livre e o modelo não tem escala de gravidade para incidentes. O texto não é convertido em gravidade por suposição: `Incidente.isGrave()` devolve falso e a regra 1 de UC07 não é aplicada automaticamente a incidentes. Esta limitação está na seção 6.
- **Regra**: `PlanoDeAcao.validarPlano(ocorrencia, acoes)` recebe o conjunto de ações da ocorrência (as já gravadas somadas às novas). Rejeita conjunto vazio e ações de outra ocorrência. Se a ocorrência for grave, exige ao menos uma ação com `medidasPreventivas`; caso contrário, lança `AcaoPreventivaObrigatoriaException`, a pendência prevista no critério 2 de US07. Ações concluídas também contam.

### 2.5. Prazo, prorrogação e histórico

- **Cadastro** (UC07, exceção I; US07): o `prazo` é obrigatório e não pode ser anterior à data atual nem à data do fato (`dataOcorrencia`). O prazo igual à data atual é aceito. A violação lança `PrazoAcaoInvalidoException` e nada é criado. A data atual é parâmetro do domínio, como em `validarPlano(..., dataAtual)` do PNG.
- **Prorrogação** (UC07, alternativo I): só em ação `ABERTA`. Exige justificativa técnica não vazia, e o novo prazo deve ser posterior ao prazo atual e não anterior à data atual. Por ser posterior ao prazo vigente, também não antecede a ocorrência.
- **Histórico**: `prorrogarPrazo(...)` devolve `ProrrogacaoPrazo(idAcao, prazoAnterior, novoPrazo, justificativa)`. É o dado a registrar no histórico, e o `LogAuditoria` já possui `justificativa`. O valor anterior nunca é sobrescrito sem registro. A forma de gravação, na tabela de auditoria ou em histórico próprio, é decidida em #98, desde que a consulta preserve cada prorrogação.
- **Alerta reajustado**: a data do alerta é calculada a partir do prazo (seção 2.6), portanto se reajusta sozinha após a prorrogação.
- **Correspondência**: `validarProrrogacao` → `PlanoDeAcao.validarProrrogacao(...)`; `atualizarPrazo` → `PlanoDeAcao.prorrogarPrazo(...)`.

### 2.6. Alertas e canal de notificação do responsável sem conta

**Antecedência configurável** (UC07, regra 2; US07, regra 3):

- Cada ação tem `alertaAtivo` (sim ou não) e `antecedenciaAlertaDias` (inteiro ≥ 0, obrigatório quando o alerta está ativo). Não há valor padrão nem constante no código; os três dias de UC07 são apenas exemplo. Com o alerta inativo, a antecedência é descartada.
- `dataAlerta = prazo − antecedenciaAlertaDias` (`getDataAlerta()`). O alerta é devido a partir dessa data enquanto a ação estiver `ABERTA` (`deveEmitirAlerta(data)`), inclusive depois do prazo. A conclusão encerra o alerta.
- `configurarAlerta(ativo, antecedencia)` permite ao Gestor ativar, desativar ou alterar a antecedência de uma ação aberta.
- **Emissão única**: a verificação periódica e a deduplicação são de #98/#99. A chave recomendada é `(idAcao, prazo)`: uma prorrogação cria novo prazo e rearma o alerta.

**Canal para o responsável sem conta** (UC07, alternativo II; US07, regra 5):

- O Colaborador não acessa o sistema e o `Setor` não tem atributo de contato. O aviso (`AvisoAcao`, com motivo `PRAZO` ou `REATRIBUICAO`, a ação, a ocorrência, o responsável, o prazo e a data do aviso) é entregue como **aviso interno** aos usuários com acesso responsáveis pelo acompanhamento. No MVP, esse usuário é o Gestor de Segurança, ator do UC07. O aviso identifica o encarregado, e a comunicação a ele ocorre fora do sistema, sem criar conta nem credencial.
- Na Arquitetura Hexagonal, o aviso sai por uma porta de saída de notificação (seção 3). E-mail, SMS ou outro meio externo **não são presumidos**: o atributo `email` de `Colaborador` é opcional e o `Setor` não tem contato. Outro canal pode ser acrescentado depois como novo adaptador da mesma porta, com aprovação do grupo.
- **Reatribuição**: `atualizarResponsavel(Colaborador | Setor, dataAtual)` exige ação aberta e responsável cadastrado diferente do atual, e devolve o `AvisoAcao` para o novo encarregado. O responsável anterior fica registrado na auditoria da alteração.

### 2.7. Auditoria, autoria e preservação

- Cadastro, conclusão, prorrogação, reatribuição e configuração de alerta são alterações rastreáveis (RNF05; [#121](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/121)) com operador autenticado, data e hora. A prorrogação registra a justificativa.
- Não há exclusão física de ação nem exclusão em cascata a partir da ocorrência. O arquivamento da ocorrência conserva as ações (#98).

### 2.8. Estado existente anterior ao contrato

- **Plano embutido no cadastro**: hoje `ocorrencias.plano_de_acao_id` aponta para um único registro de `planos_de_acao`, recebido em `POST /api/ocorrencias`, e `Ocorrencia.getPlanoDeAcao()` expõe esse registro. Ele é mantido sem alteração de comportamento até #98/#99. O domínio o reconstitui pelo construtor de cinco argumentos, sem ocorrência, responsável nem alerta.
- **Texto de `status` legado**: a API anterior aceitava qualquer texto (por exemplo, `PENDENTE` e `EM_ANDAMENTO` nos testes). Esses valores são preservados como gravados e **não são convertidos por suposição**: `getSituacao()` devolve vazio e as operações de acompanhamento os rejeitam com mensagem explícita. A migração de #98 deve evoluir `planos_de_acao` sem recriá-la, preencher `ocorrencia_id` a partir de `ocorrencias.plano_de_acao_id` e tratar os textos legados por decisão registrada, sem inventar responsável, autoria ou datas ausentes.

---

## 3. Matriz de Correspondência: Operações do Diagrama vs. Arquitetura Hexagonal

Os elementos marcados como *a criar* são o contrato para #98/#99 e não existem no código desta entrega.

| Operação no Diagrama | Destino / Camada Hexagonal | Papel arquitetural e semântica |
| :--- | :--- | :--- |
| `validarPlano(dadosPlano, ocorrencia, dataAtual)` | Domínio: `PlanoDeAcao.definir(...)` valida cada ação; `PlanoDeAcao.validarPlano(ocorrencia, acoes)` valida o conjunto | Vínculo com ocorrência registrada, medida, responsável único, prazo não retroativo e regra da ocorrência grave. Lança exceções de domínio em vez de devolver `boolean`. |
| `salvarVinculado(idOcorrencia, dadosPlano)` | Porta de entrada `GerenciarPlanoDeAcaoUseCase.definirAcoes` e porta de saída `PlanoDeAcaoRepositoryPort.salvar` (*a criar*) | Grava as ações vinculadas à ocorrência numa transação, com auditoria. |
| `buscarPlano(idPlano)` / `buscarAcao(idAcao)` | `PlanoDeAcaoRepositoryPort.buscarPorId` (*a criar*) | Mesmo identificador (seção 2.1). |
| `buscarPorFiltros(criterios)` | `PlanoDeAcaoRepositoryPort.listarPorOcorrencia` e filtros por situação, responsável ou prazo (*a criar*) | Consulta do andamento (US07, critério 1; UC05, alternativo I, passo 9). |
| `validarAtualizacaoStatus(idAcao, novoStatus)` | Domínio: `PlanoDeAcao.validarAtualizacaoStatus(StatusPlanoDeAcao)` | Apenas `ABERTA` → `CONCLUIDA`. |
| `salvarStatusMedida(idAcao, novoStatus)` | Domínio: `PlanoDeAcao.atualizarStatus(...)`, gravado por `PlanoDeAcaoRepositoryPort.salvar` (*a criar*) | Grava a nova situação e registra a auditoria. |
| `validarProrrogacao(idAcao, novoPrazo, justificativa)` | Domínio: `PlanoDeAcao.validarProrrogacao(...)` | Justificativa obrigatória; novo prazo posterior ao atual e não retroativo. |
| `atualizarPrazo(idAcao, novoPrazo)` | Domínio: `PlanoDeAcao.prorrogarPrazo(...)` → `ProrrogacaoPrazo` | Altera o prazo e devolve o registro para o histórico e a auditoria. |
| `atualizarResponsavel(idAcao, novoResponsavel : Colaborador)` | Domínio: `PlanoDeAcao.atualizarResponsavel(Colaborador \| Setor, dataAtual)` → `AvisoAcao` | Reatribui e produz o aviso ao novo encarregado. A sobrecarga com `Setor` atende UC07/US07. |
| Gatilho de alerta (UC07, regra 2) | Domínio: `configurarAlerta`, `getDataAlerta`, `deveEmitirAlerta`, `emitirAvisoPrazo`; porta de saída `NotificacaoAcaoPort.notificar(AvisoAcao)` (*a criar*) | Antecedência por ação, sem constante. Canal da seção 2.6. |
| Ocorrência grave | Domínio: `Ocorrencia.isGrave()` / `Acidente.isGrave()` | `gravidade` `ALTO` ou `CRITICO`; incidente sem escala aprovada. |

---

## 4. Orientações para #98 (persistência) e #99 (API)

- **#98**: nova migração com a próxima versão livre, evoluindo `planos_de_acao` sem recriá-la e sem reutilizar V5. Acrescentar `ocorrencia_id` (sem `ON DELETE CASCADE`), responsável `colaborador_id` ou `setor_id` com restrição de exclusividade, `alerta_ativo` e `antecedencia_alerta_dias` (≥ 0), identificação do gestor e registro das prorrogações. Manter `status` como texto com o vocabulário da seção 2.3. Teste com PostgreSQL de gravação seguida de nova consulta.
- **#99**: operações do Gestor para definir ações, consultar o andamento, concluir, prorrogar com justificativa, reatribuir e configurar alertas. Em ocorrência grave, a falta de ação preventiva deve voltar como pendência de validação. Prazo retroativo é rejeitado sem gravação. O perfil do Supervisor nas consultas segue UC09/RNF03 e deve ser decidido ali.

---

## 5. Rastreabilidade com Casos de Uso e Requisitos

| Fonte | Item | Atendimento no contrato |
| --- | --- | --- |
| RF07 | Plano de ação para cada acidente ou incidente | Ações `PlanoDeAcao` 0..* por ocorrência (2.1, 2.2). |
| UC07, cenário principal | Ações, prazos, responsáveis e gatilhos de alerta | `definir(...)` e `configurarAlerta(...)` (2.2, 2.5, 2.6). |
| UC07, regra 1 / US07, critério 2 | Ocorrência grave exige ação preventiva | `validarPlano` com `isGrave()` (2.4). |
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
| Normalização do `status` legado | A cargo da migração de #98, por decisão registrada (2.8). |
