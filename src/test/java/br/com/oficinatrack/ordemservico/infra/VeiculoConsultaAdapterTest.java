package br.com.oficinatrack.ordemservico.infra;

import br.com.oficinatrack.ordemservico.application.port.VeiculoConsultaPort.VeiculoInfo;
import br.com.oficinatrack.shared.exception.RecursoNaoEncontradoException;
import br.com.oficinatrack.veiculo.api.dto.VeiculoResponse;
import br.com.oficinatrack.veiculo.application.VeiculoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VeiculoConsultaAdapterTest {

    @Mock
    private VeiculoService veiculoService;

    @InjectMocks
    private VeiculoConsultaAdapter adapter;

    private VeiculoResponse veiculo(Long id, Long clienteId, boolean ativo) {
        VeiculoResponse response = new VeiculoResponse();
        response.setId(id);
        response.setClienteId(clienteId);
        response.setAtivo(ativo);
        return response;
    }

    @Test
    void deveTraduzirVeiculoParaOModeloDaOs() {
        when(veiculoService.detalhar(2L)).thenReturn(veiculo(2L, 1L, true));

        Optional<VeiculoInfo> resultado = adapter.buscar(2L);

        assertEquals(Optional.of(new VeiculoInfo(2L, 1L, true)), resultado);
    }

    @Test
    void deveRepassarVeiculoInativo() {
        when(veiculoService.detalhar(2L)).thenReturn(veiculo(2L, 1L, false));

        assertEquals(Optional.of(new VeiculoInfo(2L, 1L, false)), adapter.buscar(2L));
    }

    @Test
    void deveRetornarVazioQuandoVeiculoNaoExiste() {
        when(veiculoService.detalhar(99L))
                .thenThrow(new RecursoNaoEncontradoException("Veículo não encontrado: 99"));

        assertTrue(adapter.buscar(99L).isEmpty());
    }
}
