package br.com.oficinatrack.ordemservico.infra.acl;

import br.com.oficinatrack.cliente.domain.Cliente;
import br.com.oficinatrack.cliente.infra.ClienteRepository;
import br.com.oficinatrack.ordemservico.application.port.ClienteVeiculoPort;
import br.com.oficinatrack.shared.exception.RegraDeNegocioException;
import br.com.oficinatrack.veiculo.domain.Veiculo;
import br.com.oficinatrack.veiculo.infra.VeiculoRepository;
import org.springframework.stereotype.Component;

@Component
class ClienteVeiculoAdapter implements ClienteVeiculoPort {
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;

    ClienteVeiculoAdapter(ClienteRepository clienteRepository, VeiculoRepository veiculoRepository) {
        this.clienteRepository = clienteRepository;
        this.veiculoRepository = veiculoRepository;
    }

    @Override
    public void validarVinculo(Long clienteId, Long veiculoId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .filter(c -> Boolean.TRUE.equals(c.getAtivo()))
                .orElseThrow(() -> new RegraDeNegocioException("Cliente inexistente ou inativo."));
        Veiculo veiculo = veiculoRepository.findById(veiculoId)
                .filter(Veiculo::isAtivo)
                .orElseThrow(() -> new RegraDeNegocioException("Veículo inexistente ou inativo."));
        if (!veiculo.getCliente().getId().equals(cliente.getId()))
            throw new RegraDeNegocioException("Veículo não pertence ao cliente informado.");
    }
}
