package br.com.oficinatrack.ordemservico.infra;

import br.com.oficinatrack.cliente.api.dto.ClienteResponse;
import br.com.oficinatrack.cliente.application.ClienteService;
import br.com.oficinatrack.shared.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteConsultaAdapterTest {

    @Mock
    private ClienteService clienteService;

    @InjectMocks
    private ClienteConsultaAdapter adapter;

    private ClienteResponse cliente(Boolean ativo) {
        ClienteResponse response = new ClienteResponse();
        response.setId(1L);
        response.setAtivo(ativo);
        return response;
    }

    @Test
    void deveRetornarTrueQuandoClienteAtivo() {
        when(clienteService.buscarPorId(1L)).thenReturn(cliente(true));

        assertTrue(adapter.existeAtivo(1L));
    }

    @Test
    void deveRetornarFalseQuandoClienteInativoOuSemFlag() {
        when(clienteService.buscarPorId(1L)).thenReturn(cliente(false));
        when(clienteService.buscarPorId(2L)).thenReturn(cliente(null));

        assertFalse(adapter.existeAtivo(1L));
        assertFalse(adapter.existeAtivo(2L));
    }

    @Test
    void deveRetornarFalseQuandoClienteNaoExiste() {
        when(clienteService.buscarPorId(99L))
                .thenThrow(new RecursoNaoEncontradoException("cliente não encontrado com o id: 99"));

        assertFalse(adapter.existeAtivo(99L));
    }
}
