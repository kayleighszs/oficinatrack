package br.com.oficinatrack.ordemservico.infra;

import br.com.oficinatrack.catalogoservicos.api.dto.out.ServicoRequestOut;
import br.com.oficinatrack.catalogoservicos.application.ServicoService;
import br.com.oficinatrack.ordemservico.application.port.ServicoCatalogoPort.ServicoCatalogo;
import br.com.oficinatrack.shared.exception.RecursoNaoEncontradoException;
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
class ServicoCatalogoAdapterTest {

    @Mock
    private ServicoService servicoService;

    @InjectMocks
    private ServicoCatalogoAdapter adapter;

    @Test
    void deveTraduzirServicoDoCatalogoParaOModeloDaOs() {
        when(servicoService.buscarPorId(10L))
                .thenReturn(new ServicoRequestOut(10L, "Troca de óleo", new BigDecimal("80.00"), 30, true));

        Optional<ServicoCatalogo> resultado = adapter.buscar(10L);

        assertTrue(resultado.isPresent());
        assertEquals(new ServicoCatalogo(10L, "Troca de óleo", new BigDecimal("80.00"), true), resultado.get());
    }

    @Test
    void deveRetornarVazioQuandoServicoNaoExiste() {
        when(servicoService.buscarPorId(99L))
                .thenThrow(new RecursoNaoEncontradoException("Serviço não encontrado com o ID: 99"));

        assertTrue(adapter.buscar(99L).isEmpty());
    }

    @Test
    void deveMarcarComoInativoQuandoAtivoForFalsoOuNulo() {
        when(servicoService.buscarPorId(1L))
                .thenReturn(new ServicoRequestOut(1L, "Antigo", BigDecimal.TEN, 30, false));
        when(servicoService.buscarPorId(2L))
                .thenReturn(new ServicoRequestOut(2L, "Sem flag", BigDecimal.TEN, 30, null));

        assertFalse(adapter.buscar(1L).orElseThrow().ativo());
        assertFalse(adapter.buscar(2L).orElseThrow().ativo());
    }
}
