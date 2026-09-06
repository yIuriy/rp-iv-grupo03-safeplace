# Interface web do SafePlace

A interface usa React 19, TypeScript e Vite 8. A biblioteca de componentes compartilhados está disponível para o desenvolvimento das funcionalidades. O catálogo interativo fica em `/design-system`, e a tela de referência de áreas de risco fica em `/design-system/examples/risk-areas`.

Consulte o [guia de componentes](design-system/README.md) para conhecer as importações, propriedades, referências do Figma, regras de organização visual e convenções da equipe. Os exemplos usam dados fictícios e não se conectam ao servidor da aplicação.

## Executar em desenvolvimento

Use Node.js 22.12 ou superior com npm. A partir da raiz do repositório:

```sh
cd frontend
npm ci
npm run dev
```

Abra o endereço exibido no terminal, normalmente [http://localhost:5173](http://localhost:5173). O catálogo pode ser usado sem backend ou banco de dados. Para executar o conjunto com PostgreSQL e API, consulte o [README principal](../README.md#executar-com-docker-compose).

## Configurar a URL da API

O cliente HTTP em [src/shared/api/https.ts](src/shared/api/https.ts) lê `VITE_URL_API`. Sem essa variável, usa `http://localhost:8080/api`. Para usar outro endereço, crie `frontend/.env.local` com o conteúdo abaixo e reinicie o Vite:

```dotenv
VITE_URL_API=http://localhost:8080/api
```

O [Compose atual](../docker-compose.yml) fornece `VITE_API_URL`, nome diferente do lido pelo cliente. Com os valores locais padrão, o cliente usa o endereço padrão. A correção dessa divergência de configuração continua pendente; alterar apenas `VITE_API_URL` não muda a URL usada pelo código. Os exemplos do catálogo ainda não utilizam esse cliente.

## Validar e gerar a interface

Execute em `frontend/`:

```sh
npm run lint
npm run build
npx playwright install chromium
npm test
```

O lint verifica as regras de código configuradas. O build verifica os tipos e gera os arquivos em `dist/`. Os testes Playwright verificam o catálogo e seus exemplos no Chromium; sua configuração inicia um servidor local na porta 4173. Eles não demonstram integração com a API nem persistência dos dados.

Para conferir o resultado do build no navegador:

```sh
npm run preview
```

Use o endereço exibido no terminal, normalmente [http://localhost:4173](http://localhost:4173). O comando serve para conferir o build localmente. O Dockerfile atual executa o servidor de desenvolvimento do Vite.

## Organização

- `src/features/design-system/`: catálogo e exemplos com dados fictícios.
- `src/shared/components/` e `src/shared/layout/`: componentes e estrutura visual compartilhados.
- `src/shared/api/`: cliente HTTP para a integração com a API.
- `design-system/`: tokens, contratos e guias de componentes.
- `design/`: referências visuais exportadas do Figma.
- `tests/`: testes do catálogo no navegador.

Referências: [especificação arquitetural](../docs/arquitetura/especificacao-arquitetural.md) e [guia de variáveis de ambiente do Vite](https://vite.dev/guide/env-and-mode).
