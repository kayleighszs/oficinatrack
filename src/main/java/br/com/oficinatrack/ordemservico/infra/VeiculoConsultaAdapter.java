package br.com.oficinatrack.ordemservico.infra;

import br.com.oficinatrack.ordemservico.application.port.VeiculoConsultaPort;
import br.com.oficinatrack.shared.exception.RecursoNaoEncontradoException;
import br.com.oficinatrack.veiculo.api.dto.VeiculoResponse;
import br.com.oficinatrack.veiculo.application.VeiculoService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class VeiculoConsultaAdapter implements VeiculoConsultaPort {

    private final VeiculoService veiculoService;

    public VeiculoConsultaAdapter(VeiculoService veiculoService) {
        this.veiculoService = veiculoService;
    }

    @Override
    public Optional<VeiculoInfo> buscar(Long veiculoId) {
        try {
            VeiculoResponse veiculo = veiculoService.detalhar(veiculoId);
            return Optional.of(new VeiculoInfo(veiculo.getId(), veiculo.getClienteId(), veiculo.isAtivo()));
        } catch (RecursoNaoEncontradoException ex) {
            return Optional.empty();
        }
    }
}
