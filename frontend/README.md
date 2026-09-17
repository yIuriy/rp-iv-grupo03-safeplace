# Interface web do SafePlace

A interface usa React 19, TypeScript e Vite 8. A aplicação abre na tela de login e, depois de autenticar, em `/usuarios`, reutilizando o layout e os componentes compartilhados. O catálogo interativo fica em `/design-system`, e a tela de referência de áreas de risco fica em `/design-system/examples/risk-areas`; os dois continuam públicos e não chamam a API.

Consulte o [guia de componentes](design-system/README.md) para conhecer as importações, propriedades, referências do Figma, regras de organização visual e convenções da equipe. Os exemplos usam dados fictícios e não se conectam ao servidor da aplicação.

## Telas disponíveis

| Rota | Comportamento atual |
| --- | --- |
| `/login` | Formulário de e-mail e senha que chama `POST /api/auth/login`. Sem sessão, qualquer rota da aplicação redireciona para aqui e volta ao destino pedido depois de entrar. |
| `/` | Redireciona para `/usuarios`. |
| `/usuarios` | Gestor: abas de supervisores e colaboradores, cadastro de supervisor com senha inicial, cadastro e edição de supervisores e colaboradores. Supervisor: busca, cadastro e edição de colaboradores. Integrada a `/api/usuarios`, com carregamento, lista vazia, erro e nova tentativa. |
| `/ocorrencias` | Tela inicial de ocorrências. |
| `/epis` | Tela inicial de EPIs. |
| `/areas-risco` | Tela inicial de áreas de risco. |
| `/tarefas` | Tela inicial de tarefas. |

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
- A busca envia o CPF sem máscara. Recarregar a página mantém a sessão e mostra os dados persistidos pela API.
- A edição (issue #122) reaproveita o formulário do cadastro com os dados atuais. Só nome, data de nascimento e e-mail são enviados; o CPF aparece somente leitura e não vai no corpo, porque a alteração de CPF e a desativação aguardam decisão do grupo (especificação do MVP, seção 11.1). Perfil e senha nunca mudam por esse caminho: a API recusa esses campos com 400. Um 404 na edição indica que o cadastro mudou de perfil ou não existe mais.
- Implementação em `src/features/usuarios/`: `api.ts` (contratos e chamadas), `UsersPage.tsx` (abas por perfil), `SupervisoresPainel.tsx`, `ColaboradoresPainel.tsx`, `CadastroDialog.tsx`, `PessoaFormulario.tsx` e `validacaoPessoa.ts`.

Fora desta entrega: consulta offline de RNF08 (issues #105 e #127), a carga inicial do primeiro Gestor, descrita a seguir, e a autoria das alterações, que depende do módulo de auditoria (issue #121).

### Primeiro acesso

A especificação do MVP prevê que o primeiro Gestor de Segurança venha de carga inicial no banco, mas o repositório ainda não traz essa carga nem uma migração com esse registro. Para entrar na interface em um banco novo é preciso inserir um Gestor com hash BCrypt na tabela `usuarios`. A forma dessa carga é decisão pendente do grupo.

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

O lint verifica as regras de código configuradas. O build verifica os tipos e gera os arquivos em `dist/`. Os testes Playwright verificam a aplicação, o catálogo e seus exemplos no Chromium; sua configuração inicia um servidor local na porta 4173. Os testes da aplicação cobrem login, sessão, saída, expiração de token, matriz de perfis, cadastro de supervisor com senha inicial, cadastro, edição e busca de colaboradores e supervisores, navegação, histórico, teclado e larguras de 360, 768 e 1344 pixels. Somente as respostas HTTP são simuladas; componentes, rotas, sessão e cliente HTTP são executados. Esses testes não substituem a verificação contra o backend real com PostgreSQL.

Para conferir o resultado do build no navegador:

```sh
npm run preview
```

Use o endereço exibido no terminal, normalmente [http://localhost:4173](http://localhost:4173). O comando serve para conferir o build localmente. O Dockerfile atual executa o servidor de desenvolvimento do Vite.

## Organização

- `src/app/`: composição do layout, navegação e estilo das páginas da aplicação.
- `src/features/autenticacao/`: login, sessão e proteção de rotas.
- `src/features/usuarios/`: consulta e cadastro de supervisores e colaboradores por perfil.
- `src/features/ocorrencias/`, `epis/`, `areas-risco/` e `tarefas/`: telas iniciais por funcionalidade.
- `src/features/design-system/`: catálogo e exemplos com dados fictícios.
- `src/shared/components/` e `src/shared/layout/`: componentes e estrutura visual compartilhados.
- `src/shared/api/`: cliente HTTP para a integração com a API.
- `design-system/`: tokens, contratos e guias de componentes.
- `design/`: referências visuais exportadas do Figma.
- `tests/`: testes da aplicação e do catálogo no navegador.

Referências: [especificação arquitetural](../docs/arquitetura/especificacao-arquitetural.md) e [guia de variáveis de ambiente do Vite](https://vite.dev/guide/env-and-mode).
