package br.com.oficinatrack.ordemservico.infra;

import br.com.oficinatrack.catalogoservicos.api.dto.out.ServicoRequestOut;
import br.com.oficinatrack.catalogoservicos.application.ServicoService;
import br.com.oficinatrack.ordemservico.application.port.ServicoCatalogoPort;
import br.com.oficinatrack.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ServicoCatalogoAdapter implements ServicoCatalogoPort {

    private final ServicoService servicoService;

    public ServicoCatalogoAdapter(ServicoService servicoService) {
        this.servicoService = servicoService;
    }

    @Override
    public Optional<ServicoCatalogo> buscar(Long servicoId) {
        try {
            ServicoRequestOut servico = servicoService.buscarPorId(servicoId);
            return Optional.of(new ServicoCatalogo(
                    servico.id(),
                    servico.nome(),
                    servico.valor(),
                    Boolean.TRUE.equals(servico.ativo())));
        } catch (RecursoNaoEncontradoException ex) {
            return Optional.empty();
        }
    }
}
