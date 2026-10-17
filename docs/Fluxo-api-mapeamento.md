# Guia técnico dos épicos da oficina mecânica

**Situação atual:** o repositório contém diagramas e uma aplicação para abrir desenhos. Não há backend implementado. As classes, os componentes e as rotas deste guia são propostas para implementação, independentes de framework.

Fontes:

- [Fluxo técnico e histórias](fluxo-tecnico-historias.excalidraw): referência principal para os épicos, histórias e dependências.
- [Fluxo de negócio](fluxo-negocio-as-is-to-be.excalidraw): referência para atendimento, aprovação, estoque e entrega.
- [Mapeamento DDD](ddd-oficina-mecanica.excalidraw): referência para os conceitos, as responsabilidades e os eventos do negócio.

## 1. Como ler este guia

| Termo | Significado simples |
| --- | --- |
| Classe | Representa um conceito ou uma responsabilidade no código. `Cliente`, por exemplo, representa quem solicita o atendimento. |
| API | Conjunto de operações que permite a uma aplicação conversar com o sistema da oficina. Aqui, os módulos pertencem à mesma API; não representam necessariamente sistemas separados. |
| Rota | Endereço de uma operação, como `/clientes`. `{id}` representa o identificador do registro. |
| Método HTTP | Indica a ação: `GET` consulta, `POST` cria ou registra uma operação, `PATCH` altera parte de um registro e `DELETE` remove um recurso quando essa remoção é permitida. |
| Evento de domínio | Aviso de que algo aconteceu no negócio. `OrcamentoAprovado` informa que o cliente aprovou o orçamento. Não é uma rota. |
| História (HU) | Uma necessidade do usuário ou uma tarefa do projeto, identificada pelo número presente no diagrama. |
| Contexto de negócio | Grupo de responsabilidades relacionadas, como Clientes, Estoque ou Ordens de Serviço. |

### Convenções propostas

- `Controller` recebe a chamada HTTP e encaminha a solicitação; `Service` coordena a operação; `Repository` consulta e salva os dados. Exemplo: `ClienteController`, `ClienteService` e `ClienteRepository`.
- `Servico` é o trabalho oferecido pela oficina, como troca de óleo. `CatalogoServicoService` é uma classe que coordena o cadastro desses trabalhos; não é um serviço de mecânica.
- As tabelas mostram classes diretamente relacionadas à história. Os repositórios de cada módulo são apresentados separadamente para evitar repetição.
- **Autenticado** significa exigir JWT administrativo válido e permissão conforme a seção 7. **Público** significa acesso do cliente sem conta administrativa; exige o token individual de acesso à OS descrito no épico 7 e na seção 7.
- As rotas são relativas ao endereço da API. Não se define framework, linguagem de programação ou prefixo de versão neste guia.
- O nome de uma rota não precisa corresponder a uma classe ou tabela. Os sufixos técnicos indicam responsabilidades; não obrigam criar uma classe para cada história. `Handler` significa tratador de um evento.

## 2. Glossário do negócio

| Termo usado pela oficina | Nome proposto no código | O que representa |
| --- | --- | --- |
| Cliente | `Cliente` | Pessoa ou empresa responsável pelo atendimento, identificada por CPF ou CNPJ. |
| CPF/CNPJ | `DocumentoCliente` | Documento de identificação do cliente; não representa uma senha. |
| Veículo | `Veiculo` | Veículo vinculado a um cliente. |
| Placa | `Placa` | Identificação do veículo. |
| Serviço | `Servico` | Trabalho disponível no catálogo da oficina, com valor e tempo estimado. |
| Peça | `Peca` | Material ou insumo cadastrado para utilização nos atendimentos. |
| Estoque | `Estoque` | Controle da quantidade disponível de uma peça. |
| Movimentação de estoque | `MovimentacaoEstoque` | Registro de entrada ou saída de uma quantidade de peças. |
| Ordem de Serviço (OS) | `OrdemServico` | Registro do atendimento de um veículo, do recebimento até a entrega. |
| Item de serviço | `ItemServico` | Serviço incluído em uma OS, com os dados necessários para compor seu valor. |
| Item de peça | `ItemPeca` | Peça incluída em uma OS, com quantidade e valor unitário. |
| Orçamento | `Orcamento` | Composição dos valores de serviços e peças submetida à decisão do cliente. |
| Situação da OS | `StatusOrdemServico` | Etapa atual do atendimento. |
| Histórico de status | `HistoricoStatusOrdemServico` | Registro das mudanças de etapa e de quando ocorreram. |
| Usuário | `Usuario` | Pessoa com credenciais para acessar as operações administrativas. |
| Perfil de acesso | `PerfilAcesso` | Conjunto de permissões de um usuário, associado à sua função. |
| Situação do orçamento | `StatusOrcamento` | Indica se o orçamento está calculado, enviado, aprovado, recusado, substituído ou cancelado. É diferente da etapa da OS. |
| Cancelamento | `CancelamentoOrdemServico` | Registro do encerramento antecipado permitido, com motivo, responsável e data. |
| Acesso do cliente à OS | `AcessoPublicoOrdemServico` | Credencial temporária que autoriza consultar e decidir sobre o orçamento de uma única OS. |

Cliente e usuário são conceitos distintos: o cliente acompanha seu atendimento pela API pública; o usuário acessa as operações internas da oficina.

## 3. Épicos e histórias

Os números e títulos abaixo preservam o diagrama técnico, inclusive suas abreviações. As explicações e os identificadores técnicos usam os termos completos.

### Épico 0 — Fundação

| História | Objetivo em linguagem simples | Componentes e entregáveis propostos | Método HTTP e rota | Acesso |
| --- | --- | --- | --- | --- |
| 0.1 Setup do projeto | Preparar a estrutura inicial do backend. | Módulos de clientes, veículos, catálogo, estoque, ordens de serviço e autenticação; ponto de entrada da aplicação. | Não se aplica. | Não se aplica. |
| 0.2 Config. PostgreSQL + migrations | Conectar o sistema ao banco e controlar a evolução de suas tabelas. | Configuração do PostgreSQL e scripts de migração versionados. | Não se aplica. | Não se aplica. |
| 0.3 Config. Swagger/OpenAPI | Documentar as operações e permitir sua consulta. | Especificação OpenAPI e interface Swagger; os endereços da documentação dependem da tecnologia escolhida. | Não se aplica como rota de negócio. | Local na demonstração; desabilitado fora dela por padrão. |
| 0.4 Dockerfile + docker-compose | Facilitar a execução da aplicação e do banco. | `Dockerfile` e `docker-compose.yml`. | Não se aplica. | Não se aplica. |
| 0.5 README.md | Explicar como preparar, executar e usar o projeto. | `README.md` com pré-requisitos, configuração e instruções de execução. | Não se aplica. | Não se aplica. |

### Épico 1 — Cliente

API de Clientes. Persistência proposta: `ClienteRepository`.

| História | Objetivo em linguagem simples | Classes e responsabilidades propostas | Método HTTP e rota | Acesso |
| --- | --- | --- | --- | --- |
| 1.1 Cadastrar cliente | Registrar quem solicita o atendimento. | `ClienteController`: recebe o cadastro; `ClienteService`: coordena a criação; `Cliente` e `DocumentoCliente`: representam os dados e o documento. | `POST /clientes` | Autenticado. |
| 1.2 Listar/buscar clientes | Encontrar clientes cadastrados. | `ClienteController` e `ClienteService`: consultam clientes e aplicam filtros de busca. | `GET /clientes` | Autenticado. |
| 1.3 Detalhar cliente | Consultar os dados de um cliente. | `ClienteController` e `ClienteService`: recuperam um `Cliente` pelo identificador. | `GET /clientes/{id}` | Autenticado. |
| 1.4 Editar/inativar cliente | Corrigir dados ou marcar o cadastro como inativo. | `ClienteService`: coordena a alteração de `Cliente`, preservando seu vínculo com atendimentos anteriores. | `PATCH /clientes/{id}` | Autenticado. |

### Épico 2 — Veículo

API de Veículos. Persistência proposta: `VeiculoRepository`.

| História | Objetivo em linguagem simples | Classes e responsabilidades propostas | Método HTTP e rota | Acesso |
| --- | --- | --- | --- | --- |
| 2.1 Cadastrar veículo | Registrar um veículo e vinculá-lo ao cliente. | `VeiculoController`: recebe o cadastro; `VeiculoService`: coordena o vínculo; `Veiculo` e `Placa`: representam o veículo e sua placa. | `POST /veiculos` | Autenticado. |
| 2.2 Listar veículos | Consultar veículos, inclusive por cliente. | `VeiculoController` e `VeiculoService`: listam os cadastros. | `GET /veiculos` | Autenticado. |
| 2.3 Detalhar veículo | Ver os dados de um veículo. | `VeiculoController` e `VeiculoService`: recuperam um `Veiculo`. | `GET /veiculos/{id}` | Autenticado. |
| 2.4 Editar/inativar veículo | Atualizar dados ou marcar o veículo como inativo. | `VeiculoService`: altera `Veiculo`, preservando o histórico de atendimentos. | `PATCH /veiculos/{id}` | Autenticado. |

### Épico 3 — Catálogo de Serviços

API do Catálogo de Serviços. Persistência proposta: `ServicoRepository`.

| História | Objetivo em linguagem simples | Classes e responsabilidades propostas | Método HTTP e rota | Acesso |
| --- | --- | --- | --- | --- |
| 3.1 Cadastrar serviço | Registrar um trabalho oferecido pela oficina. | `ServicoController`: recebe os dados; `CatalogoServicoService`: coordena o cadastro; `Servico`: representa o trabalho, seu valor e tempo estimado. | `POST /servicos` | Autenticado. |
| 3.2 Listar/detalhar serviços | Consultar os trabalhos disponíveis e seus dados. | `ServicoController` e `CatalogoServicoService`: consultam o catálogo. | `GET /servicos` e `GET /servicos/{id}` | Autenticado. |
| 3.3 Editar/inativar serviço | Atualizar o catálogo ou deixar de oferecer um trabalho. | `CatalogoServicoService`: altera os dados ou a situação de `Servico`. | `PATCH /servicos/{id}` | Autenticado. |

### Épico 4 — Peças e Estoque

API de Peças e Estoque. Persistência proposta: `PecaRepository`, `EstoqueRepository` e `MovimentacaoEstoqueRepository`.

| História | Objetivo em linguagem simples | Classes e responsabilidades propostas | Método HTTP e rota | Acesso |
| --- | --- | --- | --- | --- |
| 4.1 Cadastrar peça | Registrar uma peça ou insumo utilizado pela oficina. | `PecaController` e `PecaService`: coordenam o cadastro; `Peca`: representa o material. | `POST /pecas` | Autenticado. |
| 4.2 Listar/detalhar peças | Consultar peças e a quantidade disponível. | `PecaController` e `PecaService`: consultam `Peca` e o saldo de `Estoque`. | `GET /pecas` e `GET /pecas/{id}` | Autenticado. |
| 4.3 Editar peça | Corrigir os dados cadastrais de uma peça. | `PecaService`: altera `Peca`; alterações de quantidade são registradas como movimentações na história 4.4. | `PATCH /pecas/{id}` | Autenticado. |
| 4.4 Movimentar estoque manual | Registrar uma entrada ou saída manual de peças. | `EstoqueController`: recebe a operação; `EstoqueService`: atualiza `Estoque` e registra `MovimentacaoEstoque`. | `POST /pecas/{id}/movimentacoes-estoque` | Autenticado. |
| 4.5 Baixa automática (evento) | Descontar as peças da OS quando o cliente aprovar o orçamento. | `OrcamentoAprovadoHandler`: recebe o evento `OrcamentoAprovado`; `EstoqueService`: registra a saída das quantidades de `ItemPeca`, vinculada à OS. | Não se aplica: processamento interno por evento. | Interno; não é uma chamada pública. |

A reação ao evento não exige, por si só, uma fila ou um sistema separado. Para a primeira implementação acadêmica, propõe-se tratar `OrcamentoAprovado` internamente, na mesma transação de banco da aprovação: decisão, baixa, status e histórico são confirmados juntos. Se faltar saldo ou ocorrer falha, nenhuma dessas alterações é confirmada e a OS continua aguardando aprovação. Assim, a execução não é liberada com a baixa pendente.

O estoque deve ser atualizado com controle de concorrência para que duas aprovações simultâneas não consumam o mesmo saldo. Uma aprovação já processada não pode gerar nova saída; registrar a referência à OS e ao orçamento em cada movimentação automática. Se futuramente forem usados eventos assíncronos, será necessário projetar estados intermediários, repetição e recuperação de falhas; isso não faz parte desta proposta inicial.

### Épico 5 — Criação de OS e Orçamento

API de Ordens de Serviço. Persistência proposta: `OrdemServicoRepository`, incluindo os itens e o orçamento da OS.

| História | Objetivo em linguagem simples | Classes e responsabilidades propostas | Método HTTP e rota | Acesso |
| --- | --- | --- | --- | --- |
| 5.1 Criar OS (Recebida) | Abrir o atendimento para um cliente e seu veículo. | `OrdemServicoController` e `OrdemServicoService`: coordenam a criação; `OrdemServico`: inicia como `RECEBIDA` e origina `OrdemServicoCriada`. | `POST /ordens-servico` | Autenticado. |
| 5.2 Adicionar serviços | Incluir na OS os trabalhos identificados no diagnóstico. | `OrdemServicoService`: inclui `ItemServico` em `OrdemServico`, usando o catálogo como referência. | `POST /ordens-servico/{id}/itens-servico` | Autenticado. |
| 5.3 Adicionar peças | Incluir as peças e quantidades necessárias. | `OrdemServicoService`: inclui `ItemPeca` em `OrdemServico`, usando o cadastro de peças como referência. | `POST /ordens-servico/{id}/itens-peca` | Autenticado. |
| 5.4 Calcular orçamento | Somar os valores dos serviços e das peças da OS. | `OrcamentoController` e `OrcamentoService`: coordenam o cálculo; `Orcamento`: representa a composição e o total. | `POST /ordens-servico/{id}/orcamentos` | Autenticado. |
| 5.5 Enviar p/ aprovação | Disponibilizar o orçamento para a decisão do cliente. | `OrcamentoService`: disponibiliza o orçamento; `OrdemServico`: passa para `AGUARDANDO_APROVACAO`. | `POST /ordens-servico/{id}/orcamentos/{orcamentoId}/envios` | Autenticado. |

**Correção de contrato proposta:** gerar um orçamento retorna seu identificador `orcamentoId`. Envio, consulta e decisão identificam explicitamente esse orçamento e verificam se pertence à OS da rota. Isso evita que o cliente aprove uma composição diferente daquela que visualizou. A seção 7 define a proposta acadêmica de revisão após recusa, preservando os orçamentos anteriores.

O orçamento registra os itens, quantidades e preços usados no cálculo. Alterar o preço do catálogo não altera um orçamento já gerado. Antes do envio, uma alteração nos itens invalida o orçamento anterior e exige gerar outro; somente o orçamento atual e válido pode ser enviado. Depois do envio, os itens ficam bloqueados; edição só é liberada após recusa e abertura explícita de revisão conforme C1. “Enviar” significa disponibilizar para aprovação; não pressupõe integração com e-mail ou WhatsApp.

### Épico 6 — Acompanhamento (Status)

Regras de acompanhamento da API de Ordens de Serviço. Persistência proposta: `OrdemServicoRepository` e `HistoricoStatusOrdemServicoRepository`.

| História | Objetivo em linguagem simples | Classes e responsabilidades propostas | Método HTTP e rota | Acesso |
| --- | --- | --- | --- | --- |
| 6.1 Transições de status | Garantir que a OS avance entre etapas permitidas. | `OrdemServico` e `StatusOrdemServico`: validam as mudanças; `StatusOrdemServicoAlterado`: informa a alteração para registro do histórico. | Não há rota exclusiva: regra usada pelas operações de criação, diagnóstico, envio, aprovação, finalização e entrega. | Conforme a operação de origem. |
| 6.2 Iniciar diagnóstico | Registrar o início da avaliação do veículo. | `OrdemServicoController` e `OrdemServicoService`: solicitam a mudança; `OrdemServico`: passa para `EM_DIAGNOSTICO`. | `POST /ordens-servico/{id}/diagnosticos` | Autenticado. |
| 6.3 Aprovar orçamento | Aplicar a decisão do cliente e liberar a execução. | `OrcamentoService`: aplica a aprovação e origina `OrcamentoAprovado`; `OrdemServico`: passa para `EM_EXECUCAO`. | Mesma operação pública da história 7.2; não criar uma segunda rota de aprovação. | Público, com token individual vinculado à OS. |
| 6.4 Finalizar execução | Informar que os trabalhos da OS terminaram. | `OrdemServicoService`: coordena a conclusão; `OrdemServico`: passa para `FINALIZADA` e origina `OrdemServicoFinalizada`. | `POST /ordens-servico/{id}/finalizacoes` | Autenticado. |
| 6.5 Confirmar entrega + histórico | Registrar a entrega do veículo e permitir consultar as etapas percorridas. | `OrdemServicoService`: confirma a entrega; `OrdemServico`: passa para `ENTREGUE`; `HistoricoStatusOrdemServico`: registra as mudanças desde o início do atendimento. | `POST /ordens-servico/{id}/entregas` e `GET /ordens-servico/{id}/historico-status` | Autenticado. |

Sequência principal proposta para os identificadores de status:

`RECEBIDA` → `EM_DIAGNOSTICO` → `AGUARDANDO_APROVACAO` → `EM_EXECUCAO` → `FINALIZADA` → `ENTREGUE`.

O fluxo técnico liga a aprovação à execução; por isso, este guia não acrescenta uma rota para iniciar a execução separadamente. Como complemento acadêmico, a recusa leva a OS a `ORCAMENTO_RECUSADO`; ela pode voltar ao diagnóstico por uma revisão explícita ou ser cancelada. `CANCELADA` é uma etapa terminal. As condições estão definidas na seção 7.

### Épico 7 — Consulta Pública

API Pública de Acompanhamento. `ConsultaPublicaService` consulta a OS após `AcessoPublicoService` validar o token e seu vínculo com o atendimento. A decisão sobre o orçamento utiliza `OrcamentoService`, compartilhado com o fluxo interno.

| História | Objetivo em linguagem simples | Classes e responsabilidades propostas | Método HTTP e rota | Acesso |
| --- | --- | --- | --- | --- |
| 7.1 Consultar status (público) | Permitir que o cliente acompanhe a etapa do atendimento. | `ConsultaPublicaController` e `ConsultaPublicaService`: recebem a chamada autorizada pelo token e devolvem o status da OS correspondente. | `GET /publico/ordens-servico/{numeroOs}/status` | Público, com token individual vinculado à OS. |
| 7.2 Aprovar/recusar orçamento | Permitir que o cliente registre sua decisão sobre o orçamento. | `OrcamentoPublicoController`: recebe a decisão; `AcessoPublicoService`: valida o token e seu vínculo; `ConsultaPublicaService`: localiza a OS autorizada; `OrcamentoService`: registra aprovação ou recusa. A aprovação executa a regra da história 6.3. | `POST /publico/ordens-servico/{numeroOs}/orcamentos/{orcamentoId}/decisoes` | Público, com token individual vinculado à OS. |

**Requisito dos diagramas:** essas operações ficam fora da exigência de JWT administrativo e usam CPF/CNPJ e número da OS como identificação. O número público da OS não precisa ser o mesmo identificador interno `{id}`. **Evolução proposta para o projeto acadêmico:** o documento é conferido pelo atendente na entrega do acesso; as chamadas públicas exigem um token individual, pois conhecer um CPF/CNPJ não autoriza decidir pelo cliente.

**Proposta de interface:** enviar `Authorization: Bearer <tokenDeAcessoDaOs>` em todas as operações públicas de acompanhamento e orçamento, e o número da OS na rota. Esse token é opaco, não é o JWT administrativo e só concede acesso à OS vinculada. Não enviar CPF/CNPJ no cabeçalho `Documento-Cliente`. Na história 7.2, enviar `decisao`, com `APROVADO` ou `RECUSADO`, e `motivo` obrigatório na recusa. Validar que `orcamentoId` pertence à OS autorizada. Emissão, entrega, validade e revogação estão definidas na seção 7.

**Complemento necessário ao fluxo:** a resposta de status informa o identificador do orçamento disponibilizado, quando existir. O cliente consulta seus itens e valores em `GET /publico/ordens-servico/{numeroOs}/orcamentos/{orcamentoId}` antes de decidir. Essa rota usa `OrcamentoPublicoController`, `ConsultaPublicaService` e `OrcamentoService`, com o mesmo token de acesso e sem expor dados administrativos. Orçamentos ainda não enviados não são disponibilizados ao cliente.

Apenas o orçamento atual, enviado e ainda sem decisão pode receber uma decisão inicial. Repetir a mesma decisão já registrada devolve o resultado existente, sem novos efeitos; tentar trocar uma decisão ou decidir sobre um orçamento substituído é um conflito. A recusa encerra a possibilidade de decidir novamente sobre esse orçamento, sem liberar a execução.

A recusa não publica `OrcamentoAprovado` e não provoca baixa de estoque. A consulta pública retorna informações de acompanhamento, sem reutilizar a resposta administrativa completa da história 8.2.

### Épico 8 — Administração e Relatórios

Consultas administrativas da API de Ordens de Serviço e API de Relatórios. As consultas usam `OrdemServicoRepository` e `HistoricoStatusOrdemServicoRepository`.

| História | Objetivo em linguagem simples | Classes e responsabilidades propostas | Método HTTP e rota | Acesso |
| --- | --- | --- | --- | --- |
| 8.1 Listar OS (filtros) | Encontrar atendimentos usando critérios de busca. | `OrdemServicoController` e `OrdemServicoConsultaService`: listam ordens; filtros sugeridos: status, cliente, veículo e período de criação. | `GET /ordens-servico` | Autenticado. |
| 8.2 Detalhar OS completa | Consultar os dados completos de um atendimento. | `OrdemServicoConsultaService`: reúne cliente, veículo, itens, orçamento, status e histórico da OS. | `GET /ordens-servico/{id}` | Autenticado. |
| 8.3 Relatório tempo médio | Mostrar o tempo médio de execução dos atendimentos. | `RelatorioController` e `RelatorioService`: calculam a média com os registros de `HistoricoStatusOrdemServico`. | `GET /relatorios/tempo-medio-execucao` | Autenticado. |

Proposta de cálculo para a história 8.3: média do tempo entre a entrada em `EM_EXECUCAO` e a entrada em `FINALIZADA`, considerando somente OS com os dois registros. Sem atendimentos elegíveis, informar que não há dados para calcular a média. Essa definição diferencia o tempo de execução do tempo total até a entrega.

### Épico 9 — Segurança

API de Autenticação e componentes que controlam acesso. Persistência proposta: `UsuarioRepository`.

| História | Objetivo em linguagem simples | Classes e responsabilidades propostas | Método HTTP e rota | Acesso |
| --- | --- | --- | --- | --- |
| 9.1 Autenticação JWT | Permitir que o usuário entre no sistema e receba um token. | `AutenticacaoController` e `AutenticacaoService`: verificam credenciais; `Usuario`: representa a conta; `TokenService`: emite e valida JWT. | `POST /autenticacoes` | Público para envio de credenciais; token emitido somente após validação. |
| 9.2 Proteção de rotas admin | Exigir autenticação nas operações internas. | `FiltroAutenticacaoJwt`: componente que valida o token nas rotas administrativas, preservando as exceções públicas. | Não se aplica: proteção das rotas marcadas como autenticadas. | Interno. |
| 9.3 Perfis/roles | Restringir operações conforme a função do usuário. | `PerfilAcesso` e `AutorizacaoService`: verificam as permissões associadas a `Usuario`. | Não se aplica: verificação nas operações protegidas. | Interno. |

Os atores Atendente, Mecânico e Gestor/Admin vêm do diagrama DDD. A seção 7 define a matriz proposta para os perfis `ATENDENTE`, `MECANICO` e `GESTOR`, além do provisionamento inicial de contas para a demonstração. Não são necessárias rotas de gestão de usuários nesta versão.

### Épico 10 — Qualidade

Este épico reúne verificações do backend a ser implementado; não cria uma API de testes.

| História | Objetivo em linguagem simples | Componentes e entregáveis propostos | Método HTTP e rota | Acesso |
| --- | --- | --- | --- | --- |
| 10.1 Testes unit. OS | Verificar as regras do atendimento de forma isolada. | `OrdemServicoTest` e `OrcamentoTest`: criação, transições permitidas e inválidas, composição do valor, aprovação e recusa. | Não se aplica. | Não se aplica. |
| 10.2 Testes unit. Estoque | Verificar entradas e saídas de peças. | `EstoqueTest` e `EstoqueServiceTest`: movimentação manual, baixa por aprovação, saldo insuficiente e prevenção de baixa duplicada. | Não se aplica. | Não se aplica. |
| 10.3 Testes de integração | Conferir se API, regras e banco funcionam em conjunto. | `OrdemServicoIntegracaoTest`, `EstoqueIntegracaoTest` e `AutenticacaoIntegracaoTest`: fluxo principal, persistência, reação ao evento e controle de acesso. | Não se aplica: os testes exercitam as rotas existentes. | Conforme a rota exercitada. |
| 10.4 Cobertura mínima (JaCoCo) | Medir quanto do código foi exercitado pelos testes. | Relatório de cobertura e verificação do limite exigido pela disciplina. JaCoCo é a ferramenta citada no diagrama; sua adoção depende de uma implementação compatível. | Não se aplica. | Não se aplica. |

Os diagramas não informam o percentual mínimo de cobertura. Não se presume um valor nem a escolha de linguagem a partir da menção ao JaCoCo. Os casos de saldo insuficiente e duplicação são recomendações técnicas adicionais para o domínio de estoque.

## 4. Nomes padronizados e ajustes em relação aos diagramas

| Como aparece no material | Proposta deste guia | Motivo |
| --- | --- | --- |
| Veículo, Serviço e Orçamento | `Veiculo`, `Servico` e `Orcamento` | Preservar o português com identificadores sem acentos. |
| CPF/CNPJ | `DocumentoCliente` | Dar um nome único ao conceito que aceita um dos dois documentos. |
| Peça/Insumo | `Peca` | Usar um único cadastro para os materiais consumidos no atendimento. |
| Orçamento como objeto de valor no DDD | Composição de valores em `Orcamento`, com `orcamentoId` para identificar o registro submetido à decisão. | A identidade do registro é um complemento da revisão para vincular envio e decisão à mesma composição; deve ser conciliada com o modelo DDD na implementação. Não implica um repositório ou contexto independente. |
| StatusOS | `StatusOrdemServico` | Explicitar o significado da abreviação. |
| StatusOSAlterado | `StatusOrdemServicoAlterado` | Usar o mesmo vocabulário do status e da ordem de serviço. |
| OSFinalizada | `OrdemServicoFinalizada` | Manter o nome do evento alinhado com `OrdemServicoCriada`. |
| OrdemServicoCriada, OrcamentoAprovado e EstoqueBaixo | `OrdemServicoCriada`, `OrcamentoAprovado` e `EstoqueBaixo` | Preservar nomes já claros. `EstoqueBaixo` consta do DDD, mas não há história específica nem limite de saldo definido para ele. |
| Cadastro inicial Cliente + Veículo com indicação de `POST /clientes` | `POST /clientes` seguido de `POST /veiculos` | Distinguir os cadastros das histórias 1.1 e 2.1. |
| Diagnóstico + serviços/peças em `PATCH /ordens-servico/{id}` | Rotas de `diagnosticos`, `itens-servico` e `itens-peca` | Deixar explícita a intenção de cada história. |
| Aprovação em `PATCH /aprovacao` | `POST /publico/ordens-servico/{numeroOs}/orcamentos/{orcamentoId}/decisoes` | Identificar a OS e contemplar aprovação e recusa na mesma operação. |
| Consulta pública em `GET /ordens-servico/{id}/status` | `GET /publico/ordens-servico/{numeroOs}/status` | Distinguir o acesso público e usar o número da OS previsto para identificação. |

Esses ajustes são sugestões de contrato, não alterações de uma API existente. Os diagramas originais permanecem como fontes, e as diferenças ficam registradas nesta tabela.

## 5. Exemplo completo: troca de óleo

Considere uma cliente chamada Ana, seu veículo, um serviço de troca de óleo de R$ 100,00 e quatro unidades de óleo de R$ 30,00 cada. Os valores são apenas ilustrativos. O catálogo e o estoque precisam estar preparados antes de compor o orçamento.

| Passo | O que acontece na oficina | Operação ou evento | Histórias |
| --- | --- | --- | --- |
| 1 | O atendente entra no sistema e recebe o JWT. | `POST /autenticacoes` | 9.1–9.3 |
| 2 | O atendente cadastra Ana e depois vincula seu veículo ao cadastro. | `POST /clientes` e `POST /veiculos` | 1.1, 2.1 |
| 3 | Um usuário autorizado cadastra o serviço, a peça “óleo” e uma entrada de dez unidades no estoque. | `POST /servicos`, `POST /pecas` e `POST /pecas/{id}/movimentacoes-estoque` | 3.1, 4.1, 4.4 |
| 4 | O atendente abre a OS do veículo. A situação inicial é `RECEBIDA`. | `POST /ordens-servico`; evento `OrdemServicoCriada` | 5.1, 6.1 |
| 4a | O atendente confere a identidade de Ana, emite o acesso e entrega o token de forma privada. | `POST /ordens-servico/{id}/acessos-publicos` | Complemento C5 |
| 5 | O mecânico inicia a avaliação. A OS passa para `EM_DIAGNOSTICO`. | `POST /ordens-servico/{id}/diagnosticos` | 6.2 |
| 6 | São incluídos o serviço de troca e quatro unidades de óleo. | `POST /ordens-servico/{id}/itens-servico` e `POST /ordens-servico/{id}/itens-peca` | 5.2, 5.3 |
| 7 | O sistema calcula R$ 100,00 + 4 × R$ 30,00 = R$ 220,00 e retorna `orcamentoId`. | `POST /ordens-servico/{id}/orcamentos` | 5.4 |
| 8 | O orçamento é disponibilizado para Ana. A OS passa para `AGUARDANDO_APROVACAO`. | `POST /ordens-servico/{id}/orcamentos/{orcamentoId}/envios` | 5.5 |
| 9 | Ana consulta a situação e visualiza os itens e o total do orçamento usando o número da OS e o token recebido da oficina, sem JWT administrativo. | `GET /publico/ordens-servico/{numeroOs}/status` e `GET /publico/ordens-servico/{numeroOs}/orcamentos/{orcamentoId}` | 7.1 e complemento da revisão |
| 10 | Ana registra a decisão `APROVADO`. A regra compartilhada aprova o orçamento e a OS passa para `EM_EXECUCAO`. | `POST /publico/ordens-servico/{numeroOs}/orcamentos/{orcamentoId}/decisoes`; evento `OrcamentoAprovado` | 7.2, 6.3 |
| 11 | O sistema reage à aprovação e registra a saída de quatro unidades. O saldo passa de dez para seis. | `OrcamentoAprovadoHandler` aciona `EstoqueService`; sem nova chamada HTTP do cliente. | 4.5 |
| 12 | O mecânico conclui o trabalho. A OS passa para `FINALIZADA`. | `POST /ordens-servico/{id}/finalizacoes`; evento `OrdemServicoFinalizada` | 6.4 |
| 13 | O atendente confirma a entrega do veículo. A OS passa para `ENTREGUE`. | `POST /ordens-servico/{id}/entregas` | 6.5 |
| 14 | Um usuário autorizado consulta a OS completa, seu histórico e o relatório de tempo médio. | `GET /ordens-servico/{id}`, `GET /ordens-servico/{id}/historico-status` e `GET /relatorios/tempo-medio-execucao` | 6.5, 8.2, 8.3 |

Os passos 10 e 11 estão separados para explicar o negócio, mas são confirmados na mesma transação na proposta inicial. Cada mudança de etapa gera `StatusOrdemServicoAlterado` e é registrada no histórico; a criação registra a entrada inicial em `RECEBIDA`. A consulta pública pode ocorrer ao longo do atendimento; ela não é uma etapa obrigatória para a execução avançar.

Se Ana registrar `RECUSADO` no passo 10, informando o motivo, não ocorre baixa automática e a OS passa para `ORCAMENTO_RECUSADO`. O atendente registra uma revisão, a OS retorna a `EM_DIAGNOSTICO`, os itens são ajustados e um novo orçamento é calculado e enviado. Ana decide sobre o novo `orcamentoId`; a recusa anterior permanece no histórico. Se não houver acordo, o atendente ou gestor cancela a OS conforme a seção 7.

## 6. Revisão de CRUD e completude

CRUD significa criar, consultar, atualizar e excluir. Nem todo conceito do negócio precisa das quatro operações: uma movimentação de estoque ou um histórico não deve ser apagado como um cadastro comum. Inativar é uma atualização que retira um cadastro de uso, preservando seus dados; não é exclusão física.

### Cobertura dos cadastros e registros

| Recurso | Criar | Consultar | Atualizar | Excluir ou inativar | Resultado da revisão |
| --- | --- | --- | --- | --- | --- |
| Cliente | 1.1 | 1.2 e 1.3 | 1.4 | Inativar em 1.4. | Cobre o ciclo cadastral previsto. |
| Veículo | 2.1 | 2.2 e 2.3 | 2.4 | Inativar em 2.4. | Cobre o ciclo cadastral previsto. |
| Serviço | 3.1 | 3.2 | 3.3 | Inativar em 3.3. | Cobre o ciclo cadastral previsto. |
| Peça | 4.1 | 4.2 | 4.3 | Inativar e reativar pelo complemento C3. | Ciclo cadastral completado na seção 7, preservando estoque e histórico. |
| Itens da OS | 5.2 e 5.3 | Na OS completa, 8.2. | Complemento abaixo. | Complemento abaixo, antes do envio. | Faltava permitir corrigir itens incluídos por engano. |
| Orçamento | 5.4 | Na OS completa e na consulta pública complementar. | Envio e decisão por operações específicas. | Não previsto. | Não é um cadastro de edição livre; valores enviados precisam ser preservados. |
| Ordem de Serviço | 5.1 | 8.1, 8.2 e consulta pública 7.1. | Operações dos épicos 5 e 6 e revisão C1. | Cancelamento C2; sem exclusão física. | Cancelar registra uma etapa terminal e preserva os dados. |
| Movimentação de estoque | 4.4 e 4.5 | Complemento abaixo. | Não editar registros anteriores. | Não apagar registros anteriores. | Corrigir por movimentação compensatória com motivo e referência à original. |
| Histórico de status | Gerado pelo sistema. | 6.5 e 8.2. | Não permitido diretamente. | Não permitido diretamente. | Registro de auditoria, sem CRUD público. |
| Usuário e perfil | Provisionamento local C4. | Sem API cadastral. | Sem API cadastral. | Sem API cadastral. | Contas acadêmicas e permissões definidas na seção 7. |

Para clientes, veículos e serviços, propõe-se inativar com `PATCH` e corpo `{"ativo": false}`. Cadastros inativos continuam disponíveis nas consultas históricas e não podem ser usados em novos atendimentos ou novas inclusões. Inativar não cancela uma OS existente. Reativação não foi especificada nas histórias.

### Complementos propostos, sem renumerar as histórias originais

As operações abaixo corrigem lacunas funcionais identificadas na revisão. Devem entrar no refinamento do backlog; não são apresentadas como histórias já existentes no diagrama.

| Necessidade | Método HTTP e rota propostos | Classes responsáveis | Acesso e restrição |
| --- | --- | --- | --- |
| Corrigir um item de serviço | `PATCH /ordens-servico/{id}/itens-servico/{itemId}` | `OrdemServicoController` e `OrdemServicoService`. | Autenticado; apenas em `EM_DIAGNOSTICO`. |
| Remover um item de serviço incluído por engano | `DELETE /ordens-servico/{id}/itens-servico/{itemId}` | `OrdemServicoController` e `OrdemServicoService`. | Autenticado; apenas em `EM_DIAGNOSTICO`. |
| Corrigir peça ou quantidade de um item | `PATCH /ordens-servico/{id}/itens-peca/{itemId}` | `OrdemServicoController` e `OrdemServicoService`. | Autenticado; apenas em `EM_DIAGNOSTICO`. |
| Remover um item de peça incluído por engano | `DELETE /ordens-servico/{id}/itens-peca/{itemId}` | `OrdemServicoController` e `OrdemServicoService`. | Autenticado; apenas em `EM_DIAGNOSTICO`, antes de qualquer baixa automática. |
| Visualizar o orçamento antes da decisão | `GET /publico/ordens-servico/{numeroOs}/orcamentos/{orcamentoId}` | `OrcamentoPublicoController`, `ConsultaPublicaService` e `OrcamentoService`. | Público com token da OS; somente versões já enviadas do atendimento correspondente. |
| Conferir entradas e saídas de uma peça | `GET /pecas/{id}/movimentacoes-estoque` | `EstoqueController`, `EstoqueService` e `MovimentacaoEstoqueRepository`. | Autenticado. |

`itemId` deve pertencer à OS indicada. Quantidades e referências podem ser corrigidas antes do envio; preços são obtidos do catálogo pelo backend, sem aceitar totais arbitrários do cliente da API. Incluir, alterar ou remover itens invalida o orçamento calculado anteriormente. A correção cadastral da peça em 4.3 não altera o saldo nem substitui a edição do item da OS.

### Regras mínimas propostas para evitar inconsistências

- Validar dados obrigatórios, documento e placa; impedir duplicidade de documento de cliente e placa de veículo conforme sua representação normalizada. A OS deve referenciar um veículo pertencente ao cliente informado.
- Aceitar quantidades positivas e valores monetários não negativos. Calcular valores com precisão decimal; total de peças é quantidade vezes preço unitário, somado aos serviços. Não aceitar o total enviado pelo consumidor como fonte do cálculo.
- Permitir inclusão e correção de itens somente em `EM_DIAGNOSTICO`; enviar somente orçamento atual e válido com pelo menos um item. Não permitir editar diretamente status, total, saldo ou histórico por um `PATCH` genérico.
- Validar a origem de cada transição: diagnóstico a partir de `RECEBIDA`, envio a partir de `EM_DIAGNOSTICO`, aprovação a partir de `AGUARDANDO_APROVACAO`, finalização a partir de `EM_EXECUCAO` e entrega a partir de `FINALIZADA`.
- Manter consultas de listas com paginação e filtros documentados no OpenAPI. Para cadastros inativáveis, oferecer filtro `ativo`; para veículos, `clienteId`; para OS, os filtros do épico 8. Os limites e formatos serão definidos no contrato detalhado.
- Documentar respostas de sucesso e falha: `201` para criação de recurso, `200` para consulta ou alteração com resposta e `204` para remoção sem corpo; `400` para entrada inválida, `401` para autenticação ausente ou inválida em rota protegida, `403` para falta de permissão, `404` para recurso não encontrado e `409` para conflito com o estado atual, duplicidade ou saldo insuficiente. Nas consultas públicas, responder de forma uniforme quando a combinação token/OS não corresponder, sem revelar cadastros de terceiros.


### Cenários de validação para a implementação futura

- Criar, listar, detalhar, editar e inativar cliente, veículo e serviço; verificar duplicidades, vínculos e preservação do histórico.
- Criar, consultar e editar peças; registrar entradas e saídas e conferir seu histórico; impedir saldo negativo em operações concorrentes.
- Incluir, corrigir e remover itens antes do envio; rejeitar item de outra OS; invalidar orçamento anterior e preservar preços já registrados.
- Consultar orçamento público com identificação correspondente; impedir acesso a rascunho ou a orçamento de outra OS; rejeitar decisão sobre orçamento substituído.
- Aprovar e baixar estoque uma única vez; repetir a mesma decisão sem novos efeitos; simular falha na baixa e verificar que aprovação, status e histórico não foram confirmados parcialmente.
- Recusar sem baixar estoque ou liberar execução; impedir alteração posterior dessa decisão; rejeitar transições que pulam etapas.
- Verificar token ausente, inválido ou expirado e perfil sem permissão; garantir que rotas públicas apliquem sua própria identificação.
- Exercitar os casos de revisão, cancelamento, peça inativa e credenciais públicas definidos nos critérios de aceite da seção 7.
- Conferir histórico desde `RECEBIDA` e média de execução, inclusive quando não houver OS elegível.

## 7. Complementos funcionais para a versão acadêmica

As propostas C1 a C5 completam o comportamento do sistema sem renumerar as 43 histórias originais. A aplicação pode continuar como um único backend com PostgreSQL, sem filas, notificações automáticas ou serviços externos obrigatórios.

### C1 — Recusa e revisão do orçamento

**Objetivo:** permitir corrigir a proposta da oficina sem apagar a decisão anterior do cliente. Complementa 5.2–5.5, 6.1 e 7.2.

- Recusar exige `motivo` não vazio, registra decisão e data, marca o orçamento `RECUSADO` e a OS `ORCAMENTO_RECUSADO`, na mesma transação. Não há baixa de estoque.
- Atendente ou gestor inicia a revisão com `POST /ordens-servico/{id}/revisoes-orcamento`, corpo `{"motivo": "Ajustar os serviços solicitados"}`. `OrdemServicoController` e `OrdemServicoService` registram responsável, motivo e data e retornam a OS para `EM_DIAGNOSTICO`. Somente `ORCAMENTO_RECUSADO` permite essa operação; nos demais estados, retornar `409`.
- Os itens atuais servem como ponto de partida. A equipe pode alterá-los ou removê-los; o conteúdo do orçamento recusado é imutável. Novo cálculo gera outro `orcamentoId`, com número de versão sequencial por OS; o novo envio segue o mesmo fluxo de aprovação.
- `StatusOrcamento` usa `CALCULADO`, `ENVIADO`, `APROVADO`, `RECUSADO`, `SUBSTITUIDO` e `CANCELADO`. Antes do envio, uma mudança nos itens ou um novo cálculo marca o orçamento calculado anterior como `SUBSTITUIDO`. Orçamento recusado permanece `RECUSADO` mesmo após revisão; sua decisão não é apagada.
- A consulta administrativa completa inclui todas as versões e decisões; a consulta pública permite ler versões já enviadas da OS autorizada. Apenas a versão atual em `ENVIADO`, com a OS em `AGUARDANDO_APROVACAO`, aceita uma decisão inicial. Rascunhos nunca são públicos.
- A OS cancelada ou entregue não pode ser reaberta. Não há edição de orçamento aprovado nem revisão durante execução nesta versão.

### C2 — Cancelamento de OS

**Objetivo:** encerrar atendimentos que não seguirão para execução, preservando a rastreabilidade. Complementa o épico 6.

| Operação | Contrato e responsabilidade |
| --- | --- |
| Cancelar | `POST /ordens-servico/{id}/cancelamentos`, com `motivo` obrigatório. `OrdemServicoController` e `OrdemServicoService` validam as condições e registram `CancelamentoOrdemServico`. |
| Quem pode | `ATENDENTE` ou `GESTOR`, com JWT administrativo. O cliente solicita à oficina; não há rota pública de cancelamento. |
| Quando pode | Somente em `RECEBIDA`, `EM_DIAGNOSTICO`, `AGUARDANDO_APROVACAO` ou `ORCAMENTO_RECUSADO`. |
| Resultado | OS em `CANCELADA`, com motivo, usuário e data no histórico; orçamento atual `CALCULADO` ou `ENVIADO` passa a `CANCELADO`. Orçamentos já recusados ou substituídos preservam sua situação anterior. |
| Repetição | Uma OS já cancelada devolve o cancelamento existente, sem novo registro ou alteração do motivo. |
| Bloqueio | Em `EM_EXECUCAO`, `FINALIZADA` ou `ENTREGUE`, retornar `409` sem alterar dados. |

**Regra acadêmica de estoque:** o cancelamento só existe antes da aprovação, portanto não há baixa automática a estornar. Não devolver peças automaticamente: após a aprovação elas podem já ter sido consumidas. Cancelamento após aprovação, cobrança parcial e devolução de materiais ficam explicitamente fora desta versão. Uma correção manual de estoque não reabre nem cancela uma OS.

Cancelamento, revisão e decisão devem serializar alterações sobre a mesma OS. Se aprovação e cancelamento concorrerem, apenas uma operação compatível com o estado persistido é confirmada; a outra recebe `409`. Registrar status, orçamento e histórico juntos. O acesso público continua permitindo consulta até a expiração, mas nenhuma decisão nova em OS cancelada.

### C3 — Inativação e reativação de peças

**Objetivo:** retirar peças de uso sem apagar o histórico. Complementa 4.3.

- Usar `PATCH /pecas/{id}` com `{"ativo": false}` para inativar e `{"ativo": true}` para reativar. `PecaController` e `PecaService` executam a operação; somente `GESTOR` pode alterar o cadastro. Novas peças começam ativas.
- Não exigir saldo zero para inativar. Preservar saldo, preço histórico e movimentações; nunca excluir a peça fisicamente. Listagem aceita filtro `ativo`, e o detalhe informa a situação cadastral.
- Bloquear inclusão de peça inativa em novos itens. Antes de calcular, enviar ou aprovar um orçamento, validar novamente que todas as peças referenciadas estão ativas. Se alguma tiver sido inativada, retornar `409` sem aprovação ou baixa.
- Não alterar silenciosamente um orçamento enviado. A oficina pode reativar a peça para prosseguir ou, se ela não estiver mais disponível, cancelar a OS ainda não aprovada. Revisão após recusa segue C1.
- Uma OS já aprovada pode ser concluída normalmente se a peça for inativada depois da baixa. A inativação não desfaz consumos anteriores.
- Permitir movimentação manual de ajuste de peça inativa somente ao gestor, com motivo; bloquear entradas de reposição comum até reativação. Para isso, a movimentação informa `tipo` (`ENTRADA` ou `SAIDA`), `quantidade` positiva, `finalidade` (`REPOSICAO` ou `AJUSTE`) e `motivo`; reposição só pode ser entrada. Toda saída respeita o saldo disponível.

### C4 — Permissões e contas da demonstração

**Objetivo:** definir quem pode fazer cada operação, aplicando a história 9.3. Cada usuário tem exatamente um perfil. “Consultar” abaixo refere-se às rotas administrativas; acesso público é separado.

| Operações | ATENDENTE | MECANICO | GESTOR |
| --- | --- | --- | --- |
| Consultar clientes e veículos | Sim | Sim | Sim |
| Cadastrar, editar e inativar clientes e veículos | Sim | Não | Sim |
| Consultar serviços, peças e saldo | Sim | Sim | Sim |
| Cadastrar, editar, inativar e reativar peças; manter catálogo de serviços | Não | Não | Sim |
| Movimentar estoque e consultar movimentações | Não | Não | Sim |
| Criar OS | Sim | Não | Sim |
| Listar OS, consultar detalhe e histórico | Sim | Sim | Sim |
| Iniciar diagnóstico, incluir/corrigir/remover itens | Não | Sim | Sim |
| Calcular e enviar orçamento | Sim | Não | Sim |
| Iniciar revisão após recusa | Sim | Não | Sim |
| Finalizar execução | Não | Sim | Sim |
| Confirmar entrega e cancelar antes da execução | Sim | Não | Sim |
| Emitir, substituir ou revogar acesso público da OS | Sim | Não | Sim |
| Consultar relatório de tempo médio | Não | Não | Sim |
| Aprovar ou recusar com JWT administrativo | Não | Não | Não |

O cliente decide exclusivamente pela credencial pública da OS. O gestor não ignora restrições de estado ou regras de estoque. `AutorizacaoService` verifica a permissão em toda chamada; operações sem permissão explícita são negadas. `FiltroAutenticacaoJwt` exige assinatura e validade corretas, usuário ativo e perfil atual consultado no backend. Sem autenticação válida, `401`; usuário autenticado sem permissão, `403`.

Para a apresentação, um comando local de provisionamento, descrito no README do futuro backend, cria uma conta por perfil. Logins e senhas vêm de variáveis de ambiente; não há senha padrão no código. O comando armazena senhas com Argon2id usando biblioteca de segurança e não sobrescreve contas existentes. Não expor esse comando por HTTP nem criar cadastro público. JWT administrativo dura 30 minutos; expirado, exige novo login. Interface Swagger fica disponível no ambiente local de demonstração e desabilitada fora dele por padrão.

### C5 — Proteção do acompanhamento e da aprovação pública

**Objetivo:** permitir acesso sem cadastro de cliente no sistema de usuários, usando uma credencial restrita ao atendimento. Complementa 7.1, 7.2 e 9.2.

| Operação | Rota e comportamento |
| --- | --- |
| Emitir ou substituir acesso | `POST /ordens-servico/{id}/acessos-publicos`, exclusivo de atendente ou gestor. Retorna `201` com `acessoId`, `token`, `numeroOs` e `expiraEm`. Uma nova emissão revoga o acesso anterior da OS na mesma transação. |
| Revogar acesso | `DELETE /ordens-servico/{id}/acessos-publicos/{acessoId}`, exclusivo de atendente ou gestor. Marca como revogado, preserva auditoria e retorna `204`; repetir a revogação mantém `204`. Validar que o acesso pertence à OS. |
| Usar acesso | As três rotas públicas de status, orçamento e decisão exigem `Authorization: Bearer <tokenDeAcessoDaOs>`. CPF/CNPJ ou número da OS, isoladamente, nunca dão acesso. |

`AcessoPublicoController` recebe emissão e revogação; `AcessoPublicoService` gera e valida credenciais; `AcessoPublicoRepository` persiste `AcessoPublicoOrdemServico`. `ConsultaPublicaService` só consulta a OS autorizada. O backend deve manter regras distintas para JWT administrativo e token público; não aceitar uma credencial em substituição à outra.

Proposta simples de ciclo do token:

1. Gerar 32 bytes aleatórios criptograficamente seguros e codificar em Base64URL. Vincular o token a uma única OS e ao cliente registrado na abertura; persistir somente seu hash SHA-256, validade, revogação e responsável pela emissão. O token em texto é retornado apenas na emissão.
2. O atendente confere presencialmente a identidade do responsável e entrega o código de forma privada. Na demonstração, usar clientes e documentos fictícios. Não é necessária integração com e-mail ou SMS; perda ou expiração exige nova conferência e emissão pela oficina.
3. Validade de sete dias a partir da emissão, sem renovação automática. Até expirar ou ser revogado, o token permite consultas repetidas e decisões válidas sobre os orçamentos da mesma OS, inclusive nova versão após recusa. Cada orçamento admite uma única decisão; a repetição idêntica é apenas confirmação do resultado anterior. O token de acompanhamento não é consumido na primeira consulta ou decisão.
4. Transportar a credencial somente no cabeçalho de autorização, sobre HTTPS fora de `localhost`; não colocar token ou documento em URL, logs ou respostas de consulta. O cliente HTTP deve manter o token apenas durante a sessão de uso; respostas públicas e de emissão usam `Cache-Control: no-store`.
5. Retornar `401` genérico para token ausente, inválido, expirado ou revogado; com token válido, retornar `404` uniforme para OS ou orçamento inexistente ou fora do vínculo autorizado. Validar autorização antes de revelar dados. Limitar a 30 chamadas públicas por minuto por IP e, após validação, a 30 por token; excedido o limite, responder `429` com `Retry-After`. Esses limites são escolhas para a demonstração, configuráveis no backend.
6. Registrar decisão, orçamento, data e `acessoId` em auditoria, sem armazenar o token em texto. Aprovação, baixa de estoque e histórico continuam atômicos. Consultas não alteram decisões; somente o `POST` de decisão pode fazê-lo.

A posse do token concede acesso à OS; por isso, sua entrega deve ser privada. Isso oferece autorização por credencial do atendimento, sem afirmar que equivale a assinatura digital ou prova de identidade civil. O prazo, os limites e a entrega presencial são decisões deste projeto. Os princípios de aleatoriedade, expiração e armazenamento seguro se apoiam nas [orientações da OWASP para tokens](https://cheatsheetseries.owasp.org/cheatsheets/Forgot_Password_Cheat_Sheet.html), adaptadas aqui a uma credencial de acompanhamento reutilizável. A verificação de permissões por operação segue as [orientações de autorização da OWASP](https://cheatsheetseries.owasp.org/cheatsheets/Authorization_Cheat_Sheet.html).

### Exemplos e critérios de aceite dos complementos

| Cenário | Resultado esperado |
| --- | --- |
| Cliente recusa e a oficina revisa | OS: `AGUARDANDO_APROVACAO` → `ORCAMENTO_RECUSADO` → `EM_DIAGNOSTICO` → `AGUARDANDO_APROVACAO`. Novo orçamento tem outro identificador; versão recusada e motivo permanecem consultáveis. |
| Cliente tenta aprovar a versão recusada | `409`, sem baixa ou mudança de status. A decisão deve usar a nova versão enviada. |
| Cancelamento antes da aprovação | OS `CANCELADA`, motivo e responsável registrados, sem movimentação automática de estoque. |
| Cancelamento após aprovação ou concorrente com aprovação já confirmada | `409`, sem estorno nem alteração do atendimento. |
| Peça inativada com saldo | Saldo e histórico permanecem; novas inclusões e aprovações que dependem dela são bloqueadas. Reativação permite retomar operações válidas. |
| Mecânico tenta alterar cadastro de peça ou emitir acesso | `403`, sem alteração de dados. |
| JWT de gestor tenta decidir pela rota pública | `401`; JWT administrativo não substitui token de acesso da OS. |
| Token da OS A tenta consultar ou decidir na OS B | `404` uniforme, sem revelar dados de B. |
| Token expira, é revogado ou substituído | Token antigo recebe `401`; somente nova credencial válida permite acesso. |
| Decisão repetida ou concorrente | Mesma decisão retorna resultado anterior; decisão oposta retorna `409`; aprovação produz uma única baixa. |
| Cliente conhece apenas CPF/CNPJ e número da OS | Acesso negado sem credencial válida. |

## 8. Limites e conferência do guia

- Foram mapeadas as **43 histórias dos 11 épicos**, de acordo com a numeração do fluxo técnico.
- Os nomes e contratos são propostas: não afirmam que classes, rotas, testes ou infraestrutura já existem.
- As histórias 6.3 e 7.2 representam a regra e a entrada pública da mesma aprovação; a história 4.5 é sua consequência interna.
- Os complementos da revisão incluem remoção de itens ainda em diagnóstico e consultas ausentes; não acrescentam exclusão definitiva dos cadastros, mensageria externa, envio de notificações ou gerenciamento de usuários.
- As propostas C1–C5 definem recusa/revisão, cancelamento anterior à aprovação, inativação de peças, permissões e acesso público protegido. Percentual de cobertura e tecnologia ainda dependem dos requisitos da disciplina. Nenhuma dessas propostas representa código já implementado.
