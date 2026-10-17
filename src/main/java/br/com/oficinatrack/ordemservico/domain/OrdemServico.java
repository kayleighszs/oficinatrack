package br.com.oficinatrack.ordemservico.domain;

import br.com.oficinatrack.shared.exception.ConflitoException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "ordens_servico")
public class OrdemServico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_os", nullable = false, unique = true, length = 24)
    private String numeroOs;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "veiculo_id", nullable = false)
    private Long veiculoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusOrdemServico status;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @Version
    private Long versaoRegistro;

    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemServicoOrdem> itensServico = new ArrayList<>();

    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPecaOrdem> itensPeca = new ArrayList<>();

    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("versao ASC")
    private List<Orcamento> orcamentos = new ArrayList<>();

    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("alteradoEm ASC")
    private List<HistoricoStatusOrdemServico> historico = new ArrayList<>();

    protected OrdemServico() {}

    public static OrdemServico abrir(Long clienteId, Long veiculoId) {
        OrdemServico os = new OrdemServico();
        os.numeroOs = "OS-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        os.clienteId = clienteId;
        os.veiculoId = veiculoId;
        os.status = StatusOrdemServico.RECEBIDA;
        os.registrarHistorico(null, os.status);
        return os;
    }

    @PrePersist
    void aoCriar() {
        LocalDateTime agora = LocalDateTime.now();
        criadoEm = agora;
        atualizadoEm = agora;
    }

    @PreUpdate
    void aoAtualizar() { atualizadoEm = LocalDateTime.now(); }

    public void iniciarDiagnostico() { transicionar(StatusOrdemServico.RECEBIDA, StatusOrdemServico.EM_DIAGNOSTICO); }

    public void adicionarServico(Long id, String descricao, BigDecimal valor, Integer quantidade) {
        exigirDiagnostico();
        if (itensServico.stream().anyMatch(item -> item.getServicoId().equals(id)))
            throw new ConflitoException("Serviço já incluído na ordem.");
        validarQuantidade(quantidade);
        invalidarOrcamentoCalculado();
        itensServico.add(new ItemServicoOrdem(this, id, descricao, valor, quantidade));
    }

    public void adicionarPeca(Long id, String descricao, BigDecimal valor, Integer quantidade) {
        exigirDiagnostico();
        if (itensPeca.stream().anyMatch(item -> item.getPecaId().equals(id)))
            throw new ConflitoException("Peça já incluída na ordem.");
        validarQuantidade(quantidade);
        invalidarOrcamentoCalculado();
        itensPeca.add(new ItemPecaOrdem(this, id, descricao, valor, quantidade));
    }

    public Orcamento calcularOrcamento() {
        exigirDiagnostico();
        if (itensServico.isEmpty() && itensPeca.isEmpty())
            throw new ConflitoException("Inclua ao menos um item antes de calcular o orçamento.");
        invalidarOrcamentoCalculado();
        BigDecimal total = itensServico.stream().map(ItemServicoOrdem::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(itensPeca.stream().map(ItemPecaOrdem::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add))
                .setScale(2, RoundingMode.HALF_UP);
        Orcamento orcamento = new Orcamento(this, orcamentos.size() + 1, total);
        orcamentos.add(orcamento);
        return orcamento;
    }

    public void enviarOrcamento(Long orcamentoId) {
        exigirDiagnostico();
        Orcamento atual = localizarOrcamento(orcamentoId);
        if (orcamentos.get(orcamentos.size() - 1) != atual)
            throw new ConflitoException("Somente o orçamento atual pode ser enviado.");
        atual.enviar();
        alterarStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);
    }

    public void aprovarOrcamento(Orcamento orcamento) {
        exigirStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);
        orcamento.aprovar();
        alterarStatus(StatusOrdemServico.EM_EXECUCAO);
    }

    public void recusarOrcamento(Orcamento orcamento, String motivo) {
        exigirStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);
        orcamento.recusar(motivo);
        alterarStatus(StatusOrdemServico.ORCAMENTO_RECUSADO);
    }

    public void finalizarExecucao() { transicionar(StatusOrdemServico.EM_EXECUCAO, StatusOrdemServico.FINALIZADA); }
    public void confirmarEntrega() { transicionar(StatusOrdemServico.FINALIZADA, StatusOrdemServico.ENTREGUE); }

    public Orcamento localizarOrcamento(Long id) {
        return orcamentos.stream().filter(o -> Objects.equals(o.getId(), id)).findFirst()
                .orElseThrow(() -> new ConflitoException("Orçamento não pertence à ordem de serviço."));
    }

    private void transicionar(StatusOrdemServico origem, StatusOrdemServico destino) {
        exigirStatus(origem);
        alterarStatus(destino);
    }

    private void alterarStatus(StatusOrdemServico destino) {
        StatusOrdemServico anterior = status;
        status = destino;
        registrarHistorico(anterior, destino);
    }

    private void registrarHistorico(StatusOrdemServico anterior, StatusOrdemServico novo) {
        historico.add(new HistoricoStatusOrdemServico(this, anterior, novo));
    }

    private void exigirDiagnostico() { exigirStatus(StatusOrdemServico.EM_DIAGNOSTICO); }
    private void exigirStatus(StatusOrdemServico esperado) {
        if (status != esperado) throw new ConflitoException("Operação incompatível com o status " + status + ".");
    }
    private void validarQuantidade(Integer quantidade) {
        if (quantidade == null || quantidade <= 0) throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
    }
    private void invalidarOrcamentoCalculado() {
        orcamentos.stream().filter(o -> o.getStatus() == StatusOrcamento.CALCULADO).forEach(Orcamento::substituir);
    }

    public Long getId() { return id; }
    public String getNumeroOs() { return numeroOs; }
    public Long getClienteId() { return clienteId; }
    public Long getVeiculoId() { return veiculoId; }
    public StatusOrdemServico getStatus() { return status; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public List<ItemServicoOrdem> getItensServico() { return List.copyOf(itensServico); }
    public List<ItemPecaOrdem> getItensPeca() { return List.copyOf(itensPeca); }
    public List<Orcamento> getOrcamentos() { return List.copyOf(orcamentos); }
    public List<HistoricoStatusOrdemServico> getHistorico() { return List.copyOf(historico); }
}
