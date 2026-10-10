package br.com.oficinatrack.ordemservico.infra;

import br.com.oficinatrack.estoque.application.PecaService;
import br.com.oficinatrack.estoque.application.dto.PecaResponse;
import br.com.oficinatrack.estoque.exceptions.PecaNaoEncontradaException;
import br.com.oficinatrack.ordemservico.application.port.EstoquePort.PecaEstoque;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstoqueAdapterTest {

    @Mock
    private PecaService pecaService;

    @InjectMocks
    private EstoqueAdapter adapter;

    @Test
    void deveTraduzirPecaParaOModeloDaOs() {
        when(pecaService.buscarPorId(20L))
                .thenReturn(new PecaResponse(20L, "Filtro de óleo", new BigDecimal("35.25"), 10, 2, false, true));

        Optional<PecaEstoque> resultado = adapter.consultar(20L);

        assertEquals(Optional.of(new PecaEstoque(20L, "Filtro de óleo", new BigDecimal("35.25"), 10, true)),
                resultado);
    }

    @Test
    void deveMarcarComoInativaQuandoAtivoForFalsoOuNulo() {
        when(pecaService.buscarPorId(1L))
                .thenReturn(new PecaResponse(1L, "Antiga", BigDecimal.TEN, 5, 0, false, false));
        when(pecaService.buscarPorId(2L))
                .thenReturn(new PecaResponse(2L, "Sem flag", BigDecimal.TEN, 5, 0, false, null));

        assertFalse(adapter.consultar(1L).orElseThrow().ativa());
        assertFalse(adapter.consultar(2L).orElseThrow().ativa());
    }

    @Test
    void deveRetornarVazioQuandoPecaNaoExiste() {
        when(pecaService.buscarPorId(99L))
                .thenThrow(new PecaNaoEncontradaException("Peça não encontrada com o ID: 99"));

        assertTrue(adapter.consultar(99L).isEmpty());
    }
}
