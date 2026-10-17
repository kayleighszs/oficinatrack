ALTER TABLE peca ADD COLUMN versao_registro BIGINT NOT NULL DEFAULT 0;
ALTER TABLE movimentacao_estoque ADD COLUMN orcamento_id BIGINT;
ALTER TABLE movimentacao_estoque
    ADD CONSTRAINT fk_movimentacao_orcamento FOREIGN KEY (orcamento_id) REFERENCES orcamentos(id);
CREATE UNIQUE INDEX uk_baixa_orcamento_peca
    ON movimentacao_estoque(orcamento_id, peca_id)
    WHERE origem = 'BAIXA_OS';

CREATE TABLE acessos_publicos_ordem_servico (
    id BIGSERIAL PRIMARY KEY,
    ordem_servico_id BIGINT NOT NULL REFERENCES ordens_servico(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    criado_em TIMESTAMP NOT NULL,
    expira_em TIMESTAMP NOT NULL,
    revogado_em TIMESTAMP
);

CREATE INDEX idx_acesso_publico_ordem ON acessos_publicos_ordem_servico(ordem_servico_id);
