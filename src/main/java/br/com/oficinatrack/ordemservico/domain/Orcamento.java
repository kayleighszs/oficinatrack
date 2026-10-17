package br.com.oficinatrack.ordemservico.domain;

import br.com.oficinatrack.shared.exception.ConflitoException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orcamentos", uniqueConstraints =
        @UniqueConstraint(name = "uk_orcamento_versao", columnNames = {"ordem_servico_id", "versao"}))
public class Orcamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    private OrdemServico ordemServico;

    @Column(nullable = false)
    private Integer versao;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusOrcamento status;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "enviado_em")
    private LocalDateTime enviadoEm;

    @Column(name = "decidido_em")
    private LocalDateTime decididoEm;

    @Column(name = "motivo_recusa", length = 500)
    private String motivoRecusa;

    protected Orcamento() {}

    Orcamento(OrdemServico ordemServico, Integer versao, BigDecimal total) {
        this.ordemServico = ordemServico;
        this.versao = versao;
        this.total = total;
        this.status = StatusOrcamento.CALCULADO;
        this.criadoEm = LocalDateTime.now();
    }

    void substituir() {
        if (status == StatusOrcamento.CALCULADO) status = StatusOrcamento.SUBSTITUIDO;
    }

    void enviar() {
        if (status != StatusOrcamento.CALCULADO) throw new ConflitoException("Orçamento não pode ser enviado.");
        status = StatusOrcamento.ENVIADO;
        enviadoEm = LocalDateTime.now();
    }

    public void aprovar() {
        if (status != StatusOrcamento.ENVIADO) throw new ConflitoException("Orçamento não está disponível para decisão.");
        status = StatusOrcamento.APROVADO;
        decididoEm = LocalDateTime.now();
    }

    public void recusar(String motivo) {
        if (status != StatusOrcamento.ENVIADO) throw new ConflitoException("Orçamento não está disponível para decisão.");
        if (motivo == null || motivo.isBlank()) throw new IllegalArgumentException("Motivo da recusa é obrigatório.");
        status = StatusOrcamento.RECUSADO;
        motivoRecusa = motivo.trim();
        decididoEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Integer getVersao() { return versao; }
    public BigDecimal getTotal() { return total; }
    public StatusOrcamento getStatus() { return status; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getEnviadoEm() { return enviadoEm; }
    public LocalDateTime getDecididoEm() { return decididoEm; }
    public String getMotivoRecusa() { return motivoRecusa; }
}
