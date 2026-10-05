# Matriz de Rastreabilidade com Evidências do MVP

Complementa a [matriz documental da seção 9](especificacao-mvp-arquitetura.md#9-matriz-de-rastreabilidade) da especificação do MVP, que descreve cobertura de documentação. Esta matriz liga cada requisito `Deve ter` do MVP à história ou caso de uso, ao modelo aprovado, à API ou tela e à evidência de teste existente no repositório. Responde à [issue #129](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/129).

## 1. Regras de leitura

- **Evidência** é um teste automatizado ou uma execução registrada na seção 3. Existir um teste não significa que o requisito esteja concluído.
- **Pendência** aparece sempre que falta tela, API, decisão ou evidência. Pendência não é marcada como atendida.
- **Teste isolado** exercita domínio ou controlador com dependências simuladas (`*Test`, `*ControllerTest`, `*UseCaseTest`). **Teste de integração** usa PostgreSQL real (`*IntegracaoTest`) ou o navegador (Playwright, `frontend/tests`).
- Nenhuma linha depende de RF09, RF12, RF18 ou RF19, que estão fora do MVP. RF09 (`InspecaoController`) e RF12 (`PUT /api/tarefas/{id}/epis`) existem no código como trabalho adiantado e não sustentam nenhum requisito abaixo.

## 2. Matriz

### 2.1. Requisitos funcionais

| Req. | UC / US | Modelo aprovado | API e tela | Evidência | Pendência |
| --- | --- | --- | --- | --- | --- |
| RF01 | UC05; US13 | [DS09](../arquitetura/correspondencia-classes-ds09.md) | `POST/GET/PUT /api/ocorrencias`, `/{id}/triagem`, `/{id}/arquivar`; tela `/ocorrencias` | `AcidenteTest`, `OcorrenciaUseCaseTest`, `OcorrenciaControllerTest` (isolados); `OcorrenciaConsultaPersistenciaIntegracaoTest`; `ocorrencias.spec.ts`, `ocorrencias-acompanhamento.spec.ts` | Ver 3.2: dois testes de `OcorrenciaPersistenciaIntegracaoTest` falham. |
| RF02 | UC05 | DS09 | Mesma API; causa de falha de equipamento em `Acidente.causaRaiz` | `CausaRaizAcidenteTest` (isolado) | Não há teste de integração específico para a causa por equipamento. Sem relação direta com `EPI` no modelo. |
| RF03 | UC06; US03 | Decisão #124 | `GET /api/epis`, `POST /api/epis/{id}/movimentacoes`; tela `/epis` (`/estoque` redireciona) | `EpiTest`, `MovimentacaoEstoqueTest`, `EpiUseCaseTest`, `EpiControllerTest` (isolados); `EpiEstoqueConcorrenciaIntegracaoTest`, `EstoqueCriticoPersistenciaIntegracaoTest`; `estoque.spec.ts` | Movimentação não identifica o operador (issue #76/#121). |
| RF04 | UC01; US04 | Decisão #124 | `POST/GET /api/epis/{id}/manutencoes`; tela de EPIs | `ManutencaoEpiTest`, `ControlarManutencaoEpiServiceTest` (isolados); `epis.spec.ts` | Sem teste de integração específico em PostgreSQL para manutenção. |
| RF05 | UC03; US05 | `AreaRisco` e `Setor` | `/api/areas-risco`, `/api/setores`; tela `/areas-risco` | `AreaRiscoUseCaseTest`, `AreaRiscoControllerTest` (isolados); `SetorAreaRiscoPersistenciaIntegracaoTest`, `AreaRiscoTarefaApiIntegracaoTest`; `areas-risco.spec.ts`, `setores.spec.ts` | `setores.spec.ts:96` falha (ver 3.1). |
| RF06 | UC11; US06 | [UC11](../arquitetura/correspondencia-classes-tarefa-uc11.md) | `PATCH /api/tarefas/{id}/classificacao`, `/historico`; tela `/tarefas` | `NivelPerigoTest`, `TarefaTest`, `TarefaUseCaseTest` (isolados); `AreaRiscoTarefaPersistenciaIntegracaoTest`; `tarefas.spec.ts`, `historico.spec.ts` | Exigências normativas fora da relação. |
| RF07 | UC07; US07 | [UC07](../arquitetura/correspondencia-classes-plano-de-acao.md), proposta (#97) | `/api/ocorrencias/{id}/planos-acao`, `/api/planos-acao/{id}/*`; painel de plano em `/ocorrencias` | `PlanoDeAcaoTest`, `PlanoDeAcaoUseCaseTest`, `PlanoDeAcaoControllerTest` (isolados); `ocorrencias-acompanhamento.spec.ts` | `PlanoDeAcaoPersistenciaIntegracaoTest` falha nos 5 testes (ver 3.2). Aprovação da #97 não registrada. |
| RF10 | UC02; US10 | Capacitações | `GET /api/capacitacoes/*`; tela `/capacitacoes` | `CertificacaoTest`, `TreinamentoTest`, `AcompanharCapacitacoesServiceTest` (isolados); `CapacitacaoApiIntegracaoTest`, `CapacitacaoPersistenciaIntegracaoTest`; `capacitacoes.spec.ts` | Fluxo de cadastro pela interface não identificado (seção 11.1 do MVP). |
| RF11 | UC12; US11 | Empréstimo | `/api/emprestimos`, `/{id}/devolucao`, `/epis-disponiveis`; tela `/emprestimos` | `EmprestimoTest`, `EmprestimoUseCaseTest` (isolados); `EmprestimoApiIntegracaoTest`, `EmprestimoPersistenciaIntegracaoTest`; `emprestimos.spec.ts` | Recorte limitado a colaboradores. |
| RF13 | UC09, UC05 | DS09 | `POST /api/ocorrencias`; formulário de relato em `/ocorrencias` | `AcidenteTest`, `NumeroProtocoloCATTest` (isolados); `ocorrencias.spec.ts` | Dois testes de integração de protocolo falham (ver 3.2). Envio offline não implementado. |
| RF16 | UC09, UC05 | DS09 | `POST /api/ocorrencias` | `IncidenteTest`, `OcorrenciaTest` (isolados); `ocorrencias.spec.ts` | Gravidade de incidente pendente (seção 11.1 do MVP). |
| RF21 | UC10; US21 | [DS10/DS10.1](../arquitetura/correspondencia-classes-ds10.md) | `POST /api/projecoes/analise`, `/epi/{id}`, `/alertas` | `ProjecaoSubstituicaoTest`, `PlanejarSubstituicaoUseCaseTest`, `SubstituicaoInteligenteServiceTest` (isolados); `ProjecaoApiIntegracaoTest`, `ProjecaoSubstituicaoPersistenciaIntegracaoTest` | Nenhuma tela dedicada em `frontend/src/features`. Requisições de compra e parâmetros pendentes. |
| RF23 | UC13 proposto; US23, US24 | Usuários | `/api/usuarios/supervisores`, `/colaboradores`; tela `/usuarios` | `UsuarioCasoDeUsoCadastroPorPapelTest`, `ColaboradorSemCredenciaisTest` (isolados); `UsuarioIntegracaoTest`; `users.spec.ts` | Fluxo de UC13 não especificado. Primeiro Gestor (seed) e autoria das alterações (#121) pendentes. |

### 2.2. Requisitos não funcionais

| Req. | Evidência esperada pelo MVP | Evidência existente | Pendência |
| --- | --- | --- | --- |
| RNF03 | Testes de permissão para Gestor e Supervisor; Colaborador sem credenciais | `SegurancaRbacIntegrationTest`, `SegurancaControleAcessoIntegracaoTest`, `SegurancaOcorrenciaPlanoRbacTest`, `ColaboradorSemCredenciaisTest`, `TratamentoDeErroHttpTest` | Cobertura por perfil de todos os endpoints não conferida. |
| RNF05 | Log imutável e retenção mínima de 5 anos | `AuditoriaEvidenciaIntegracaoTest` (alteração seguida de consulta do log para área de risco, colaborador, estoque e classificação de tarefa), `LogAuditoriaTest`, `LogAuditoriaJpaAdapterTest`, `UsuarioCasoDeUsoAuditoriaTest` | Nenhuma proteção de imutabilidade no banco nem política de retenção de 5 anos foram encontradas no código. Evidência parcial: o registro existe, a garantia não. |
| RNF08 | Consulta sem conexão | `frontend/tests/offline.spec.ts` (5 testes): cópia local sinalizada até o servidor confirmar, nova consulta ao voltar a rede, falha de atualização, sem cópia sem sessão, isolamento por conta | Recorte provisório somente leitura nas telas existentes. Envio offline de relatos e dados de indicadores e relatórios seguem pendentes. Decisão da #105 não registrada. |
| RNF11 | Verificação desktop, tablet, móvel e WCAG 2.1 AA | Testes Playwright que variam a largura da janela (`estoque.spec.ts`, `ocorrencias.spec.ts`, `login.spec.ts`, entre outros) | Nenhuma auditoria WCAG automatizada (sem axe) nem avaliação manual registrada. Evidência apenas de responsividade. |
| RNF15 | Arquitetura, API e modelo de dados versionados | `docs/`, 27 migrações Flyway, `OpenApiConfig` com anotações em parte dos controladores | OpenAPI não cobre todos os fluxos. Esta matriz não prova a atualidade dos demais documentos. |

## 3. Execuções registradas

Medições feitas em 4 e 5 de outubro de 2026 na branch `Sidnei`, a partir do commit `81a9371`. Os números de PR #149 (456 testes de backend, 9 de integração PostgreSQL, 34 de Playwright) vieram de comentário de colaborador e **não coincidem** com as medições abaixo; ficam como referência secundária, não verificada, para RF23 e RNF03.

### 3.1. Frontend

| Verificação | Resultado |
| --- | --- |
| `npm run lint` | 4 erros `react-hooks/set-state-in-effect` (por exemplo `VincularEpisTarefaDialog.tsx:37`). Preexistentes. |
| `tsc -b` / `npm run build` | 1 erro em `EpiPage.tsx:244` (tipo de `aoSalvar`). Preexistente. |
| Playwright | 144 testes: 143 passam, 1 falha (`setores.spec.ts:96`, seletor ambíguo em modo estrito). Preexistente. Executado com Chromium completo, pois o headless shell não estava instalado. |

### 3.2. Backend

Compilação exige JDK 25 (`JAVA_HOME=/usr/lib/jvm/java-25-openjdk`); o JDK 27 padrão da máquina quebra a compilação.

- `./mvnw test` com Testcontainers: 899 testes, 0 falhas, **109 ignorados**. Todos os ignorados são testes de integração PostgreSQL, porque o Testcontainers 1.20.4 não alcança o daemon Docker 29. Sem alteração do ambiente, esses testes não rodam.
- Contorno: contêiner `postgres:16` descartável e `-Dsafeplace.test.datasource.url`, com `-Duser.timezone=America/Sao_Paulo` (o fuso padrão `Brazil/East` é recusado pelo PostgreSQL).
- Suíte completa em um único banco: 25 erros, todos por dependência de ordem na limpeza dos testes (`DELETE FROM ocorrencias/epis` bloqueado por chaves estrangeiras de dados deixados por outras classes).
- Uma classe por vez, com esquema novo e as 27 migrações: 20 classes de integração, 0 ignorados, 18 passam por inteiro. Duas falham mesmo isoladas:

| Classe | Falhas | Causa |
| --- | --- | --- |
| `PlanoDeAcaoPersistenciaIntegracaoTest` | 5 de 5 | `limparDados` apaga `ocorrencias` antes de `planos_de_acao`, violando `planos_de_acao_ocorrencia_id_fkey`. Defeito do teste, que falha na preparação e na limpeza. |
| `OcorrenciaPersistenciaIntegracaoTest` | 2 de 10 (`bancoRejeitaProtocoloDuplicado`, `deveEmitirProtocolosDistintosSobConcorrencia`) | `OcorrenciaUseCase.registrarAcidente` já emite o protocolo CAT; o `consolidarCAT` seguinte do teste lança "Protocolo CAT já emitido". Teste desatualizado em relação ao comportamento atual. |

Nenhuma das duas falhas indica defeito de produto, mas ambas deixam esses fluxos **sem evidência de integração verde** até a correção dos testes.

## 4. Resumo das lacunas

1. Duas classes de teste de integração falham por defeito de teste (3.2).
2. A integração PostgreSQL não roda no ambiente padrão (Testcontainers com Docker 29); só roda com o contorno acima.
3. RNF05: imutabilidade e retenção de 5 anos sem evidência. RNF11: sem evidência de WCAG. RNF08: escopo parcial.
4. RF21 não tem tela. RF07 depende de aprovação da #97. RF23 depende de UC13, do primeiro Gestor e da autoria das alterações (#121).
5. Lint, build e um teste Playwright do frontend falham por problemas preexistentes.
