package br.com.oficinatrack.publico.domain;

import br.com.oficinatrack.ordemservico.domain.OrdemServico;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "acessos_publicos_ordem_servico")
public class AcessoPublicoOrdemServico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    private OrdemServico ordemServico;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;

    @Column(name = "revogado_em")
    private LocalDateTime revogadoEm;

    protected AcessoPublicoOrdemServico() {}

    public AcessoPublicoOrdemServico(OrdemServico ordemServico, String tokenHash, LocalDateTime expiraEm) {
        this.ordemServico = ordemServico;
        this.tokenHash = tokenHash;
        this.criadoEm = LocalDateTime.now();
        this.expiraEm = expiraEm;
    }

    public void revogar() {
        if (revogadoEm == null) revogadoEm = LocalDateTime.now();
    }

    public boolean estaValido() {
        return revogadoEm == null && expiraEm.isAfter(LocalDateTime.now());
    }

    public Long getId() { return id; }
    public OrdemServico getOrdemServico() { return ordemServico; }
    public String getTokenHash() { return tokenHash; }
    public LocalDateTime getExpiraEm() { return expiraEm; }
}
