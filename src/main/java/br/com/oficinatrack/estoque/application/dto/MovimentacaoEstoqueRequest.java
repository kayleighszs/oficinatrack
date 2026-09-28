package br.com.oficinatrack.estoque.application.dto;

import br.com.oficinatrack.estoque.domain.TipoMovimentacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class MovimentacaoEstoqueRequest {

    @NotNull
    private TipoMovimentacao tipo;
    @NotNull
    @Positive
    private Integer quantidade;

    public MovimentacaoEstoqueRequest() {}

    public MovimentacaoEstoqueRequest(TipoMovimentacao tipo, Integer quantidade) {
        this.tipo = tipo;
        this.quantidade = quantidade;
    }

    public TipoMovimentacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimentacao tipo) {
        this.tipo = tipo;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }
}
