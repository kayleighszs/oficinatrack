package br.com.oficinatrack.ordemservico.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "itens_peca_os")
public class ItemPecaOs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    private OrdemServico ordemServico;

    @Column(name = "peca_id", nullable = false)
    private Long pecaId;

    // copiados do estoque no momento da inclusão (não são referência viva)
    @Column(name = "descricao_peca", length = 150, nullable = false)
    private String descricaoPeca;

    @Column(name = "valor_unitario", precision = 10, scale = 2, nullable = false)
    private BigDecimal valorUnitario;

    @Column(name = "quantidade", nullable = false)
    private Integer quantidade;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    protected ItemPecaOs() {
    }

    ItemPecaOs(OrdemServico ordemServico, Long pecaId, String descricaoPeca,
               BigDecimal valorUnitario, Integer quantidade) {
        this.ordemServico = ordemServico;
        this.pecaId = pecaId;
        this.descricaoPeca = descricaoPeca;
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
    public Long getPecaId() { return pecaId; }
    public String getDescricaoPeca() { return descricaoPeca; }
    public BigDecimal getValorUnitario() { return valorUnitario; }
    public Integer getQuantidade() { return quantidade; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
