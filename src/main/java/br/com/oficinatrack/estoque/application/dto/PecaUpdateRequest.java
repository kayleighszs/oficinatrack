package br.com.oficinatrack.estoque.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class PecaUpdateRequest {

    @NotBlank
    private String nome;
    @Positive
    private BigDecimal valorUnitario;
    @PositiveOrZero
    private Integer estoqueMinimo;

    public PecaUpdateRequest(String nome, BigDecimal valorUnitario, Integer estoqueMinimo) {
        this.nome = nome;
        this.valorUnitario = valorUnitario;
        this.estoqueMinimo = estoqueMinimo;
    }

    public PecaUpdateRequest() {}

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public Integer getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public void setEstoqueMinimo(Integer estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
    }
}
