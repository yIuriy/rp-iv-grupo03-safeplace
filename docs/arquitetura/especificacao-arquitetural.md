# Especificação Arquitetural

## 2. Padrão Arquitetural Adotado: Arquitetura Hexagonal (Ports & Adapters)

### Justificativa de Readequação Arquitetural

A concepção inicial do nosso projeto para o sistema previa uma Arquitetura Híbrida, mesclando Camadas Lógicas, Fronteiras Hexagonais e Barramento de Mensageria Orientado a Eventos (EDA), conforme descrito no documento de nossa entrega anterior, no qual apresentamos as primeiras versões dos diagramas de pacotes e de componentes. No entanto, durante o aprofundamento das fases de análise e projeto, identificamos a necessidade de readequar o padrão arquitetural, migrando para uma abordagem centralizada na Arquitetura Hexagonal (Ports & Adapters) pura. Sendo assim, detalhamos abaixo os fatores técnicos e acadêmicos que motivaram nossa decisão, bem como as estratégias de mitigação que adotamos para garantir o cumprimento integral dos Requisitos Não-Funcionais (RNFs).

### Redução da Complexidade e Carga Cognitiva

O principal motivo para a mudança foi o nível de complexidade acidental introduzido pela abordagem híbrida. Percebemos que a sobreposição de um barramento de eventos (EDA) com uma estrutura de camadas e fronteiras hexagonais gerou um modelo de difícil compreensão. Em termos de implementação de software, concluímos que isso resultaria em um alto custo de desenvolvimento, testes e manutenção para o futuro sistema. Considerando nossa realidade como acadêmicos e projetando o ciclo de vida real de um software corporativo, a manutenibilidade (RNF16) e a clareza estrutural são fundamentais. A Arquitetura Hexagonal simplifica a nossa visão do sistema ao focar em um único paradigma estrutural: o isolamento completo do domínio de negócio no centro da aplicação, comunicando-se com o mundo externo (banco de dados, interfaces web, APIs) exclusivamente por meio de Portas (interfaces) e Adaptadores. Isso reduz a nossa curva de aprendizado e torna o código mais coeso e previsível para o trabalho em equipe futuramente.

### Isolamento do Domínio e Conformidade (Referente aos requisitos RNF10 e RNF15)

Quando focamos no padrão Hexagonal, garantimos que todas as regras de negócio sensíveis do SafePlace, tal como auditorias, emissões de CATs e regras da LGPD, fiquem completamente agnósticas em relação à tecnologia de infraestrutura. Qualquer mudança no banco de dados, no framework web ou nos serviços externos afetará apenas os adaptadores periféricos, protegendo o núcleo do sistema e garantindo a extensibilidade exigida pelo RNF15.

### Mitigação do Processamento de Eventos

A nossa decisão de remover o Barramento de Mensageria Orientado a Eventos (EDA) do escopo trouxe o desafio de atender ao RNF02 (processamento de dados de sensores com latência máxima de 500 milissegundos). Na arquitetura híbrida inicial, os eventos gerenciavam esse fluxo em tempo real. Para suprir essa ausência sem reinserirmos a complexidade do EDA, propomos a utilização de processamentos transversais (Cross-cutting Concerns) acoplados diretamente aos Adaptadores de Entrada (Driving Adapters) do hexágono. De modo geral, a estratégia conceitual que adotamos consiste em:

- Comunicação Direta nas Fronteiras: A captura contínua de dados dos sensores é gerenciada por adaptadores de entrada dedicados, projetados para lidar nativamente com o fluxo de informações dos dispositivos. Isso nos permite recepcionar e isolar os dados sensoriais logo na borda do sistema, antes que entrem no fluxo principal das regras de negócio.
- Tratamento Transversal de Alertas: Aspectos que exigem uma resposta imediata, como o monitoramento em tempo real (RNF02), são gerenciados de forma transversal. Assim, quando um dado crítico é identificado na fronteira do sistema, a ocorrência é encaminhada diretamente aos componentes responsáveis por sua tratativa. A partir disso, essa abordagem nos permite garantir respostas rápidas e o cumprimento das restrições de latência, preservando o desempenho geral da aplicação sem onerar o núcleo de domínio com o roteamento de eventos complexos.

## 3. Diagramas Arquiteturais do Sistema

Toda a modelagem do sistema SafePlace foi desenvolvida utilizando a ferramenta Astah. Esta seção apresenta as visões da arquitetura, divididas por escopo de representação do software.

### Nota sobre a Revisão dos Diagramas de Pacotes e Componentes

Para a elaboração desta etapa final, revisamos e corrigimos os diagramas de pacotes e de componentes entregues anteriormente. Esse refinamento foi necessário para eliminar inconsistências e garantir o total alinhamento com o novo diagrama de implantação.

### 3.1. Diagrama de Pacotes

Esse diagrama está em anexo junto com o email de entrega.

### 3.2. Diagrama de Componentes

Esse diagrama está em anexo junto com o email de entrega.

### 3.3. Diagrama de Implantação

Esse diagrama está em anexo junto com o email de entrega.

### Nota sobre a Representação da Arquitetura Hexagonal na Implantação

Cabe esclarecer uma decisão de modelagem em relação à ausência da representação explícita das Portas de Entrada e de Saída no Diagrama de Implantação. Optamos por não desenhar essas interfaces como blocos separados para manter o rigor técnico exigido pela UML. O diagrama de implantação tem o objetivo de ilustrar a infraestrutura física (servidores, redes, dispositivos) e os artefatos físicos implantáveis (executáveis, pacotes, containers). Como as portas da arquitetura hexagonal são conceitos estritamente lógicos do código (interfaces), elas não existem como artefatos físicos independentes a serem instalados em um servidor. Representá-las como nós ou peças de hardware configuraria um erro conceitual grave, além de gerar poluição visual no modelo. A consistência entre as visões do sistema é garantida pelo empacotamento: os elementos lógicos (Casos de Uso, Portas e Entidades), detalhados previamente no diagrama de componentes, encontram-se implicitamente contidos dentro do artefato do Núcleo da Aplicação (Core), o qual está devidamente mapeado e implantado no nó físico do Servidor de Aplicação. Dessa forma, separamos corretamente a visão lógica da estrutura física.

### Diagrama de componentes:

(está metade certo porque metade eu fiz e metade não deu pra fazer e o pessoal não entendeu nada)
