package br.com.oficinatrack.ordemservico.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "itens_servico_os")
public class ItemServicoOs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    private OrdemServico ordemServico;

    @Column(name = "servico_id", nullable = false)
    private Long servicoId;

    // copiados do catálogo no momento da inclusão (não são referência viva)
    @Column(name = "descricao_servico", length = 150, nullable = false)
    private String descricaoServico;

    @Column(name = "valor_unitario", precision = 10, scale = 2, nullable = false)
    private BigDecimal valorUnitario;

    @Column(name = "quantidade", nullable = false)
    private Integer quantidade;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    protected ItemServicoOs() {
    }

    ItemServicoOs(OrdemServico ordemServico, Long servicoId, String descricaoServico,
                  BigDecimal valorUnitario, Integer quantidade) {
        this.ordemServico = ordemServico;
        this.servicoId = servicoId;
        this.descricaoServico = descricaoServico;
        this.valorUnitario = valorUnitario;
        this.quantidade = quantidade;
    }

    @PrePersist
    void aoCriar() {
        this.criadoEm = LocalDateTime.now();
    }

    public BigDecimal subtotal() {
        return valorUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    public Long getId() { return id; }
    public Long getServicoId() { return servicoId; }
    public String getDescricaoServico() { return descricaoServico; }
    public BigDecimal getValorUnitario() { return valorUnitario; }
    public Integer getQuantidade() { return quantidade; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
