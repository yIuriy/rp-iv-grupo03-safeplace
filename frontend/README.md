# Interface web do SafePlace

A biblioteca de componentes compartilhados está pronta para o desenvolvimento das funcionalidades. Execute `npm ci` e `npm run dev`. Depois, abra `/design-system` para acessar o catálogo interativo ou `/design-system/examples/risk-areas` para consultar a tela de referência montada com os componentes.

Consulte o [guia de componentes](design-system/README.md) para conhecer as importações, propriedades, referências do Figma, regras de organização visual e convenções da equipe. Os exemplos usam dados fictícios e não se conectam ao servidor da aplicação.

Para validar o projeto, execute `npm run build`, `npm run lint` e `npm test`. Antes de executar os testes, instale o navegador usado neles com `npx playwright install chromium`.

## Notas do modelo original React + TypeScript + Vite

Este modelo fornece uma configuração mínima para usar React com Vite, atualização de módulos durante o desenvolvimento (HMR) e algumas regras de análise de código do ESLint.

Há duas extensões oficiais disponíveis:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) usa [Oxc](https://oxc.rs).
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) usa [SWC](https://swc.rs/).

## Compilador do React

O compilador do React não está habilitado neste modelo devido ao impacto no desempenho durante o desenvolvimento e a geração da versão de produção. Para adicioná-lo, consulte a [documentação de instalação](https://react.dev/learn/react-compiler/installation).

## Ampliação da configuração do ESLint

Para uma aplicação de produção, recomenda-se atualizar a configuração para habilitar regras de análise de código que considerem os tipos do TypeScript:

```js
export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      // Outras configurações...

      // Substitua tseslint.configs.recommended pela configuração abaixo
      tseslint.configs.recommendedTypeChecked,
      // Como alternativa, use esta opção para regras mais rígidas
      tseslint.configs.strictTypeChecked,
      // Opcionalmente, inclua esta configuração para regras de estilo
      tseslint.configs.stylisticTypeChecked,

      // Outras configurações...
    ],
    languageOptions: {
      parserOptions: {
        project: ['./tsconfig.node.json', './tsconfig.app.json'],
        tsconfigRootDir: import.meta.dirname,
      },
      // Outras opções...
    },
  },
])

```

Também é possível instalar [eslint-plugin-react-x](https://npmx.dev/package/eslint-plugin-react-x) e [eslint-plugin-react-dom](https://npmx.dev/package/eslint-plugin-react-dom) para usar regras de análise de código específicas do React:

```js
// eslint.config.js
import reactX from 'eslint-plugin-react-x'
import reactDom from 'eslint-plugin-react-dom'

export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      // Outras configurações...
      // Habilita as regras de análise para React
      reactX.configs['recommended-typescript'],
      // Habilita as regras de análise para React DOM
      reactDom.configs.recommended,
    ],
    languageOptions: {
      parserOptions: {
        project: ['./tsconfig.node.json', './tsconfig.app.json'],
        tsconfigRootDir: import.meta.dirname,
      },
      // Outras opções...
    },
  },
])

```
