# Correspondência entre Diagrama de Classes e DS09 (Relato de Ocorrência)

## 1. Visão Geral e Contexto

Este documento formaliza as decisões arquiteturais e a matriz de correspondência entre o **Diagrama de Classes** e o **Diagrama de Sequência DS09 (Relatar Acidente/Incidente)** do SafePlace, atendendo às regras de consistência da revisão de escopo do MVP (RF01, RF02, RF13, RF16; UC05, UC09; US01, US02, US13, US16).

---

## 2. Decisões de Modelo Registradas

### 2.1. Protocolo de Acompanhamento vs. Protocolo CAT
- **Decisão**: Apenas a classe `Acidente` possui o atributo de protocolo (`numeroProtocoloCAT`), padronizado pelo Value Object `NumeroProtocoloCAT` (anteriormente `ProtocoloCAT`).
- **Incidente e Ocorrência Genérica**: Não possuem protocolo de acompanhamento.
- **Relação com o DS09**: A operação `gerarProtocolo()` do DS09 aplica-se estritamente ao fluxo de persistência de `Acidente`. Quando um `Incidente` é registrado, o fluxo é confirmado sem emissão de protocolo.

### 2.2. Usuário que Registra vs. Colaborador Envolvido
- **Usuário que Registra (relator)**: `Supervisor` (relato operacional, UC09) ou `GestorDeSeguranca` (UC05). No diagrama de classes, as duas classes têm associação "registra" com `Ocorrencia` (1 para 0..\*). Na implementação, o relator é mantido como `relator : Colaborador` (superclasse dos dois perfis), gravado em `ocorrencias.relator_id` e também na trilha de auditoria (`LogAuditoria`). É sempre o usuário autenticado, definido só pelo servidor: o cliente não informa o relator e a requisição não aceita `gestorId` (#195). A coluna se chamava `gestor_id` e ficava nula nos relatos do Supervisor; a migração V32 a renomeou e completou os relatos antigos a partir da auditoria do cadastro.
- **Colaborador Envolvido**: Cada ocorrência possui **exatamente 1 (um)** colaborador envolvido (`colaborador : Colaborador`), sem concessão de acesso ou credenciais ao colaborador. É obrigatório no relato e na edição, e a coluna `colaborador_id` é `NOT NULL` (#195).

### 2.3. Localização da Ocorrência: Área de Risco (Sem Setor e Sem Local Diretos)
- **Decisão**: A classe `Ocorrencia` **não possui** o atributo textual `local : String` e **não possui** ligação direta com `Setor`.
- **Estruturação**: A localização física do fato é determinada exclusivamente pela associação:
  - `area : AreaRisco` (Área de risco associada, a qual pertence a um Setor no modelo), obrigatória no relato e na edição, com `area_risco_id NOT NULL` (issue #195).
  - O setor físico é obtido indiretamente através de `area.getSetor()`, sem redundância estrutural em `Ocorrencia`.
- **Texto legado**: a coluna `ocorrencias.local` foi mantida apenas para leitura dos registros anteriores à associação com a área (`Ocorrencia.getLocalInformado()`). A requisição de relato não aceita mais o campo `local` (issue #195).

### 2.4. Vínculo de EPI Relacionado à Falha
- **Decisão**: Não há associação estrutural direta entre `Ocorrencia` (ou `Acidente`) e a classe `EPI` no escopo do MVP.
- **Detalhamento**: Eventual falha de equipamento relacionada ao acidente é indicada pelo valor `Falha_epi` na enumeração `causaRaiz : CausaRaizAcidente` e descrita textualmente no campo `descricao`.

### 2.5. Data do Fato e Data do Cadastro
- **dataOcorrencia (DateTime)**: Representa a data e hora do fato (ocorrência). Deve ser obrigatoriamente informada na abertura do relato (passada explicitamente pelo contrato de entrada do DS09 e da API).
- **dataRegistro (DateTime)**: Representa o momento exato em que o registro foi gravado no sistema, gerado automaticamente no salvamento.

### 2.6. Estados e Transições de StatusOcorrencia
- **Enumeração**: `StatusOcorrencia` possui os estados literais:
  - `Aberta` (estado inicial no cadastro).
  - `Em_triagem` (ocorrência sob análise e triagem pelo gestor).
  - `Arquivada` (ocorrência arquivada após tratativa/análise).
- **Transições**:
  - `Aberta` → `Em_triagem`
  - `Em_triagem` → `Arquivada`
  - Não é permitida exclusão física de registros de ocorrência (apenas transição para `Arquivada`).

### 2.7. Gravidade em Acidente para UC07
- **Atributo**: `gravidade : NivelPerigo` na subclasse `Acidente` (com valores `Baixo`, `Medio`, `Alto`, `Critico`). É obrigatória no relato de acidente e não recebe valor padrão; valor fora da escala é recusado com 400, e o banco tem `CHECK` com os quatro valores (issue #195).
- **Regra de Negócio de UC07**: Acidentes com gravidade classificada como `Alto` ou `Critico` exigem a definição obrigatória de ao menos uma ação preventiva no plano de ação (`PlanoDeAcao`). Em incidentes, a relevância preventiva é avaliada pelo `potencialDano`.

### 2.8. Relato Rápido e Não Exigência de Documentos Aprofundados
- No cadastro / relato inicial (UC09 e fluxo principal de UC05), **não são exigidos**:
  - CAT emitida ou número de protocolo CAT prévio;
  - Laudo pericial;
  - Testemunhas;
  - Mídias / anexos;
  - Investigação formal de causa raiz (para incidentes, nem para a abertura imediata do relato).
- O relato também **não aceita** plano de ação. `POST /api/ocorrencias` não recebe `planoDeAcao`: o plano é cadastrado pelo Gestor de Segurança em `POST /api/ocorrencias/{id}/planos-acao`, com responsável, prazo e a regra de gravidade de UC07 (issue #195).

### 2.9. Campos Fora do Recorte do MVP (issue #195)
- **Removidos do domínio**: `testemunhas` e `midias` saíram de `Ocorrencia`. Pertencem a UC04 (backlog), nunca foram persistidos e não eram expostos pela API.
- **Mantidos como complementos opcionais do acidente**: `tipo` (tipo de lesão), `dano` e `destino` continuam em `Acidente` como texto livre opcional, preenchido no formulário e exibido no detalhe. Não fazem parte do preenchimento mínimo de UC05/UC09 nem participam de regra de negócio.
- **Mantido só para leitura**: `local`, texto livre legado descrito na seção 2.3.

### 2.10. Validação do Relato no Backend (issue #195)
O backend aplica as regras do relato sem depender da validação do frontend:
- Área de risco e colaborador envolvido são obrigatórios (400 quando ausentes ou inexistentes), com colunas `NOT NULL`.
- No acidente, `causaRaiz` e `gravidade` são obrigatórias e lidas sem diferenciar maiúsculas. Valor ausente ou fora do vocabulário (`CausaRaizAcidente` e `NivelPerigo`) responde 400; não há conversão para `OUTRO` ou `BAIXO`. Na edição, campo em branco mantém o valor atual e valor inválido responde 400.
- `tipoOcorrencia` desconhecido responde 400.
- O protocolo CAT do acidente é sempre gerado pelo servidor no registro (US13); a requisição não aceita `numeroProtocolo`.

---

## 3. Matriz de Correspondência: Operações do Diagrama vs. Arquitetura Hexagonal

A tabela abaixo associa cada operação presente no Diagrama de Classes e no DS09 ao elemento correspondente na Arquitetura Hexagonal (Porta de Entrada, Porta de Saída, Serviço de Aplicação ou Núcleo de Domínio).

Na revisão do diagrama de classes de 16/10/2026, as operações de criação, validação, gravação e consulta foram retiradas de `Ocorrencia`, porque na implementação hexagonal pertencem aos casos de uso e às portas de saída. Elas aparecem na coluna Origem como "Classes (retirada em 16/10/2026)" e continuam como mensagens do DS09 até a revisão dos diagramas de sequência. As transições de estado passaram a ser representadas por `enviarParaTriagem()`, `arquivar()` e `transicionarPara(status)`.

| Operação no Diagrama | Origem | Destino / Camada Hexagonal | Papel Arquitetural e Semântica |
| :--- | :--- | :--- | :--- |
| `criarOcorrencia(tipo, setor, area, colaborador, descricao, dataOcorrencia, gravidade, ...)` | DS09 (3.2.1) / Classes (retirada em 16/10/2026) | Domínio (`Ocorrencia.novo(...)`, `Acidente.novo(...)`, `Incidente.novo(...)`) | Cria a instância da entidade com seus invariantes e validações iniciais. |
| `validarDadosOcorrencia(relato)` | DS09 (3.2.2) / Classes (retirada em 16/10/2026) | Domínio (`Ocorrencia.validar()`) | Valida obrigatoriedade de campos, datas não futuras e consistência dos relacionamentos. |
| `salvarOcorrencia(relato)` | DS09 (3.2.4) / Classes (retirada em 16/10/2026) | Porta de Saída (`OcorrenciaRepositoryPort.salvar`) | Persiste a entidade no banco de dados via adaptador JPA (`OcorrenciaJpaAdapter`). |
| `gerarProtocolo()` | DS09 (3.2.5) / Classes | Domínio (`Acidente.consolidarCAT` / `NumeroProtocoloCAT`) | Gera ou formata o protocolo de identificação da CAT exclusivamente para instâncias de `Acidente`. No registro do acidente, o protocolo é sempre gerado pelo servidor (#195); `consolidarCAT` atende acidentes antigos que ficaram sem protocolo. |
| `LogAuditoria.registrarCadastro(relato, data, hora, usuario)` | DS09 (3.2.6) / Classes | Porta de Saída (`AuditoriaPort.registrar`) | Registra a trilha de auditoria imutável do cadastro da ocorrência. |
| `VisaoNotificacao.exibirNotificacao(protocolo, tipo, setor)` | DS09 (3.2.7) | Porta de Saída (`NotificacaoOcorrenciaPort`) / Adaptador (`NotificacaoInternaAdapter`) / Porta de Entrada (`ConsultarNotificacoesOcorrenciaUseCase`) / Controller (`GET /api/notificacoes`) | Emite notificação interna ao Gestor de Segurança sobre a nova ocorrência (entregue em #99). Desde a #195, a notificação (`NotificacaoOcorrencia`: ocorrência, tipo, protocolo, área, mensagem e data) é gravada em `notificacoes_ocorrencia` na mesma transação do relato e consultada pelo Gestor, da mais recente para a mais antiga; o Supervisor recebe 403. |
| `buscarOcorrencia(idOcorrencia)` / `buscarDetalhesOcorrencia` | Classes (retirada em 16/10/2026) | Porta de Saída (`OcorrenciaRepositoryPort.buscarPorId`) / Porta de Entrada (`GerenciarOcorrenciaUseCase.buscarPorId`) / Controller (`GET /api/ocorrencias/{id}`) | Consulta e retorna detalhes da ocorrência por ID (entregue em #99). O Supervisor só abre os relatos que registrou; um relato alheio responde 404, como um inexistente (#128). |
| `consultarOcorrencias(criterios)` | Classes (retirada em 16/10/2026) | Porta de Entrada (`RegistrarOcorrenciaUseCase.listar(FiltroOcorrenciasDTO)`) / Porta de Saída (`OcorrenciaRepositoryPort.listar(filtro)`) / Controller (`GET /api/ocorrencias?dataInicio=&dataFim=&setorId=&colaboradorId=&tipoOcorrencia=&gravidade=`) | Lista ocorrências registradas, isolando os relatos do supervisor autenticado conforme UC09 e oferecendo visão ampla ao Gestor (entregue em #99). Os `criterios` são opcionais e combináveis (#128): período da data do fato, com os dois dias incluídos; setor alcançado pela área de risco (seção 2.3); colaborador envolvido (seção 2.2); tipo (`ACIDENTE` ou `INCIDENTE`) e gravidade do acidente, que deixa os incidentes de fora (#195). Período invertido, tipo ou gravidade inválidos respondem 400. Os relatos do Supervisor são localizados na auditoria pelos cadastros de `Acidente` e `Incidente`, sem confundir com ids de outros cadastros dele. |
| `buscarPorProtocolo(protocolo)` | DS09 | Porta de Saída (`OcorrenciaRepositoryPort.buscarPorProtocolo`) / Porta de Entrada (`GerenciarOcorrenciaUseCase.buscarPorProtocolo`) / Controller (`GET /api/ocorrencias/protocolo/{protocolo}`) | Busca ocorrência específica por protocolo de acompanhamento/CAT (entregue em #99). Pela seção 2.1, só acidentes têm protocolo. A mesma regra de visibilidade do detalhe vale aqui (#128). |
| Atualizar ocorrência (UC05, alternativo IV) | UC05 | Porta de Entrada (`GerenciarOcorrenciaUseCase.atualizar`) / Controller (`PUT /api/ocorrencias/{id}`) | Edição exclusiva do Gestor de Segurança (#100). Altera data do fato, descrição, área, colaborador envolvido (ambos obrigatórios, #195) e os campos próprios de acidente ou incidente; preserva tipo, `dataRegistro`, status, relator e protocolo CAT. Ocorrência arquivada não é editada. A auditoria (`LogAuditoria.registrarAlteracao`) registra operador e campos alterados. |
| `enviarParaTriagem()` / `arquivar()` / `transicionarPara(status)` | Classes / UC05 | Domínio (`Ocorrencia`, validação por `StatusOcorrencia`) / Porta de Entrada (`GerenciarOcorrenciaUseCase`) / Controller (`PATCH /api/ocorrencias/{id}/triagem`, `PATCH /api/ocorrencias/{id}/arquivar`) | Transições de status operadas exclusivamente pelo Gestor de Segurança (entregue em #99). |
| `obterDadosAcidente(idOcorrencia)` | Classes | Porta de Entrada (a definir no backlog de RF19) | Extensão futura (RF19 / Backlog); obtenção dos dados do acidente para a CAT. A rota `PATCH /api/ocorrencias/{id}/consolidar-cat` foi removida na #201, pois o protocolo já é gerado no registro. |
| `vincularEvidencias(...)` / `desvincularEvidencia(...)` | Classes | Porta de Entrada (`GerenciarEvidenciasUseCase`) | Extensão futura (UC04 - Backlog); vinculação e desvinculação de mídias e testemunhas. |
| `vincularLaudoPericial(...)` / `salvarLaudoPericial()` | Classes | Porta de Entrada (`GerenciarInvestigacaoUseCase`) | Extensão futura (RF15 / Backlog); vinculação de laudos periciais e investigações formais. |
| `criarInvestigacaoFormal(idRelato)` / `validarInvestigacao(...)` | Classes | Porta de Entrada (`InvestigacaoAcidenteUseCase`) | Extensão futura (RF15 / Backlog); conversão de relato em processo formal de investigação. |
| `verificarRegraExclusao()` | Classes (retirada em 16/10/2026) | Ausência de rota `DELETE` em `/api/ocorrencias` | A regra legal de impedimento de exclusão física é garantida por não haver operação de exclusão; o encerramento ocorre apenas por `arquivar()` (entregue em #99). |
| `compilarDadosCAT(...)` / `gerarDocumentoPadronizado()` | Classes | Porta de Saída (`GeradorDocumentoCatPort`) / Serviço de Domínio | Extensão futura (RF19 / Backlog); geração e emissão do documento padronizado da CAT. |

---

## 4. Rastreabilidade com Casos de Uso e Requisitos

- **RF01 (Acidentes por Fator Humano)**: Atendido por `Acidente` com `causaRaiz = CausaRaizAcidente.Fator_humano` e colaborador associado.
- **RF02 (Acidentes por Falha de Equipamento)**: Atendido por `Acidente` com `causaRaiz = CausaRaizAcidente.Falha_epi`.
- **RF13 (Criar Registros de Acidente)**: Coberto pelo fluxo de `Acidente` em UC09 e UC05.
- **RF16 (Registrar Incidentes)**: Coberto pelo fluxo de `Incidente` em UC09 e UC05, sem emissão de protocolo CAT.
- **UC05 / UC09**: Relato rápido viabilizado com preenchimento mínimo (tipo, setor, área, colaborador, descrição, dataOcorrencia, gravidade), sem bloqueios por anexos ou laudos.
- **UC07 (Plano de Ação)**: Identificação objetiva de ocorrências graves por `gravidade in (Alto, Critico)`.
