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
- **Usuário que Registra**: `GestorDeSeguranca` (ou `Supervisor` no relato operacional rápido), registrado via log de auditoria (`LogAuditoria`) e mantido como responsável pelo registro (`gestor : GestorDeSeguranca`).
- **Colaborador Envolvido**: Cada ocorrência possui **exatamente 1 (um)** colaborador envolvido (`colaborador : Colaborador`), sem concessão de acesso ou credenciais ao colaborador.

### 2.3. Localização da Ocorrência: Área de Risco (Sem Setor e Sem Local Diretos)
- **Decisão**: A classe `Ocorrencia` **não possui** o atributo textual `local : String` e **não possui** ligação direta com `Setor`.
- **Estruturação**: A localização física do fato é determinada exclusivamente pela associação:
  - `area : AreaRisco` (Área de risco associada, a qual pertence a um Setor no modelo).
  - O setor físico é obtido indiretamente através de `area.getSetor()`, sem redundância estrutural em `Ocorrencia`.

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
- **Atributo**: `gravidade : NivelPerigo` na subclasse `Acidente` (com valores `Baixo`, `Medio`, `Alto`, `Critico`).
- **Regra de Negócio de UC07**: Acidentes com gravidade classificada como `Alto` ou `Critico` exigem a definição obrigatória de ao menos uma ação preventiva no plano de ação (`PlanoDeAcao`). Em incidentes, a relevância preventiva é avaliada pelo `potencialDano`.

### 2.8. Relato Rápido e Não Exigência de Documentos Aprofundados
- No cadastro / relato inicial (UC09 e fluxo principal de UC05), **não são exigidos**:
  - CAT emitida ou número de protocolo CAT prévio;
  - Laudo pericial;
  - Testemunhas;
  - Mídias / anexos;
  - Investigação formal de causa raiz (para incidentes, nem para a abertura imediata do relato).

---

## 3. Matriz de Correspondência: Operações do Diagrama vs. Arquitetura Hexagonal

A tabela abaixo associa cada operação presente no Diagrama de Classes e no DS09 ao elemento correspondente na Arquitetura Hexagonal (Porta de Entrada, Porta de Saída, Serviço de Aplicação ou Núcleo de Domínio).

| Operação no Diagrama | Origem | Destino / Camada Hexagonal | Papel Arquitetural e Semântica |
| :--- | :--- | :--- | :--- |
| `criarOcorrencia(tipo, setor, area, colaborador, descricao, dataOcorrencia, gravidade, ...)` | DS09 (3.2.1) / Classes | Domínio (`Ocorrencia.novo(...)`, `Acidente.novo(...)`, `Incidente.novo(...)`) | Cria a instância da entidade com seus invariantes e validações iniciais. |
| `validarDadosOcorrencia(relato)` | DS09 (3.2.2) / Classes | Domínio (`Ocorrencia.validar()`) | Valida obrigatoriedade de campos, datas não futuras e consistência dos relacionamentos. |
| `salvarOcorrencia(relato)` | DS09 (3.2.4) / Classes | Porta de Saída (`OcorrenciaRepositoryPort.salvar`) | Persiste a entidade no banco de dados via adaptador JPA (`OcorrenciaJpaAdapter`). |
| `gerarProtocolo()` | DS09 (3.2.5) / Classes | Domínio (`Acidente.consolidarCAT` / `NumeroProtocoloCAT`) | Gera ou formata o protocolo de identificação da CAT exclusivamente para instâncias de `Acidente`. |
| `LogAuditoria.registrarCadastro(relato, data, hora, usuario)` | DS09 (3.2.6) / Classes | Porta de Saída (`AuditoriaPort.registrar`) | Registra a trilha de auditoria imutável do cadastro da ocorrência. |
| `VisaoNotificacao.exibirNotificacao(protocolo, tipo, setor)` | DS09 (3.2.7) | Porta de Saída (`NotificacaoOcorrenciaPort.notificarGestor`) / Adaptador (`NotificacaoInternaAdapter`) | Emite notificação interna ao Gestor de Segurança alertando sobre a nova ocorrência (entregue em #99). |
| `buscarOcorrencia(idOcorrencia)` / `buscarDetalhesOcorrencia` | Classes | Porta de Saída (`OcorrenciaRepositoryPort.buscarPorId`) / Porta de Entrada (`GerenciarOcorrenciaUseCase.buscarPorId`) / Controller (`GET /api/ocorrencias/{id}`) | Consulta e retorna detalhes da ocorrência por ID (entregue em #99). O Supervisor só abre os relatos que registrou; um relato alheio responde 404, como um inexistente (#128). |
| `consultarOcorrencias(criterios)` | Classes | Porta de Entrada (`RegistrarOcorrenciaUseCase.listar(FiltroOcorrenciasDTO)`) / Porta de Saída (`OcorrenciaRepositoryPort.listar(filtro)`) / Controller (`GET /api/ocorrencias?dataInicio=&dataFim=&setorId=&colaboradorId=`) | Lista ocorrências registradas, isolando os relatos do supervisor autenticado conforme UC09 e oferecendo visão ampla ao Gestor (entregue em #99). Os `criterios` são opcionais e combináveis (#128): período da data do fato, com os dois dias incluídos; setor alcançado pela área de risco (seção 2.3); colaborador envolvido (seção 2.2). Período invertido responde 400. Os relatos do Supervisor são localizados na auditoria pelos cadastros de `Acidente` e `Incidente`, sem confundir com ids de outros cadastros dele. |
| `buscarPorProtocolo(protocolo)` | Classes / DS09 | Porta de Saída (`OcorrenciaRepositoryPort.buscarPorProtocolo`) / Porta de Entrada (`GerenciarOcorrenciaUseCase.buscarPorProtocolo`) / Controller (`GET /api/ocorrencias/protocolo/{protocolo}`) | Busca ocorrência específica por protocolo de acompanhamento/CAT (entregue em #99). Pela seção 2.1, só acidentes têm protocolo. A mesma regra de visibilidade do detalhe vale aqui (#128). |
| `triagemOcorrencia` / `arquivarOcorrencia` | Classes / UC05 | Porta de Entrada (`GerenciarOcorrenciaUseCase`) / Controller (`PATCH /api/ocorrencias/{id}/triagem`, `PATCH /api/ocorrencias/{id}/arquivar`) | Transições de status operadas exclusivamente pelo Gestor de Segurança (entregue em #99). |
| `obterDadosAcidente(idOcorrencia)` | Classes | Porta de Entrada / Porta de Saída / Controller (`PATCH /api/ocorrencias/{id}/consolidar-cat`) | Gestão e consolidação do número de protocolo CAT pelo Gestor (entregue em #99). |
| `vincularEvidencias(...)` / `desvincularEvidencia(...)` | Classes | Porta de Entrada (`GerenciarEvidenciasUseCase`) | Extensão futura (UC04 - Backlog); vinculação e desvinculação de mídias e testemunhas. |
| `vincularLaudoPericial(...)` / `salvarLaudoPericial()` | Classes | Porta de Entrada (`GerenciarInvestigacaoUseCase`) | Extensão futura (RF15 / Backlog); vinculação de laudos periciais e investigações formais. |
| `criarInvestigacaoFormal(idRelato)` / `validarInvestigacao(...)` | Classes | Porta de Entrada (`InvestigacaoAcidenteUseCase`) | Extensão futura (RF15 / Backlog); conversão de relato em processo formal de investigação. |
| `verificarRegraExclusao()` | Classes | Domínio (`Ocorrencia.validarExclusao()`) | Garante a regra legal de impedimento de exclusão física permanente; nenhum endpoint DELETE exposto (entregue em #99). |
| `compilarDadosCAT(...)` / `gerarDocumentoPadronizado()` | Classes | Porta de Saída (`GeradorDocumentoCatPort`) / Serviço de Domínio | Extensão futura (RF19 / Backlog); geração e emissão do documento padronizado da CAT. |

---

## 4. Rastreabilidade com Casos de Uso e Requisitos

- **RF01 (Acidentes por Fator Humano)**: Atendido por `Acidente` com `causaRaiz = CausaRaizAcidente.Fator_humano` e colaborador associado.
- **RF02 (Acidentes por Falha de Equipamento)**: Atendido por `Acidente` com `causaRaiz = CausaRaizAcidente.Falha_epi`.
- **RF13 (Criar Registros de Acidente)**: Coberto pelo fluxo de `Acidente` em UC09 e UC05.
- **RF16 (Registrar Incidentes)**: Coberto pelo fluxo de `Incidente` em UC09 e UC05, sem emissão de protocolo CAT.
- **UC05 / UC09**: Relato rápido viabilizado com preenchimento mínimo (tipo, setor, área, colaborador, descrição, dataOcorrencia, gravidade), sem bloqueios por anexos ou laudos.
- **UC07 (Plano de Ação)**: Identificação objetiva de ocorrências graves por `gravidade in (Alto, Critico)`.
