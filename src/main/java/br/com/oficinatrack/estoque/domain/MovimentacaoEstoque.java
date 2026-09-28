package br.com.oficinatrack.estoque.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "peca_id", nullable = false)
    private Peca peca;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimentacao tipo;

    @Column(nullable = false)
    private Integer quantidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Origem origem;

    @Column(name = "ordem_servico_id")
    private Long ordemServicoId; // preenchido só quando origem = BAIXA_OS

    @Column(name = "usuario_id")
    private Long usuarioId; // preenchido só quando origem = MANUAL

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    public MovimentacaoEstoque() {}

    public MovimentacaoEstoque(Peca peca, TipoMovimentacao tipo, Integer quantidade, Origem origem, Long ordemServicoId, Long usuarioId, LocalDateTime criadoEm) {
        this.peca = peca;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.origem = origem;
        this.ordemServicoId = ordemServicoId;
        this.usuarioId = usuarioId;
        this.criadoEm = criadoEm;
    }

    public static MovimentacaoEstoque registrarManual(Peca peca, TipoMovimentacao tipo, Integer quantidade, Long usuarioId){
        return new MovimentacaoEstoque(peca, tipo, quantidade, Origem.MANUAL, null, usuarioId,LocalDateTime.now());
    }

    public static MovimentacaoEstoque registrarAutomatica(Peca peca, Integer quantidade, Long ordemServicoId){
        return new MovimentacaoEstoque(peca, TipoMovimentacao.SAIDA, quantidade, Origem.BAIXA_OS, ordemServicoId, null, LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }

    public Peca getPeca() {
        return peca;
    }

    public TipoMovimentacao getTipo() {
        return tipo;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public Origem getOrigem() {
        return origem;
    }

    public Long getOrdemServicoId() {
        return ordemServicoId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
