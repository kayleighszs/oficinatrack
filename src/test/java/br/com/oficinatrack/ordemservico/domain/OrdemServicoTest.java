package br.com.oficinatrack.ordemservico.domain;

import br.com.oficinatrack.shared.exception.ConflitoException;
import br.com.oficinatrack.shared.exception.RegraDeNegocioException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class OrdemServicoTest {

    @Test
    void devePercorrerFluxoAteEnvioDoOrcamento() {
        OrdemServico os = OrdemServico.abrir(1L, 2L);
        os.iniciarDiagnostico();
        os.adicionarServico(3L, "Troca de óleo", new BigDecimal("100.00"), 1);
        os.adicionarPeca(4L, "Filtro", new BigDecimal("25.50"), 2);

        Orcamento orcamento = os.calcularOrcamento();
        os.enviarOrcamento(orcamento.getId());

        assertEquals(new BigDecimal("151.00"), orcamento.getTotal());
        assertEquals(StatusOrcamento.ENVIADO, orcamento.getStatus());
        assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO, os.getStatus());
        assertEquals(3, os.getHistorico().size());
    }

    @Test
    void deveBloquearPuloDeEtapa() {
        OrdemServico os = OrdemServico.abrir(1L, 2L);
        assertThrows(ConflitoException.class, os::finalizarExecucao);
        assertEquals(StatusOrdemServico.RECEBIDA, os.getStatus());
    }

    @Test
    void deveSubstituirOrcamentoCalculadoQuandoItensMudam() {
        OrdemServico os = OrdemServico.abrir(1L, 2L);
        os.iniciarDiagnostico();
        os.adicionarServico(3L, "Diagnóstico", new BigDecimal("50.00"), 1);
        Orcamento anterior = os.calcularOrcamento();

        os.adicionarPeca(4L, "Peça", new BigDecimal("10.00"), 1);
        Orcamento atual = os.calcularOrcamento();

        assertEquals(StatusOrcamento.SUBSTITUIDO, anterior.getStatus());
        assertEquals(2, atual.getVersao());
        assertEquals(new BigDecimal("60.00"), atual.getTotal());
    }

    @Test
    void deveRecusarOrcamentoSemLiberarExecucao() {
        OrdemServico os = OrdemServico.abrir(1L, 2L);
        os.iniciarDiagnostico();
        os.adicionarServico(3L, "Diagnóstico", new BigDecimal("50.00"), 1);
        Orcamento orcamento = os.calcularOrcamento();
        os.enviarOrcamento(orcamento.getId());

        os.recusarOrcamento(orcamento, "Valor não aprovado");

        assertEquals(StatusOrcamento.RECUSADO, orcamento.getStatus());
        assertEquals(StatusOrdemServico.ORCAMENTO_RECUSADO, os.getStatus());
    }

    @Test
    void deveCancelarAposRecusaPreservandoOrcamentoRecusado() {
        OrdemServico os = OrdemServico.abrir(1L, 2L);
        os.iniciarDiagnostico();
        os.adicionarServico(3L, "Diagnóstico", new BigDecimal("50.00"), 1);
        Orcamento orcamento = os.calcularOrcamento();
        os.enviarOrcamento(orcamento.getId());
        os.recusarOrcamento(orcamento, "Valor não aprovado");

        os.cancelar("Cliente desistiu do serviço");

        assertEquals(StatusOrdemServico.CANCELADA, os.getStatus());
        assertEquals(StatusOrcamento.RECUSADO, orcamento.getStatus());
        assertEquals("Cliente desistiu do serviço", os.getMotivoCancelamento());
        assertNotNull(os.getCanceladoEm());
        assertEquals(StatusOrdemServico.CANCELADA, os.getHistorico().getLast().getStatusNovo());
    }

    @Test
    void deveCancelarOrcamentoEnviadoAoCancelarOrdem() {
        OrdemServico os = OrdemServico.abrir(1L, 2L);
        os.iniciarDiagnostico();
        os.adicionarServico(3L, "Diagnóstico", new BigDecimal("50.00"), 1);
        Orcamento orcamento = os.calcularOrcamento();
        os.enviarOrcamento(orcamento.getId());

        os.cancelar("Peça indisponível");

        assertEquals(StatusOrcamento.CANCELADO, orcamento.getStatus());
        assertEquals(StatusOrdemServico.CANCELADA, os.getStatus());
    }

    @Test
    void deveIgnorarCancelamentoRepetido() {
        OrdemServico os = OrdemServico.abrir(1L, 2L);
        os.cancelar("Primeiro motivo");
        int registros = os.getHistorico().size();

        os.cancelar("Outro motivo");

        assertEquals("Primeiro motivo", os.getMotivoCancelamento());
        assertEquals(registros, os.getHistorico().size());
    }

    @Test
    void deveBloquearCancelamentoAposAprovacao() {
        OrdemServico os = OrdemServico.abrir(1L, 2L);
        os.iniciarDiagnostico();
        os.adicionarServico(3L, "Diagnóstico", new BigDecimal("50.00"), 1);
        Orcamento orcamento = os.calcularOrcamento();
        os.enviarOrcamento(orcamento.getId());
        os.aprovarOrcamento(orcamento);

        assertThrows(ConflitoException.class, () -> os.cancelar("Tarde demais"));
        assertEquals(StatusOrdemServico.EM_EXECUCAO, os.getStatus());
    }

    @Test
    void deveExigirMotivoDoCancelamento() {
        OrdemServico os = OrdemServico.abrir(1L, 2L);
        assertThrows(RegraDeNegocioException.class, () -> os.cancelar(" "));
        assertEquals(StatusOrdemServico.RECEBIDA, os.getStatus());
    }
}
