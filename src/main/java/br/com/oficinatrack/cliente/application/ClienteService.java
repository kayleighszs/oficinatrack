package br.com.oficinatrack.cliente.application;

import br.com.oficinatrack.cliente.api.dto.AtualizarClienteRequest;
import br.com.oficinatrack.cliente.api.dto.CadastrarClienteRequest;
import br.com.oficinatrack.cliente.domain.Cliente;
import br.com.oficinatrack.cliente.domain.TipoPessoa;
import br.com.oficinatrack.cliente.api.dto.ClienteResponse;
import br.com.oficinatrack.cliente.infra.ClienteRepository;
import br.com.oficinatrack.cliente.mapper.ClienteMapperInterface;
import br.com.oficinatrack.shared.exception.RecursoNaoEncontradoException;
import br.com.oficinatrack.shared.exception.RegraDeNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private static final int TAMANHO_CPF = 11;

    private final ClienteRepository clienteRepository;
    private final ClienteMapperInterface clienteMapper;

    public ClienteService(ClienteRepository clienteRepository, ClienteMapperInterface clienteMapper) {
        this.clienteRepository = clienteRepository;
        this.clienteMapper = clienteMapper;
    }

    @Transactional
    public void cadastrar(CadastrarClienteRequest request) {
        String documento = somenteDigitos(request.getCpfCnpj());

        if (clienteRepository.existsByCpfCnpj(documento)) {
            throw new RegraDeNegocioException("já existe um cliente cadastrado com o CPF/CNPJ informado");
        }

        Cliente cliente = clienteMapper.toEntity(request);
        cliente.setNome(cliente.getNome().trim());
        cliente.setCpfCnpj(documento);
        cliente.setTipoPessoa(documento.length() == TAMANHO_CPF ? TipoPessoa.FISICA : TipoPessoa.JURIDICA);
        cliente.setAtivo(true);
        clienteRepository.save(cliente);
    }

    @Transactional
    public ClienteResponse atualizar(Long id, AtualizarClienteRequest request) {
        Cliente cliente = buscarEntidade(id);

        clienteMapper.atualizarCliente(cliente, request);
        cliente.setNome(cliente.getNome().trim());
        return clienteMapper.toResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteResponse inativar(Long id) {
        Cliente cliente = buscarEntidade(id);

        cliente.setAtivo(false);
        return clienteMapper.toResponse(clienteRepository.save(cliente));
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listarClientes(String cpfCnpj, String nome) {
        // string vazia = "sem filtro"; evita enviar null, que o PostgreSQL tipa como bytea
        String nomeFiltro = nome == null ? "" : nome.trim();
        String cpfFiltro = cpfCnpj == null ? "" : somenteDigitos(cpfCnpj);

        List<Cliente> clientes = clienteRepository.buscarClientesComFiltros(nomeFiltro, cpfFiltro);
        return clienteMapper.toResponseList(clientes);
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Long id) {
        return clienteMapper.toResponse(buscarEntidade(id));
    }

    private Cliente buscarEntidade(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("cliente não encontrado com o id: " + id));
    }

    private String somenteDigitos(String valor) {
        return valor.replaceAll("\\D", "");
    }
}
