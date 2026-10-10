package br.com.oficinatrack.ordemservico.domain;

import br.com.oficinatrack.shared.exception.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrdemServicoTest {

    private OrdemServico novaOs() {
        return OrdemServico.abrir(1L, 2L);
    }

    @Test
    void abrirDeveIniciarRecebidaComTotalZero() {
        OrdemServico os = novaOs();

        assertEquals(StatusOrdemServico.RECEBIDA, os.getStatus());
        assertEquals(new BigDecimal("0.00"), os.getValorTotal());
    }

    @Test
    void totalDeveSerSomaDeServicosEPecas() {
        OrdemServico os = novaOs();
        os.adicionarServico(10L, "Troca de óleo", new BigDecimal("80.00"), 1);
        os.adicionarServico(11L, "Alinhamento", new BigDecimal("50.50"), 2);
        os.adicionarPeca(20L, "Filtro", new BigDecimal("35.25"), 3);

        // 80.00 + 101.00 + 105.75
        assertEquals(new BigDecimal("286.75"), os.getValorTotal());
        assertEquals(new BigDecimal("286.75"), os.calcularTotal());
    }

    @Test
    void quantidadeDaPecaDeveSomarLinhasDaMesmaPeca() {
        OrdemServico os = novaOs();
        os.adicionarPeca(20L, "Filtro", BigDecimal.TEN, 2);
        os.adicionarPeca(20L, "Filtro", BigDecimal.TEN, 3);
        os.adicionarPeca(21L, "Vela", BigDecimal.TEN, 1);

        assertEquals(5, os.quantidadeDaPeca(20L));
        assertEquals(0, os.quantidadeDaPeca(99L));
    }

    @Test
    void enviarOrcamentoSemItensDeveFalhar() {
        OrdemServico os = novaOs();

        assertThrows(RegraDeNegocioException.class, os::enviarOrcamento);
        assertEquals(StatusOrdemServico.RECEBIDA, os.getStatus());
    }

    @Test
    void enviarOrcamentoDeveMudarStatusEBloquearNovosItens() {
        OrdemServico os = novaOs();
        os.adicionarServico(10L, "Troca de óleo", new BigDecimal("80.00"), 1);

        StatusOrdemServico anterior = os.enviarOrcamento();

        assertEquals(StatusOrdemServico.RECEBIDA, anterior);
        assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO, os.getStatus());
        assertThrows(RegraDeNegocioException.class,
                () -> os.adicionarServico(11L, "Outro", BigDecimal.ONE, 1));
        assertThrows(RegraDeNegocioException.class,
                () -> os.adicionarPeca(20L, "Filtro", BigDecimal.ONE, 1));
        assertThrows(RegraDeNegocioException.class, os::enviarOrcamento);
    }

    @Test
    void statusAceitaNovosItensSomenteEmRecebidaOuDiagnostico() {
        for (StatusOrdemServico status : StatusOrdemServico.values()) {
            boolean esperado = status == StatusOrdemServico.RECEBIDA || status == StatusOrdemServico.EM_DIAGNOSTICO;
            assertEquals(esperado, status.aceitaNovosItens(), status.name());
        }
    }
}
