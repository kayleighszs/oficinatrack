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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final ClienteRepository clienteRepository;

    public VeiculoService(VeiculoRepository veiculoRepository, ClienteRepository clienteRepository) {
        this.veiculoRepository = veiculoRepository;
        this.clienteRepository = clienteRepository;
    }

    // 2.1 Cadastrar veículo
    @Transactional
    public VeiculoResponse cadastrar(VeiculoRequest request) {
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado: " + request.getClienteId()));

        Placa placa = new Placa(request.getPlaca());
        validarPlacaDisponivel(placa);

        Veiculo veiculo = new Veiculo();
        veiculo.setCliente(cliente);
        veiculo.setPlaca(placa);
        veiculo.setMarca(request.getMarca());
        veiculo.setModelo(request.getModelo());
        veiculo.setAno(request.getAno());

        return toResponse(veiculoRepository.save(veiculo));
    }

    // 2.2 Listar veículos (filtros opcionais por cliente e situação)
    @Transactional(readOnly = true)
    public List<VeiculoResponse> listar(Long clienteId, Boolean ativo) {
        return veiculoRepository.buscar(clienteId, ativo).stream()
                .map(this::toResponse)
                .toList();
    }

    // 2.3 Detalhar veículo
    @Transactional(readOnly = true)
    public VeiculoResponse detalhar(Long id) {
        return toResponse(buscarPorId(id));
    }

    // 2.4 Editar/inativar veículo (sem exclusão física)
    @Transactional
    public VeiculoResponse atualizar(Long id, VeiculoUpdateRequest request) {
        Veiculo veiculo = buscarPorId(id);

        if (request.getPlaca() != null) {
            Placa novaPlaca = new Placa(request.getPlaca());
            if (!novaPlaca.getValue().equals(veiculo.getPlaca().getValue())) {
                validarPlacaDisponivel(novaPlaca);
                veiculo.setPlaca(novaPlaca);
            }
        }
        if (request.getMarca() != null) {
            veiculo.setMarca(request.getMarca());
        }
        if (request.getModelo() != null) {
            if (request.getModelo().isBlank()) {
                throw new RegraDeNegocioException("modelo não pode ser vazio");
            }
            veiculo.setModelo(request.getModelo());
        }
        if (request.getAno() != null) {
            veiculo.setAno(request.getAno());
        }
        if (request.getAtivo() != null) {
            veiculo.setAtivo(request.getAtivo());
        }

        return toResponse(veiculo);
    }

    private Veiculo buscarPorId(Long id) {
        return veiculoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo não encontrado: " + id));
    }

    private void validarPlacaDisponivel(Placa placa) {
        if (veiculoRepository.existsByPlacaValue(placa.getValue())) {
            throw new RegraDeNegocioException("Já existe veículo com a placa " + placa.getValue());
        }
    }

    private VeiculoResponse toResponse(Veiculo veiculo) {
        VeiculoResponse response = new VeiculoResponse();
        response.setId(veiculo.getId());
        response.setPlaca(veiculo.getPlaca().getValue());
        response.setMarca(veiculo.getMarca());
        response.setModelo(veiculo.getModelo());
        response.setAno(veiculo.getAno());
        response.setClienteId(veiculo.getCliente().getId());
        response.setAtivo(veiculo.isAtivo());
        return response;
    }
}
