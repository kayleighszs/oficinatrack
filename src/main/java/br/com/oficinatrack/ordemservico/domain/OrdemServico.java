package br.com.oficinatrack.ordemservico.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "servico")
public class OrdemServico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "tempo_medio_estimado_minutos")
    private Integer tempoMedioEstimadoMinutos;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    public OrdemServico() {}

    public OrdemServico(String nome, BigDecimal valor, Integer tempoMedioEstimadoMinutos) {
        validarNome(nome);
        validarValor(valor);

        this.nome = nome;
        this.valor = valor;
        this.tempoMedioEstimadoMinutos = tempoMedioEstimadoMinutos;
        this.ativo = true;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime agora = LocalDateTime.now();
        this.criadoEm = agora;
        this.atualizadoEm = agora;
        if (this.ativo == null) {
            this.ativo = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }

    private void validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do serviço é obrigatório.");
        }
    }

    private void validarValor(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do serviço deve ser maior que zero.");
        }
    }

    public void atualizarDados(String nome, BigDecimal valor, Integer tempoMedioEstimadoMinutos) {
        validarNome(nome);
        validarValor(valor);

        this.nome = nome;
        this.valor = valor;
        this.tempoMedioEstimadoMinutos = tempoMedioEstimadoMinutos;
    }

    public void inativar() {
        this.ativo = false;
    }

    public void reativar() {
        this.ativo = true;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public Integer getTempoMedioEstimadoMinutos() {
        return tempoMedioEstimadoMinutos;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
}
