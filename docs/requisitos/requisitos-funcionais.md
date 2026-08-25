# Requisitos Funcionais

Este documento apresenta a especificação dos Requisitos Funcionais (RFs) do sistema SafePlace.

## RF01: O sistema deve gerenciar os acidentes causados por um funcionário

Este requisito refere-se a acidentes causados individualmente por colaborador, sem a interferência de outros fatores. Por exemplo: quando um colaborador causa um acidente por não estar utilizando o EPI corretamente ou por não estar o utilizando.

## RF02: O sistema deve gerenciar os acidentes causados por uma falha de equipamento de segurança

Este requisito refere-se a acidentes causados por equipamentos de segurança individual (EPIs), ou seja, quando o estado do EPI não está nos conformes.

## RF03: O sistema deve controlar o estoque dos EPIs

Este requisito refere-se ao controle de quais equipamentos estão sendo utilizados, suas quantidades e seu estado de manutenção em relação à qualidade de proteção.

## RF04: O sistema deve controlar a manutenção dos EPIs

Este requisito refere-se ao controle de seu estado de manutenção em relação à qualidade de proteção dos EPIs que estão sendo utilizados e dos que estão armazenados no estoque.

## RF05: O sistema deve mapear as áreas de risco do ambiente de trabalho

Este requisito refere-se ao mapeamento das áreas internas do ambiente de trabalho, a fim de analisar quais áreas apresentam riscos de acidentes.

## RF06: O sistema deve permitir classificar o nível de periculosidade da tarefa a ser realizada

Este requisito refere-se ao nível de perigo que uma determinada tarefa aponta a um colaborador, sendo gerenciada por um gestor de segurança.

## RF07: O sistema deve permitir informar o plano de ação para cada tipo de acidente ocorrido

Este requisito refere-se a o que deve ser feito mediante um acidente ocorrido, sendo gerenciada por um gestor de segurança.

## RF08: O sistema deve gerar alertas automáticos quando um colaborador ou visitante acumular ocorrências de comportamento de risco

Este requisito refere-se à análise do histórico de ações de um determinado colaborador ou visitante, mapeando as suas ocorrências acima de um limite configurável, acionando revisão obrigatória de conduta.

## RF09: O sistema deve registrar o histórico de inspeções periódicas das áreas de risco

Este requisito refere-se ao controle das inspeções necessárias em áreas de risco de acidentes, com o objetivo de indicar conformidade ou não conformidade com as normas regulamentadoras (NRs) aplicáveis.

## RF10: O sistema deve controlar o vencimento das certificações e treinamentos obrigatórios dos funcionários

Este requisito refere-se ao controle das certificações e treinamentos obrigatórios de cada funcionário, bloqueando a alocação em tarefas que exijam habilitação não renovada.

## RF11: O sistema deve controlar a rastreabilidade dos EPIs

Este requisito refere-se ao controle dos equipamentos que estão disponíveis para uso, sendo utilizados ou que não foram devolvidos na data determinada.

## RF12: O sistema deve permitir a conexão entre as tarefas e os EPIs

Este requisito refere-se à interligação entre tarefas e seus devidos EPIs, ou seja, os que devem ser utilizados durante sua execução.

## RF13: O sistema deve permitir que um supervisor crie registros de acidente

Este requisito refere-se à capacidade de um supervisor registrar acidentes ocorridos, informando os dados necessários para identificação, acompanhamento e análise da ocorrência.

## RF14: O sistema deve permitir a gestão de descarte dos EPIs

Este requisito refere-se ao sistema registrar o descarte correto de equipamentos danificados ou vencidos, garantindo o registro de baixa definitiva no estoque.

## RF15: O sistema deve permitir o registro de investigação de acidente

Este requisito refere-se ao sistema realizar o registro detalhado da investigação (ex: Metodologia da Árvore de Causas ou 5 Porquês) para identificar a causa raiz, além da simples descrição do culpado ou falha de equipamento.

## RF16: O sistema deve permitir o registro de incidentes

Este requisito refere-se ao sistema permitir que funcionários relatem situações de risco ou incidentes que não geraram lesão, mas que poderiam ter gerado, para fins de prevenção proativa.

## RF17: O sistema deve permitir a geração de dashboards de segurança

Este requisito refere-se ao sistema exibir indicadores em tempo real, como taxas de frequência de acidentes, gravidade e status de conformidade de EPIs em formato de gráficos.

## RF18: O sistema deve permitir a emissão de relatórios estatísticos

Este requisito refere-se ao sistema permitir a exportação de relatórios periódicos (mensais/anuais) sobre acidentes, treinamentos realizados e áreas de maior risco em formatos PDF ou CSV.

## RF19: O sistema deve permitir a geração de documentação legal

Este requisito refere-se ao sistema permitir o preenchimento automático dos dados para a emissão da Comunicação de Acidente de Trabalho (CAT), aproveitando os dados já registrados no sistema.

## RF20: O sistema deve permitir a gestão de fornecedores e Certificado de Aprovação

Este requisito refere-se ao sistema registrar os fornecedores de EPIs e o número do CA de cada equipamento, alertando caso a data de validade do certificado cadastrado internamente no equipamento esteja vencida.

## RF21: O sistema deve gerenciar o ciclo de vida e substituição inteligente de EPIs

Este requisito refere-se à capacidade do sistema de calcular automaticamente a data estimada de descarte de cada EPI com base na frequência de uso e condições do ambiente, gerando alertas de compra antes do esgotamento do estoque ou vencimento do CA.

## RF22: O sistema deve permitir a gerência de visitantes

Este requisito refere-se à capacidade de um supervisor ou gestor de segurança realizar o registro de visitantes, incluindo informações que os identifiquem e quais EPIs estão temporariamente em sua posse.

## RF23: O sistema deve permitir a gestão de supervisores e colaboradores

Este requisito refere-se a capacidade de um supervisor gerenciar colaboradores e um gestor de segurança gerenciar supervisores, com o sistema gerando automaticamente a senha de ambos os casos.
