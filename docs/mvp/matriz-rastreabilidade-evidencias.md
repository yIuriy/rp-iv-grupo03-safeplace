# Matriz de Rastreabilidade com Evidências do MVP

Complementa a [matriz documental da seção 9](especificacao-mvp-arquitetura.md#9-matriz-de-rastreabilidade) da especificação do MVP, que descreve cobertura de documentação. Esta matriz liga cada requisito `Deve ter` do MVP à história ou caso de uso, ao modelo aprovado, à API ou tela e à evidência de teste existente no repositório. Responde à [issue #129](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/129).

## 1. Regras de leitura

- **Evidência** é um teste automatizado ou uma execução registrada na seção 3. Existir um teste não significa que o requisito esteja concluído.
- **Pendência** aparece sempre que falta tela, API, decisão ou evidência. Pendência não é marcada como atendida.
- **Teste isolado** exercita domínio ou controlador com dependências simuladas (`*Test`, `*ControllerTest`, `*UseCaseTest`).
- **Teste de integração** conecta componentes reais, como PostgreSQL real (`*IntegracaoTest`, `*PersistenciaTest`). Testes Playwright (`frontend/tests`) exercitam a interface no navegador, mas grande parte deles (`ocorrencias*.spec.ts`, `capacitacoes.spec.ts`, `tarefas.spec.ts`, `setores.spec.ts`, `areas-risco.spec.ts`, `historico.spec.ts`) simula a API via `page.route` — funcionando como testes de interface isolados, e não integração ponta a ponta com o backend real.
- RF09 e RF12 entraram no MVP em 17/10/2026 e têm linhas próprias na matriz. Nenhuma linha depende de RF18 ou RF19, que estão fora do MVP.

## 2. Matriz

### 2.1. Requisitos funcionais

| Req. | UC / US | Modelo aprovado | API e tela | Evidência | Pendência |
| --- | --- | --- | --- | --- | --- |
| RF01 | UC05; US13 | [DS09](../arquitetura/correspondencia-classes-ds09.md) | `POST/GET/PUT /api/ocorrencias`, `/{id}/triagem`, `/{id}/arquivar`; tela `/ocorrencias` | `AcidenteTest`, `OcorrenciaUseCaseTest`, `OcorrenciaControllerTest` (isolados); `OcorrenciaConsultaPersistenciaIntegracaoTest`, `OcorrenciaPersistenciaIntegracaoTest`; `ocorrencias.spec.ts`, `ocorrencias-acompanhamento.spec.ts` (Playwright simulado) | Nenhuma (testes de persistência corrigidos em 3.2). |
| RF02 | UC05 | DS09 | Mesma API; causa de falha de equipamento em `Acidente.causaRaiz` | `CausaRaizAcidenteTest` (isolado) | Não há teste de integração específico para a causa por equipamento. Sem relação direta com `EPI` no modelo. |
| RF03 | UC06; US03 | Decisão #124 | `GET /api/epis`, `POST /api/epis/{id}/movimentacoes`; tela `/epis` (`/estoque` redireciona) | `EpiTest`, `MovimentacaoEstoqueTest`, `EpiUseCaseTest`, `EpiControllerTest` (isolados); `EpiEstoqueConcorrenciaIntegracaoTest`, `EstoqueCriticoPersistenciaIntegracaoTest`; `estoque.spec.ts` | Nenhuma (responsavel_id criado nas migrações V9/V10 e operador exibido na tela de movimentações). |
| RF04 | UC01; US04 | Decisão #124 | `POST/GET /api/epis/{id}/manutencoes`; tela de EPIs | `ManutencaoEpiTest`, `ControlarManutencaoEpiServiceTest` (isolados); `epis.spec.ts` | Sem teste de integração específico em PostgreSQL para manutenção. |
| RF05 | UC03; US05 | `AreaRisco` e `Setor` | `/api/areas-risco`, `/api/setores`; tela `/areas-risco` | `AreaRiscoUseCaseTest`, `AreaRiscoControllerTest` (isolados); `SetorAreaRiscoPersistenciaIntegracaoTest`, `AreaRiscoTarefaApiIntegracaoTest`; `areas-risco.spec.ts`, `setores.spec.ts` (Playwright simulado) | Nenhuma (`setores.spec.ts:96` corrigido em 17/10/2026; ver 3.3). |
| RF06 | UC11; US06 | [UC11](../arquitetura/correspondencia-classes-tarefa-uc11.md) | `PATCH /api/tarefas/{id}/classificacao`, `/historico`; tela `/tarefas` | `NivelPerigoTest`, `TarefaTest`, `TarefaUseCaseTest` (isolados); `AreaRiscoTarefaPersistenciaTest`; `tarefas.spec.ts`, `historico.spec.ts` (Playwright simulado) | Exigências normativas fora da relação. |
| RF07 | UC07; US07 | [UC07](../arquitetura/correspondencia-classes-plano-de-acao.md) | `/api/ocorrencias/{id}/planos-acao`, `/api/planos-acao/{id}/*`, `GET /api/notificacoes/acoes`; rotina `AlertaPrazoAcaoAgendador` (alertas de prazo, #196); painel de plano em `/ocorrencias` | `PlanoDeAcaoTest`, `NotificacaoAcaoTest`, `PlanoDeAcaoUseCaseTest`, `NotificacaoAcaoUseCaseTest`, `AlertaPrazoAcaoAgendadorTest`, `PlanoDeAcaoControllerTest`, `SegurancaOcorrenciaPlanoRbacTest` (isolados); `PlanoDeAcaoPersistenciaIntegracaoTest`, `OcorrenciaPersistenciaIntegracaoTest`; `ocorrencias-acompanhamento.spec.ts` (Playwright simulado) | A tela não mostra os avisos de prazo e de reatribuição; o Gestor só os consulta em `GET /api/notificacoes/acoes`. |
| RF09 | US09 | [RF09](../arquitetura/correspondencia-classes-inspecao-rf09.md) | `GET` e `POST /api/areas-risco/{id}/inspecoes`; aba "Inspeções" em `/areas-risco` (Gestor) | `InspecaoTest`, `InspecaoUseCaseTest`, `InspecaoControllerTest` (isolados); `InspecaoPersistenciaIntegracaoTest`; `inspecoes.spec.ts` (Playwright simulado) | Consulta pelo Supervisor, periodicidade e critérios pendentes. |
| RF10 | UC02; US10 | [UC02](../arquitetura/correspondencia-classes-capacitacao-uc02.md) | `GET` e `POST /api/capacitacoes/*`; tela `/capacitacoes` | `CertificacaoTest`, `TreinamentoTest`, `ValidadorAptidaoColaboradorServiceTest`, `AcompanharCapacitacoesServiceTest`, `CadastrarCapacitacaoServiceTest`, `CapacitacaoControllerTest` (isolados); `CapacitacaoApiIntegracaoTest`, `CapacitacaoPersistenciaIntegracaoTest`; `capacitacoes.spec.ts` (Playwright simulado) | Cadastro de novas capacitações no catálogo não existe na API nem na interface (restringe-se a vincular certificações/treinamentos existentes a colaboradores). |
| RF11 | UC12; US11 | Empréstimo | `/api/emprestimos`, `/{id}/devolucao`, `/epis-disponiveis`; tela `/emprestimos` | `EmprestimoTest`, `EmprestimoUseCaseTest` (isolados); `EmprestimoApiIntegracaoTest`, `EmprestimoPersistenciaIntegracaoTest`; `emprestimos.spec.ts` | Recorte limitado a colaboradores. |
| RF12 | UC08; US12 | [RF12](../arquitetura/correspondencia-classes-tarefa-rf12.md) | `PUT` e `GET /api/tarefas/{id}/epis`, `GET /api/tarefas/{id}/exigencias`; botão "EPIs" em `/tarefas` (Gestor) | `TarefaTest`, `TarefaUseCaseTest`, `TarefaControllerTest` (isolados); `TarefaEpiApiIntegracaoTest` | Sem teste Playwright do vínculo na tela. Sugestão automática no empréstimo fora do MVP. |
| RF13 | UC09, UC05 | DS09 | `POST /api/ocorrencias`; formulário de relato em `/ocorrencias` | `AcidenteTest`, `NumeroProtocoloCATTest` (isolados); `OcorrenciaPersistenciaIntegracaoTest`; `ocorrencias.spec.ts` (Playwright simulado) | Envio offline não implementado (protocolo CAT gerado e testado sob concorrência). |
| RF16 | UC09, UC05 | DS09 | `POST /api/ocorrencias` | `IncidenteTest`, `OcorrenciaTest` (isolados); `ocorrencias.spec.ts` (Playwright simulado) | Gravidade de incidente pendente (seção 11.1 do MVP). |
| RF21 | UC10; US21 | [DS10/DS10.1](../arquitetura/correspondencia-classes-ds10.md) | `POST /api/projecoes/analise`, `/epi/{id}`, `/alertas`; tela `/emprestimos` (aba `ProjecoesPainel.tsx`) | `ProjecaoSubstituicaoTest`, `PlanejarSubstituicaoUseCaseTest`, `SubstituicaoInteligenteServiceTest` (isolados); `ProjecaoApiIntegracaoTest`, `ProjecaoSubstituicaoPersistenciaIntegracaoTest`; `emprestimos.spec.ts` | Requisições de compra e parâmetros pendentes fora do MVP. Tela de projeções implementada em `ProjecoesPainel`. |
| RF23 | UC13; US23, US24 | Usuários | `/api/usuarios/supervisores`, `/colaboradores`; tela `/usuarios` | `UsuarioCasoDeUsoCadastroPorPapelTest`, `ColaboradorSemCredenciaisTest`, `UsuarioCasoDeUsoAuditoriaTest` (isolados); `UsuarioIntegracaoTest`; `users.spec.ts` | Desativação e alteração de CPF fora do MVP (UC13). Primeiro Gestor provisionado por seed. Autoria das alterações implementada em `UsuarioCasoDeUso` (#121). |

### 2.2. Requisitos não funcionais

| Req. | Evidência esperada pelo MVP | Evidência existente | Pendência |
| --- | --- | --- | --- |
| RNF03 | Testes de permissão para Gestor e Supervisor; Colaborador sem credenciais | `SegurancaRbacIntegrationTest`, `SegurancaControleAcessoIntegracaoTest`, `SegurancaOcorrenciaPlanoRbacTest`, `ColaboradorSemCredenciaisTest`, `TratamentoDeErroHttpTest` | Cobertura por perfil de todos os endpoints não conferida. |
| RNF05 | Log imutável e retenção mínima de 5 anos | `AuditoriaEvidenciaIntegracaoTest` (alteração seguida de consulta do log para área de risco, colaborador, estoque e classificação de tarefa), `LogAuditoriaTest`, `LogAuditoriaJpaAdapterTest`, `UsuarioCasoDeUsoAuditoriaTest` | Nenhuma proteção de imutabilidade no banco nem política de retenção de 5 anos foram encontradas no código. Evidência parcial: o registro existe, a garantia não. |
| RNF08 | Consulta sem conexão | `frontend/tests/offline.spec.ts` (5 testes): cópia local sinalizada até o servidor confirmar, nova consulta ao voltar a rede, falha de atualização, sem cópia sem sessão, isolamento por conta | Recorte provisório somente leitura nas telas existentes. Envio offline de relatos e dados de indicadores e relatórios seguem pendentes. Decisão da #105 não registrada. |
| RNF11 | Verificação desktop, tablet, móvel e WCAG 2.1 AA | Testes Playwright que variam a largura da janela (`estoque.spec.ts`, `ocorrencias.spec.ts`, `login.spec.ts`, entre outros) | Nenhuma auditoria WCAG automatizada (sem axe) nem avaliação manual registrada. Evidência apenas de responsividade. |
| RNF15 | Arquitetura, API e modelo de dados versionados | `docs/`, 33 migrações Flyway, `OpenApiConfig` com anotações `@Tag` e `@Operation` nos 17 controladores REST | Nenhuma na documentação interativa OpenAPI (17 controladores documentados). Manter sincronismo dos demais documentos de arquitetura. |

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
- Uma classe por vez, com esquema novo e as 27 migrações: 20 classes de integração, 0 ignorados, todas as 20 passam por inteiro após correções:

| Classe | Situação | Causa identificada e correção realizada |
| --- | --- | --- |
| `PlanoDeAcaoPersistenciaIntegracaoTest` | Corrigido (5 de 5 passam) | `limparDados` falhava devido à referência circular V1 + V22 entre `ocorrencias` e `planos_de_acao`, além de `acidentes`/`incidentes` não serem apagados. Corrigido desvinculando as chaves estrangeiras com `UPDATE ... SET ... = NULL` antes da exclusão. |
| `OcorrenciaPersistenciaIntegracaoTest` | Corrigido (10 de 10 passam) | `limparDados` tinha a mesma pendência de ciclo circular; testes de protocolo CAT falhavam porque `registrarAcidente` já emite o protocolo no cadastro, tornando `consolidarCAT` redundante. Corrigido com desvinculação circular e teste concorrente disparando registros simultâneos via CyclicBarrier. |

Com essas correções, todos os fluxos de persistência possuem **evidência de integração verde**.

### 3.3. Execução de 17 de outubro de 2026

Medições na branch `amanda-201`, a partir da `dev` em `64599af`, depois de atualizar os testes desatualizados por mudanças de 10/10 e 15/10 (perfil nulo para Colaborador, agentes de risco e limites obrigatórios na área, devolução que grava manutenção).

| Verificação | Resultado |
| --- | --- |
| Backend, `TESTCONTAINERS_RYUK_DISABLED=true ./mvnw test -Dapi.version=1.44` (PostgreSQL 16 novo por execução) | 987 testes, 0 falhas, 0 erros, 0 ignorados. |
| `npm run lint` | 0 erros, 0 avisos. |
| `npm run build` | Sem erros. |
| Playwright (`--workers=2`) | 162 testes, 162 passam. Inclui a largura de 320 px em `/capacitacoes`, corrigida no cabeçalho comum (`layout.css`). |

## 4. Resumo das lacunas

1. Testes de persistência de ocorrências e planos de ação corrigidos (3.2).
2. A integração PostgreSQL não roda no ambiente padrão com Docker 29 (Testcontainers 1.20.4); requer contorno via `-Dsafeplace.test.datasource.url`, daemon compatível ou `-Dapi.version=1.44` com `TESTCONTAINERS_RYUK_DISABLED=true` (3.3).
3. RNF05: imutabilidade no banco e retenção de 5 anos sem evidência física de infraestrutura. RNF11: sem evidência de WCAG automatizada. RNF08: escopo offline somente leitura.
4. RF21 possui tela no painel do gestor (`ProjecoesPainel.tsx` em `/emprestimos`). RF07 possui modelo UC07 aprovado (#97). RF23 grava autoria via `LogAuditoria` em `UsuarioCasoDeUso` (#121); primeiro Gestor (seed) permanece fora do escopo MVP.
5. Lint, build e Playwright do frontend passam desde 17/10/2026 (3.3).
