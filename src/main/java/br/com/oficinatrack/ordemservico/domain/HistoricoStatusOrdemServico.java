package br.com.oficinatrack.ordemservico.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historico_status_ordem_servico")
public class HistoricoStatusOrdemServico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    private OrdemServico ordemServico;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior", length = 30)
    private StatusOrdemServico statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_novo", nullable = false, length = 30)
    private StatusOrdemServico statusNovo;

    @Column(name = "alterado_em", nullable = false)
    private LocalDateTime alteradoEm;

    protected HistoricoStatusOrdemServico() {}

    HistoricoStatusOrdemServico(OrdemServico ordemServico, StatusOrdemServico anterior,
                                StatusOrdemServico novo) {
        this.ordemServico = ordemServico;
        this.statusAnterior = anterior;
        this.statusNovo = novo;
        this.alteradoEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public StatusOrdemServico getStatusAnterior() { return statusAnterior; }
    public StatusOrdemServico getStatusNovo() { return statusNovo; }
    public LocalDateTime getAlteradoEm() { return alteradoEm; }
}
