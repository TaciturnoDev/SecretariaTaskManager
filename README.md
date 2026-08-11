Taciturno ( nome temporário, ainda não dei um nome ao projeto.)

Sistema corporativo de gerenciamento de demandas desenvolvido com Spring Boot, Java e MySQL.

Este projeto, é um sistema de gerenciamento de demandas desenvolvido para a Secretaria Municipal do Desenvolvimento de sistemas -  SDE
O projeto foi, e está sendo construído utilizando arquitetura em camadas, controle de permissões por perfil, histórico completos das movimentações e foco em escalabilidade para futuras integrações. 
Projeto FullStack planejado, e construído por mim, no qual inicialmente foi requisitado para que se torne um sistema legado e futuramente seja usado por outras secretarias. 

O motivo deste projeto, é centralizar o controle, tornar as atividades da secretaria dinâmica, com histórico inteligente, sistemas de notificações para que as demandas sejam cumpridas em prazo. 
aumentando assim a produção, segurança, e sendo controlado e auditável. sem perder informações, com documentações, dashboards, métricas inteligentes, sistemas de setores, eventos e muito mais.

Tecnologias: Java17, SpringBoot, SpringSecurity, SpringDataJPA, Hibernate,MySQL, Maven, Bootstrap, HTML, CSS e JavaScript

Autentitação: Login Seguro, JWT, Primeiro Acesso, Alteração de senha.
Gestão de Demandas: Criar Demandas, Editar, Atualizar status/Comentários/Histórico, realizar Uploads de arquivos. (integrados ao banco de dados com backup), Prioridades, Exclusão lógica
Delegação: Encaminhamento entre usuários, Histórico de movimentação, Comentários na delegação

Setores: Administração de setores, Solicitação de demandas entre setores, Controle de responsáveis
Auditoria: Histórico completo, Usuário Responsável, Data da movimentação, Comentários
Arquivos: Upload de anexos, Armazenamento em disco, Registro em banco, Download
Relatórios: PDF Completo da demanda, organizado para que também possa ser acompanhado em folha, caso prefira isso ao digital.
Administração: Painel administrativo, Gestão de usuários, Controle de permssões

Arquitetura: Controller -- Service -- Repository -- Banco

Estrutura do projeto: 
Controller -- Service -- Repository -- Entity -- DTO -- Config -- Security -- Exception -- Util

Roadmap completo
o que ja foi desenvolvido:
* Gestão de demandas, * Histórico, * Delegação, * PDF, * Anexos

O que ainda está em Desenvolvimento e foi requisitado que o projeto tenha:
* Notificações, * Dashboard, * Timeline, * SLA, * Chat interno
<img width="1620" height="890" alt="image" src="https://github.com/user-attachments/assets/2f17ede5-f280-4511-b11a-a76865d90284" />
<img width="1627" height="893" alt="image" src="https://github.com/user-attachments/assets/52207dfa-1e37-44e8-bb26-f1701582edda" />
<img width="1626" height="886" alt="image" src="https://github.com/user-attachments/assets/d444599c-ab39-4bb4-bd6d-8405a7e8e400" />
<img width="522" height="740" alt="image" src="https://github.com/user-attachments/assets/d0d601b8-59e9-4a9f-8771-fd2a6046f598" />
<img width="519" height="737" alt="image" src="https://github.com/user-attachments/assets/ba4a6ab1-2122-488b-9ea6-5726814d673c" />
<img width="1628" height="885" alt="image" src="https://github.com/user-attachments/assets/c00d2263-12ad-4c4f-b536-4806b55b020b" />
<img width="1622" height="890" alt="image" src="https://github.com/user-attachments/assets/fae7d72a-d7d7-4e2a-a228-a2be03f78b96" />
<img width="1620" height="891" alt="image" src="https://github.com/user-attachments/assets/c9f69e1d-c5f3-4c0c-afc2-7b56242af7dc" />
<img width="269" height="462" alt="image" src="https://github.com/user-attachments/assets/3f851d92-d6f3-4ea6-bd4a-c905228e9afd" />
<img width="1565" height="620" alt="image" src="https://github.com/user-attachments/assets/04efe012-1168-4770-b662-a90c0b67fd7a" />

Desafios enfrentados durante o desenvolvimento
Modelagem de histórico imutável
Controle de permissões entre user,admin e superadmin, exclusão logica
desenvolvimento solo em curto tempo. com reuniões constantes para o acompanhamento.
documentação técnica, e documentação ilustrativa, para que o sistema possa ser compreendido facilmente por ooutro desenvolvedor
e se torne um sistema legado.
Armazenamento de anexos em disco com persistência no banco também foi um desafio, atualizar as tabelas sem perdas dos projetos.

O que diferencia esse projeto? Foi inteiramente desenvolvido para suprir demandas reais do ambiente corporativo da Secretaria.
o Sistema possui separação entre criador e responsável pela demanda, histórico imutável de auditoria, delegação de tarefas, controles de acesso por paéis,
anexos persistido em disco com separação bimestral para que seja feito backup. geração de relatórios em PDF. uma arquitetura preparada e desenvolvida
com lógica na expansão e evolução. o foco do projeto é suprir demandas em tempo real alinhado com desenvolvimento, sempre mantendo boas práticas de engenharia de software
e modelarem de regras de negócios, Clean Code, Documentação e etc. indo MUITO ALÉM do que se espera, Além de que foi inteiramente FullStack
Desenvolvido somente por mim, do início ao fim.






