package br.com.oficinatrack.ordemservico.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "historico_status_os")
public class HistoricoStatusOs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ordem_servico_id", nullable = false)
    private Long ordemServicoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior", length = 25)
    private StatusOrdemServico statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_novo", length = 25, nullable = false)
    private StatusOrdemServico statusNovo;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "alterado_em", nullable = false)
    private LocalDateTime alteradoEm;

    protected HistoricoStatusOs() {
    }

    public HistoricoStatusOs(Long ordemServicoId, StatusOrdemServico statusAnterior, StatusOrdemServico statusNovo) {
        this.ordemServicoId = ordemServicoId;
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.alteradoEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getOrdemServicoId() { return ordemServicoId; }
    public StatusOrdemServico getStatusAnterior() { return statusAnterior; }
    public StatusOrdemServico getStatusNovo() { return statusNovo; }
    public Long getUsuarioId() { return usuarioId; }
    public LocalDateTime getAlteradoEm() { return alteradoEm; }
}
