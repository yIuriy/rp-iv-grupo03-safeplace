# Correspondência entre Diagrama de Classes e UC02 (Certificação, Treinamento e consulta pela API)

## 1. Visão Geral e Contexto

Este documento registra a correspondência entre as classes `Certificacao`, `Treinamento`, `ExigenciaCapacitacao` e `Colaborador` do **Diagrama de Classes** e a API de consulta de capacitações entregue na [issue #119](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/119) (RF10; RNF03; UC02; US10).

- **Situação**: a consulta, os filtros e os alertas estão implementados. O fluxo de cadastro e os papéis de escrita foram definidos na seção 8 ([#116](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/116), [#126](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/126), [#192](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/192)).
- **Fontes**: [Diagrama de classes](../diagramas/classes/Diagrama%20de%20Classes%20-%20SafePlace.png), [UC02](../casos-de-uso/casos-de-uso.md#uc02--controlar-certificações-e-treinamentos), [US10](../historias-de-usuario.md#us10--acompanhar-certificações-e-treinamentos), [RF10](../requisitos/requisitos-funcionais.md#rf10-o-sistema-deve-controlar-o-vencimento-das-certificações-e-treinamentos-obrigatórios-dos-funcionários) e [RNF03](../requisitos/requisitos-nao-funcionais.md).
- **Implementação de referência**: domínio em `backend/src/main/java/br/edu/safeplace/backend/domain/capacitacao/`; casos de uso `ConsultarCapacitacaoService` e `AcompanharCapacitacoesService`; persistência `CapacitacaoJdbcAdapter` sobre a migração `V15__create_capacitacoes.sql`; API `CapacitacaoController` em `/api/capacitacoes`.

---

## 2. Atributos

| Classe no PNG | Atributo e tipo no PNG | Campo na API | Observação |
| --- | --- | --- | --- |
| `Certificacao` | `nome : String` | `nome` | |
| `Certificacao` | `status : String` | `status` | Calculado na data da consulta com os valores de UC02 (cenário IV): `EM_DIA`, `PROXIMA_DO_VENCIMENTO` e `VENCIDA`. Trafega como texto. |
| `Certificacao` | `dataVencimento : Date` | `dataVencimento` | Data ISO (`aaaa-mm-dd`). |
| `Certificacao` | `dataEmissao : Date` | `dataEmissao` | Data ISO. |
| `Treinamento` | `nome : String` | `nome` | |
| `Treinamento` | `conteudo : String` | `conteudo` | |
| `Treinamento` | `instrutor : String` | `instrutor` | |
| `Treinamento` | `dataRealizacao : Date` | `dataRealizacao` | Data ISO. |
| `Treinamento` | `cargaHoraria : int` | `cargaHoraria` | Inteiro. |
| `Treinamento` | (sem atributo) | `status` | Calculado na data da consulta: `PENDENTE`, `REALIZADO`, `PROXIMO_DO_VENCIMENTO` ou `VENCIDO`; não é persistido. Os dois últimos só existem para treinamento com validade (seção 8.2). |
| `Treinamento` | (acréscimo) | `validadeMeses` e `dataVencimento` | Opcionais; ver seção 8.2. Sem `validadeMeses`, o treinamento não vence. |
| `Colaborador` | `nome : String` | `colaboradorNome` | A pessoa é identificada por `id` e `nome`. CPF, e-mail e senha não são expostos nem exigidos (RNF03). |

O único atributo novo persistido é `Treinamento.validadeMeses` (seção 8.2). `id` é identificador técnico. Os campos de agregação (`situacaoCertificacoes`, `semCertificacoes`, `pendenteIntegracaoAdmissional`, `pendencias`) são derivados na consulta.

## 3. Operações

| Operação no PNG | Correspondência na API |
| --- | --- |
| `Colaborador.buscarColaboradores()` | `GET /api/capacitacoes/colaboradores` (UC02, passo 2). |
| `Colaborador.buscarColaboradoresPorStatus(status)` e `Certificacao.verificarStatusCertificacoes(lista)` | `GET /api/capacitacoes/colaboradores?situacao=` (UC02, cenário IV). A situação do colaborador é a mais crítica entre suas certificações. |
| `Certificacao.buscarCertificacoes(cpf)` e `Treinamento.buscarTreinamentos()` | `GET /api/capacitacoes/colaboradores/{colaboradorId}` (UC02, passos 4 e 5). |
| `Certificacao.verificarCertificacaoObrigatoria(cpf, tarefa)` | `GET /api/capacitacoes/colaboradores/{colaboradorId}/aptidao?tarefaId=`, que reutiliza a regra de #117 (`ValidadorAptidaoColaboradorService`). |
| Regra 1 do UC02 (alerta de 30 dias) e cenários I e III | `GET /api/capacitacoes/alertas`. |
| `Certificacao.validarInconsistenciaCronologica(...)` | Aplicada no domínio ao cadastrar uma certificação (seção 8). |

- **CPF e identificador**: o PNG recebe `cpf` em `buscarCertificacoes` e `verificarCertificacaoObrigatoria`. A API usa o identificador do colaborador, como as demais rotas do sistema; a busca por CPF continua em `/api/usuarios`. A divergência de assinaturas CPF ou lista no PNG está registrada como pendente em #116 e não é resolvida aqui.

## 4. Acesso (RNF03)

- `GET /api/capacitacoes/**`: Gestor de Segurança (ator principal do UC02) e Supervisor (ator secundário; US10, regra 1).
- `POST /api/capacitacoes/**`: somente Gestor de Segurança (seção 8).
- Qualquer outro método em `/api/capacitacoes/**` continua negado a todos os perfis.

## 5. Limites e pendências

- **Treinamentos pendentes (UC02, cenário II)**: a obrigatoriedade de um treinamento vem das exigências da tarefa (`ExigenciaCapacitacao`). Sem tarefa, não há critério para dizer que um treinamento falta; por isso o pendente é informado na consulta de aptidão para a tarefa pretendida (US10, dicionário: "Tarefa pretendida", condicional). Não se criou matriz geral de aptidão (#126).
- **A consulta de aptidão não autoriza a alocação**: `POST /api/tarefas/{id}/alocacoes` valida novamente no momento da confirmação (#117).
- **Edição e exclusão**: não fazem parte do escopo da #192; só há cadastro.

## 6. Ajustes de consistência feitos na issue #119

- **Data de vencimento**: na própria data de vencimento a certificação já está `VENCIDA`, regra testada em `CertificacaoTest` desde #116. A aptidão para tarefa (#117) e a entrega de EPI (UC12) consideravam a certificação válida nesse dia, e a API devolvia "vencida" nos alertas e "apto" na aptidão para a mesma certificação. As três regras passaram a usar `Certificacao.estaValidaEm`, derivado do status: a certificação habilita até a véspera do vencimento.
- **Pendência de integração (UC02, cenário III; US10, regra 4)**: a ausência de certificações basta para a pendência da trilha inicial de integração e dos treinamentos admissionais. Antes, a pendência só aparecia quando o colaborador também não tinha treinamento. A pendência é informativa: não bloqueia alocação nem entrega, que dependem das exigências da tarefa ou do EPI.

## 7. Interface web (issue #120)

Tela `/capacitacoes` em `frontend/src/features/capacitacoes/`, para Gestor de Segurança e Supervisor. A aba "Cadastro" e as ações de escrita seguem a seção 8 e são exclusivas do Gestor.

| UC02 / US10 | Na tela | Endpoint |
| --- | --- | --- |
| Passos 1 e 2: acessar o menu e carregar a listagem | Menu "Capacitações", aba "Colaboradores" com a situação das certificações de cada pessoa | `GET /colaboradores` |
| Cenário IV: filtrar por situação | Campo "Situação das certificações" (em dia, próximas do vencimento em até 30 dias, vencidas) | `GET /colaboradores?situacao=` |
| Passos 3 a 7: selecionar e visualizar | Diálogo com certificações (nome, emissão, vencimento, situação) e treinamentos (nome, conteúdo, instrutor, realização, carga horária, vencimento e situação; o vencimento só existe para treinamento com validade) | `GET /colaboradores/{id}` |
| Regra 1 e cenário I: alertar vencimentos | Aviso-resumo ao abrir a tela e aba "Alertas de vencimento", com certificações e treinamentos com validade | `GET /alertas` |
| Cenário III e US10, critério 5: ausência de histórico | "Sem certificações cadastradas" na listagem, no detalhe e nos alertas, com a pendência de integração | `GET /colaboradores`, `GET /alertas` |
| Exceção I, cenário II e US10, critérios 3 e 4: impedimento | "Aptidão para tarefa": motivos do bloqueio informados pelo servidor | `GET /colaboradores/{id}/aptidao?tarefaId=` |
| Exceção I (#192): tentar a alocação | Botão "Alocar" após a consulta; a recusa do servidor é exibida com os motivos, e a de gestor ou supervisor com motivo próprio | `POST /api/tarefas/{id}/alocacoes` |
| Cadastro (#192) | Aba "Cadastro" (só Gestor): formulários de certificação, treinamento (com validade opcional) e exigência para tarefa ou EPI | `POST /certificacoes`, `/treinamentos`, `/exigencias` |

- A consulta não grava nada. A alocação só é confirmada pelo servidor, que valida de novo as exigências, e o cadastro tem validação local igual à da API.
- Não há cópia offline: a #105 não aprova cache de capacitações, e a #127 depende dela.
- Aba, filtro e colaborador aberto ficam na URL e sobrevivem à recarga. A URL `?aba=cadastro` é ignorada para o Supervisor.

## 8. Fluxo de cadastro (issue #192)

### 8.1 Fluxo e papéis

1. O Gestor de Segurança acessa a aba "Cadastro" da tela `/capacitacoes`.
2. Registra, conforme a necessidade, uma certificação, um treinamento ou uma exigência de capacitação. Toda gravação gera trilha de auditoria (RNF05).
3. O Supervisor continua apenas consultando; o Colaborador não acessa o módulo.
4. A exigência indica o alvo (`TAREFA` ou `EPI`) e o nome da certificação ou do treinamento exigido. A alocação em tarefa passa a ser bloqueada quando a exigência não é atendida (UC02, exceção I).

| Recurso | Endpoint | Perfil |
| --- | --- | --- |
| Certificação | `POST /api/capacitacoes/certificacoes` | Gestor de Segurança |
| Treinamento | `POST /api/capacitacoes/treinamentos` | Gestor de Segurança |
| Exigência | `POST /api/capacitacoes/exigencias` | Gestor de Segurança |

### 8.2 Validade de treinamento

- `Treinamento.validadeMeses` é opcional e é o único campo acrescentado ao modelo do PNG. A exigência de vencimento de treinamentos vem de RF10.
- Sem `validadeMeses`, o treinamento não vence; não se inventou validade para todos os treinamentos (#116).
- A data de vencimento é derivada (`dataRealizacao` + `validadeMeses`) e não é persistida.
- A situação segue a regra das certificações: `PROXIMO_DO_VENCIMENTO` em até 30 dias do vencimento e `VENCIDO` a partir da data de vencimento. Treinamento vencido não atende a exigência e bloqueia a alocação; treinamento próximo do vencimento aparece nos alertas, mas não bloqueia.

### 8.3 Alocação

- Somente perfis `COLABORADOR` podem ser alocados em tarefas; Gestor de Segurança e Supervisor são rejeitados com `422`, com mensagem distinta da de pendência de capacitação.
- A tela "Aptidão para tarefa" oferece a ação de alocar. O bloqueio por capacitação vencida ou ausente é exibido com os motivos informados pelo servidor, que valida novamente no momento da confirmação.
