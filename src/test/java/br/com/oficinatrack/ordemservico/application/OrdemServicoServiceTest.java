package br.com.oficinatrack.ordemservico.application;

import br.com.oficinatrack.ordemservico.api.dto.*;
import br.com.oficinatrack.ordemservico.application.port.*;
import br.com.oficinatrack.ordemservico.domain.*;
import br.com.oficinatrack.ordemservico.infra.OrdemServicoRepository;
import br.com.oficinatrack.shared.exception.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdemServicoServiceTest {
    @Mock OrdemServicoRepository ordemRepository;
    @Mock ClienteVeiculoPort clienteVeiculoPort;
    @Mock CatalogoServicoPort catalogoServicoPort;
    @Mock EstoquePort estoquePort;
    @InjectMocks OrdemServicoService service;

    @Test
    void deveExecutarOperacoesDoFluxoAdministrativo() {
        when(ordemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(StatusOrdemServico.RECEBIDA,
                service.criar(new CriarOrdemServicoRequest(1L, 2L)).status());
        verify(clienteVeiculoPort).validarVinculo(1L, 2L);

        OrdemServico os = OrdemServico.abrir(1L, 2L);
        when(ordemRepository.findById(10L)).thenReturn(Optional.of(os));
        service.iniciarDiagnostico(10L);

        when(catalogoServicoPort.buscarServicoAtivo(3L))
                .thenReturn(new ItemOrcavel(3L, "Troca", new BigDecimal("100.00")));
        service.adicionarServico(10L, new AdicionarItemRequest(3L, 1));

        when(estoquePort.buscarPecaDisponivel(4L, 2))
                .thenReturn(new ItemOrcavel(4L, "Filtro", new BigDecimal("20.00")));
        service.adicionarPeca(10L, new AdicionarItemRequest(4L, 2));

        var calculada = service.calcularOrcamento(10L);
        assertEquals(new BigDecimal("140.00"), calculada.orcamentos().getFirst().total());
        service.enviarOrcamento(10L, calculada.orcamentos().getFirst().id());
        Orcamento orcamento = os.getOrcamentos().getFirst();
        os.aprovarOrcamento(orcamento);
        service.finalizar(10L);
        service.entregar(10L);

        assertEquals(StatusOrdemServico.ENTREGUE, service.detalhar(10L).status());
        verify(ordemRepository, times(7)).flush();
    }

    @Test
    void deveListarComFiltros() {
        OrdemServico os = OrdemServico.abrir(1L, 2L);
        Pageable pageable = PageRequest.of(0, 10);
        when(ordemRepository.buscar(any(), any(), any(), any(), any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(os), pageable, 1));
        assertEquals(1, service.listar(StatusOrdemServico.RECEBIDA, 1L, 2L, null, null, pageable).getTotalElements());
    }

    @Test
    void naoDeveCriarOrdemQuandoVinculoClienteVeiculoForInvalido() {
        doThrow(new RegraDeNegocioException("Veículo não pertence ao cliente informado."))
                .when(clienteVeiculoPort).validarVinculo(1L, 2L);

        assertThrows(RegraDeNegocioException.class,
                () -> service.criar(new CriarOrdemServicoRequest(1L, 2L)));
        verify(ordemRepository, never()).save(any());
    }

    @Test
    void naoDeveIncluirPecaIndisponivelNoEstoque() {
        OrdemServico os = OrdemServico.abrir(1L, 2L);
        os.iniciarDiagnostico();
        when(ordemRepository.findById(10L)).thenReturn(Optional.of(os));
        when(estoquePort.buscarPecaDisponivel(4L, 5))
                .thenThrow(new ConflitoException("Peça indisponível no estoque."));

        assertThrows(ConflitoException.class, () -> service.adicionarPeca(10L, new AdicionarItemRequest(4L, 5)));
        assertTrue(os.getItensPeca().isEmpty());
    }

    @Test
    void deveCancelarComLockDaOrdem() {
        OrdemServico os = OrdemServico.abrir(1L, 2L);
        when(ordemRepository.findByIdParaAtualizacao(10L)).thenReturn(Optional.of(os));

        var response = service.cancelar(10L, new CancelarOrdemServicoRequest("Cliente desistiu"));

        assertEquals(StatusOrdemServico.CANCELADA, response.status());
        assertEquals("Cliente desistiu", response.motivoCancelamento());
    }

    @Test
    void deveInformarOrdemInexistente() {
        when(ordemRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> service.detalhar(99L));
    }
}
