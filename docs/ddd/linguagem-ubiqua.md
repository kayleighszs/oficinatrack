# Linguagem Ubíqua — Sistema de Gestão de Oficina Mecânica

## Bounded Context: Atendimento (Core Domain)

| Termo | Definição |
|---|---|
| **Cliente** | Pessoa física ou jurídica identificada por CPF/CNPJ, dona de um ou mais veículos atendidos pela oficina. |
| **Veículo** | Automóvel do cliente, identificado pela placa (única entre veículos ativos), com marca, modelo e ano. |
| **Ordem de Serviço (OS)** | Agregado raiz do domínio; representa o atendimento completo de um veículo, desde o recebimento até a entrega. Contém serviços, peças, orçamento e histórico de status. |
| **Item de Serviço da OS** | Serviço do catálogo incluído em uma OS específica, com descrição e valor *copiados* no momento da inclusão (não é referência viva ao catálogo). |
| **Item de Peça da OS** | Peça/insumo incluído em uma OS específica, também com descrição e valor copiados no momento da inclusão. |
| **Status da OS** | Estado atual do atendimento. Valores: `Recebida`, `Em Diagnóstico`, `Aguardando Aprovação`, `Em Execução`, `Finalizada`, `Entregue`, `Cancelada`. |
| **Transição de Status** | Mudança controlada de um status para outro, regida por regras de negócio (ex: só pode ir para "Em Execução" se o orçamento foi aprovado). Toda transição é registrada em histórico. |
| **Histórico de Status** | Registro cronológico de cada mudança de status de uma OS, usado também para calcular o tempo médio de execução. |
| **Diagnóstico** | Avaliação técnica feita pelo mecânico sobre o veículo, que confirma quais serviços/peças realmente são necessários. |
| **Orçamento** | Valor total calculado automaticamente a partir da soma dos itens de serviço e peça de uma OS. Nunca é informado manualmente pelo cliente ou atendente. |
| **Aprovação do Orçamento** | Decisão do cliente (via API pública) que autoriza a execução dos serviços. Dispara a transição de status para "Em Execução" e a baixa automática de estoque. |
| **Recusa do Orçamento** | Decisão do cliente que rejeita o orçamento proposto. Dispara o cancelamento direto da OS (status "Cancelada"), sem possibilidade de renegociação no MVP atual. |

## Bounded Context: Estoque (Subdomínio de Suporte)

| Termo | Definição |
|---|---|
| **Peça/Insumo** | Item físico controlado em estoque, com quantidade atual e estoque mínimo configurável. |
| **Movimentação de Estoque** | Registro de entrada ou saída de peças, com origem `MANUAL` (ação humana) ou `BAIXA_OS` (automática, disparada por aprovação de orçamento). Garante rastreabilidade completa do estoque. |
| **Estoque Mínimo** | Quantidade limite configurada por peça; usada para sinalizar necessidade de reposição. |
| **Inconsistência de Estoque** | Registro de uma tentativa de baixa que encontrou estoque insuficiente entre a inclusão do item na OS e a aprovação do orçamento — tratada como exceção a resolver, não como falha silenciosa. |
| **Baixa Automática de Estoque** | Reação do sistema (via evento `OrcamentoAprovadoEvent`) que debita a quantidade de peças da OS aprovada, sem acoplamento direto entre os módulos de OS e Estoque. |

## Bounded Context: Catálogo de Serviços (Subdomínio Genérico)

| Termo | Definição |
|---|---|
| **Serviço (Catálogo)** | Tipo de serviço oferecido pela oficina (ex: troca de óleo, alinhamento), com valor-base e tempo médio estimado. Reutilizável em múltiplas OS. Alterar o valor no catálogo não afeta OS já criadas. |

## Bounded Context: Autenticação e Segurança (Subdomínio Genérico)

| Termo | Definição |
|---|---|
| **Usuário** | Funcionário com acesso ao sistema administrativo, autenticado via login/senha. |
| **Perfil (Role)** | Nível de permissão do usuário: `ADMIN` (acesso total) ou `ATENDENTE` (operação do dia a dia). |
| **Token JWT** | Credencial de sessão emitida no login, exigida em todas as rotas administrativas (exceto `/publico/**` e `/auth/login`). |

## Conceitos transversais

| Termo | Definição |
|---|---|
| **API Pública** | Conjunto de endpoints (`/publico/**`) acessíveis sem autenticação JWT, usados pelo cliente final para consultar status e decidir sobre o orçamento, validados por CPF/CNPJ. |
| **Soft Delete** | Prática de inativação lógica (campo `ativo`) em vez de exclusão física, preservando histórico de OS vinculadas a clientes/veículos/serviços/peças inativados. |
| **Cópia de Valor (Value Snapshot)** | Regra de negócio aplicada a itens de serviço e peça: descrição e valor são copiados do catálogo no momento da inclusão na OS, protegendo o histórico contra alterações futuras de preço. |