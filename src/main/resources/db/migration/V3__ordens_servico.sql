CREATE TABLE ordens_servico (
    id BIGSERIAL PRIMARY KEY,
    numero_os VARCHAR(24) NOT NULL UNIQUE,
    cliente_id BIGINT NOT NULL REFERENCES clientes(id),
    veiculo_id BIGINT NOT NULL REFERENCES veiculos(id),
    status VARCHAR(30) NOT NULL,
    criado_em TIMESTAMP NOT NULL,
    atualizado_em TIMESTAMP NOT NULL,
    versao_registro BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE itens_servico_ordem (
    id BIGSERIAL PRIMARY KEY,
    ordem_servico_id BIGINT NOT NULL REFERENCES ordens_servico(id) ON DELETE CASCADE,
    servico_id BIGINT NOT NULL REFERENCES servico(id),
    descricao VARCHAR(150) NOT NULL,
    valor_unitario NUMERIC(12,2) NOT NULL,
    quantidade INTEGER NOT NULL,
    CONSTRAINT uk_item_servico_ordem UNIQUE (ordem_servico_id, servico_id)
);

CREATE TABLE itens_peca_ordem (
    id BIGSERIAL PRIMARY KEY,
    ordem_servico_id BIGINT NOT NULL REFERENCES ordens_servico(id) ON DELETE CASCADE,
    peca_id BIGINT NOT NULL REFERENCES peca(id),
    descricao VARCHAR(150) NOT NULL,
    valor_unitario NUMERIC(12,2) NOT NULL,
    quantidade INTEGER NOT NULL,
    CONSTRAINT uk_item_peca_ordem UNIQUE (ordem_servico_id, peca_id)
);

CREATE TABLE orcamentos (
    id BIGSERIAL PRIMARY KEY,
    ordem_servico_id BIGINT NOT NULL REFERENCES ordens_servico(id) ON DELETE CASCADE,
    versao INTEGER NOT NULL,
    total NUMERIC(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    criado_em TIMESTAMP NOT NULL,
    enviado_em TIMESTAMP,
    decidido_em TIMESTAMP,
    motivo_recusa VARCHAR(500),
    CONSTRAINT uk_orcamento_versao UNIQUE (ordem_servico_id, versao)
);

CREATE TABLE historico_status_ordem_servico (
    id BIGSERIAL PRIMARY KEY,
    ordem_servico_id BIGINT NOT NULL REFERENCES ordens_servico(id) ON DELETE CASCADE,
    status_anterior VARCHAR(30),
    status_novo VARCHAR(30) NOT NULL,
    alterado_em TIMESTAMP NOT NULL
);

CREATE INDEX idx_ordem_status ON ordens_servico(status);
CREATE INDEX idx_ordem_cliente ON ordens_servico(cliente_id);
CREATE INDEX idx_ordem_veiculo ON ordens_servico(veiculo_id);
CREATE INDEX idx_historico_ordem ON historico_status_ordem_servico(ordem_servico_id, alterado_em);
