# Interface web do SafePlace

A interface usa React 19, TypeScript e Vite 8. A aplicação abre em `/usuarios` e reutiliza o layout e os componentes compartilhados. O catálogo interativo fica em `/design-system`, e a tela de referência de áreas de risco fica em `/design-system/examples/risk-areas`.

Consulte o [guia de componentes](design-system/README.md) para conhecer as importações, propriedades, referências do Figma, regras de organização visual e convenções da equipe. Os exemplos usam dados fictícios e não se conectam ao servidor da aplicação.

## Telas disponíveis

| Rota | Comportamento atual |
| --- | --- |
| `/` | Redireciona para `/usuarios`. |
| `/usuarios` | Consulta `GET /api/usuarios`, exibe nomes e trata carregamento, lista vazia, erro e nova tentativa. |
| `/ocorrencias` | Tela inicial de ocorrências. |
| `/epis` | Tela inicial de EPIs. |
| `/areas-risco` | Tela inicial de áreas de risco. |
| `/tarefas` | Tela inicial de tarefas. |

As quatro telas iniciais identificam a funcionalidade como em desenvolvimento. Não exibem cadastros fictícios nem oferecem operações de criação ou edição. Embora o backend já tenha operações de ocorrências e EPIs, suas interfaces completas não fazem parte desta base.

O menu indica a tela ativa e permite navegar sem recarregar a página. URLs diretas, atualização da página e histórico do navegador são suportados. Endereços desconhecidos mostram uma página com link para voltar ao início. A navegação reutiliza o menu compacto, os estados de foco e o link para pular ao conteúdo do design system.

### Integração inicial de usuários

O endpoint de usuários ainda depende da [issue #74](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/74). Seu contrato de resposta não está definido na base atual. O formato **provisório** consumido por esta tela é uma lista JSON com `id` (número ou texto) e `nome` (texto):

```json
[{ "id": 1, "nome": "Nome de exemplo" }]
```

Esse exemplo documenta o formato; não é usado como dado na aplicação. A leitura fica em `src/features/usuarios/UsersPage.tsx`, para ser alinhada ao contrato definitivo quando o endpoint existir. Campos adicionais são ignorados. A API continua responsável por nunca enviar senhas, conforme a issue #74.

A consulta usa o cliente compartilhado, tem limite de espera de 10 segundos e é cancelada ao sair da tela. Endpoint ausente, falha de conexão e resposta incompatível mostram uma mensagem com nova tentativa. A integração com o backend real permanece pendente até a entrega e a conferência do contrato da issue #74.

A tela não aplica autenticação nem atribui perfis. RF23 e RNF03 distinguem usuários com acesso de colaboradores cadastrados sem conta. O texto da issue #74 ainda cita `COLABORADOR` como perfil, divergência já registrada na documentação do MVP; esta entrega não redefine essa decisão.

O RNF08 continua pendente: não há PWA, cache local nem sincronização. A documentação do MVP ainda precisa delimitar os dados disponíveis offline. Os testes de teclado e tamanhos de tela apoiam o RNF11, mas não constituem uma auditoria completa de WCAG 2.1 AA.

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

O lint verifica as regras de código configuradas. O build verifica os tipos e gera os arquivos em `dist/`. Os testes Playwright verificam a aplicação, o catálogo e seus exemplos no Chromium; sua configuração inicia um servidor local na porta 4173. Os testes da aplicação cobrem navegação, histórico, teclado, larguras de 360, 768 e 1344 pixels e os estados da consulta de usuários. Somente as respostas HTTP são simuladas; componentes, rotas e cliente HTTP são executados. Esses testes não demonstram persistência nem integração com o endpoint real de usuários.

Para conferir o resultado do build no navegador:

```sh
npm run preview
```

Use o endereço exibido no terminal, normalmente [http://localhost:4173](http://localhost:4173). O comando serve para conferir o build localmente. O Dockerfile atual executa o servidor de desenvolvimento do Vite.

## Organização

- `src/app/`: composição do layout, navegação e estilo das páginas da aplicação.
- `src/features/usuarios/`: consulta inicial de usuários e leitura do contrato provisório.
- `src/features/ocorrencias/`, `epis/`, `areas-risco/` e `tarefas/`: telas iniciais por funcionalidade.
- `src/features/design-system/`: catálogo e exemplos com dados fictícios.
- `src/shared/components/` e `src/shared/layout/`: componentes e estrutura visual compartilhados.
- `src/shared/api/`: cliente HTTP para a integração com a API.
- `design-system/`: tokens, contratos e guias de componentes.
- `design/`: referências visuais exportadas do Figma.
- `tests/`: testes da aplicação e do catálogo no navegador.

Referências: [especificação arquitetural](../docs/arquitetura/especificacao-arquitetural.md) e [guia de variáveis de ambiente do Vite](https://vite.dev/guide/env-and-mode).
