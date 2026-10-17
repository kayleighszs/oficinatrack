package br.com.oficinatrack.catalogoservicos.application;

import br.com.oficinatrack.catalogoservicos.api.dto.in.ServicoRequestIn;
import br.com.oficinatrack.catalogoservicos.domain.Servico;
import br.com.oficinatrack.catalogoservicos.infra.ServicoRepository;
import br.com.oficinatrack.shared.exception.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {
    @Mock ServicoRepository repository;
    @InjectMocks ServicoService service;

    @Test
    void deveCadastrarServicoValido() throws Exception {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        var response = service.cadastrar(new ServicoRequestIn("Alinhamento", new BigDecimal("80.00"), 30));
        assertEquals(200, response.getStatusCode().value());
        verify(repository).save(any(Servico.class));
    }

    @Test
    void deveValidarCadastro() {
        assertThrows(RegraDeNegocioException.class, () -> service.cadastrar(null));
        assertThrows(RegraDeNegocioException.class, () ->
                service.cadastrar(new ServicoRequestIn(" ", BigDecimal.ONE, 10)));
        assertThrows(RegraDeNegocioException.class, () ->
                service.cadastrar(new ServicoRequestIn("Teste", BigDecimal.ZERO, 10)));
    }

    @Test
    void deveListarComESemFiltro() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Servico> pagina = new PageImpl<>(List.of(new Servico("Teste", BigDecimal.TEN, 10)), pageable, 1);
        when(repository.findAll(pageable)).thenReturn(pagina);
        when(repository.findByNomeContainingIgnoreCase("tes", pageable)).thenReturn(pagina);
        assertEquals(200, service.listar(null, pageable).getStatusCode().value());
        assertEquals(200, service.listar("tes", pageable).getStatusCode().value());
    }

    @Test
    void deveBuscarAtualizarEInativar() {
        Servico servico = new Servico("Teste", BigDecimal.TEN, 10);
        when(repository.findById(1L)).thenReturn(Optional.of(servico));
        assertEquals(200, service.buscarPorId(1L).getStatusCode().value());
        service.atualizar(1L, new ServicoRequestIn("Novo", new BigDecimal("20.00"), 20));
        service.inativar(1L);
        assertEquals("Novo", servico.getNome());
        assertFalse(servico.getAtivo());
        verify(repository, times(2)).save(servico);
    }

    @Test
    void deveInformarServicoInexistente() {
        when(repository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscarPorId(9L));
        assertThrows(RecursoNaoEncontradoException.class, () -> service.inativar(9L));
        assertThrows(RecursoNaoEncontradoException.class, () ->
                service.atualizar(9L, new ServicoRequestIn("Novo", BigDecimal.TEN, 10)));
    }

    @Test
    void deveValidarDominioDoServico() {
        assertThrows(IllegalArgumentException.class, () -> new Servico("", BigDecimal.ONE, 10));
        assertThrows(IllegalArgumentException.class, () -> new Servico("Teste", BigDecimal.ZERO, 10));
        Servico servico = new Servico("Teste", BigDecimal.ONE, 10);
        servico.reativar();
        assertTrue(servico.getAtivo());
    }
}
