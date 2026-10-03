package br.com.oficinatrack.estoque.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class PecaRequest {

    @NotBlank
    private String nome;
    @NotNull
    @Positive
    private BigDecimal valorUnitario;
    @NotNull
    @PositiveOrZero
    private Integer quantidadeEstoque;
    @PositiveOrZero
    private Integer estoqueMinimo;

    public PecaRequest(String nome, BigDecimal valorUnitario, Integer quantidadeEstoque, Integer estoqueMinimo) {
        this.nome = nome;
        this.valorUnitario = valorUnitario;
        this.quantidadeEstoque = quantidadeEstoque;
        this.estoqueMinimo = estoqueMinimo;
    }

    public PecaRequest() {}

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

    public Integer getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public void setQuantidadeEstoque(Integer quantidadeEstoque) {
        this.quantidadeEstoque = quantidadeEstoque;
    }

    public Integer getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public void setEstoqueMinimo(Integer estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
    }
}
