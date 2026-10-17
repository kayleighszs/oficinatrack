package br.com.oficinatrack.ordemservico.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "itens_servico_ordem", uniqueConstraints =
        @UniqueConstraint(name = "uk_item_servico_ordem", columnNames = {"ordem_servico_id", "servico_id"}))
public class ItemServicoOrdem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    private OrdemServico ordemServico;

    @Column(name = "servico_id", nullable = false)
    private Long servicoId;

    @Column(nullable = false, length = 150)
    private String descricao;

    @Column(name = "valor_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorUnitario;

    @Column(nullable = false)
    private Integer quantidade;

    protected ItemServicoOrdem() {}

    ItemServicoOrdem(OrdemServico ordemServico, Long servicoId, String descricao,
                     BigDecimal valorUnitario, Integer quantidade) {
        this.ordemServico = ordemServico;
        this.servicoId = servicoId;
        this.descricao = descricao;
        this.valorUnitario = valorUnitario;
        this.quantidade = quantidade;
    }

    public BigDecimal subtotal() { return valorUnitario.multiply(BigDecimal.valueOf(quantidade)); }
    public Long getId() { return id; }
    public Long getServicoId() { return servicoId; }
    public String getDescricao() { return descricao; }
    public BigDecimal getValorUnitario() { return valorUnitario; }
    public Integer getQuantidade() { return quantidade; }
}
