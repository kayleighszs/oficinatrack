package br.com.oficinatrack.veiculo.application;

import br.com.oficinatrack.cliente.domain.Cliente;
import br.com.oficinatrack.cliente.infra.ClienteRepository;
import br.com.oficinatrack.shared.exception.RecursoNaoEncontradoException;
import br.com.oficinatrack.shared.exception.RegraDeNegocioException;
import br.com.oficinatrack.veiculo.api.dto.VeiculoRequest;
import br.com.oficinatrack.veiculo.api.dto.VeiculoResponse;
import br.com.oficinatrack.veiculo.api.dto.VeiculoUpdateRequest;
import br.com.oficinatrack.veiculo.domain.Placa;
import br.com.oficinatrack.veiculo.domain.Veiculo;
import br.com.oficinatrack.veiculo.infra.VeiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VeiculoServiceTest {

    @Mock
    private VeiculoRepository veiculoRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @InjectMocks
    private VeiculoService veiculoService;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("Maria");
    }

    private VeiculoRequest request() {
        VeiculoRequest request = new VeiculoRequest();
        request.setClienteId(1L);
        request.setPlaca("abc-1d23");
        request.setMarca("Chevrolet");
        request.setModelo("Onix");
        request.setAno(2020);
        return request;
    }

    private Veiculo veiculoExistente() {
        Veiculo veiculo = new Veiculo();
        veiculo.setId(10L);
        veiculo.setCliente(cliente);
        veiculo.setPlaca(new Placa("ABC1D23"));
        veiculo.setMarca("Chevrolet");
        veiculo.setModelo("Onix");
        veiculo.setAno(2020);
        return veiculo;
    }

    @Test
    void cadastraVeiculoVinculadoAoCliente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(veiculoRepository.existsByPlacaValue("ABC1D23")).thenReturn(false);
        when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(inv -> {
            Veiculo v = inv.getArgument(0);
            v.setId(10L);
            return v;
        });

        VeiculoResponse response = veiculoService.cadastrar(request());

        assertEquals(10L, response.getId());
        assertEquals("ABC1D23", response.getPlaca());
        assertEquals(1L, response.getClienteId());
        assertTrue(response.isAtivo());
    }

    @Test
    void naoCadastraSeClienteNaoExiste() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> veiculoService.cadastrar(request()));
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    void naoCadastraPlacaDuplicada() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(veiculoRepository.existsByPlacaValue("ABC1D23")).thenReturn(true);

        assertThrows(RegraDeNegocioException.class, () -> veiculoService.cadastrar(request()));
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    void listaComFiltros() {
        when(veiculoRepository.buscar(1L, true)).thenReturn(List.of(veiculoExistente()));

        List<VeiculoResponse> lista = veiculoService.listar(1L, true);

        assertEquals(1, lista.size());
        assertEquals(10L, lista.getFirst().getId());
    }

    @Test
    void detalharInexistenteLanca404() {
        when(veiculoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> veiculoService.detalhar(99L));
    }

    @Test
    void atualizaSomenteCamposEnviados() {
        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(veiculoExistente()));
        VeiculoUpdateRequest update = new VeiculoUpdateRequest();
        update.setModelo("Onix Plus");

        VeiculoResponse response = veiculoService.atualizar(10L, update);

        assertEquals("Onix Plus", response.getModelo());
        assertEquals("Chevrolet", response.getMarca());
        assertEquals("ABC1D23", response.getPlaca());
    }

    @Test
    void inativaSemExcluir() {
        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(veiculoExistente()));
        VeiculoUpdateRequest update = new VeiculoUpdateRequest();
        update.setAtivo(false);

        VeiculoResponse response = veiculoService.atualizar(10L, update);

        assertFalse(response.isAtivo());
        verify(veiculoRepository, never()).delete(any());
    }

    @Test
    void naoTrocaParaPlacaDeOutroVeiculo() {
        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(veiculoExistente()));
        when(veiculoRepository.existsByPlacaValue("XYZ9876")).thenReturn(true);
        VeiculoUpdateRequest update = new VeiculoUpdateRequest();
        update.setPlaca("XYZ9876");

        assertThrows(RegraDeNegocioException.class, () -> veiculoService.atualizar(10L, update));
    }
}
