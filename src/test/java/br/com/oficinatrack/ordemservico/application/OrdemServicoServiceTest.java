package br.com.oficinatrack.ordemservico.application;

import br.com.oficinatrack.catalogoservicos.domain.Servico;
import br.com.oficinatrack.catalogoservicos.infra.ServicoRepository;
import br.com.oficinatrack.cliente.domain.Cliente;
import br.com.oficinatrack.cliente.infra.ClienteRepository;
import br.com.oficinatrack.estoque.domain.Peca;
import br.com.oficinatrack.estoque.infrastructure.PecaRepository;
import br.com.oficinatrack.ordemservico.api.dto.*;
import br.com.oficinatrack.ordemservico.domain.*;
import br.com.oficinatrack.ordemservico.infra.OrdemServicoRepository;
import br.com.oficinatrack.shared.exception.*;
import br.com.oficinatrack.veiculo.domain.Veiculo;
import br.com.oficinatrack.veiculo.infra.VeiculoRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdemServicoServiceTest {
    @Mock OrdemServicoRepository ordemRepository;
    @Mock ClienteRepository clienteRepository;
    @Mock VeiculoRepository veiculoRepository;
    @Mock ServicoRepository servicoRepository;
    @Mock PecaRepository pecaRepository;
    @InjectMocks OrdemServicoService service;

    @Test
    void deveExecutarOperacoesDoFluxoAdministrativo() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        Veiculo veiculo = new Veiculo();
        veiculo.setId(2L);
        veiculo.setCliente(cliente);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(veiculoRepository.findById(2L)).thenReturn(Optional.of(veiculo));
        when(ordemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(StatusOrdemServico.RECEBIDA,
                service.criar(new CriarOrdemServicoRequest(1L, 2L)).status());

        OrdemServico os = OrdemServico.abrir(1L, 2L);
        when(ordemRepository.findById(10L)).thenReturn(Optional.of(os));
        service.iniciarDiagnostico(10L);

        Servico catalogo = new Servico("Troca", new BigDecimal("100.00"), 60);
        ReflectionTestUtils.setField(catalogo, "id", 3L);
        when(servicoRepository.findById(3L)).thenReturn(Optional.of(catalogo));
        service.adicionarServico(10L, new AdicionarItemRequest(3L, 1));

        Peca peca = Peca.cadastrar("Filtro", new BigDecimal("20.00"), 10, 2);
        ReflectionTestUtils.setField(peca, "id", 4L);
        when(pecaRepository.findById(4L)).thenReturn(Optional.of(peca));
        service.adicionarPeca(10L, new AdicionarItemRequest(4L, 2));

        var calculada = service.calcularOrcamento(10L);
        service.enviarOrcamento(10L, calculada.orcamentos().getFirst().id());
        Orcamento orcamento = os.getOrcamentos().getFirst();
        os.aprovarOrcamento(orcamento);
        service.finalizar(10L);
        service.entregar(10L);

        assertEquals(StatusOrdemServico.ENTREGUE, service.detalhar(10L).status());
        verify(ordemRepository).flush();
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
    void deveValidarReferenciasDaCriacao() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RegraDeNegocioException.class,
                () -> service.criar(new CriarOrdemServicoRequest(1L, 2L)));

        Cliente cliente = new Cliente();
        cliente.setId(1L);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(veiculoRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(RegraDeNegocioException.class,
                () -> service.criar(new CriarOrdemServicoRequest(1L, 2L)));
    }

    @Test
    void deveInformarOrdemInexistente() {
        when(ordemRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> service.detalhar(99L));
    }
}
