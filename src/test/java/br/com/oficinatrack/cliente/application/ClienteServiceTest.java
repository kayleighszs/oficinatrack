package br.com.oficinatrack.cliente.application;

import br.com.oficinatrack.cliente.api.dto.AtualizarClienteRequest;
import br.com.oficinatrack.cliente.api.dto.CadastrarClienteRequest;
import br.com.oficinatrack.cliente.api.dto.ClienteResponse;
import br.com.oficinatrack.cliente.domain.Cliente;
import br.com.oficinatrack.cliente.domain.TipoPessoa;
import br.com.oficinatrack.cliente.infra.ClienteRepository;
import br.com.oficinatrack.cliente.mapper.ClienteMapperInterface;
import br.com.oficinatrack.cliente.mapper.ClienteMapperInterfaceImpl;
import br.com.oficinatrack.shared.exception.RecursoNaoEncontradoException;
import br.com.oficinatrack.shared.exception.RegraDeNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    private ClienteService clienteService;

    @BeforeEach
    void setUp() {
        ClienteMapperInterface mapper = new ClienteMapperInterfaceImpl();
        clienteService = new ClienteService(clienteRepository, mapper);
    }

    private CadastrarClienteRequest requestCadastro(String nome, String documento) {
        CadastrarClienteRequest request = new CadastrarClienteRequest();
        request.setNome(nome);
        request.setCpfCnpj(documento);
        request.setTelefone("(11) 91234-5678");
        request.setEmail("cliente@email.com");
        return request;
    }

    private Cliente clienteExistente() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João");
        cliente.setCpfCnpj("52998224725");
        cliente.setTipoPessoa(TipoPessoa.FISICA);
        cliente.setAtivo(true);
        cliente.setCriadoEm(LocalDateTime.of(2026, 1, 1, 10, 0));
        cliente.setAtualizadoEm(LocalDateTime.of(2026, 1, 1, 10, 0));
        return cliente;
    }

    // ---------- cadastrar ----------

    @Test
    void cadastrarCpfDeveNormalizarDocumentoEDefinirTipoFisica() {
        when(clienteRepository.existsByCpfCnpj("52998224725")).thenReturn(false);

        clienteService.cadastrar(requestCadastro("  João da Silva  ", "529.982.247-25"));

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(captor.capture());
        Cliente salvo = captor.getValue();
        assertEquals("52998224725", salvo.getCpfCnpj());
        assertEquals(TipoPessoa.FISICA, salvo.getTipoPessoa());
        assertEquals("João da Silva", salvo.getNome());
        assertEquals("(11) 91234-5678", salvo.getTelefone());
        assertEquals("cliente@email.com", salvo.getEmail());
        assertTrue(salvo.getAtivo());
    }

    @Test
    void cadastrarCnpjDeveDefinirTipoJuridica() {
        when(clienteRepository.existsByCpfCnpj("11222333000181")).thenReturn(false);

        clienteService.cadastrar(requestCadastro("Oficina LTDA", "11.222.333/0001-81"));

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(captor.capture());
        assertEquals("11222333000181", captor.getValue().getCpfCnpj());
        assertEquals(TipoPessoa.JURIDICA, captor.getValue().getTipoPessoa());
    }

    @Test
    void cadastrarDeveFalharQuandoDocumentoJaExiste() {
        when(clienteRepository.existsByCpfCnpj("52998224725")).thenReturn(true);

        assertThrows(RegraDeNegocioException.class,
                () -> clienteService.cadastrar(requestCadastro("João", "529.982.247-25")));

        verify(clienteRepository, never()).save(any());
    }

    // ---------- atualizar ----------

    @Test
    void atualizarDeveAlterarSomenteNomeTelefoneEEmail() {
        Cliente cliente = clienteExistente();
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        AtualizarClienteRequest request = new AtualizarClienteRequest();
        request.setNome("  João Atualizado ");
        request.setTelefone("11 98888-7777");
        request.setEmail("novo@email.com");

        ClienteResponse response = clienteService.atualizar(1L, request);

        assertEquals("João Atualizado", response.getNome());
        assertEquals("11 98888-7777", response.getTelefone());
        assertEquals("novo@email.com", response.getEmail());
        assertEquals("52998224725", response.getCpfCnpj());
        assertEquals(TipoPessoa.FISICA, response.getTipoPessoa());
        assertTrue(response.getAtivo());
        assertEquals(LocalDateTime.of(2026, 1, 1, 10, 0), response.getCriadoEm());
    }

    @Test
    void atualizarDeveFalharQuandoClienteNaoExiste() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        AtualizarClienteRequest request = new AtualizarClienteRequest();
        request.setNome("Fulano");

        assertThrows(RecursoNaoEncontradoException.class, () -> clienteService.atualizar(99L, request));
        verify(clienteRepository, never()).save(any());
    }

    // ---------- inativar ----------

    @Test
    void inativarDeveMarcarClienteComoInativo() {
        Cliente cliente = clienteExistente();
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        ClienteResponse response = clienteService.inativar(1L);

        assertFalse(response.getAtivo());
        assertFalse(cliente.getAtivo());
    }

    @Test
    void inativarDeveFalharQuandoClienteNaoExiste() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> clienteService.inativar(99L));
        verify(clienteRepository, never()).save(any());
    }

    // ---------- buscarPorId ----------

    @Test
    void buscarPorIdDeveRetornarResponseCompleto() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente()));

        ClienteResponse response = clienteService.buscarPorId(1L);

        assertEquals(1L, response.getId());
        assertEquals("João", response.getNome());
        assertEquals("52998224725", response.getCpfCnpj());
        assertEquals(TipoPessoa.FISICA, response.getTipoPessoa());
        assertTrue(response.getAtivo());
        assertEquals(LocalDateTime.of(2026, 1, 1, 10, 0), response.getAtualizadoEm());
    }

    @Test
    void buscarPorIdDeveFalharQuandoNaoExiste() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> clienteService.buscarPorId(99L));
    }

    // ---------- listar ----------

    @Test
    void listarSemFiltrosDeveEnviarStringsVazias() {
        when(clienteRepository.buscarClientesComFiltros("", "")).thenReturn(List.of(clienteExistente()));

        List<ClienteResponse> resultado = clienteService.listarClientes(null, null);

        assertEquals(1, resultado.size());
        verify(clienteRepository).buscarClientesComFiltros("", "");
    }

    @Test
    void listarDeveAparaNomeENormalizarCpfCnpj() {
        when(clienteRepository.buscarClientesComFiltros(anyString(), anyString())).thenReturn(List.of());

        clienteService.listarClientes("529.982.247-25", "  silva ");

        verify(clienteRepository).buscarClientesComFiltros("silva", "52998224725");
    }

    @Test
    void listarComFiltrosEmBrancoDeveTratarComoSemFiltro() {
        when(clienteRepository.buscarClientesComFiltros("", "")).thenReturn(List.of());

        clienteService.listarClientes("   ", "   ");

        verify(clienteRepository).buscarClientesComFiltros("", "");
    }
}
