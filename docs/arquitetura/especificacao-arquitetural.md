# Especificação Arquitetural

## 1. Objetivo

Este documento registra as decisões arquiteturais do SafePlace e delimita a arquitetura da entrega de MVP. O escopo funcional, os itens excluídos e a matriz de rastreabilidade estão em [Especificação do MVP e Arquitetura](../mvp/especificacao-mvp-arquitetura.md).

O SafePlace acompanha acidentes e incidentes de trabalho, áreas de risco e o uso de Equipamentos de Proteção Individual (EPIs). Sensores, Internet das Coisas (IoT), rotas de evacuação e simulações de emergência não fazem parte da versão atual do sistema.

## 2. Conceitos adotados

MVP, MVC e Arquitetura Hexagonal tratam de decisões diferentes:

- MVP define quais funcionalidades serão entregues primeiro.
- MVC organiza a interface em Model, View e Controller.
- Arquitetura Hexagonal organiza as dependências do sistema por meio de portas e adaptadores.

O MVC pode ser usado no adaptador web sem substituir a Arquitetura Hexagonal. Nesse arranjo, os Controllers recebem as ações do usuário e chamam as portas de entrada da aplicação.

## 3. Padrão arquitetural

O SafePlace adota a Arquitetura Hexagonal, também chamada de Ports and Adapters. O objetivo é manter as regras de negócio independentes da interface, do banco de dados e de serviços externos.

A arquitetura é dividida em três partes:

1. Domínio: contém entidades e regras de negócio.
2. Aplicação: coordena os casos de uso e declara as portas.
3. Adaptadores: conectam a aplicação à interface web, à persistência, à autenticação e à auditoria.

As dependências apontam para o núcleo. O domínio não conhece frameworks, banco de dados ou detalhes da interface.

### 3.1. Portas de entrada

As portas de entrada representam as operações disponíveis para os atores do sistema. No MVP, elas devem cobrir:

- gestão de usuários e perfis;
- registro e consulta de acidentes e incidentes;
- controle de estoque de EPIs;
- controle de manutenção de EPIs;
- empréstimo e devolução de EPIs;
- cadastro e consulta de áreas de risco.

### 3.2. Portas de saída

As portas de saída representam recursos externos ao núcleo:

- repositórios de usuários, ocorrências, EPIs e áreas de risco;
- autenticação e controle de acesso;
- registro de auditoria;
- criptografia e armazenamento seguro.

## 4. Organização lógica do MVP

O núcleo é organizado pelas funcionalidades `usuarios`, `ocorrencias`, `epis` e `areas-de-risco`. Cada funcionalidade reúne suas regras e casos de uso. Os adaptadores ficam separados do núcleo para evitar dependência de tecnologia nas regras de negócio.

Essa organização atende ao RNF14, que exige desacoplamento e extensibilidade. A documentação deste repositório atende parcialmente ao RNF15 ao registrar o escopo, a rastreabilidade e as decisões arquiteturais. A documentação da API e do modelo de dados ainda depende da implementação.

## 5. Requisitos não funcionais e decisões arquiteturais

| Requisito | Decisão arquitetural |
| --- | --- |
| RNF01 | Medir o tempo dos fluxos principais e limitar consultas desnecessárias. |
| RNF03 | Verificar o perfil do usuário antes de executar cada caso de uso protegido. |
| RNF04 | Usar TLS 1.3 na transmissão e AES-256 no armazenamento de dados sensíveis. |
| RNF05 | Manter log imutável das operações, com usuário, data, hora e retenção mínima de 5 anos. |
| RNF09 | Manter as validações normativas no domínio, sem vinculá-las à interface. |
| RNF12 | Manter os fluxos principais curtos e com mensagens de validação claras. |
| RNF14 | Isolar domínio, aplicação e adaptadores por contratos definidos. |
| RNF15 | Versionar a arquitetura, a API e o modelo de dados junto ao projeto. |
| RNF16 | Evitar recursos exclusivos de um navegador ou sistema operacional. |

As decisões acima descrevem a solução esperada. O atendimento de cada RNF deve ser comprovado por testes ou evidências da implementação.

## 6. Artefatos arquiteturais

Os diagramas não são alterados nesta revisão. O grupo fará a atualização em uma etapa posterior. O conteúdo esperado de cada artefato está definido na [Especificação do MVP e Arquitetura](../mvp/especificacao-mvp-arquitetura.md#8-artefatos-arquiteturais-pendentes).

| Artefato | Status |
| --- | --- |
| Diagrama de pacotes | Pendente de atualização |
| Diagrama de componentes lógico | Pendente de atualização |
| Diagrama de componentes executável | Pendente de atualização |
| Diagrama de sequência | Pendente de atualização |
| Diagrama de classes reduzido ao MVP | Pendente de atualização |

## 7. Limites da arquitetura atual

- O barramento de eventos não faz parte do MVP.
- Não há adaptadores para sensores ou dispositivos IoT.
- RF21 e UC10 permanecem documentados como evolução futura, mas não entram nos componentes do MVP.
- Relatórios, CAT, dashboards, funcionamento offline e integrações externas permanecem fora do núcleo da primeira entrega.
- Tecnologias de implementação ainda não foram definidas neste documento.
