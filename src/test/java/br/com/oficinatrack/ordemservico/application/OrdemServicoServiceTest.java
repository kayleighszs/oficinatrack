package br.com.oficinatrack.ordemservico.application;

import br.com.oficinatrack.ordemservico.api.dto.ItemPecaRequest;
import br.com.oficinatrack.ordemservico.api.dto.ItemServicoRequest;
import br.com.oficinatrack.ordemservico.api.dto.OrdemServicoRequest;
import br.com.oficinatrack.ordemservico.api.dto.OrdemServicoResponse;
import br.com.oficinatrack.ordemservico.application.port.ClienteConsultaPort;
import br.com.oficinatrack.ordemservico.application.port.EstoquePort;
import br.com.oficinatrack.ordemservico.application.port.EstoquePort.PecaEstoque;
import br.com.oficinatrack.ordemservico.application.port.ServicoCatalogoPort;
import br.com.oficinatrack.ordemservico.application.port.ServicoCatalogoPort.ServicoCatalogo;
import br.com.oficinatrack.ordemservico.application.port.VeiculoConsultaPort;
import br.com.oficinatrack.ordemservico.application.port.VeiculoConsultaPort.VeiculoInfo;
import br.com.oficinatrack.ordemservico.domain.HistoricoStatusOs;
import br.com.oficinatrack.ordemservico.domain.OrdemServico;
import br.com.oficinatrack.ordemservico.domain.OrdemServicoCriada;
import br.com.oficinatrack.ordemservico.domain.StatusOrdemServico;
import br.com.oficinatrack.ordemservico.infra.HistoricoStatusOsRepository;
import br.com.oficinatrack.ordemservico.infra.OrdemServicoRepository;
import br.com.oficinatrack.shared.exception.RecursoNaoEncontradoException;
import br.com.oficinatrack.shared.exception.RegraDeNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdemServicoServiceTest {

    @Mock private OrdemServicoRepository ordemServicoRepository;
    @Mock private HistoricoStatusOsRepository historicoRepository;
    @Mock private ClienteConsultaPort clientePort;
    @Mock private VeiculoConsultaPort veiculoPort;
    @Mock private ServicoCatalogoPort servicoPort;
    @Mock private EstoquePort estoquePort;
    @Mock private ApplicationEventPublisher eventPublisher;

    private OrdemServicoService service;

    @BeforeEach
    void setUp() {
        service = new OrdemServicoService(ordemServicoRepository, historicoRepository,
                clientePort, veiculoPort, servicoPort, estoquePort, eventPublisher);
    }

    private OrdemServicoRequest requestCriar(Long clienteId, Long veiculoId) {
        OrdemServicoRequest r = new OrdemServicoRequest();
        r.setClienteId(clienteId);
        r.setVeiculoId(veiculoId);
        return r;
    }

    private void salvarRetornandoArgumento() {
        when(ordemServicoRepository.save(any(OrdemServico.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // ---------- HU-5.1 criar ----------

    @Test
    void criarDeveAbrirOsRecebidaPublicarEventoERegistrarHistorico() {
        when(clientePort.existeAtivo(1L)).thenReturn(true);
        when(veiculoPort.buscar(2L)).thenReturn(Optional.of(new VeiculoInfo(2L, 1L, true)));
        salvarRetornandoArgumento();

        OrdemServicoResponse response = service.criar(requestCriar(1L, 2L));

        assertEquals(StatusOrdemServico.RECEBIDA, response.status());
        assertEquals(new BigDecimal("0.00"), response.valorTotal());
        verify(eventPublisher).publishEvent(any(OrdemServicoCriada.class));

        ArgumentCaptor<HistoricoStatusOs> captor = ArgumentCaptor.forClass(HistoricoStatusOs.class);
        verify(historicoRepository).save(captor.capture());
        assertNull(captor.getValue().getStatusAnterior());
        assertEquals(StatusOrdemServico.RECEBIDA, captor.getValue().getStatusNovo());
    }

    @Test
    void criarDeveFalharQuandoClienteInexistenteOuInativo() {
        when(clientePort.existeAtivo(1L)).thenReturn(false);

        assertThrows(RegraDeNegocioException.class, () -> service.criar(requestCriar(1L, 2L)));

        verify(ordemServicoRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void criarDeveFalharQuandoVeiculoInexistente() {
        when(clientePort.existeAtivo(1L)).thenReturn(true);
        when(veiculoPort.buscar(2L)).thenReturn(Optional.empty());

        assertThrows(RegraDeNegocioException.class, () -> service.criar(requestCriar(1L, 2L)));
        verify(ordemServicoRepository, never()).save(any());
    }

    @Test
    void criarDeveFalharQuandoVeiculoInativo() {
        when(clientePort.existeAtivo(1L)).thenReturn(true);
        when(veiculoPort.buscar(2L)).thenReturn(Optional.of(new VeiculoInfo(2L, 1L, false)));

        assertThrows(RegraDeNegocioException.class, () -> service.criar(requestCriar(1L, 2L)));
        verify(ordemServicoRepository, never()).save(any());
    }

    @Test
    void criarDeveFalharQuandoVeiculoPertenceAOutroCliente() {
        when(clientePort.existeAtivo(1L)).thenReturn(true);
        when(veiculoPort.buscar(2L)).thenReturn(Optional.of(new VeiculoInfo(2L, 99L, true)));

        assertThrows(RegraDeNegocioException.class, () -> service.criar(requestCriar(1L, 2L)));
        verify(ordemServicoRepository, never()).save(any());
    }

    // ---------- HU-5.2 adicionarServico ----------

    private ItemServicoRequest requestServico(Long servicoId, int quantidade) {
        ItemServicoRequest r = new ItemServicoRequest();
        r.setServicoId(servicoId);
        r.setQuantidade(quantidade);
        return r;
    }

    @Test
    void adicionarServicoDeveCopiarValorDoCatalogoERecalcularTotal() {
        when(ordemServicoRepository.findById(1L)).thenReturn(Optional.of(OrdemServico.abrir(1L, 2L)));
        when(servicoPort.buscar(10L))
                .thenReturn(Optional.of(new ServicoCatalogo(10L, "Troca de óleo", new BigDecimal("80.00"), true)));
        salvarRetornandoArgumento();

        OrdemServicoResponse response = service.adicionarServico(1L, requestServico(10L, 2));

        assertEquals(1, response.itensServico().size());
        assertEquals("Troca de óleo", response.itensServico().get(0).descricao());
        assertEquals(new BigDecimal("80.00"), response.itensServico().get(0).valorUnitario());
        assertEquals(new BigDecimal("160.00"), response.valorTotal());
    }

    @Test
    void adicionarServicoDeveFalharQuandoOsNaoExiste() {
        when(ordemServicoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.adicionarServico(99L, requestServico(10L, 1)));
    }

    @Test
    void adicionarServicoDeveFalharQuandoServicoInexistenteOuInativo() {
        when(ordemServicoRepository.findById(1L)).thenReturn(Optional.of(OrdemServico.abrir(1L, 2L)));
        when(servicoPort.buscar(10L)).thenReturn(Optional.empty());
        assertThrows(RegraDeNegocioException.class, () -> service.adicionarServico(1L, requestServico(10L, 1)));

        when(servicoPort.buscar(11L))
                .thenReturn(Optional.of(new ServicoCatalogo(11L, "Antigo", BigDecimal.TEN, false)));
        assertThrows(RegraDeNegocioException.class, () -> service.adicionarServico(1L, requestServico(11L, 1)));

        verify(ordemServicoRepository, never()).save(any());
    }

    @Test
    void adicionarServicoDeveFalharQuandoStatusNaoPermite() {
        OrdemServico os = OrdemServico.abrir(1L, 2L);
        os.adicionarServico(10L, "X", BigDecimal.ONE, 1);
        os.enviarOrcamento();
        when(ordemServicoRepository.findById(1L)).thenReturn(Optional.of(os));

        assertThrows(RegraDeNegocioException.class, () -> service.adicionarServico(1L, requestServico(10L, 1)));
        verify(servicoPort, never()).buscar(any());
    }

    // ---------- HU-5.3 adicionarPeca ----------

    private ItemPecaRequest requestPeca(Long pecaId, int quantidade) {
        ItemPecaRequest r = new ItemPecaRequest();
        r.setPecaId(pecaId);
        r.setQuantidade(quantidade);
        return r;
    }

    @Test
    void adicionarPecaDeveValidarEstoqueCopiarValorERecalcularTotal() {
        when(ordemServicoRepository.findById(1L)).thenReturn(Optional.of(OrdemServico.abrir(1L, 2L)));
        when(estoquePort.consultar(20L))
                .thenReturn(Optional.of(new PecaEstoque(20L, "Filtro", new BigDecimal("35.25"), 10, true)));
        salvarRetornandoArgumento();

        OrdemServicoResponse response = service.adicionarPeca(1L, requestPeca(20L, 3));

        assertEquals(1, response.itensPeca().size());
        assertEquals(new BigDecimal("105.75"), response.valorTotal());
    }

    @Test
    void adicionarPecaDeveFalharComEstoqueInsuficiente() {
        when(ordemServicoRepository.findById(1L)).thenReturn(Optional.of(OrdemServico.abrir(1L, 2L)));
        when(estoquePort.consultar(20L))
                .thenReturn(Optional.of(new PecaEstoque(20L, "Filtro", BigDecimal.TEN, 2, true)));

        assertThrows(RegraDeNegocioException.class, () -> service.adicionarPeca(1L, requestPeca(20L, 3)));
        verify(ordemServicoRepository, never()).save(any());
    }

    @Test
    void adicionarPecaDeveConsiderarQuantidadeJaReservadaNaOs() {
        OrdemServico os = OrdemServico.abrir(1L, 2L);
        os.adicionarPeca(20L, "Filtro", BigDecimal.TEN, 4);
        when(ordemServicoRepository.findById(1L)).thenReturn(Optional.of(os));
        when(estoquePort.consultar(20L))
                .thenReturn(Optional.of(new PecaEstoque(20L, "Filtro", BigDecimal.TEN, 5, true)));

        // 4 já na OS + 2 novas = 6 > 5 disponíveis
        assertThrows(RegraDeNegocioException.class, () -> service.adicionarPeca(1L, requestPeca(20L, 2)));
    }

    @Test
    void adicionarPecaDeveFalharQuandoPecaInexistenteOuInativa() {
        when(ordemServicoRepository.findById(1L)).thenReturn(Optional.of(OrdemServico.abrir(1L, 2L)));
        when(estoquePort.consultar(20L)).thenReturn(Optional.empty());
        assertThrows(RegraDeNegocioException.class, () -> service.adicionarPeca(1L, requestPeca(20L, 1)));

        when(estoquePort.consultar(21L))
                .thenReturn(Optional.of(new PecaEstoque(21L, "Inativa", BigDecimal.TEN, 50, false)));
        assertThrows(RegraDeNegocioException.class, () -> service.adicionarPeca(1L, requestPeca(21L, 1)));
    }

    @Test
    void adicionarPecaDeveFalharQuandoOsNaoExisteOuStatusInvalido() {
        when(ordemServicoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> service.adicionarPeca(99L, requestPeca(20L, 1)));

        OrdemServico os = OrdemServico.abrir(1L, 2L);
        os.adicionarServico(10L, "X", BigDecimal.ONE, 1);
        os.enviarOrcamento();
        when(ordemServicoRepository.findById(1L)).thenReturn(Optional.of(os));
        assertThrows(RegraDeNegocioException.class, () -> service.adicionarPeca(1L, requestPeca(20L, 1)));
        verify(estoquePort, never()).consultar(any());
    }

    // ---------- HU-5.5 enviarOrcamento ----------

    @Test
    void enviarOrcamentoDeveMudarStatusERegistrarHistorico() {
        OrdemServico os = OrdemServico.abrir(1L, 2L);
        os.adicionarServico(10L, "Troca de óleo", new BigDecimal("80.00"), 1);
        when(ordemServicoRepository.findById(1L)).thenReturn(Optional.of(os));
        salvarRetornandoArgumento();

        OrdemServicoResponse response = service.enviarOrcamento(1L);

        assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO, response.status());
        ArgumentCaptor<HistoricoStatusOs> captor = ArgumentCaptor.forClass(HistoricoStatusOs.class);
        verify(historicoRepository).save(captor.capture());
        assertEquals(StatusOrdemServico.RECEBIDA, captor.getValue().getStatusAnterior());
        assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO, captor.getValue().getStatusNovo());
    }

    @Test
    void enviarOrcamentoSemItensDeveFalhar() {
        when(ordemServicoRepository.findById(1L)).thenReturn(Optional.of(OrdemServico.abrir(1L, 2L)));

        assertThrows(RegraDeNegocioException.class, () -> service.enviarOrcamento(1L));
        verify(historicoRepository, never()).save(any());
    }

    @Test
    void enviarOrcamentoDeveFalharQuandoOsNaoExiste() {
        when(ordemServicoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.enviarOrcamento(99L));
    }
}
