package br.com.oficinatrack.ordemservico.domain;

import br.com.oficinatrack.shared.exception.RegraDeNegocioException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrdemServico {
    private Long id;
    private StatusOrdemServico status;
    private final List<HistoricoStatusOrdemServico> historico = new ArrayList<>();
    private final List<StatusOrdemServicoAlterado> eventos = new ArrayList<>();

    public OrdemServico() {
        this.status = StatusOrdemServico.RECEBIDA;
        historico.add(new HistoricoStatusOrdemServico(null, StatusOrdemServico.RECEBIDA, LocalDateTime.now()));
    }

    public void iniciarDiagnostico() {
        alterarStatus(StatusOrdemServico.EM_DIAGNOSTICO);
    }

    public void enviarParaAprovacao() {
        alterarStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);
    }

    public void aprovarOrcamento() {
        alterarStatus(StatusOrdemServico.EM_EXECUCAO);
    }

    public void recusarOrcamento() {
        alterarStatus(StatusOrdemServico.ORCAMENTO_RECUSADO);
    }

    public void finalizarExecucao() {
        alterarStatus(StatusOrdemServico.FINALIZADA);
    }

    public void confirmarEntrega() {
        alterarStatus(StatusOrdemServico.ENTREGUE);
    }

    private void alterarStatus(StatusOrdemServico destino) {
        if (!status.podeTransicionarPara(destino)) {
            throw new RegraDeNegocioException(
                    "Transição de status inválida: " + status + " -> " + destino);
        }
        StatusOrdemServico anterior = status;
        LocalDateTime agora = LocalDateTime.now();
        status = destino;
        historico.add(new HistoricoStatusOrdemServico(anterior, destino, agora));
        eventos.add(new StatusOrdemServicoAlterado(id, anterior, destino, agora));
    }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public StatusOrdemServico getStatus() {return status;}
    public List<HistoricoStatusOrdemServico> getHistorico() {return List.copyOf(historico);}
    public List<StatusOrdemServicoAlterado> getEventos() {return List.copyOf(eventos);}
}
