# Interface web do SafePlace

A interface usa React 19, TypeScript e Vite 8. A aplicação abre na tela de login e, depois de autenticar, em `/usuarios`, reutilizando o layout e os componentes compartilhados. O catálogo interativo fica em `/design-system`, e a tela de referência de áreas de risco fica em `/design-system/examples/risk-areas`; os dois continuam públicos e não chamam a API.

Consulte o [guia de componentes](design-system/README.md) para conhecer as importações, propriedades, referências do Figma, regras de organização visual e convenções da equipe. Os exemplos usam dados fictícios e não se conectam ao servidor da aplicação.

## Telas disponíveis

| Rota | Comportamento atual |
| --- | --- |
| `/login` | Formulário de e-mail e senha que chama `POST /api/auth/login`. Sem sessão, qualquer rota da aplicação redireciona para aqui e volta ao destino pedido depois de entrar. |
| `/` | Redireciona para `/usuarios`. |
| `/usuarios` | Gestor: abas de supervisores e colaboradores, cadastro de supervisor com senha inicial, cadastro e edição de supervisores e colaboradores. Supervisor: busca, cadastro e edição de colaboradores. Integrada a `/api/usuarios`, com carregamento, lista vazia, erro e nova tentativa. |
| `/ocorrencias` | Consulta de acidentes e incidentes por período da data do fato, setor e colaborador envolvido, no servidor, e por texto, tipo e situação, na tela; busca de acidente pelo protocolo CAT; detalhe de cada ocorrência (UC05, UC09, issue #128). O Gestor vê todos os relatos e o Supervisor, os que registrou. Integrada a `/api/ocorrencias`, `/api/setores` e `/api/usuarios/colaboradores`. |
| `/epis` | Tela inicial de EPIs. |
| `/estoque` | Catálogo de EPIs com saldo, mínimo e situação crítica em texto, busca, detalhes com histórico e operador; o Gestor registra entradas e saídas (UC06, issues #123 e #133). Integrada a `/api/epis`. |
| `/emprestimos` | Supervisor e Gestor: novo empréstimo com termo de cautela, duplicidade com justificativa, impedimentos informados pelo servidor, posse, atrasos e devolvidos por situação e devolução com condição e destino. Gestor: aba de projeções de substituição e lista de reposição, com análise e nova tentativa (UC12, UC10, issue #110). Integrada a `/api/emprestimos` e `/api/projecoes`. |
| `/areas-risco` | Abas de áreas de risco e de setores físicos (UC03). Áreas: busca, filtro por grau de perigo e consulta; o Gestor cadastra e edita. Setores: lista com grau de perigo e áreas vinculadas; o Gestor cadastra (issue #174). No detalhe da área, o Gestor abre o histórico de alterações (issue #114). Inspeções: só o Gestor vê a aba, consulta e registra (RF09, issue #136). Integrada a `/api/areas-risco`, `/api/areas-risco/{id}/inspecoes`, `/api/setores` e `/api/epis`. |
| `/tarefas` | Matriz de periculosidade com busca e filtro por grau de perigo (UC11). O Gestor cadastra, classifica e reavalia tarefas e abre o histórico de cada uma (issue #114); o Supervisor consulta. Integrada a `/api/tarefas`. |

As quatro telas iniciais identificam a funcionalidade como em desenvolvimento. Não exibem cadastros fictícios nem oferecem operações de criação ou edição. Embora o backend já tenha operações de ocorrências e EPIs, suas interfaces completas não fazem parte desta base.

O menu indica a tela ativa e permite navegar sem recarregar a página. URLs diretas, atualização da página e histórico do navegador são suportados. Endereços desconhecidos mostram uma página com link para voltar ao início. A navegação reutiliza o menu compacto, os estados de foco e o link para pular ao conteúdo do design system.

### Autenticação e sessão

- O login usa apenas e-mail e senha em `POST /api/auth/login`, como define a [especificação do MVP, seção 3.1](../docs/mvp/especificacao-mvp-arquitetura.md#31-pessoas-cadastradas-fluxo-de-acesso-e-ausência-de-autocadastro). Não há autocadastro nem recuperação de senha.
- A resposta (token, nome, e-mail e perfil) fica em `sessionStorage`, na chave `safeplace.sessao`: sobrevive a recarregar a página e termina ao fechar a aba. A issue #94 pede para não impor `localStorage` nem nova política de autenticação; a validade do token é decidida pelo backend (RNF03).
- O cliente HTTP compartilhado anexa `Authorization: Bearer` a toda chamada. Um 401 fora do login encerra a sessão e volta ao `/login` com aviso de sessão expirada. O botão "Sair" do cabeçalho apaga a sessão.
- A interface aceita os perfis `GESTOR_SEGURANCA` e `SUPERVISOR`. Colaborador não tem conta nem entra no sistema (RF23, RNF03).
- Implementação em `src/features/autenticacao/`: `sessao.ts` guarda e lê a sessão, `interceptadores.ts` anexa o token e detecta 401, `AutenticacaoProvider.tsx` e `contexto.ts` expõem o estado ao React, `ExigirSessao.tsx` protege as rotas e `LoginPage.tsx` é a tela.

### Gestão de supervisores e colaboradores

A tela `/usuarios` aplica a matriz de RF23 que o `SecurityConfig` do backend também aplica:

| Ação | Gestor | Supervisor | Endpoint |
| --- | --- | --- | --- |
| Listar supervisores | Sim | Não | `GET /api/usuarios`, filtrado por perfil na interface |
| Cadastrar supervisor | Sim | Não | `POST /api/usuarios/supervisores` |
| Listar e buscar colaboradores | Sim | Sim | `GET /api/usuarios/colaboradores?nome={nome}&cpf={cpf}` |
| Cadastrar colaborador | Sim | Sim | `POST /api/usuarios/colaboradores` |
| Editar supervisor | Sim | Não | `PUT /api/usuarios/supervisores/{id}` |
| Editar colaborador | Sim | Sim | `PUT /api/usuarios/colaboradores/{id}` |

- A senha inicial do Supervisor aparece uma única vez, no resultado do cadastro. Listagens nunca a mostram e a interface não a guarda.
- O formulário de colaborador não tem campos de senha ou perfil e não envia essas chaves; o backend as recusaria com 400.
- A validação local repete só o que o backend exige: campos obrigatórios, CPF com 11 dígitos, data de nascimento no passado e e-mail válido. Erros 400 e 409 mostram a mensagem da API e mantêm os dados digitados.
- Os campos de CPF (cadastro e busca) aplicam a máscara `000.000.000-00` enquanto o usuário digita, mas o cadastro e a busca enviam só os 11 dígitos à API. Recarregar a página mantém a sessão e mostra os dados persistidos pela API.
- A edição (issue #122) reaproveita o formulário do cadastro com os dados atuais. Só nome, data de nascimento e e-mail são enviados; o CPF aparece somente leitura e não vai no corpo, porque a alteração de CPF e a desativação aguardam decisão do grupo (especificação do MVP, seção 11.1). Perfil e senha nunca mudam por esse caminho: a API recusa esses campos com 400. Um 404 na edição indica que o cadastro mudou de perfil ou não existe mais.
- Implementação em `src/features/usuarios/`: `api.ts` (contratos e chamadas), `UsersPage.tsx` (abas por perfil), `SupervisoresPainel.tsx`, `ColaboradoresPainel.tsx`, `CadastroDialog.tsx`, `PessoaFormulario.tsx` e `validacaoPessoa.ts`.

Fora desta entrega: consulta offline de RNF08 (issues #105 e #127), a carga inicial do primeiro Gestor, descrita a seguir, e a autoria das alterações, que depende do módulo de auditoria (issue #121).

### Primeiro acesso

A especificação do MVP prevê que o primeiro Gestor de Segurança venha de carga inicial no banco, mas o repositório ainda não traz essa carga nem uma migração com esse registro. Para entrar na interface em um banco novo é preciso inserir um Gestor com hash BCrypt na tabela `usuarios`. A forma dessa carga é decisão pendente do grupo.

### Empréstimos, devoluções e projeções

A tela `/emprestimos` (issue #110) segue UC12, UC10, US11 e US21 e aplica a matriz do `SecurityConfig`:

| Ação | Gestor | Supervisor | Endpoint |
| --- | --- | --- | --- |
| Consultar posse, atrasos e devolvidos | Sim | Sim | `GET /api/emprestimos?situacao=` com `PENDENTES`, `ATRASADOS` ou `TODOS` |
| Registrar entrega | Sim | Sim | `POST /api/emprestimos` |
| Registrar devolução | Sim | Sim | `POST /api/emprestimos/{id}/devolucao` |
| Consultar projeções e lista de reposição | Sim | Não | `GET /api/projecoes` e `GET /api/projecoes/alertas` |
| Solicitar análise de projeção | Sim | Não | `POST /api/projecoes/analise` |

- A entrega lista colaboradores ativos de `GET /api/usuarios/colaboradores` e EPIs de `GET /api/emprestimos/epis-disponiveis` (situação Disponível, com saldo e CA válido). A data prevista de devolução é obrigatória e não pode ser anterior a hoje. Data, hora e responsável são registrados pelo servidor com o usuário do token.
- Um 409 de duplicidade (mesmo tipo em posse, UC12 exceção III) abre a confirmação com justificativa obrigatória e reenvia com `confirmarSubstituicao: true`. Um 422 mostra o impedimento do servidor (saldo, CA, situação do EPI, capacitação ou colaborador inativo) e mantém os dados digitados.
- Ao concluir, o diálogo mostra o termo de cautela devolvido pela API. Os detalhes mostram datas, responsáveis (nomes de `GET /api/usuarios/{id}`), condição da devolução e justificativa de substituição.
- A devolução pede a condição (apto, manutenção ou descarte) e mostra o destino antes da confirmação: só o item apto volta ao saldo, e a baixa definitiva do descarte (RF14, backlog) não é feita nesta tela.
- Projeções: a tabela mostra a projeção mais recente de cada EPI, com a data estimada e a justificativa do servidor. A lista de reposição segue a ordem da API, sem indicador de prioridade, que a US21 deixa a confirmar. Pendências da análise aparecem com o motivo e a opção de nova tentativa (UC10, exceção I); uma falha não cria alertas na tela. A API também aceita consultas do Supervisor, mas a issue #110 reserva as projeções ao Gestor.
- Não há cópia offline: a issue #105 não aprova cache de empréstimos.
- Implementação em `src/features/emprestimos/`: `api.ts` e `apiProjecoes.ts` (contratos e chamadas), `EmprestimosPage.tsx` (abas por perfil), `EmprestimosPainel.tsx`, `EmprestimosTabela.tsx`, `EntregaDialog.tsx`, `EntregaEtapas.tsx`, `DevolucaoDialog.tsx`, `EmprestimoDetalheDialog.tsx`, `ProjecoesPainel.tsx`, `validacaoEntrega.ts` e `formatos.ts`.

### Setores físicos

A aba "Setores físicos" de `/areas-risco` (issue #174) cobre o passo 2 do UC03, que exibe os setores cadastrados, e aplica a matriz do `SecurityConfig`:

| Ação | Gestor | Supervisor | Endpoint |
| --- | --- | --- | --- |
| Consultar setores | Sim | Sim | `GET /api/setores` |
| Cadastrar setor | Sim | Não | `POST /api/setores` |

- O formulário segue `CriarSetorRequest` e o `Setor` do diagrama de classes: nome e grau de perigo obrigatórios, localização e descrição opcionais. Campos opcionais vazios não são enviados. A validação local repete os obrigatórios e os tamanhos das colunas da migração V13 (nome com até 160 caracteres e localização com até 255).
- O setor não tem código: o modelo não define esse atributo e a API não o devolve. Seletores e cartões mostram o nome e, quando houver, a localização.
- A lista de setores é carregada pela tela de áreas e compartilhada com o formulário de área. Um setor cadastrado aparece no seletor sem recarregar a página.
- A coluna "Áreas de risco" conta as áreas que apontam para o setor (associação entre `AreaRisco` e `Setor` no diagrama de classes). Se as áreas não carregarem, a contagem aparece como indisponível, e não como zero.
- O grau de perigo do setor é lido pela projeção de substituição de EPIs (UC10): a análise associa um EPI ao setor cujo nome é igual à localização do EPI. Por isso a ajuda do campo pede o mesmo nome usado na localização dos EPIs.
- A consulta usa a cópia offline por conta já existente nas áreas de risco; sem conexão, o cadastro fica desabilitado.
- Fora desta entrega: edição e exclusão de setor, que a API não oferece, e a carga inicial de setores, que segue a mesma pendência do primeiro Gestor.
- Implementação em `src/features/areas-risco/`: `SetoresPainel.tsx`, `SetorFormDialog.tsx` e `validacaoSetor.ts`, além do contrato `Setor` e de `cadastrarSetor` em `api.ts`.

### Histórico de alterações

O detalhe da área de risco e a matriz de tarefas mostram ao Gestor o histórico gravado na auditoria (issue #114; UC03, cenário alternativo II, passo 4; UC11, regra 1):

| Ação | Gestor | Supervisor | Endpoint |
| --- | --- | --- | --- |
| Histórico de uma área de risco | Sim | Não | `GET /api/areas-risco/{id}/historico` |
| Histórico de uma tarefa | Sim | Não | `GET /api/tarefas/{id}/historico` |

- Cada entrada mostra data e hora, tipo da operação, e-mail de quem a fez e o que mudou, com o valor anterior e o novo, do registro mais recente ao mais antigo. Na área, o texto também traz a justificativa informada na edição.
- Na área, o botão "Ver histórico de alterações" fica no detalhe e só consulta a API quando aberto. Na tarefa, o botão "Histórico" da linha abre uma janela.
- Falha de rede mostra um aviso com "Tentar novamente". Não há cópia offline do histórico.
- O Supervisor não vê os botões, e a API responde 403 se ele chamar as rotas diretamente.
- Implementação em `HistoricoAlteracoes.tsx` e `historico.css` (`src/features/areas-risco/`), `HistoricoTarefaDialog.tsx` (`src/features/tarefas/`) e no contrato `RegistroHistorico` de `areas-risco/api.ts`.

### Inspeções de áreas de risco

A aba "Inspeções" de `/areas-risco` (issue #136; RF09 e US09, backlog) segue a proposta da [correspondência Classes vs RF09](../docs/arquitetura/correspondencia-classes-inspecao-rf09.md) (issue #134), ainda aguardando aprovação do grupo:

| Ação | Gestor | Supervisor | Endpoint |
| --- | --- | --- | --- |
| Consultar inspeções de uma área | Sim | Não (aba oculta; API responde 403) | `GET /api/areas-risco/{id}/inspecoes` |
| Registrar inspeção | Sim | Não | `POST /api/areas-risco/{id}/inspecoes` |

- O Gestor escolhe a área e vê as inspeções da data mais recente para a mais antiga: data, quem registrou, quantidade de itens e de não conformidades e, em cada item, a referência avaliada, o resultado em texto ("Conformidade" ou "Não conformidade") e as observações.
- O formulário pede a data (hoje por padrão, sem datas futuras) e ao menos um item verificado com referência e resultado; observações são opcionais. Itens podem ser acrescentados e removidos. A validação local repete a da API, foca o primeiro campo com erro e mantém o que foi preenchido; a mensagem de recusa do servidor aparece no diálogo.
- Inspeção registrada não é editada nem excluída. Não há situação da área, interdição, periodicidade nem anexos, que dependem de decisões ainda não tomadas.
- Sem conexão, o registro fica desabilitado; o histórico de inspeções não tem cópia offline.
- Implementação em `src/features/areas-risco/`: `InspecoesPainel.tsx`, `InspecaoFormDialog.tsx`, `validacaoInspecao.ts`, `apiInspecoes.ts` e `inspecoes.css`.

### Consulta de ocorrências

A tela `/ocorrencias` (issue #128) segue UC05, UC09, US13 e US16 e a decisão DS09, e aplica a matriz do `SecurityConfig` e do caso de uso:

| Ação | Gestor | Supervisor | Endpoint |
| --- | --- | --- | --- |
| Consultar ocorrências por critérios | Todas | Só as que registrou | `GET /api/ocorrencias?dataInicio=&dataFim=&setorId=&colaboradorId=` |
| Ver detalhes | Todas | Só as que registrou | `GET /api/ocorrencias/{id}` |
| Buscar acidente pelo protocolo CAT | Todos | Só os que registrou | `GET /api/ocorrencias/protocolo/{protocolo}` |

- Critérios enviados ao servidor: período da data do fato (os dois dias incluídos), setor e colaborador envolvido. O setor é o da área de risco da ocorrência, porque a ocorrência não tem vínculo direto com setor (DS09, seção 2.3). Data inicial posterior à final é barrada na tela, com foco na data final, e o servidor também a recusa com 400.
- Texto, tipo e situação refinam a lista já recebida, sem nova consulta. A mensagem de lista vazia distingue "nenhuma ocorrência registrada" de "nenhuma ocorrência atende aos critérios".
- O detalhe é sempre consultado no servidor e mostra a data do fato separada da data do registro, a área, o colaborador envolvido e os dados próprios de acidente (protocolo CAT, gravidade, causa raiz, tipo de lesão, dano e destino) ou de incidente (situação de risco e potencial de dano). Um 404 informa que a ocorrência não existe ou não está entre as que o perfil pode consultar.
- Só acidentes têm protocolo, o número da CAT (DS09, seção 2.1). Protocolo inexistente gera aviso e lembra que incidentes se consultam pelos critérios.
- A lista mantém a cópia offline por conta e por combinação de critérios (RNF08). Sem conexão, a busca por protocolo e o registro de relato ficam desabilitados.
- Fora desta entrega: o registro de relato, a triagem e os planos de ação na interface, que pertencem à issue #100.
- Implementação em `src/features/ocorrencias/`: `api.ts`, `OccurrencesPage.tsx`, `ConsultaOcorrenciasForm.tsx`, `criteriosConsulta.ts`, `ProtocoloBusca.tsx`, `OcorrenciaDetalheDialog.tsx`, `formatos.ts` e `ocorrencias.css`.

## Executar em desenvolvimento

Use Node.js 22.12 ou superior com npm. A partir da raiz do repositório:

```sh
cd frontend
npm ci
npm run dev
```

Abra o endereço exibido no terminal, normalmente [http://localhost:5173](http://localhost:5173). O catálogo pode ser usado sem backend ou banco de dados. Para executar o conjunto com PostgreSQL e API, consulte o [README principal](../README.md#executar-com-docker-compose).

## Configurar a URL da API

O cliente HTTP em [src/shared/api/https.ts](src/shared/api/https.ts) usa `/api` por padrão. O Vite encaminha essas requisições para `http://localhost:8080`, mantendo o caminho `/api`. Assim, o navegador usa a mesma origem da interface, sem exigir mudanças de CORS no backend para o desenvolvimento local.

Para encaminhar a consulta a outro servidor, crie `frontend/.env.local` com o conteúdo abaixo e reinicie o Vite:

```dotenv
API_PROXY_TARGET=http://localhost:8080
```

No [Compose](../docker-compose.yml), `VITE_API_URL=/api` mantém as chamadas na origem da interface e `API_PROXY_TARGET=http://backend:8080` permite que o Vite encontre o serviço Java dentro da rede Docker.

Para usar uma URL diretamente no navegador, configure `VITE_API_URL`, incluindo o prefixo `/api`. O nome anterior, `VITE_URL_API`, continua aceito quando `VITE_API_URL` não está preenchido. Uma URL de outra origem exige CORS no servidor. As variáveis `VITE_*` são incorporadas ao build e não devem conter segredos. Em uma hospedagem de produção, configure o servidor web para encaminhar `/api` ao backend e servir `index.html` nas rotas da aplicação.

## Validar e gerar a interface

Execute em `frontend/`:

```sh
npm run lint
npm run build
npx playwright install chromium
npm test
```

O lint verifica as regras de código configuradas. O build verifica os tipos e gera os arquivos em `dist/`. Os testes Playwright verificam a aplicação, o catálogo e seus exemplos no Chromium; sua configuração inicia um servidor local na porta 4173. Os testes da aplicação cobrem login, sessão, saída, expiração de token, matriz de perfis, cadastro de supervisor com senha inicial, cadastro, edição e busca de colaboradores e supervisores, estoque de EPIs, entrega, duplicidade, impedimento, devolução e detalhes de empréstimos, projeções com pendências e nova tentativa, áreas de risco, cadastro e consulta de setores físicos, histórico de alterações de áreas e tarefas, consulta e registro de inspeções, consulta de ocorrências por critérios, protocolo e detalhe, navegação, histórico, teclado e larguras de 360, 768 e 1344 pixels. Somente as respostas HTTP são simuladas; componentes, rotas, sessão e cliente HTTP são executados. Esses testes não substituem a verificação contra o backend real com PostgreSQL.

Para conferir o resultado do build no navegador:

```sh
npm run preview
```

Use o endereço exibido no terminal, normalmente [http://localhost:4173](http://localhost:4173). O comando serve para conferir o build localmente. O Dockerfile atual executa o servidor de desenvolvimento do Vite.

## Organização

- `src/app/`: composição do layout, navegação e estilo das páginas da aplicação.
- `src/features/autenticacao/`: login, sessão e proteção de rotas.
- `src/features/usuarios/`: consulta e cadastro de supervisores e colaboradores por perfil.
- `src/features/estoque/`: catálogo, saldo, histórico e movimentação do estoque de EPIs.
- `src/features/emprestimos/`: entregas, devoluções, posse e atrasos e, para o Gestor, projeções de substituição.
- `src/features/areas-risco/`: áreas de risco e setores físicos (UC03).
- `src/features/ocorrencias/`: consulta, protocolo CAT e detalhe de ocorrências (UC05, UC09).
- `src/features/epis/` e `tarefas/`: telas por funcionalidade.
- `src/features/design-system/`: catálogo e exemplos com dados fictícios.
- `src/shared/components/` e `src/shared/layout/`: componentes e estrutura visual compartilhados.
- `src/shared/api/`: cliente HTTP para a integração com a API.
- `design-system/`: tokens, contratos e guias de componentes.
- `design/`: referências visuais exportadas do Figma.
- `tests/`: testes da aplicação e do catálogo no navegador.

Referências: [especificação arquitetural](../docs/arquitetura/especificacao-arquitetural.md) e [guia de variáveis de ambiente do Vite](https://vite.dev/guide/env-and-mode).
