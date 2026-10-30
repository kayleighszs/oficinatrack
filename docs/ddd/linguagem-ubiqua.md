# Linguagem Ubíqua — Sistema de Gestão de Oficina Mecânica

Este glossário segue o **Event Storming** (`Event Storming.pdf`) e o desenho de **Bounded Contexts**. Cada termo pertence a um único contexto; quando um contexto usa um termo de outro, ele o recebe traduzido pela ACL (ver "Relações entre contextos").

## Atores

| Ator | Papel |
|---|---|
| **Cliente** | Dono do veículo. Consulta o status da OS e decide sobre o orçamento pela API pública. |
| **Funcionário** | Atendente da oficina. Cadastra cliente e veículo, cria a OS, inclui serviços e peças, entrega o veículo e cancela a OS. |
| **Mecânico** | Realiza o diagnóstico, inicia e conclui o serviço. |
| **Estoquista** | Repõe o estoque de peças. |
| **Sistema** | Executa as ações automáticas: gerar e enviar orçamento, verificar disponibilidade de peças e finalizar a OS. |

## Bounded Context: Ordens de Serviço (Domínio Principal)

**Agregado raiz:** Ordem de Serviço.

| Termo | Definição |
|---|---|
| **Ordem de Serviço (OS)** | Agregado raiz do domínio. Representa o atendimento completo de um veículo, do recebimento à entrega. Contém itens de serviço, itens de peça, orçamentos e histórico de status. Toda alteração passa por ela. |
| **Serviço (Catálogo)** | Tipo de serviço oferecido pela oficina (ex: troca de óleo, alinhamento), com valor-base e tempo médio estimado. É reutilizável em várias OS. Alterar o valor no catálogo não afeta OS já criadas. |
| **Item de Serviço da OS** | Serviço do catálogo incluído em uma OS específica, com descrição e valor *copiados* no momento da inclusão. Não é referência viva ao catálogo. |
| **Item de Peça da OS** | Peça incluída em uma OS específica, também com descrição e valor copiados no momento da inclusão. |
| **Status da OS** | Estado atual do atendimento: `Recebida`, `Em Diagnóstico`, `Aguardando Aprovação`, `Orçamento Recusado`, `Em Execução`, `Finalizada`, `Entregue`, `Cancelada`. |
| **Transição de Status** | Mudança controlada de um status para outro. Só ocorre se a regra permitir: `Em Diagnóstico` exige serviço incluído; `Aguardando Aprovação` exige orçamento enviado; `Em Execução` exige orçamento aprovado; `Finalizada` exige serviço concluído; `Entregue` exige OS finalizada. |
| **Histórico de Status** | Registro cronológico de cada mudança de status da OS. Também serve para calcular o tempo médio de execução. |
| **Diagnóstico** | Avaliação técnica do mecânico sobre o veículo. Confirma quais serviços e peças são necessários e é pré-requisito para gerar o orçamento. |
| **Orçamento** | Valor total calculado automaticamente pelo sistema, somando os itens de serviço e de peça da OS. Nunca é informado manualmente. Uma OS pode ter várias versões, e só a mais recente é válida. |
| **Status do Orçamento** | `Calculado`, `Enviado`, `Aprovado`, `Recusado`, `Substituído` (quando um novo orçamento toma o lugar do anterior) ou `Cancelado` (quando a OS é cancelada antes da decisão do cliente). |
| **Aprovação do Orçamento** | Decisão do cliente (API pública) que autoriza a execução. Leva a OS a `Em Execução` e dispara a baixa de estoque das peças da OS. |
| **Recusa do Orçamento** | Decisão do cliente que rejeita o orçamento, com motivo obrigatório. Leva a OS a `Orçamento Recusado`, sem baixa de estoque. Depois da recusa, o funcionário cancela a OS. |
| **Cancelamento da OS** | Ação do funcionário que encerra a OS com status `Cancelada`, com motivo obrigatório. Só é permitido antes da aprovação (`Recebida`, `Em Diagnóstico`, `Aguardando Aprovação` ou `Orçamento Recusado`), pois depois dela o estoque já foi baixado. A OS cancelada não pode ser reaberta. |
| **Execução do Serviço** | Etapa em que o mecânico inicia e conclui o serviço aprovado. Concluído o serviço, o sistema finaliza a OS. |
| **Entrega do Veículo** | Ação do funcionário que devolve o veículo ao cliente, ou solicita que ele o busque. Só ocorre com a OS finalizada. |

**Eventos de domínio:** OS Criada, Serviço Incluído, Diagnóstico Concluído, Orçamento Gerado, Orçamento Enviado, Orçamento Aprovado, Orçamento Recusado, OS Cancelada, Execução Iniciada, Serviço Concluído, OS Finalizada, Veículo Entregue.

## Bounded Context: Cliente e Veículos (Subdomínio de Suporte)

**Agregados raiz:** Cliente e Veículo.

| Termo | Definição |
|---|---|
| **Cliente** | Pessoa física ou jurídica identificada por CPF/CNPJ, dona de um ou mais veículos atendidos pela oficina. |
| **Identificação do Cliente** | Busca do cliente pelo documento. Resulta em *Cliente Identificado* ou *Cliente Não Identificado*. Se não identificado, o funcionário o cadastra. |
| **Veículo** | Automóvel do cliente, identificado pela placa, com marca, modelo e ano. A placa é única entre os veículos ativos e deve seguir o padrão vigente (Mercosul ou antigo). |
| **Placa** | Identificador do veículo, validado nos formatos antigo (`AAA0000`) e Mercosul (`AAA0A00`). |

**Eventos de domínio:** Cliente Identificado, Cliente Não Identificado, Cliente Cadastrado, Veículo Cadastrado.

## Bounded Context: Peças e Estoque (Subdomínio de Suporte)

**Agregado raiz:** Peça/Insumo.

| Termo | Definição |
|---|---|
| **Peça/Insumo** | Item físico controlado em estoque, com quantidade atual e estoque mínimo configurável. |
| **Estoque Mínimo** | Quantidade limite configurada por peça, usada para sinalizar necessidade de reposição. |
| **Verificação de Disponibilidade** | Consulta do sistema ao incluir uma peça na OS. Resulta em *Peça Incluída na OS* ou *Peça Indisponível no Estoque*. |
| **Peça Indisponível** | Situação em que a quantidade em estoque não atende à OS. Notifica a falta de estoque, e o estoquista deve repor a peça. |
| **Reposição de Estoque** | Entrada de peças feita pelo estoquista. Após a reposição, o sistema verifica o estoque novamente. |
| **Movimentação de Estoque** | Registro de entrada ou saída de peças, com origem `MANUAL` (ação humana) ou `BAIXA_OS` (automática, vinda da aprovação do orçamento). Garante rastreabilidade completa. |
| **Baixa Automática de Estoque** | Reação do sistema à aprovação do orçamento: debita as peças da OS aprovada. |
| **Inconsistência de Estoque** | Tentativa de baixa que encontra estoque insuficiente entre a inclusão da peça na OS e a aprovação do orçamento. É tratada como exceção a resolver, nunca como falha silenciosa. |

**Eventos de domínio:** Peça Incluída no Serviço, Peça Incluída na OS, Peça Indisponível no Estoque, Estoque Reposto.

**Ponto de atenção:** não está definido quanto tempo leva para repor o estoque quando falta peça.

## Bounded Context: Autenticação e Segurança (Subdomínio Genérico)

| Termo | Definição |
|---|---|
| **Usuário** | Funcionário com acesso ao sistema administrativo, autenticado por login e senha. |
| **Perfil (Role)** | Nível de permissão do usuário: `GESTOR` (acesso total), `ATENDENTE` (atendimento e orçamento) ou `MECANICO` (diagnóstico e execução). |
| **Autenticação** | Processo de login que valida as credenciais e emite o token. |
| **Token JWT** | Credencial de sessão emitida no login, exigida em todas as rotas administrativas (exceto `/publico/**` e `/auth/login`). |

## Relações entre contextos

| Relação | Descrição |
|---|---|
| **Cliente e Veículos → Ordens de Serviço** | *Upstream → Downstream.* A OS recebe cliente e veículo por uma ACL. |
| **Peças e Estoque → Ordens de Serviço** | *Upstream → Downstream.* A OS consulta disponibilidade e dispara a baixa por uma ACL. |
| **ACL (camada anticorrupção)** | Traduz os modelos de Cliente/Veículos, do catálogo e de Peças/Estoque para o modelo da OS, que fica protegido de mudanças nesses contextos. No código, a OS depende só de portas escritas na sua linguagem (`ClienteVeiculoPort`, `CatalogoServicoPort`, `EstoquePort`), e os adaptadores em `ordemservico/infra/acl` fazem a tradução. |
| **Autenticação e Segurança → demais contextos** | O token JWT protege as rotas administrativas de todos os contextos. |

## Conceitos transversais

| Termo | Definição |
|---|---|
| **API Pública** | Endpoints (`/publico/**`) acessíveis sem JWT, usados pelo cliente final para consultar o status e decidir sobre o orçamento, validados por CPF/CNPJ. |
| **Soft Delete** | Inativação lógica (campo `ativo`) em vez de exclusão física. Preserva o histórico de OS ligadas a clientes, veículos, serviços e peças inativados. |
| **Cópia de Valor (Value Snapshot)** | Regra aplicada aos itens de serviço e peça: descrição e valor são copiados do catálogo na inclusão na OS, protegendo o histórico contra mudanças futuras de preço. |
