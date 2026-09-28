package br.com.oficinatrack.ordemservico.domain;

import br.com.oficinatrack.shared.exception.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static br.com.oficinatrack.ordemservico.domain.StatusOrdemServico.*;
import static org.junit.jupiter.api.Assertions.*;

class OrdemServicoTest {

    @Test
    void novaOrdemIniciaRecebidaComEntradaNoHistorico() {
        OrdemServico os = new OrdemServico();

        assertEquals(RECEBIDA, os.getStatus());
        assertEquals(1, os.getHistorico().size());
        assertNull(os.getHistorico().get(0).anterior());
        assertEquals(RECEBIDA, os.getHistorico().get(0).novo());
        assertTrue(os.getEventos().isEmpty());
    }

    @Test
    void fluxoCompletoAteEntregaRegistraHistoricoEmOrdem() {
        OrdemServico os = new OrdemServico();

        os.iniciarDiagnostico();
        os.enviarParaAprovacao();
        os.aprovarOrcamento();
        os.finalizarExecucao();
        os.confirmarEntrega();

        assertEquals(ENTREGUE, os.getStatus());
        List<StatusOrdemServico> etapas = os.getHistorico().stream()
                .map(HistoricoStatusOrdemServico::novo).toList();
        assertEquals(List.of(RECEBIDA, EM_DIAGNOSTICO, AGUARDANDO_APROVACAO, EM_EXECUCAO, FINALIZADA, ENTREGUE), etapas);
        assertEquals(5, os.getEventos().size());
    }

    @Test
    void transicaoEmiteEventoComStatusAnteriorENovo() {
        OrdemServico os = new OrdemServico();
        os.setId(10L);

        os.iniciarDiagnostico();

        StatusOrdemServicoAlterado evento = os.getEventos().get(0);
        assertEquals(10L, evento.ordemServicoId());
        assertEquals(RECEBIDA, evento.anterior());
        assertEquals(EM_DIAGNOSTICO, evento.novo());
        assertNotNull(evento.ocorridoEm());
    }

    @Test
    void pularEtapaLancaExcecaoSemAlterarEstado() {
        OrdemServico os = new OrdemServico();

        assertThrows(RegraDeNegocioException.class, os::finalizarExecucao);
        assertEquals(RECEBIDA, os.getStatus());
        assertEquals(1, os.getHistorico().size());
        assertTrue(os.getEventos().isEmpty());
    }

    @Test
    void aprovarSemEstarAguardandoAprovacaoLancaExcecao() {
        OrdemServico os = new OrdemServico();
        os.iniciarDiagnostico();

        assertThrows(RegraDeNegocioException.class, os::aprovarOrcamento);
        assertEquals(EM_DIAGNOSTICO, os.getStatus());
    }

    @Test
    void entregaSoAposFinalizacao() {
        OrdemServico os = new OrdemServico();
        os.iniciarDiagnostico();
        os.enviarParaAprovacao();
        os.aprovarOrcamento();

        assertThrows(RegraDeNegocioException.class, os::confirmarEntrega);
        assertEquals(EM_EXECUCAO, os.getStatus());
    }

    @Test
    void recusaPermiteVoltarAoDiagnostico() {
        OrdemServico os = new OrdemServico();
        os.iniciarDiagnostico();
        os.enviarParaAprovacao();
        os.recusarOrcamento();

        assertEquals(ORCAMENTO_RECUSADO, os.getStatus());
        os.iniciarDiagnostico();
        assertEquals(EM_DIAGNOSTICO, os.getStatus());
    }

    @Test
    void historicoRetornadoEhImutavel() {
        OrdemServico os = new OrdemServico();

        assertThrows(UnsupportedOperationException.class, () -> os.getHistorico().clear());
    }
}
