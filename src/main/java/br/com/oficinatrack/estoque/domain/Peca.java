package br.com.oficinatrack.estoque.domain;

import br.com.oficinatrack.estoque.exceptions.EstoqueInsuficienteExcepetion;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Peca {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    @Column(name = "valor_unitario")
    private BigDecimal valorUnitario;
    @Column(name = "quantidade_estoque")
    private Integer quantidadeEstoque;
    @Column(name = "estoque_minimo")
    private Integer estoqueMinimo;
    private Boolean ativo;
    @Column(name = "criado_em")
    private LocalDateTime criadoEm;
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    private Peca(Long id, String nome, BigDecimal valorUnitario, Integer quantidadeEstoque, Integer estoqueMinimo, Boolean ativo, LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        validarValorUnitario(valorUnitario);
        validarEstoqueMinimo(estoqueMinimo);
        validarQuantidadeEstoque(quantidadeEstoque);
        validarNome(nome);

        this.id = id;
        this.nome = nome;
        this.valorUnitario = valorUnitario;
        this.quantidadeEstoque = quantidadeEstoque;
        this.estoqueMinimo = estoqueMinimo;
        this.ativo = ativo;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public static Peca cadastrar(String nome, BigDecimal valorUnitario, Integer quantidadeEstoque, Integer estoqueMinimo){
        LocalDateTime horarioAtual = LocalDateTime.now();
        return new Peca(null, nome, valorUnitario, quantidadeEstoque, estoqueMinimo, true, horarioAtual, horarioAtual);
    }

    public void atualizarDadosCadastrais(String nome, BigDecimal valorUnitario, Integer estoqueMinimo){
        validarValorUnitario(valorUnitario);
        validarEstoqueMinimo(estoqueMinimo);
        validarNome(nome);

        this.nome = nome;
        this.valorUnitario = valorUnitario;
        this.estoqueMinimo = estoqueMinimo;
        this.atualizadoEm = LocalDateTime.now();
    }

    public Peca() {}

    private static void validarValorUnitario(BigDecimal valorUnitario){
        if (valorUnitario == null || valorUnitario.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Valor Unitário não pode ser menor que ZERO.");
        }
    }

    private static void validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome não pode ser vazio");
        }
    }

    private static void validarQuantidadeEstoque(Integer quantidadeEstoque){
        if(quantidadeEstoque == null || quantidadeEstoque < 0){
            throw new IllegalArgumentException("Quantidade Estoque minima insuficiente");
        }
    }

    private static void validarEstoqueMinimo(Integer estoqueMinimo){
        if(estoqueMinimo == null || estoqueMinimo < 0){
            throw new IllegalArgumentException("Quantidade Estoque minima insuficiente");
        }
    }

    public void debitarEstoque(Integer quantidade){
        if (quantidade == null || quantidade <= 0){
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }

        if ((this.quantidadeEstoque - quantidade) < 0){
            throw new EstoqueInsuficienteExcepetion(
                    "Estoque insuficiente para peça " + this.id +
                            ". Disponível: " + this.quantidadeEstoque + ", solicitado: " + quantidade
            );
        }

        this.quantidadeEstoque = this.quantidadeEstoque - quantidade;
    }

    public void incrementarEstoque(Integer quantidade){
        if(quantidade == null || quantidade <= 0){
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
        this.quantidadeEstoque = this.quantidadeEstoque + quantidade;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public Integer getEstoqueMinimo() {
        return estoqueMinimo;
    }

//    public void setQuantidadeEstoque(Integer quantidadeEstoque) {
//        this.quantidadeEstoque = quantidadeEstoque;
//    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public Integer getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public String getNome() {
        return nome;
    }

    public Long getId() {
        return id;
    }

    public boolean isEstoqueBaixo(){
        return this.quantidadeEstoque <= this.estoqueMinimo;
    }
}


