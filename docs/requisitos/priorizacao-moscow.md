# Priorização de Requisitos — MoSCoW

Este documento apresenta a estrutura inicial para priorização dos requisitos do sistema SafePlace utilizando a técnica MoSCoW.

A priorização deve começar pelos Requisitos Funcionais (RFs), pois eles representam as capacidades que o sistema precisa oferecer. Em seguida, os Requisitos Não-Funcionais (RNFs) devem ser priorizados para indicar quais critérios de qualidade, segurança, desempenho e manutenção são indispensáveis para sustentar essas funcionalidades.

## Critérios da Técnica MoSCoW

| Classificação | Significado em inglês | Critério de uso |
| --- | --- | --- |
| Deve ter | Must have | Requisito indispensável para o sistema cumprir seu objetivo principal. |
| Deveria ter | Should have | Requisito importante, mas que pode ser ajustado ou entregue após os itens indispensáveis. |
| Poderia ter | Could have | Requisito desejável, com menor impacto caso não seja implementado inicialmente. |
| Não terá agora | Won't have | Requisito fora do escopo da versão atual, mas que pode ser retomado futuramente. |

## Priorização dos Requisitos Funcionais

| ID | Requisito | Prioridade | Justificativa |
| --- | --- | --- | --- |
| RF01 | Gerenciar acidentes causados por um funcionário | Deveria ter | Complementa o registro de acidentes, mas pode ser tratado como classificação após o cadastro principal. |
| RF02 | Gerenciar acidentes causados por falha de equipamento de segurança | Deveria ter | Complementa a análise do acidente, mas depende do registro principal e do controle de EPIs. |
| RF03 | Controlar o estoque dos EPIs | Deve ter | Sustenta o controle básico de equipamentos de proteção, parte central do sistema. |
| RF04 | Controlar a manutenção dos EPIs | Deve ter | Garante que os EPIs cadastrados possam ser acompanhados quanto ao estado de uso e conservação. |
| RF05 | Mapear as áreas de risco do ambiente de trabalho | Deve ter | Permite relacionar acidentes, incidentes e inspeções ao contexto de risco do ambiente. |
| RF06 | Classificar o nível de periculosidade da tarefa | Deveria ter | Ajuda na análise preventiva, mas pode ser implementado após o cadastro das áreas e ocorrências. |
| RF07 | Informar o plano de ação para cada tipo de acidente | Deveria ter | É importante para resposta aos acidentes, mas pode evoluir após a estrutura inicial de registros. |
| RF08 | Gerar alertas automáticos por comportamento de risco | Poderia ter | Exige regras automáticas e análise de histórico, podendo ficar para uma etapa posterior. |
| RF09 | Registrar histórico de inspeções periódicas das áreas de risco | Deveria ter | Fortalece o acompanhamento preventivo, mas pode ser entregue após o mapeamento das áreas. |
| RF10 | Controlar vencimento de certificações e treinamentos obrigatórios | Poderia ter | É relevante para prevenção, mas amplia o escopo para gestão de capacitações. |
| RF11 | Controlar a rastreabilidade dos EPIs | Deve ter | Permite saber quais EPIs estão disponíveis, em uso ou pendentes de devolução. |
| RF12 | Conectar tarefas e EPIs | Deveria ter | Melhora a consistência do uso de EPIs, mas depende do cadastro de tarefas e equipamentos. |
| RF13 | Criar registros de acidente | Deve ter | Representa uma das funcionalidades centrais para acompanhamento de acidentes de trabalho. |
| RF14 | Gerenciar descarte dos EPIs | Deveria ter | Complementa o ciclo de vida dos EPIs, mas pode vir após estoque, manutenção e rastreabilidade. |
| RF15 | Registrar investigação de acidente | Poderia ter | Aprofunda a análise do acidente, mas pode ser simplificado ou adiado na versão inicial. |
| RF16 | Registrar incidentes | Deve ter | Permite acompanhar situações de risco antes que gerem acidentes, fortalecendo a prevenção. |
| RF17 | Gerar dashboards de segurança | Poderia ter | Depende dos dados já cadastrados e pode começar como visualização simples em etapa posterior. |
| RF18 | Emitir relatórios estatísticos | Poderia ter | Depende da consolidação dos dados e pode ser implementado depois do fluxo principal. |
| RF19 | Gerar documentação legal | Poderia ter | É importante, mas exige maior cuidado com regras legais e dados padronizados. |
| RF20 | Gerenciar fornecedores e Certificado de Aprovação | Deveria ter | Apoia o controle dos EPIs, mas pode ser entregue após o cadastro e rastreabilidade dos equipamentos. |
| RF21 | Gerenciar ciclo de vida e substituição inteligente de EPIs | Não terá agora | Exige cálculo preditivo, regras de uso e alertas de compra, ficando fora do escopo da versão inicial. |
| RF22 | Gerenciar visitantes | Poderia ter | É útil para segurança, mas não é essencial para o fluxo principal de acidentes e EPIs. |
| RF23 | Gerenciar supervisores e colaboradores | Deve ter | É necessário para controlar usuários e responsabilidades dentro dos fluxos principais. |

## Priorização dos Requisitos Não-Funcionais

| ID | Requisito | Prioridade | Justificativa |
| --- | --- | --- | --- |
| RNF01 | Tempo de resposta | Deve ter | O sistema precisa responder rapidamente às solicitações feitas, a fim de não gerar descontentamento. |
| RNF02 | Capacidade de usuários simultâneos | Deveria ter | O sistema deve permitir um número adequado de usuários simultâneos, mas, em um primeiro momento, deve atender a um número reduzido. |
| RNF03 | Controle de acesso por perfil | Deve ter | As funcionalidades são baseadas nos perfis, sendo uma das bases do sistema. |
| RNF04 | Criptografia de dados sensíveis | Deveria ter | Os dados devem ser criptografados, porém, para uma versão inicial, uma criptografia mais básica pode ser aplicada. |
| RNF05 | Rastreabilidade de ações (auditoria) | Poderia ter | Sua importância se dá na implementação final do software, para acompanhar as ações dos usuários. |
| RNF06 | Disponibilidade mínima | Deveria ter | O sistema deve estar disponível na maior parte do tempo, mas, em sua versão inicial, tempos maiores de manutenção podem ser necessários. |
| RNF07 | Backup e recuperação de dados | Poderia ter | Não é necessário nas versões iniciais, cujos dados serão usados para teste. |
| RNF08 | Funcionamento offline parcial | Poderia ter | Deve ser implementado após as demais funcionalidades, pois depende de sincronização e armazenamento local. |
| RNF09 | Conformidade com normas regulamentadoras | Deve ter | Necessário para a base legal do software. |
| RNF10 | Integridade e validade dos documentos gerados | Deveria ter | Relacionado às normas e aos processos legais, torna-se essencial quando houver geração de documentos oficiais pelo sistema. |
| RNF11 | Interface responsiva e acessível | Deveria ter | Deve ser implementado após o desenvolvimento das principais funcionalidades, como refinamento do software. |
| RNF12 | Facilidade de aprendizado | Deve ter | Mesmo em suas versões iniciais, o software não deve ser difícil de usar. |
| RNF13 | Suporte multilíngue | Poderia ter | Não afeta o software como um todo, sendo voltado apenas para fins de maior visibilidade e suporte. |
| RNF14 | Modularidade e extensibilidade | Deve ter | Necessário com base nos bons princípios de desenvolvimento. |
| RNF15 | Documentação técnica | Deve ter | Essencial para acompanhar o desenvolvimento do software. |
| RNF16 | Compatibilidade com navegadores e sistemas operacionais | Deve ter | Requisito essencial para que o software funcione no maior número de dispositivos possível. |
| RNF17 | Integração via API | Poderia ter | Deve ter sua implementação analisada após a criação do MVP. |

## Orientações para Revisão

- Priorizar primeiro os RFs diretamente ligados ao controle de acidentes, EPIs, áreas de risco e documentação legal.
- Usar os RNFs para validar se os requisitos essenciais possuem segurança, rastreabilidade, disponibilidade e conformidade suficientes.
- Evitar classificar muitos requisitos como `Deve ter`, para que a priorização realmente diferencie o que é indispensável do que pode ficar para uma versão futura.
- Registrar uma justificativa curta para cada prioridade escolhida.
