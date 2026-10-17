CREATE TABLE clientes (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    cpf_cnpj VARCHAR(14) NOT NULL UNIQUE,
    tipo_pessoa VARCHAR(10) NOT NULL,
    telefone VARCHAR(20),
    email VARCHAR(150),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL,
    atualizado_em TIMESTAMP NOT NULL
);

CREATE TABLE veiculos (
    id BIGSERIAL PRIMARY KEY,
    marca VARCHAR(255),
    modelo VARCHAR(255),
    ano INTEGER,
    placa VARCHAR(10) NOT NULL UNIQUE,
    cliente_id BIGINT NOT NULL REFERENCES clientes(id),
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE servico (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    valor NUMERIC(10,2) NOT NULL,
    tempo_medio_estimado_minutos INTEGER,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL,
    atualizado_em TIMESTAMP NOT NULL
);

CREATE TABLE peca (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255),
    valor_unitario NUMERIC(38,2),
    quantidade_estoque INTEGER,
    estoque_minimo INTEGER,
    ativo BOOLEAN,
    criado_em TIMESTAMP,
    atualizado_em TIMESTAMP
);

CREATE TABLE movimentacao_estoque (
    id BIGSERIAL PRIMARY KEY,
    peca_id BIGINT NOT NULL REFERENCES peca(id),
    tipo VARCHAR(10) NOT NULL,
    quantidade INTEGER NOT NULL,
    origem VARCHAR(20) NOT NULL,
    ordem_servico_id BIGINT,
    usuario_id BIGINT,
    criado_em TIMESTAMP NOT NULL
);

CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL
);

CREATE INDEX idx_veiculos_cliente ON veiculos(cliente_id);
CREATE INDEX idx_movimentacao_peca ON movimentacao_estoque(peca_id);
