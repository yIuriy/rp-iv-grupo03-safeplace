# Avaliação de Acessibilidade do MVP (RNF11)

[RNF11](../requisitos/requisitos-nao-funcionais.md#rnf11--interface-responsiva-e-acessível) exige interface responsiva em desktop, tablet e celular e conformidade com a WCAG 2.1 nível AA. A [especificação do MVP](especificacao-mvp-arquitetura.md#52-requisitos-não-funcionais-incluídos) pede verificação nessas larguras com avaliação das diretrizes WCAG 2.1 AA. Este documento registra a avaliação feita na issue #202, em 18 de outubro de 2026, sobre a `dev` em `1de52b2`.

## 1. Método

### 1.1. Verificação automática

O teste `frontend/tests/acessibilidade.spec.ts` roda com `npm test`, como os demais testes Playwright. Ele usa o [axe-core](https://github.com/dequelabs/axe-core) 4.14 pelo pacote `@axe-core/playwright` e executa apenas as regras com as etiquetas `wcag2a`, `wcag2aa`, `wcag21a` e `wcag21aa`, que correspondem aos níveis A e AA da WCAG 2.0 e 2.1.

| Item | Valor |
| --- | --- |
| Telas | Login, ocorrências, EPIs, empréstimos, áreas de risco, tarefas, capacitações e usuários. |
| Estados | Tela carregada com dados; login com credenciais recusadas; formulário principal de cada tela enviado vazio, com as mensagens de erro na página. |
| Formulários | Relato de ocorrência, cadastro de EPI, entrega de EPI, cadastro de área de risco, cadastro de tarefa, cadastro de certificação e cadastro de supervisor. |
| Perfis | Gestor de Segurança e Supervisor nas telas; Gestor nos formulários, que usam os mesmos componentes para os dois perfis. |
| Larguras | 320 px (celular), 768 px (tablet) e 1344 px (desktop). |
| Dados | API simulada no formato do backend, para que tabelas e listas sejam avaliadas preenchidas. |

O teste também verifica dois critérios que o axe não avalia:

- **1.4.10 Reflow**: nenhuma tela pode ser mais larga que a janela, o que obrigaria a rolar na horizontal. A largura de 320 px é a referência desse critério.
- **2.4.7 Foco visível**: o teste percorre cada tela só com a tecla Tab e confere que todo elemento focado tem contorno ou sombra de foco. Confere também que o percurso começa pelo link "Pular para o conteúdo" e termina quando o foco sai da página ou volta ao primeiro elemento, sem ficar preso em outro ponto (critério 2.1.2). No desktop, com o perfil Gestor, o percurso passou por 18 a 33 elementos por tela.

### 1.2. Prova de que as verificações detectam falhas

Antes de aceitar o resultado sem violações, duas execuções descartáveis inseriram falhas conhecidas:

- Na tela de login, uma imagem sem texto alternativo, um texto de baixo contraste e um campo sem rótulo. O axe acusou `image-alt`, `color-contrast` e `label`.
- Nas sete telas, um estilo que remove o contorno e a sombra de foco. A verificação de foco acusou de 17 a 32 elementos por tela.

## 2. Resultado da verificação automática

| Verificação | Execuções | Resultado |
| --- | --- | --- |
| Regras WCAG 2.1 A e AA do axe nas telas | 7 telas × 2 perfis × 3 larguras, mais login (2 estados × 3 larguras) | 0 violações. |
| Regras WCAG 2.1 A e AA do axe nos formulários com erros | 7 formulários × 3 larguras | 0 violações. |
| Reflow sem rolagem horizontal | Login e 7 telas × 2 perfis, nas 3 larguras | Nenhuma tela mais larga que a janela. |
| Foco visível e percurso com Tab | 7 telas × 2 perfis × 3 larguras | Todos os elementos focados têm indicador; o percurso começa pelo link de salto e não fica preso. |

Total: 111 testes, todos aprovados. Nenhuma correção de código foi necessária.

## 3. Avaliação complementar

### 3.1. Teclado

Feita com Playwright, simulando apenas o teclado, sem sessão com uma pessoa usando o sistema.

- Em todas as telas, o primeiro Tab chega ao link "Pular para o conteúdo", que leva à região principal (`main`).
- O percurso com Tab da seção 1.1 alcançou os controles das telas com foco visível e sem ficar preso. Nos campos de data, o Tab passa por dia, mês e ano antes de seguir, comportamento próprio do navegador.
- Os testes já existentes cobrem o restante do uso pelo teclado: navegação entre telas (`app.spec.ts`); login (`login.spec.ts`); diálogo de confirmação que mantém o foco dentro dele (`design-system.spec.ts`); diálogos que fecham com Escape e devolvem o foco ao botão que os abriu (`emprestimos.spec.ts`, `ocorrencias-acompanhamento.spec.ts`, `capacitacoes.spec.ts`); foco no primeiro campo com erro (`ocorrencias-acompanhamento.spec.ts`, `capacitacoes.spec.ts`); e abas operadas pelas setas (`setores.spec.ts`).

### 3.2. Contraste

O contraste de texto (critério 1.4.3) é verificado pela regra `color-contrast` do axe em todos os estados da seção 1.1. O contraste de componentes (critério 1.4.11, mínimo de 3:1) foi calculado a partir das cores de `frontend/design-system/tokens.css`:

| Elemento | Cor | Fundo | Contraste |
| --- | --- | --- | --- |
| Borda de campo, caixa de seleção e botão secundário (`--sp-control-border`) | `#52645C` | Branco `#FFFFFF` | 6,29:1 |
| Borda de campo (`--sp-control-border`) | `#52645C` | Papel `#F6F7F2` | 5,85:1 |
| Contorno de foco (`--sp-brand-primary`) | `#174B3F` | Branco `#FFFFFF` | 9,93:1 |
| Contorno de foco (`--sp-brand-primary`) | `#174B3F` | Papel `#F6F7F2` | 9,22:1 |
| Contorno de foco (`--sp-brand-primary`) | `#174B3F` | Menta `#DDF2E7` | 8,48:1 |
| Borda de campo com erro (`--sp-feedback-danger`) | `#A4303B` | Branco `#FFFFFF` | 6,85:1 |
| Borda decorativa (`--sp-border-default`) | `#CBD7D0` | Branco `#FFFFFF` | 1,48:1 |

A borda decorativa fica abaixo de 3:1, mas só contorna cartões, tabelas, diálogos e divisórias. Nenhum controle depende dela para ser identificado, e o critério 1.4.11 não se aplica a elementos decorativos.

### 3.3. Leitor de tela

A árvore de acessibilidade, que é a informação lida pelos leitores de tela, foi conferida nas sete telas, nos dois perfis, no desktop:

- Cada tela tem um único título de nível 1, uma região principal (`main`), duas regiões de navegação com nome e o link "Pular para o conteúdo".
- Todas as tabelas têm legenda (`caption`) e cabeçalhos de coluna (`th`).
- Cada tela tem de 1 a 4 regiões de anúncio (`role="status"` ou `aria-live`), como a contagem de resultados da consulta de ocorrências.
- As regras de boas práticas do axe (`best-practice`), que incluem regiões da página e hierarquia de títulos, não acusaram violações.

**Pendente**: o teste com um leitor de tela real não foi feito. A conferência acima mostra o que o leitor de tela recebe, mas não substitui ouvir a leitura. Roteiro sugerido, com NVDA no Windows ou Orca no Linux, no Chrome:

1. Entrar no sistema só com o teclado e conferir se os campos e as mensagens de erro do login são lidos.
2. Em cada tela, usar o link "Pular para o conteúdo" e a navegação por títulos.
3. Ler uma tabela célula a célula e conferir se o cabeçalho de cada coluna é anunciado.
4. Registrar um relato de ocorrência, enviar com campos vazios e conferir se os erros são anunciados.
5. Abrir e fechar um diálogo e conferir se o foco volta ao botão de origem.

## 4. Observações

Os pontos abaixo não são violações da WCAG 2.1 AA, mas diferem do padrão dos campos do design system (`frontend/src/shared/components/Fields.tsx`), em que o campo com erro é marcado (`aria-invalid`) e a mensagem fica ligada a ele (`aria-describedby`):

- **Cadastro de área de risco e de tarefa** (`AreaRiscoFormDialog.tsx`, `TarefaFormDialog.tsx`): o erro aparece num aviso no topo do formulário (`role="alert"`), uma mensagem por vez, sem marcar o campo. O texto nomeia o campo, o que atende o critério 3.3.1 (identificação do erro).
- **Cadastro de EPI e registro de manutenção** (`EpiPage.tsx`): a validação é a do navegador, que bloqueia o envio, foca o primeiro campo vazio e mostra uma mensagem própria. A mensagem não faz parte da página, e o texto depende do navegador.

## 5. Limites

- A avaliação cobre o recorte da seção 1.1. Diálogos secundários, como detalhe de ocorrência, plano de ação, devolução e histórico, não passaram pelo axe. Parte deles tem testes de teclado, como o detalhe de ocorrência em `ocorrencias.spec.ts`.
- O axe detecta parte dos critérios da WCAG. Critérios que dependem de julgamento, como clareza das instruções e ordem de leitura com significado, dependem do teste com pessoas e do leitor de tela da seção 3.3.
- Ampliação de texto até 200% (1.4.4) e espaçamento de texto (1.4.12) não foram verificados.
