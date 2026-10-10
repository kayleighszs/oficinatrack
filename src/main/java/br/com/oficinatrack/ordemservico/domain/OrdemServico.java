package br.com.oficinatrack.ordemservico.domain;

import br.com.oficinatrack.shared.exception.RegraDeNegocioException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "ordens_servico")
public class OrdemServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "veiculo_id", nullable = false)
    private Long veiculoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 25, nullable = false)
    private StatusOrdemServico status;

    @Column(name = "valor_total", precision = 10, scale = 2, nullable = false)
    private BigDecimal valorTotal = BigDecimal.ZERO.setScale(2);

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemServicoOs> itensServico = new ArrayList<>();

    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPecaOs> itensPeca = new ArrayList<>();

    protected OrdemServico() {
    }

    public static OrdemServico abrir(Long clienteId, Long veiculoId) {
        OrdemServico os = new OrdemServico();
        os.clienteId = clienteId;
        os.veiculoId = veiculoId;
        os.status = StatusOrdemServico.RECEBIDA;
        return os;
    }

    @PrePersist
    void aoCriar() {
        LocalDateTime agora = LocalDateTime.now();
        this.criadoEm = agora;
        this.atualizadoEm = agora;
    }

    @PreUpdate
    void aoAtualizar() {
        this.atualizadoEm = LocalDateTime.now();
    }

    public void exigirAceitaNovosItens() {
        if (!status.aceitaNovosItens()) {
            throw new RegraDeNegocioException(
                    "não é possível incluir itens em uma OS com status " + status);
        }
    }

    public ItemServicoOs adicionarServico(Long servicoId, String descricao, BigDecimal valorUnitario, int quantidade) {
        exigirAceitaNovosItens();
        ItemServicoOs item = new ItemServicoOs(this, servicoId, descricao, valorUnitario, quantidade);
        itensServico.add(item);
        calcularTotal();
        return item;
    }

    public ItemPecaOs adicionarPeca(Long pecaId, String descricao, BigDecimal valorUnitario, int quantidade) {
        exigirAceitaNovosItens();
        ItemPecaOs item = new ItemPecaOs(this, pecaId, descricao, valorUnitario, quantidade);
        itensPeca.add(item);
        calcularTotal();
        return item;
    }

    /** envia o orçamento para aprovação; a partir daqui não aceita novos itens */
    public StatusOrdemServico enviarOrcamento() {
        exigirAceitaNovosItens();
        if (itensServico.isEmpty() && itensPeca.isEmpty()) {
            throw new RegraDeNegocioException("a OS precisa ter ao menos um item para enviar o orçamento");
        }
        StatusOrdemServico anterior = status;
        status = StatusOrdemServico.AGUARDANDO_APROVACAO;
        return anterior;
    }

    /** valorTotal = soma(itens de serviço) + soma(itens de peça) */
    public BigDecimal calcularTotal() {
        BigDecimal servicos = itensServico.stream()
                .map(ItemServicoOs::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pecas = itensPeca.stream()
                .map(ItemPecaOs::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.valorTotal = servicos.add(pecas).setScale(2, RoundingMode.HALF_UP);
        return valorTotal;
    }

    public int quantidadeDaPeca(Long pecaId) {
        return itensPeca.stream()
                .filter(i -> i.getPecaId().equals(pecaId))
                .mapToInt(ItemPecaOs::getQuantidade)
                .sum();
    }

    public Long getId() { return id; }
    public Long getClienteId() { return clienteId; }
    public Long getVeiculoId() { return veiculoId; }
    public StatusOrdemServico getStatus() { return status; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public List<ItemServicoOs> getItensServico() { return Collections.unmodifiableList(itensServico); }
    public List<ItemPecaOs> getItensPeca() { return Collections.unmodifiableList(itensPeca); }
}
